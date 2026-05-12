package se.sundsvall.teamssender.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@Schema(description = "Request model for sending a Teams message")
public class SendTeamsMessageRequest {

	@NotBlank
	@Schema(description = "The user ID or email to send the message to", examples = "first.last@municipaladress.com")
	private String recipient;

	@NotBlank
	@Schema(description = "The content of the message", examples = "Don't forget the meeting at 3 PM")
	private String message;

	public SendTeamsMessageRequest() {}

	public SendTeamsMessageRequest(final String recipient, final String message) {
		this.recipient = recipient;
		this.message = message;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(final String recipient) {
		this.recipient = recipient;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final SendTeamsMessageRequest that)) {
			return false;
		}
		return Objects.equals(recipient, that.recipient) && Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(recipient, message);
	}

	@Override
	public String toString() {
		return "SendTeamsMessageRequest{recipient='" + recipient + "', message='" + message + "'}";
	}
}
