package se.sundsvall.teamssender.exceptions;

public class MessageSendException extends RuntimeException {
	public MessageSendException(String message) {
		super(message);
	}

	public MessageSendException(String message, Throwable cause) {
		super(message, cause);
	}
}
