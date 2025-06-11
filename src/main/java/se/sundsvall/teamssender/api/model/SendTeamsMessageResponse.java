package se.sundsvall.teamssender.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class SendTeamsMessageResponse {
	private String chatId;
	private String user;
	private String title;
	private String message;

	public SendTeamsMessageResponse() {}

	public SendTeamsMessageResponse(String chatId, String user, String title, String message) {
		this.chatId = chatId;
		this.user = user;
		this.title = title;
		this.message = message;
	}
}
