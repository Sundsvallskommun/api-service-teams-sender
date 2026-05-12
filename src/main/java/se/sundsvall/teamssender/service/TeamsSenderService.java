package se.sundsvall.teamssender.service;

import org.springframework.stereotype.Service;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.integration.microsoftgraph.MicrosoftGraphIntegration;

@Service
public class TeamsSenderService {

	private final MicrosoftGraphIntegration microsoftGraphIntegration;

	public TeamsSenderService(final MicrosoftGraphIntegration microsoftGraphIntegration) {
		this.microsoftGraphIntegration = microsoftGraphIntegration;
	}

	public void sendTeamsMessage(final SendTeamsMessageRequest request, final String municipalityId) {
		microsoftGraphIntegration.sendChatMessage(municipalityId, request.getRecipient(), request.getMessage());
	}
}
