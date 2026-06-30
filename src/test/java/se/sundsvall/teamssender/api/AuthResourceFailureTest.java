package se.sundsvall.teamssender.api;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.teamssender.Application;
import se.sundsvall.teamssender.integration.microsoftgraph.AzureAdTokenService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class AuthResourceFailureTest {

	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String INVALID_MUNICIPALITY_ID = "not-a-valid-id";
	private static final String LOGIN_PATH = "/api/teamssender/{municipalityId}/login";
	private static final String CALLBACK_PATH = "/api/teamssender/callback";

	@MockitoBean
	private AzureAdTokenService tokenServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void loginWithInvalidMunicipalityId() {
		final var problem = webTestClient.get()
			.uri(builder -> builder.path(LOGIN_PATH).build(Map.of("municipalityId", INVALID_MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(problem.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("login.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(tokenServiceMock);
	}

	@Test
	void callbackWithBlankCode() {
		final var problem = webTestClient.get()
			.uri(builder -> builder.path(CALLBACK_PATH).queryParam("code", " ").queryParam("state", VALID_MUNICIPALITY_ID).build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(problem.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("callback.code", "must not be blank"));

		verifyNoInteractions(tokenServiceMock);
	}

	@Test
	void callbackWithInvalidState() {
		final var problem = webTestClient.get()
			.uri(builder -> builder.path(CALLBACK_PATH).queryParam("code", "auth-code-123").queryParam("state", INVALID_MUNICIPALITY_ID).build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(problem.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("callback.state", "not a valid municipality ID"));

		verifyNoInteractions(tokenServiceMock);
	}

	@Test
	void callbackWithMissingCode() {
		webTestClient.get()
			.uri(builder -> builder.path(CALLBACK_PATH).queryParam("state", VALID_MUNICIPALITY_ID).build())
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(tokenServiceMock);
	}

	@Test
	void callbackWithMissingState() {
		webTestClient.get()
			.uri(builder -> builder.path(CALLBACK_PATH).queryParam("code", "auth-code-123").build())
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(tokenServiceMock);
	}
}
