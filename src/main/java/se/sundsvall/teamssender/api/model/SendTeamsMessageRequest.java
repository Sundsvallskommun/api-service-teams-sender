package se.sundsvall.teamssender.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendTeamsMessageRequest {
	private String user;
	private String title;
	private String message;

	public SendTeamsMessageRequest() {}

	public SendTeamsMessageRequest(String user, String title, String message) {
		this.user = user;
		this.title = title;
		this.message = message;
	}

}
