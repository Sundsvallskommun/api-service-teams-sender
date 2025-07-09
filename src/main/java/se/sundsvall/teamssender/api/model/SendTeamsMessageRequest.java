package se.sundsvall.teamssender.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Request model for sending a Teams message")
public class SendTeamsMessageRequest {

	@NotBlank
	@Schema(description = "The user ID or email to send the message to", example = "first.last@municipaladress.com", required = true)
	private String user;

	@NotBlank
	@Schema(description = "The user ID or email to send the message to", example = "first.last@municipaladress.com", required = true)
	private String sender;

	@Schema(description = "The title of the message", example = "Weekly Update", required = true)
	private String title;

	@NotBlank
	@Schema(description = "The content of the message", example = "Don't forget the meeting at 3 PM", required = true)
	private String message;

	public SendTeamsMessageRequest(String user, String sender, String title, String message) {
		this.user = user;
		this.sender = sender;
		this.title = title;
		this.message = message;
	}
}
