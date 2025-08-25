package se.sundsvall.teamssender.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

class TeamsExceptionHandlerTest {

	private TeamsExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new TeamsExceptionHandler();
	}

	@Test
	void testHandleChatNotFound() {
		var exception = new ChatNotCreatedException("Chat not created");
		ResponseEntity<Problem> response = handler.handleChatNotFound(exception);

		assertThat(response.getStatusCode().value()).isEqualTo(Status.NOT_FOUND.getStatusCode());
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Chat Not Found");
		assertThat(response.getBody().getDetail()).isEqualTo("Chat not created");
		assertThat(response.getBody().getType()).isEqualTo(URI.create("http://localhost:8080/problem/chat-not-found"));
	}

	@Test
	void testHandleMessageSendFailure() {
		var exception = new MessageSendException("Sending failed");
		ResponseEntity<Problem> response = handler.handleMessageSendFailure(exception);

		assertThat(response.getStatusCode().value()).isEqualTo(Status.UNPROCESSABLE_ENTITY.getStatusCode());
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Message Send Failure");
		assertThat(response.getBody().getDetail()).isEqualTo("Sending failed");
		assertThat(response.getBody().getType()).isEqualTo(URI.create("http://localhost:8080/problem/message-failure"));
	}

	@Test
	void testHandleAuth() {
		var exception = new AuthenticationException("Auth issue");
		ResponseEntity<Problem> response = handler.handleAuth(exception);

		assertThat(response.getStatusCode().value()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Authentication or Authorization Error");
		assertThat(response.getBody().getDetail()).isEqualTo("Auth issue");
		assertThat(response.getBody().getType()).isEqualTo(URI.create("http://localhost:8080/problem/auth-error"));
	}

	@Test
	void testHandleGraphConnection() {
		var exception = new GraphConnectionException("Graph API unavailable");
		ResponseEntity<Problem> response = handler.handleGraphConnection(exception);

		assertThat(response.getStatusCode().value()).isEqualTo(Status.SERVICE_UNAVAILABLE.getStatusCode());
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Graph API Connection Error");
		assertThat(response.getBody().getDetail()).isEqualTo("Graph API unavailable");
		assertThat(response.getBody().getType()).isEqualTo(URI.create("http://localhost:8080/problem/graph-error"));
	}

	@Test
	void testHandleRecipient() {
		var exception = new RecipientException("Recipient not found");
		ResponseEntity<Problem> response = handler.handleRecipient(exception);

		assertThat(response.getStatusCode().value()).isEqualTo(Status.NOT_FOUND.getStatusCode());
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Recipient not found Error");
		assertThat(response.getBody().getDetail()).isEqualTo("Recipient not found");
		assertThat(response.getBody().getType()).isEqualTo(URI.create("http://localhost:8080/problem/recipient-error"));
	}

	@Test
	void testHandleThrowable() {
		var exception = new RuntimeException("Something went wrong");
		NativeWebRequest request = mock(NativeWebRequest.class);

		ResponseEntity<Problem> response = handler.handleThrowable(exception, request);

		assertThat(response.getStatusCode().value()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Unexpected Internal Server Error");
		assertThat(response.getBody().getDetail()).isEqualTo("Something went wrong");
		assertThat(response.getBody().getType()).isEqualTo(URI.create("http://localhost:8080/problem/internal-error"));
	}
}
