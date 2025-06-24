package se.sundsvall.teamssender.api;

public class AuthenticationException extends RuntimeException {
	public AuthenticationException(String message) {
		super(message);
	}
}
