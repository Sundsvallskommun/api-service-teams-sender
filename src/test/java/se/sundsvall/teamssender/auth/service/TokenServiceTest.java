package se.sundsvall.teamssender.auth.service;

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
import se.sundsvall.teamssender.auth.integration.DatabaseTokenCache;
import se.sundsvall.teamssender.auth.model.TokenCacheEntity;
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
	void exchangeAuthCodeForToken_shouldReturnSuccess() throws Exception {
		// Mocka AzureConfig
		AzureConfig.Azure azure = new AzureConfig.Azure();
		azure.setClientId("fake-client-id");
		azure.setTenantId("fake-tenant-id");
		azure.setClientSecret("fake-client-secret");
		azure.setAuthorityUrl("https://login.microsoftonline.com/fake-tenant-id");
		azure.setRedirectUri("https://localhost/fake-redirect");
		azure.setScopes("user.read");
		when(azureConfig.getAd()).thenReturn(Map.of("municipality1", azure));

		// Mocka MSAL
		ConfidentialClientApplication.Builder mockBuilder = mock(ConfidentialClientApplication.Builder.class);
		ConfidentialClientApplication.Builder mockAuthorityBuilder = mock(ConfidentialClientApplication.Builder.class);
		ConfidentialClientApplication mockApp = mock(ConfidentialClientApplication.class);

		IAuthenticationResult mockResult = mock(IAuthenticationResult.class);
		IAccount mockAccount = mock(IAccount.class);
		when(mockResult.account()).thenReturn(mockAccount);
		when(mockResult.accessToken()).thenReturn("fake-access-token");

		when(mockApp.acquireToken(any(AuthorizationCodeParameters.class)))
			.thenReturn(CompletableFuture.completedFuture(mockResult));

		when(mockBuilder.authority(anyString())).thenReturn(mockAuthorityBuilder);
		when(mockAuthorityBuilder.setTokenCacheAccessAspect(any())).thenReturn(mockAuthorityBuilder);
		when(mockAuthorityBuilder.build()).thenReturn(mockApp);

		// Mocka acquireTokenSilently
		when(mockApp.acquireTokenSilently(any(SilentParameters.class)))
			.thenReturn(CompletableFuture.completedFuture(mockResult));

		// Skapa spy på DatabaseTokenCache så save() anropas
		DatabaseTokenCache realCache = new DatabaseTokenCache("municipality1", tokenCacheRepository);
		DatabaseTokenCache spyCache = spy(realCache);
		doAnswer(invocation -> {
			// När afterCacheAccess anropas, ska token sparas
			tokenCacheRepository.save(new TokenCacheEntity("municipality1", "fake-cache".getBytes()));
			return null;
		}).when(spyCache).afterCacheAccess(any());

		try (var mockedStatic = Mockito.mockStatic(ConfidentialClientApplication.class)) {
			mockedStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any(IClientCredential.class)))
				.thenReturn(mockBuilder);

			// Anropa metoden vi testar
			tokenService.exchangeAuthCodeForToken("fakeAuthCode", "municipality1");

			// Verifiera att token cache sparades
			verify(tokenCacheRepository, atLeastOnce()).save(any(TokenCacheEntity.class));
		}
	}
}
