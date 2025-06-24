package se.sundsvall.teamssender.api;

public class MessageSendException extends RuntimeException {
	public MessageSendException(String message) {
		super(message);
	}
}
