package se.sundsvall.teamssender.api;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.teamssender.Application;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.service.TeamsSenderService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.teamssender.api.model.SendTeamsMessageRequest.MESSAGE_TYPE_NOTIFICATION;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class TeamsSenderResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/{municipalityId}/teams/messages";

	@MockitoBean
	private TeamsSenderService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void sendTeamsMessage() {
		final var request = SendTeamsMessageRequest.create()
			.withMessageType(MESSAGE_TYPE_NOTIFICATION)
			.withRecipient("recipient@example.com")
			.withMessage("Hello, world!")
			.withTargetUrl("https://status.example.com/123");

		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isNoContent();

		verify(serviceMock).sendTeamsMessage(request, MUNICIPALITY_ID);
		verifyNoMoreInteractions(serviceMock);
	}
}
