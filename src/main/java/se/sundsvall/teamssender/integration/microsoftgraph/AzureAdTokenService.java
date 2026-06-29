package se.sundsvall.teamssender.integration.microsoftgraph;

import com.azure.core.credential.TokenCredential;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.teamssender.configuration.AzureConfig;
import se.sundsvall.teamssender.integration.db.DatabaseTokenCache;
import se.sundsvall.teamssender.integration.db.TokenCacheRepository;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class AzureAdTokenService {

	private final AzureConfig multiConfig;
	private final TokenCacheRepository tokenCacheRepository;

	// MSAL applications are thread-safe and meant to be long-lived; one per municipality keeps the in-process token
	// cache warm and lets MSAL serialise access to the persisted token_cache row within this process.
	private final Map<String, ConfidentialClientApplication> clientApplications = new ConcurrentHashMap<>();

	public AzureAdTokenService(final AzureConfig azureConfig, final TokenCacheRepository tokenCacheRepository) {
		this.multiConfig = azureConfig;
		this.tokenCacheRepository = tokenCacheRepository;
	}

	public ResponseEntity<String> exchangeAuthCodeForToken(final String authCode, final String municipalityId) {
		final AzureConfig.Azure config = getAzureConfig(municipalityId);
		final ConfidentialClientApplication app = clientApplication(municipalityId);

		try {
			final AuthorizationCodeParameters parameters = AuthorizationCodeParameters
				.builder(authCode, new URI(config.getRedirectUri()))
				.scopes(Collections.singleton(config.getScopes()))
				.build();

			final IAuthenticationResult result = app.acquireToken(parameters).get();

			final SilentParameters silentParameters = SilentParameters.builder(Collections.singleton(config.getScopes()))
				.account(result.account())
				.build();

			app.acquireTokenSilently(silentParameters).get();

			return ResponseEntity.ok("Token successfully saved");
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw Problem.valueOf(BAD_GATEWAY, "Interrupted while exchanging the authorization code for a token");
		} catch (final Exception e) {
			throw Problem.valueOf(UNAUTHORIZED, "Failed to exchange the authorization code for a token: " + e.getMessage());
		}
	}

	public String getAccessTokenForUser(final String municipalityId) {
		final AzureConfig.Azure config = getAzureConfig(municipalityId);
		final ConfidentialClientApplication app = clientApplication(municipalityId);

		final IAccount account = findCachedAccount(app, config.getUser())
			.orElseThrow(() -> Problem.valueOf(UNAUTHORIZED,
				"No cached login found for user '%s' (municipalityId '%s') - perform the one-time login via GET /api/teamssender/%s/login".formatted(config.getUser(), municipalityId, municipalityId)));

		try {
			final SilentParameters silentParameters = SilentParameters.builder(Collections.singleton(config.getScopes()))
				.account(account)
				.build();

			return app.acquireTokenSilently(silentParameters).get().accessToken();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw Problem.valueOf(BAD_GATEWAY, "Interrupted while acquiring an access token");
		} catch (final Exception e) {
			throw Problem.valueOf(UNAUTHORIZED, "Could not acquire an access token for municipalityId '%s': %s".formatted(municipalityId, e.getMessage()));
		}
	}

	public String getSenderUser(final String municipalityId) {
		return getAzureConfig(municipalityId).getUser();
	}

	private static Optional<IAccount> findCachedAccount(final ConfidentialClientApplication app, final String user) {
		// UPNs are case-insensitive; the cached account casing may differ from the configured value.
		return app.getAccounts().join().stream()
			.filter(account -> account.username().equalsIgnoreCase(user))
			.findFirst();
	}

	public String getLoginUrl(final String municipalityId) {
		return getAzureConfig(municipalityId).getLoginUrl();
	}

	public GraphServiceClient initializeGraphServiceClient(final String municipalityId) {
		final String accessToken = getAccessTokenForUser(municipalityId);
		final TokenCredential credential = new StaticTokenCredential(accessToken);

		return new GraphServiceClient(credential);
	}

	private ConfidentialClientApplication clientApplication(final String municipalityId) {
		return clientApplications.computeIfAbsent(municipalityId, id -> {
			final AzureConfig.Azure config = getAzureConfig(id);
			final IClientCredential clientSecret = ClientCredentialFactory.createFromSecret(config.getClientSecret());
			try {
				return ConfidentialClientApplication.builder(config.getClientId(), clientSecret)
					.authority(config.getAuthorityUrl())
					.setTokenCacheAccessAspect(new DatabaseTokenCache(config.getUser(), tokenCacheRepository))
					.build();
			} catch (final MalformedURLException e) {
				throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Invalid Azure authority URL configured for municipalityId '%s': %s".formatted(id, e.getMessage()));
			}
		});
	}

	private AzureConfig.Azure getAzureConfig(final String municipalityId) {
		final AzureConfig.Azure config = multiConfig.getAd().get(municipalityId);

		if (config == null) {
			throw Problem.valueOf(NOT_FOUND, "No Azure configuration found for municipalityId: " + municipalityId);
		}

		return config;
	}
}
