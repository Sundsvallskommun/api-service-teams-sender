package se.sundsvall.teamssender.exceptions;

public class RecipientException extends RuntimeException {
	public RecipientException(String message) {
		super(message);
	}

	public RecipientException(String message, Throwable cause) {
		super(message);
	}

}
