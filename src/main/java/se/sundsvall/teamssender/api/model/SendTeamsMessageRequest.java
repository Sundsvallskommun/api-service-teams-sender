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
	private String User;

	@NotBlank
	@Schema(description = "The user ID or email to send the message to", example = "first.last@municipaladress.com", required = true)
	private String Sender;

	@NotBlank
	@Schema(description = "The content of the message", example = "Don't forget the meeting at 3 PM", required = true)
	private String Message;



	public SendTeamsMessageRequest(String user, String sender, String message) {
		this.User = user;
		this.Sender = sender;
		this.Message = message;
	}
}
