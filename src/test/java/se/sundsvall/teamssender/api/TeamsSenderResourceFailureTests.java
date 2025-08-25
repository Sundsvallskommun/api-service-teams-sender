package se.sundsvall.teamssender.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.teamssender.TestDataFactory.createValidSendTeamsMessageRequest;

import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.teamssender.Application;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.configuration.AzureConfig;
import se.sundsvall.teamssender.service.TeamsSenderService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TeamsSenderResourceFailureTests {
	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/teams/messages";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private TeamsSenderService teamsSenderService;

	@MockitoBean
	private AzureConfig azureConfig;

	@ParameterizedTest
	@MethodSource({
		"requestInvalidRecipient", "requestInvalidMessage"
	})
	void sendTeamsMessageWithInvalidRequest(final SendTeamsMessageRequest request, final String badArgument, final String expectedMessage) {
		var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build())
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple(badArgument, expectedMessage));
		});
	}

	private static Stream<Arguments> requestInvalidRecipient() {
		var request = createValidSendTeamsMessageRequest();
		request.setRecipient("not a valid email address");
		request.setMessage("message");

		return Stream.of(
			Arguments.of(request, "recipient", "must be a well-formed email address"));
	}

	private static Stream<Arguments> requestInvalidMessage() {
		var request = createValidSendTeamsMessageRequest();
		request.setMessage("");

		return Stream.of(
			Arguments.of(request, "message", "must not be blank"));
	}

	@Test
	void sendTeamsMessageWithFaultyMunicipalityId() {
		var request = createValidSendTeamsMessageRequest();

		var response = webTestClient.post().uri(PATH.replace(MUNICIPALITY_ID, "22-81")).contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(Tuple.tuple("sendTeamsMessage.municipalityId", "not a valid municipality ID"));
	}
}
