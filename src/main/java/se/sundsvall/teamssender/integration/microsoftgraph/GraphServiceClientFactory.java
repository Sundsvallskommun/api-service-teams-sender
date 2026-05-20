package se.sundsvall.teamssender.integration.microsoftgraph;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.teamssender.configuration.AzureConfig;
import se.sundsvall.teamssender.configuration.TeamsProperties;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Builds and caches one {@link GraphServiceClient} per municipality. Each client is wired with an Azure
 * {@code ClientSecretCredential} (OAuth2 client credentials) — the same pattern used by api-service-email-sender. The
 * Azure SDK handles access-token caching and refresh internally, so no MSAL or local token cache is needed.
 *
 * <p>
 * Tests can override the per-client base URL via {@link TeamsProperties#getGraphBaseUrl()}, which gets applied to the
 * client's request adapter once after construction. In production this property is unset and the SDK's default
 * ({@code https://graph.microsoft.com/v1.0}) is used.
 */
@Component
public class GraphServiceClientFactory {

	private final AzureConfig azureConfig;
	private final TeamsProperties teamsProperties;
	private final Map<String, GraphServiceClient> clientsByMunicipality = new ConcurrentHashMap<>();

	public GraphServiceClientFactory(final AzureConfig azureConfig, final TeamsProperties teamsProperties) {
		this.azureConfig = azureConfig;
		this.teamsProperties = teamsProperties;
	}

	public GraphServiceClient forMunicipality(final String municipalityId) {
		return clientsByMunicipality.computeIfAbsent(municipalityId, this::buildClient);
	}

	private GraphServiceClient buildClient(final String municipalityId) {
		final AzureConfig.Azure config = Optional.ofNullable(azureConfig.getAd())
			.map(map -> map.get(municipalityId))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No Azure configuration found for municipalityId: " + municipalityId));

		final var credential = buildCredential(config);
		final var client = new GraphServiceClient(credential, teamsProperties.getScope());
		Optional.ofNullable(teamsProperties.getGraphBaseUrl())
			.ifPresent(baseUrl -> client.getRequestAdapter().setBaseUrl(baseUrl));
		return client;
	}

	private TokenCredential buildCredential(final AzureConfig.Azure config) {
		// Test hook: short-circuit Azure AD when a static token is configured (IT profile). Production leaves this unset
		// and the real ClientSecretCredential handles token acquisition + refresh internally.
		final String staticToken = teamsProperties.getStaticAccessToken();
		if (staticToken != null) {
			return request -> Mono.just(new AccessToken(staticToken, OffsetDateTime.now().plusHours(1)));
		}
		return new ClientSecretCredentialBuilder()
			.tenantId(config.getTenantId())
			.clientId(config.getClientId())
			.clientSecret(config.getClientSecret())
			.build();
	}
}
