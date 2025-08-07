package se.sundsvall.teamssender.auth.service;

import com.azure.core.credential.TokenCredential;
import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.sundsvall.teamssender.auth.integration.DatabaseTokenCache;
import se.sundsvall.teamssender.auth.integration.StaticTokenCredential;
import se.sundsvall.teamssender.auth.repository.ITokenCacheRepository;
import se.sundsvall.teamssender.configuration.AzureMultiConfig;

@Service
public class TokenService {

	private final AzureMultiConfig multiConfig;

	private final ITokenCacheRepository tokenCacheRepository;

	@Autowired
	public TokenService(AzureMultiConfig azureMultiConfig, ITokenCacheRepository tokenCacheRepository) {
		this.multiConfig = azureMultiConfig;
		this.tokenCacheRepository = tokenCacheRepository;
	}

	public ResponseEntity<String> exchangeAuthCodeForToken(String authCode, String municipalityId) throws Exception {
		AzureMultiConfig.AzureConfig config = getAzureConfig(municipalityId);

		IClientCredential clientSecret = ClientCredentialFactory.createFromSecret(config.getClientSecret());

		ConfidentialClientApplication app = ConfidentialClientApplication.builder(config.getClientId(), clientSecret)
			.authority(config.getAuthorityUrl())
			.setTokenCacheAccessAspect(new DatabaseTokenCache(config.getUser(), tokenCacheRepository))
			.build();

		AuthorizationCodeParameters parameters = AuthorizationCodeParameters
			.builder(authCode, new URI(config.getRedirectUri()))
			.scopes(Collections.singleton(config.getScopes()))
			.build();

		IAuthenticationResult result = app.acquireToken(parameters).get();

		SilentParameters silentParameters = SilentParameters.builder(Collections.singleton(config.getScopes()))
			.account(result.account())
			.build();

		app.acquireTokenSilently(silentParameters).get();

		return ResponseEntity.ok("Token successfully saved");
	}

	public String getAccessTokenForUser(String municipalityId) throws Exception {
		AzureMultiConfig.AzureConfig config = getAzureConfig(municipalityId);

		IClientCredential clientSecret = ClientCredentialFactory.createFromSecret(config.getClientSecret());

		ConfidentialClientApplication confApp = ConfidentialClientApplication.builder(config.getClientId(), clientSecret)
			.authority(config.getAuthorityUrl())
			.setTokenCacheAccessAspect(new DatabaseTokenCache(config.getUser(), tokenCacheRepository))
			.build();

		Set<IAccount> accounts = confApp.getAccounts().join();
		Optional<IAccount> account = accounts.stream().filter(a -> a.username().equals(config.getUser())).findFirst();

		SilentParameters silentParameters = SilentParameters.builder(Collections.singleton(config.getScopes()))
			.account(account.orElse(null))
			.build();

		IAuthenticationResult result = confApp.acquireTokenSilently(silentParameters).get();

		return result.accessToken();
	}

	public GraphServiceClient initializeGraphServiceClient(String municipalityId) throws Exception {
		String accessToken = getAccessTokenForUser(municipalityId);
		TokenCredential credential = new StaticTokenCredential(accessToken);

		return new GraphServiceClient(credential);
	}

	private AzureMultiConfig.AzureConfig getAzureConfig(String municipalityId) {
		AzureMultiConfig.AzureConfig config = multiConfig.getAd().get(municipalityId);

		if (config == null) {
			throw new IllegalArgumentException("No Azure config found for municipalityId: " + municipalityId);
		}

		return config;
	}

}
