package se.sundsvall.teamssender.exceptions;

public class ChatNotCreatedException extends RuntimeException {
	public ChatNotCreatedException(String message) {
		super(message);
	}

	public ChatNotCreatedException(String message, Throwable cause) {
		super(message, cause);
	}
}
