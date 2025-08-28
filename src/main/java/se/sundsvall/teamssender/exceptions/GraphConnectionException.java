package se.sundsvall.teamssender.exceptions;

public class GraphConnectionException extends RuntimeException {
	public GraphConnectionException(String message) {
		super(message);
	}

	public GraphConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
