package se.sundsvall.teamssender.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.microsoft.aad.msal4j.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import se.sundsvall.teamssender.auth.repository.ITokenCacheRepository;
import se.sundsvall.teamssender.configuration.AzureConfig;

class TokenServiceTest {

	private TokenService tokenService;
	private ITokenCacheRepository tokenCacheRepository;
	private AzureConfig azureConfig;

	@BeforeEach
	void setUp() {
		tokenCacheRepository = mock(ITokenCacheRepository.class);

		// Skapa en fejk Azure-konfiguration
		AzureConfig.Azure mockAzure = new AzureConfig.Azure();
		mockAzure.setClientId("client-id");
		mockAzure.setClientSecret("secret");
		mockAzure.setAuthorityUrl("https://login.microsoftonline.com/test");
		mockAzure.setRedirectUri("http://localhost/callback");
		mockAzure.setScopes("User.Read");
		mockAzure.setUser("testuser@example.com");

		azureConfig = new AzureConfig();
		Map<String, AzureConfig.Azure> map = new HashMap<>();
		map.put("municipality-1", mockAzure);
		azureConfig.setAd(map); // här sätter vi vår fejkade konfiguration

		tokenService = new TokenService(azureConfig, tokenCacheRepository);
	}

	@Test
	void exchangeAuthCodeForToken_shouldReturnSuccess() throws Exception {
		// Arrange
		String authCode = "dummy-code";
		String municipalityId = "municipality-1";

		IAuthenticationResult mockResult = mock(IAuthenticationResult.class);
		IAccount mockAccount = mock(IAccount.class);
		when(mockResult.account()).thenReturn(mockAccount);

		// Mocka konstruktionen av ConfidentialClientApplication
		try (MockedConstruction<ConfidentialClientApplication.Builder> ignored = Mockito.mockConstruction(ConfidentialClientApplication.Builder.class, (builder, context) -> {
			ConfidentialClientApplication mockApp = mock(ConfidentialClientApplication.class);
			when(builder.build()).thenReturn(mockApp);

			// Använd den redan deklarerade mockResult och mockAccount
			when(mockApp.acquireToken(any(AuthorizationCodeParameters.class)))
				.thenReturn(CompletableFuture.completedFuture(mockResult));
			when(mockApp.acquireTokenSilently(any(SilentParameters.class)))
				.thenReturn(CompletableFuture.completedFuture(mockResult));
		})) {

			// Act
			ResponseEntity<String> response = tokenService.exchangeAuthCodeForToken(authCode, municipalityId);

			// Assert
			assertThat(response.getBody()).isEqualTo("Token successfully saved");
		}
	}

}
