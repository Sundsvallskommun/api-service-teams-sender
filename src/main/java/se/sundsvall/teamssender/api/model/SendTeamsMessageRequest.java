package se.sundsvall.teamssender.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import org.hibernate.validator.constraints.URL;
import se.sundsvall.dept44.common.validators.annotation.OneOf;

@Schema(description = "Request model for sending a Teams message")
public class SendTeamsMessageRequest {

	/**
	 * Currently the only accepted message type. New types (e.g. {@code "CHAT"}) can be added later without breaking
	 * existing callers — see the project plan / TODO for the hedge rationale.
	 */
	public static final String MESSAGE_TYPE_NOTIFICATION = "NOTIFICATION";

	@NotBlank
	@OneOf(MESSAGE_TYPE_NOTIFICATION)
	@Schema(description = "Kind of Teams message to send. Currently only NOTIFICATION (bell-icon activity notification) is supported.", examples = MESSAGE_TYPE_NOTIFICATION, allowableValues = MESSAGE_TYPE_NOTIFICATION)
	private String messageType;

	@NotBlank
	@Email
	@Schema(description = "The recipient's UPN / email address", examples = "first.last@municipaladress.com")
	private String recipient;

	@NotBlank
	@Size(max = 150)
	@Schema(description = "Notification preview text (Teams enforces a 150 character limit on the preview).", examples = "Datacenter A is offline")
	private String message;

	@URL
	@Schema(description = "Optional clickable target URL for the notification. Falls back to the service-wide default (teams.default-target-url) if omitted.", examples = "https://status.sundsvall.se/incidents/123")
	private String targetUrl;

	public static SendTeamsMessageRequest create() {
		return new SendTeamsMessageRequest();
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(final String messageType) {
		this.messageType = messageType;
	}

	public SendTeamsMessageRequest withMessageType(final String messageType) {
		this.messageType = messageType;
		return this;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(final String recipient) {
		this.recipient = recipient;
	}

	public SendTeamsMessageRequest withRecipient(final String recipient) {
		this.recipient = recipient;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public SendTeamsMessageRequest withMessage(final String message) {
		this.message = message;
		return this;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(final String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public SendTeamsMessageRequest withTargetUrl(final String targetUrl) {
		this.targetUrl = targetUrl;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final SendTeamsMessageRequest that = (SendTeamsMessageRequest) o;
		return Objects.equals(messageType, that.messageType)
			&& Objects.equals(recipient, that.recipient)
			&& Objects.equals(message, that.message)
			&& Objects.equals(targetUrl, that.targetUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hash(messageType, recipient, message, targetUrl);
	}

	@Override
	public String toString() {
		return "SendTeamsMessageRequest{" +
			"messageType='" + messageType + '\'' +
			", recipient='" + recipient + '\'' +
			", message='" + message + '\'' +
			", targetUrl='" + targetUrl + '\'' +
			'}';
	}
}
