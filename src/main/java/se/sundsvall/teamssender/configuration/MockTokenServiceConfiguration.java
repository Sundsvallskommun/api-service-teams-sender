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
import se.sundsvall.teamssender.service.StaticTokenCredential;
import se.sundsvall.teamssender.service.TokenService;

@Configuration
@Profile("mock")
public class MockTokenServiceConfiguration {

	@Value("${graph.base-url}")
	private String graphBaseUrl;

	@Bean
	@Primary
	public TokenService mockTokenService(AzureConfig azureConfig, TokenCacheRepository tokenCacheRepository) {

		return new TokenService(azureConfig, tokenCacheRepository) {

			@Override
			public String getAccessTokenForUser(String municipalityId) {
				return "mock-access-token";
			}

			@Override
			public ResponseEntity<String> exchangeAuthCodeForToken(String code, String municipalityId) {
				return ResponseEntity.ok("mock-token-success");
			}

			@Override
			public GraphServiceClient initializeGraphServiceClient(String municipalityId) {
				TokenCredential credential = new StaticTokenCredential("mock-access-token");
				GraphServiceClient client = new GraphServiceClient(credential);

				try {
					client.getRequestAdapter().setBaseUrl(graphBaseUrl);
				} catch (Exception ignored) {}

				return client;
			}
		};
	}
}
