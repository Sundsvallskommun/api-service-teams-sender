package se.sundsvall.teamssender.exceptions;

public class ChatNotFoundException extends RuntimeException {
	public ChatNotFoundException(String message) {
		super(message);
	}
}
