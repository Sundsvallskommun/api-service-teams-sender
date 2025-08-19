package se.sundsvall.teamssender.auth.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.microsoft.aad.msal4j.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.teamssender.auth.repository.ITokenCacheRepository;
import se.sundsvall.teamssender.configuration.AzureConfig;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

	@Mock
	private ITokenCacheRepository tokenCacheRepository;

	@Mock
	private AzureConfig azureConfig;

	private TokenService tokenService;

	@BeforeEach
	void setup() {
		tokenService = new TokenService(azureConfig, tokenCacheRepository);
	}

	@Test
	void exchangeAuthCodeForToken_shouldReturnOkResponse() throws Exception {
		// Arrange
		AzureConfig.Azure azure = new AzureConfig.Azure();
		azure.setClientId("fake-client-id");
		azure.setTenantId("fake-tenant-id");
		azure.setClientSecret("fake-client-secret");
		azure.setAuthorityUrl("https://login.microsoftonline.com/fake-tenant-id");
		azure.setRedirectUri("https://localhost/fake-redirect");
		azure.setScopes("user.read");
		azure.setUser("fake-user");
		when(azureConfig.getAd()).thenReturn(Map.of("municipality1", azure));

		ConfidentialClientApplication.Builder mockBuilder = mock(ConfidentialClientApplication.Builder.class);
		ConfidentialClientApplication.Builder mockAuthorityBuilder = mock(ConfidentialClientApplication.Builder.class);
		ConfidentialClientApplication mockApp = mock(ConfidentialClientApplication.class);

		IAuthenticationResult mockResult = mock(IAuthenticationResult.class);
		IAccount mockAccount = mock(IAccount.class);
		when(mockResult.account()).thenReturn(mockAccount);

		when(mockApp.acquireToken(any(AuthorizationCodeParameters.class)))
			.thenReturn(CompletableFuture.completedFuture(mockResult));
		when(mockApp.acquireTokenSilently(any(SilentParameters.class)))
			.thenReturn(CompletableFuture.completedFuture(mockResult));

		when(mockBuilder.authority(anyString())).thenReturn(mockAuthorityBuilder);
		when(mockAuthorityBuilder.setTokenCacheAccessAspect(any())).thenReturn(mockAuthorityBuilder);
		when(mockAuthorityBuilder.build()).thenReturn(mockApp);

		try (var mockedStatic = Mockito.mockStatic(ConfidentialClientApplication.class)) {
			mockedStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any(IClientCredential.class)))
				.thenReturn(mockBuilder);

			// Act
			var response = tokenService.exchangeAuthCodeForToken("fakeAuthCode", "municipality1");

			// Assert
			assertEquals(200, response.getStatusCodeValue());
			assertEquals("Token successfully saved", response.getBody());
		}
	}
}
