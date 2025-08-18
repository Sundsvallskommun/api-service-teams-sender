package se.sundsvall.teamssender.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.microsoft.aad.msal4j.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import se.sundsvall.teamssender.auth.repository.ITokenCacheRepository;
import se.sundsvall.teamssender.configuration.AzureConfig;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

	@Mock
	private ITokenCacheRepository tokenCacheRepository;

	@Mock
	private AzureConfig multiConfig;

	@InjectMocks
	private TokenService tokenService;

	private final String authCode = "fake-auth-code";
	private final String municipalityId = "123";

	private IAuthenticationResult mockResult;

	@BeforeEach
	void setup() throws Exception {
		// Mock AzureConfig
		AzureConfig.Azure fakeAzure = new AzureConfig.Azure();
		fakeAzure.setClientId("fake-client-id");
		fakeAzure.setTenantId("fake-tenant-id");
		fakeAzure.setClientSecret("fake-client-secret");
		fakeAzure.setAuthorityUrl("https://login.microsoftonline.com/fake-tenant-id");
		fakeAzure.setRedirectUri("http://localhost");
		fakeAzure.setScopes("user.read");

		Map<String, AzureConfig.Azure> fakeMap = Map.of(municipalityId, fakeAzure);
		when(multiConfig.getAd()).thenReturn(fakeMap);

		// Mock IAuthenticationResult
		mockResult = mock(IAuthenticationResult.class);
		when(mockResult.accessToken()).thenReturn("fake-access-token");
		when(mockResult.idToken()).thenReturn("fake-id-token");
	}

	@Test
	void exchangeAuthCodeForToken_shouldReturnSuccess() throws Exception {
		try (MockedStatic<ConfidentialClientApplication> mockedStatic = Mockito.mockStatic(ConfidentialClientApplication.class)) {

			// Mock builder och app
			ConfidentialClientApplication.Builder mockBuilder = mock(ConfidentialClientApplication.Builder.class);
			ConfidentialClientApplication mockApp = mock(ConfidentialClientApplication.class);

			// Mock static builder method
			mockedStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any(IClientCredential.class)))
				.thenReturn(mockBuilder);

			// Mock builder chain
			when(mockBuilder.authority(any(String.class))).thenReturn(mockBuilder);
			when(mockBuilder.setTokenCacheAccessAspect(any(ITokenCacheAccessAspect.class))).thenReturn(mockBuilder);
			when(mockBuilder.build()).thenReturn(mockApp);

			// Mock app methods
			when(mockApp.acquireToken(any(AuthorizationCodeParameters.class)))
				.thenReturn(CompletableFuture.completedFuture(mockResult));
			when(mockApp.acquireTokenSilently(any(SilentParameters.class)))
				.thenReturn(CompletableFuture.completedFuture(mockResult));

			// Kontrollera att authorityUrl inte är null
			assertThat(multiConfig.getAd().get(municipalityId).getAuthorityUrl()).isNotNull();

			// Kör testet
			ResponseEntity<String> response = tokenService.exchangeAuthCodeForToken(authCode, municipalityId);

			assertThat(response.getBody()).isEqualTo("Token successfully saved");

			// Verifiera att token sparas korrekt
			verify(tokenCacheRepository, times(1)).save(argThat(entity -> municipalityId.equals(entity.getUserId()) &&
				entity.getCacheData() != null &&
				entity.getCacheData().length > 0));
		}
	}
}
