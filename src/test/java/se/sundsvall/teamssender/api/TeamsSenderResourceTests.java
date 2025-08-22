package se.sundsvall.teamssender.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.teamssender.TestDataFactory.createValidSendTeamsMessageRequest;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.teamssender.Application;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.service.TeamsSenderService;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class TeamsSenderResourceTests {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/teams/messages";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private TeamsSenderService teamsSenderService;

	@Captor
	private ArgumentCaptor<String> municipalityIdCaptor;
	@Captor
	private ArgumentCaptor<SendTeamsMessageRequest> requestCaptor;

	@Test
	void sendTeamsMessage_success() throws Exception {
		var request = createValidSendTeamsMessageRequest();

		webTestClient.post().uri(PATH).contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(teamsSenderService).sendTeamsMessage(requestCaptor.capture(), municipalityIdCaptor.capture());

		assertThat(municipalityIdCaptor.getValue()).isEqualTo(MUNICIPALITY_ID);
		assertThat(requestCaptor.getValue()).usingRecursiveComparison().isEqualTo(request);
	}
}
