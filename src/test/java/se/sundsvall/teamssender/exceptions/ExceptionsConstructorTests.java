package se.sundsvall.teamssender.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExceptionsConstructorTests {

	@Test
	void messageSendException_constructorAndMessage() {
		var ex = new MessageSendException("Could not send");
		assertThat(ex).hasMessage("Could not send");
		assertThat(ex.getCause()).isNull();
	}

	@Test
	void messageSendException_constructorAndMessageAndThrowable() {
		Throwable cause = new RuntimeException("Underlying cause of exception");
		var ex = new MessageSendException("Could not send", cause);
		assertThat(ex).hasMessage("Could not send");
		assertThat(ex.getCause()).isSameAs(cause);
	}

	@Test
	void chatNotCreatedException_constructorAndMessage() {
		var ex = new ChatNotCreatedException("Chat not created");
		assertThat(ex).hasMessage("Chat not created");
	}

	@Test
	void chatNotCreatedException_constructorAndMessageAndThrowable() {
		Throwable cause = new RuntimeException("Underlying cause of exception");
		var ex = new ChatNotCreatedException("Chat not created", cause);
		assertThat(ex).hasMessage("Chat not created");
		assertThat(ex.getCause()).isSameAs(cause);
	}

	@Test
	void authenticationException_constructorAndMessage() {
		var ex = new AuthenticationException("Auth failed");
		assertThat(ex).hasMessage("Auth failed");
	}

	@Test
	void authenticationException_constructorAndMessageAndThrowable() {
		Throwable cause = new RuntimeException("Underlying cause of exception");
		var ex = new AuthenticationException("Auth failed", cause);
		assertThat(ex).hasMessage("Auth failed");
		assertThat(ex.getCause()).isSameAs(cause);
	}

	@Test
	void graphConnectionException_constructorAndMessage() {
		var ex = new GraphConnectionException("Graph down");
		assertThat(ex).hasMessage("Graph down");
	}

	@Test
	void graphConnectionException_constructorAndMessageAndThrowable() {
		Throwable cause = new RuntimeException("Underlying cause of exception");
		var ex = new GraphConnectionException("Graph down", cause);
		assertThat(ex).hasMessage("Graph down");
		assertThat(ex.getCause()).isSameAs(cause);
	}

	@Test
	void recipientException_constructorAndMessage() {
		var ex = new RecipientException("Recipient missing");
		assertThat(ex).hasMessage("Recipient missing");
	}

	@Test
	void recipientException_constructorAndMessageAndThrowable() {
		Throwable cause = new RuntimeException("Underlying cause of exception");
		var ex = new RecipientException("Recipient missing", cause);
		assertThat(ex).hasMessage("Recipient missing");
		assertThat(ex.getCause()).isSameAs(cause);
	}
}
