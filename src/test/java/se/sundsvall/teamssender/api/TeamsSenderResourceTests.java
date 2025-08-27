package se.sundsvall.teamssender.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static se.sundsvall.teamssender.TestDataFactory.createValidSendTeamsMessageRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.service.TeamsSenderService;

@WebMvcTest(controllers = TeamsSenderResource.class)
@AutoConfigureMockMvc(addFilters = false)
class TeamsSenderResourceTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TeamsSenderService teamsSenderService;

	@Test
	void returns204OnSuccess() throws Exception {
		final String municipalityId = "2281";

		var request = createValidSendTeamsMessageRequest();

		mockMvc.perform(post("/" + municipalityId + "/teams/messages")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		ArgumentCaptor<SendTeamsMessageRequest> reqCaptor = ArgumentCaptor.forClass(SendTeamsMessageRequest.class);
		ArgumentCaptor<String> muniCaptor = ArgumentCaptor.forClass(String.class);

		verify(teamsSenderService).sendTeamsMessage(reqCaptor.capture(), muniCaptor.capture());
		assertThat(muniCaptor.getValue()).isEqualTo(municipalityId);
		assertThat(reqCaptor.getValue()).usingRecursiveComparison().isEqualTo(request);
	}
}
