package se.sundsvall.teamssender.configuration;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import se.sundsvall.teamssender.integration.db.TokenCacheRepository;
import se.sundsvall.teamssender.integration.microsoftgraph.AzureAdTokenService;
import se.sundsvall.teamssender.integration.microsoftgraph.StaticTokenCredential;

@Configuration
@Profile("mock")
public class MockTokenServiceConfiguration {

	@Value("${graph.base-url}")
	private String graphBaseUrl;

	@Bean
	@Primary
	public AzureAdTokenService mockTokenService(final AzureConfig azureConfig, final TokenCacheRepository tokenCacheRepository) {

		return new AzureAdTokenService(azureConfig, tokenCacheRepository) {

			@Override
			public String getAccessTokenForUser(final String municipalityId) {
				return "mock-access-token";
			}

			@Override
			public ResponseEntity<String> exchangeAuthCodeForToken(final String code, final String municipalityId) {
				return ResponseEntity.ok("mock-token-success");
			}

			@Override
			public GraphServiceClient initializeGraphServiceClient(final String municipalityId) {
				final TokenCredential credential = new StaticTokenCredential("mock-access-token");
				final GraphServiceClient client = new GraphServiceClient(credential);

				try {
					client.getRequestAdapter().setBaseUrl(graphBaseUrl);
				} catch (final Exception ignored) {
					// keep the SDK default base url
				}

				return client;
			}
		};
	}
}
