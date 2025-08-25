package se.sundsvall.teamssender.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.teamssender.TestDataFactory.createValidSendTeamsMessageRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.ConstraintViolationProblemModule;
import org.zalando.problem.violations.Violation;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.configuration.AzureConfig;
import se.sundsvall.teamssender.service.TeamsSenderService;

@WebMvcTest(controllers = TeamsSenderResource.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
	ProblemModule.class, ConstraintViolationProblemModule.class
})
class TeamsSenderResourceFailureTests {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH_TEMPLATE = "/{municipalityId}/teams/messages";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TeamsSenderService teamsSenderService;

	@MockitoBean
	private AzureConfig azureConfig;

	@ParameterizedTest
	@MethodSource({
		"requestInvalidRecipient", "requestInvalidMessage"
	})
	void sendTeamsMessageWithInvalidRequest(final SendTeamsMessageRequest request, final String badArgument,
		final String expectedMessage) throws Exception {

		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(PATH_TEMPLATE, MUNICIPALITY_ID)
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andReturn();

		String body = mvcResult.getResponse().getContentAsString();
		assertThat(body).isNotBlank();

		ConstraintViolationProblem response = objectMapper.readValue(body, ConstraintViolationProblem.class);

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

		return Stream.of(Arguments.of(request, "recipient", "must be a well-formed email address"));
	}

	private static Stream<Arguments> requestInvalidMessage() {
		var request = createValidSendTeamsMessageRequest();
		request.setMessage("");

		return Stream.of(Arguments.of(request, "message", "must not be blank"));
	}

	@Test
	void sendTeamsMessageWithFaultyMunicipalityId() throws Exception {
		var request = createValidSendTeamsMessageRequest();

		String faultyMunicipality = "22-81";

		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(PATH_TEMPLATE, faultyMunicipality)
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andReturn();

		String body = mvcResult.getResponse().getContentAsString();
		assertThat(body).isNotBlank();

		ConstraintViolationProblem response = objectMapper.readValue(body, ConstraintViolationProblem.class);

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(Tuple.tuple("sendTeamsMessage.municipalityId", "not a valid municipality ID"));
	}
}
