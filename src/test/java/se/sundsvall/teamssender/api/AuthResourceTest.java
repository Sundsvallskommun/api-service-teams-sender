package se.sundsvall.teamssender.api;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.teamssender.Application;
import se.sundsvall.teamssender.integration.microsoftgraph.AzureAdTokenService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class AuthResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String LOGIN_PATH = "/api/teamssender/{municipalityId}/login";
	private static final String CALLBACK_PATH = "/api/teamssender/callback";

	@MockitoBean
	private AzureAdTokenService tokenServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void loginRedirectsToConfiguredLoginUrl() {
		final var loginUrl = "https://login.microsoftonline.com/tenant/oauth2/v2.0/authorize";
		when(tokenServiceMock.getLoginUrl(MUNICIPALITY_ID)).thenReturn(loginUrl);

		webTestClient.get()
			.uri(builder -> builder.path(LOGIN_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isFound()
			.expectHeader().location(loginUrl);

		verify(tokenServiceMock).getLoginUrl(MUNICIPALITY_ID);
		verifyNoMoreInteractions(tokenServiceMock);
	}

	@Test
	void callbackExchangesCodeForToken() {
		final var code = "auth-code-123";
		when(tokenServiceMock.exchangeAuthCodeForToken(code, MUNICIPALITY_ID))
			.thenReturn(ResponseEntity.ok("Token successfully saved"));

		webTestClient.get()
			.uri(builder -> builder.path(CALLBACK_PATH).queryParam("code", code).queryParam("state", MUNICIPALITY_ID).build())
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class).isEqualTo("Token successfully saved");

		verify(tokenServiceMock).exchangeAuthCodeForToken(code, MUNICIPALITY_ID);
		verifyNoMoreInteractions(tokenServiceMock);
	}
}
