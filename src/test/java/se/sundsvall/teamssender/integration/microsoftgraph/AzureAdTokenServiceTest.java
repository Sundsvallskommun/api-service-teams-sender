package se.sundsvall.teamssender.integration.microsoftgraph;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.teamssender.configuration.AzureConfig;
import se.sundsvall.teamssender.integration.db.TokenCacheRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class AzureAdTokenServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String UNKNOWN_MUNICIPALITY_ID = "9999";
	private static final String LOGIN_URL = "https://login.microsoftonline.com";

	@Mock
	private AzureConfig azureConfigMock;

	@Mock
	private TokenCacheRepository tokenCacheRepositoryMock;

	private AzureAdTokenService service;

	@BeforeEach
	void setUp() {
		service = new AzureAdTokenService(azureConfigMock, tokenCacheRepositoryMock);
	}

	@Test
	void getLoginUrlReturnsConfiguredUrl() {
		final var azure = new AzureConfig.Azure();
		azure.setLoginUrl(LOGIN_URL);
		when(azureConfigMock.getAd()).thenReturn(Map.of(MUNICIPALITY_ID, azure));

		assertThat(service.getLoginUrl(MUNICIPALITY_ID)).isEqualTo(LOGIN_URL);
	}

	@Test
	void getLoginUrlForUnknownMunicipalityThrowsNotFound() {
		when(azureConfigMock.getAd()).thenReturn(Map.of());

		assertThatThrownBy(() -> service.getLoginUrl(UNKNOWN_MUNICIPALITY_ID))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(NOT_FOUND))
			.hasMessageContaining(UNKNOWN_MUNICIPALITY_ID);
	}

	@Test
	void getSenderUserReturnsConfiguredUser() {
		final var azure = new AzureConfig.Azure();
		azure.setUser("Sender@Example.com");
		when(azureConfigMock.getAd()).thenReturn(Map.of(MUNICIPALITY_ID, azure));

		assertThat(service.getSenderUser(MUNICIPALITY_ID)).isEqualTo("Sender@Example.com");
	}

	@Test
	void getSenderUserForUnknownMunicipalityThrowsNotFound() {
		when(azureConfigMock.getAd()).thenReturn(Map.of());

		assertThatThrownBy(() -> service.getSenderUser(UNKNOWN_MUNICIPALITY_ID))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(NOT_FOUND));
	}

	@Test
	void getAccessTokenForUserWithoutCachedLoginThrowsUnauthorizedWithLoginHint() {
		final var azure = new AzureConfig.Azure();
		azure.setUser("sender@example.com");
		azure.setClientId("client-id");
		azure.setClientSecret("client-secret");
		azure.setAuthorityUrl("https://login.microsoftonline.com/common/");
		azure.setScopes("https://graph.microsoft.com/.default");
		when(azureConfigMock.getAd()).thenReturn(Map.of(MUNICIPALITY_ID, azure));
		when(tokenCacheRepositoryMock.findById("sender@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getAccessTokenForUser(MUNICIPALITY_ID))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(UNAUTHORIZED))
			.hasMessageContaining("No cached login found for user 'sender@example.com'")
			.hasMessageContaining("/api/teamssender/%s/login".formatted(MUNICIPALITY_ID));
	}

	@Test
	void getAccessTokenForUserForUnknownMunicipalityThrowsNotFound() {
		when(azureConfigMock.getAd()).thenReturn(Map.of());

		assertThatThrownBy(() -> service.getAccessTokenForUser(UNKNOWN_MUNICIPALITY_ID))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(NOT_FOUND));
	}

	@Test
	void exchangeAuthCodeForTokenForUnknownMunicipalityThrowsNotFound() {
		when(azureConfigMock.getAd()).thenReturn(Map.of());

		assertThatThrownBy(() -> service.exchangeAuthCodeForToken("code", UNKNOWN_MUNICIPALITY_ID))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(NOT_FOUND));
	}
}
