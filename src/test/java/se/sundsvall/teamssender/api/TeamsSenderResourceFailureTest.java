package se.sundsvall.teamssender.api;

import java.util.Map;
import java.util.function.UnaryOperator;
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
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.service.TeamsSenderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.teamssender.api.model.SendTeamsMessageRequest.MESSAGE_TYPE_NOTIFICATION;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class TeamsSenderResourceFailureTest {

	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String INVALID_MUNICIPALITY_ID = "not-a-valid-id";
	private static final String PATH = "/{municipalityId}/teams/messages";

	@MockitoBean
	private TeamsSenderService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void invalidMunicipalityId() {
		final var problem = post(INVALID_MUNICIPALITY_ID, validRequest(r -> r));

		assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(problem.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("sendTeamsMessage.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(serviceMock);
	}

	@Test
	void unsupportedMessageType() {
		final var problem = post(VALID_MUNICIPALITY_ID, validRequest(r -> r.withMessageType("CHAT")));

		assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(problem.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("messageType", "must be one of: [NOTIFICATION]"));
		verifyNoInteractions(serviceMock);
	}

	@Test
	void invalidRecipientEmail() {
		final var problem = post(VALID_MUNICIPALITY_ID, validRequest(r -> r.withRecipient("not-an-email")));

		assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(problem.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("recipient", "must be a well-formed email address"));
		verifyNoInteractions(serviceMock);
	}

	@Test
	void blankMessage() {
		final var problem = post(VALID_MUNICIPALITY_ID, validRequest(r -> r.withMessage(" ")));

		assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(problem.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("message", "must not be blank"));
		verifyNoInteractions(serviceMock);
	}

	@Test
	void oversizeMessage() {
		final var problem = post(VALID_MUNICIPALITY_ID, validRequest(r -> r.withMessage("x".repeat(151))));

		assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(problem.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("message", "size must be between 0 and 150"));
		verifyNoInteractions(serviceMock);
	}

	@Test
	void invalidTargetUrl() {
		final var problem = post(VALID_MUNICIPALITY_ID, validRequest(r -> r.withTargetUrl("not-a-url")));

		assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(problem.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("targetUrl", "must be a valid URL"));
		verifyNoInteractions(serviceMock);
	}

	private static SendTeamsMessageRequest validRequest(final UnaryOperator<SendTeamsMessageRequest> tweak) {
		return tweak.apply(SendTeamsMessageRequest.create()
			.withMessageType(MESSAGE_TYPE_NOTIFICATION)
			.withRecipient("recipient@example.com")
			.withMessage("Hello, world!")
			.withTargetUrl("https://status.example.com/123"));
	}

	private ConstraintViolationProblem post(final String municipalityId, final SendTeamsMessageRequest request) {
		return webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", municipalityId)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();
	}
}
