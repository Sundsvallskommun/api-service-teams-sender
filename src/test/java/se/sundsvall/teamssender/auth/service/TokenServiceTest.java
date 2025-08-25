package se.sundsvall.teamssender.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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
		azure.setRedirectUri("https://localhost/fake-redirect"); // korrekt URI
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
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals("Token successfully saved", response.getBody());
		}
	}

	@Test
	void exchangeAuthCodeForToken_shouldThrowException_whenMunicipalityNotFound() {
		// Arrange
		when(azureConfig.getAd()).thenReturn(Collections.emptyMap());

		// Act & Assert
		assertThrows(IllegalArgumentException.class,
			() -> tokenService.exchangeAuthCodeForToken("fakeAuthCode", "unknownMunicipality"));
	}

	@Test
	void getAccessTokenForUser_shouldThrowException_whenMunicipalityNotFound() {
		// Arrange
		when(azureConfig.getAd()).thenReturn(Collections.emptyMap());

		// Act & Assert
		assertThrows(IllegalArgumentException.class,
			() -> tokenService.getAccessTokenForUser("unknownMunicipality"));
	}

	@Test
	void getAccessTokenForUser_shouldHandleMissingAccount() throws Exception {
		AzureConfig.Azure azure = new AzureConfig.Azure();
		azure.setClientId("client");
		azure.setClientSecret("secret");
		azure.setAuthorityUrl("https://authority");
		azure.setUser("user@example.com");
		azure.setScopes("user.read");

		when(azureConfig.getAd()).thenReturn(Map.of("2281", azure));

		ConfidentialClientApplication mockApp = mock(ConfidentialClientApplication.class);
		when(mockApp.getAccounts()).thenReturn(CompletableFuture.completedFuture(Collections.emptySet()));

		IAuthenticationResult result = mock(IAuthenticationResult.class);
		when(result.accessToken()).thenReturn("token");

		when(mockApp.acquireTokenSilently(any(SilentParameters.class)))
			.thenReturn(CompletableFuture.completedFuture(result));

		var builder = mock(ConfidentialClientApplication.Builder.class);
		when(builder.authority(anyString())).thenReturn(builder);
		when(builder.setTokenCacheAccessAspect(any())).thenReturn(builder);
		when(builder.build()).thenReturn(mockApp);

		try (var mocked = mockStatic(ConfidentialClientApplication.class)) {
			mocked.when(() -> ConfidentialClientApplication.builder(any(), any()))
				.thenReturn(builder);

			String accessToken = tokenService.getAccessTokenForUser("2281");

			assertThat(accessToken).isEqualTo("token");
		}
	}

	@Test
	void initializeGraphServiceClient_returnsClient() throws Exception {
		String municipalityId = "2281";
		String fakeToken = "fake-token";

		TokenService spyService = spy(tokenService);
		doReturn(fakeToken).when(spyService).getAccessTokenForUser(municipalityId);

		GraphServiceClient client = spyService.initializeGraphServiceClient(municipalityId);

		assertThat(client).isNotNull();
	}

	@Test
	void initializeGraphServiceClient_shouldThrow_whenAccessTokenFails() throws Exception {
		TokenService spy = spy(tokenService);
		doThrow(new RuntimeException("Token error")).when(spy).getAccessTokenForUser("2281");

		assertThrows(RuntimeException.class, () -> spy.initializeGraphServiceClient("2281"));
	}
}
