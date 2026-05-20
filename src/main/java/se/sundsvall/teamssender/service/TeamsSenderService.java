package se.sundsvall.teamssender.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.integration.microsoftgraph.MicrosoftGraphIntegration;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.teamssender.api.model.SendTeamsMessageRequest.MESSAGE_TYPE_NOTIFICATION;

@Service
public class TeamsSenderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TeamsSenderService.class);

	private final MicrosoftGraphIntegration microsoftGraphIntegration;

	public TeamsSenderService(final MicrosoftGraphIntegration microsoftGraphIntegration) {
		this.microsoftGraphIntegration = microsoftGraphIntegration;
	}

	public void sendTeamsMessage(final SendTeamsMessageRequest request, final String municipalityId) {
		LOGGER.info("Sending Teams message of type {} via Microsoft Graph", request.getMessageType());

		// The @OneOf validator restricts messageType to NOTIFICATION today; the explicit switch ensures that adding a
		// new value to the validator's allowed list without wiring it here fails loud (not silent fallback).
		if (request.getMessageType().equals(MESSAGE_TYPE_NOTIFICATION)) {
			microsoftGraphIntegration.sendNotification(
				municipalityId, request.getRecipient(), request.getMessage(), request.getTargetUrl());
		} else {
			throw Problem.valueOf(BAD_REQUEST, "Unsupported messageType: " + request.getMessageType());
		}
	}
}
