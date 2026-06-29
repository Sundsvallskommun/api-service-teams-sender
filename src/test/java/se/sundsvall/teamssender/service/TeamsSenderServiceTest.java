package se.sundsvall.teamssender.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.integration.microsoftgraph.MicrosoftGraphIntegration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class TeamsSenderServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String RECIPIENT = "recipient@example.com";
	private static final String MESSAGE = "Hello, world!";

	@Mock
	private MicrosoftGraphIntegration microsoftGraphIntegrationMock;

	@InjectMocks
	private TeamsSenderService service;

	@Test
	void sendTeamsMessageDelegatesToIntegration() {
		final var request = SendTeamsMessageRequest.create()
			.withRecipient(RECIPIENT)
			.withMessage(MESSAGE);

		service.sendTeamsMessage(request, MUNICIPALITY_ID);

		verify(microsoftGraphIntegrationMock).sendChatMessage(MUNICIPALITY_ID, RECIPIENT, MESSAGE);
		verifyNoMoreInteractions(microsoftGraphIntegrationMock);
	}
}
