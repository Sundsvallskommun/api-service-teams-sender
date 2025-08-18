package se.sundsvall.teamssender;

import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;

public class TestDataFactory {

	public static SendTeamsMessageRequest createValidSendTeamsMessageRequest() {
		return new SendTeamsMessageRequest("recipient@example.com", "message");
	}
}
