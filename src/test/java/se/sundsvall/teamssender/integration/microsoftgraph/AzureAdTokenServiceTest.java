package se.sundsvall.teamssender.integration.microsoftgraph;

import java.util.Map;
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
