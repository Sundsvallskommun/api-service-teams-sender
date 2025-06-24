package se.sundsvall.teamssender.api;

public class ChatNotFoundException extends RuntimeException {
	public ChatNotFoundException(String message) {
		super(message);
	}
}
