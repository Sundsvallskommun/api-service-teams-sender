package se.sundsvall.teamssender.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema

public class SendTeamsMessageResponse {

	@Schema(description = "The ID of the Teams chat", example = "19:abc123xyz@thread.tacv2")
	private String chatId;

	@Schema(description = "The user the message was sent to", example = "first.last@municipaladress.com")
	private String user;

	@Schema(description = "The title of the message", example = "Weekly Update")
	private String title;

	@Schema(description = "The content of the message", example = "Don't forget the meeting at 3 PM")
	private String message;

	public SendTeamsMessageResponse() {}

	public SendTeamsMessageResponse(String chatId, String user, String title, String message) {
		this.chatId = chatId;
		this.user = user;
		this.title = title;
		this.message = message;
	}
}
