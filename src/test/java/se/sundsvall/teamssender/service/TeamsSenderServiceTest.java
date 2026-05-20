package se.sundsvall.teamssender.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.integration.microsoftgraph.MicrosoftGraphIntegration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.teamssender.api.model.SendTeamsMessageRequest.MESSAGE_TYPE_NOTIFICATION;

@ExtendWith(MockitoExtension.class)
class TeamsSenderServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String RECIPIENT = "recipient@example.com";
	private static final String MESSAGE = "Hello, world!";
	private static final String TARGET_URL = "https://status.example.com/123";

	@Mock
	private MicrosoftGraphIntegration microsoftGraphIntegrationMock;

	@InjectMocks
	private TeamsSenderService service;

	@Test
	void notificationDelegatesToIntegration() {
		final var request = SendTeamsMessageRequest.create()
			.withMessageType(MESSAGE_TYPE_NOTIFICATION)
			.withRecipient(RECIPIENT)
			.withMessage(MESSAGE)
			.withTargetUrl(TARGET_URL);

		service.sendTeamsMessage(request, MUNICIPALITY_ID);

		verify(microsoftGraphIntegrationMock).sendNotification(MUNICIPALITY_ID, RECIPIENT, MESSAGE, TARGET_URL);
		verifyNoMoreInteractions(microsoftGraphIntegrationMock);
	}

	@Test
	void unknownMessageTypeIsBadRequest() {
		// The @OneOf validator on the request would normally catch this at the resource layer; the service-level
		// switch is the second line of defence, so test it directly.
		final var request = SendTeamsMessageRequest.create()
			.withMessageType("CHAT")
			.withRecipient(RECIPIENT)
			.withMessage(MESSAGE);

		assertThatThrownBy(() -> service.sendTeamsMessage(request, MUNICIPALITY_ID))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(BAD_REQUEST));

		verifyNoInteractions(microsoftGraphIntegrationMock);
	}
}
