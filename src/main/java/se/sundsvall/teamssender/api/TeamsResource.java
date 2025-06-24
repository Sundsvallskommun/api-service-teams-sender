package se.sundsvall.teamssender.api;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;

@ControllerAdvice
public class TeamsResource implements ProblemHandling {

	private static final URI BASE_URI = URI.create("https://example.com/problem");

	@ExceptionHandler(ChatNotFoundException.class)
	public ResponseEntity<Problem> handleChatNotFound(ChatNotFoundException ex) {
		Problem problem = Problem.builder()
			.withType(BASE_URI.resolve("/chat-not-found"))
			.withTitle("Chat Not Found")
			.withStatus(Status.NOT_FOUND)
			.withDetail(ex.getMessage())
			.build();
		return ResponseEntity.status(Status.NOT_FOUND.getStatusCode()).body(problem);
	}

	@ExceptionHandler(MessageSendException.class)
	public ResponseEntity<Problem> handleMessageSendFailure(MessageSendException ex) {
		Problem problem = Problem.builder()
			.withType(BASE_URI.resolve("/message-failure"))
			.withTitle("Message Send Failure")
			.withStatus(Status.UNPROCESSABLE_ENTITY)
			.withDetail(ex.getMessage())
			.build();
		return ResponseEntity.status(Status.UNPROCESSABLE_ENTITY.getStatusCode()).body(problem);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Problem> handleAuth(AuthenticationException ex) {
		Problem problem = Problem.builder()
			.withType(BASE_URI.resolve("/auth-error"))
			.withTitle("Authentication or Authorization Error")
			.withStatus(Status.UNAUTHORIZED)
			.withDetail(ex.getMessage())
			.build();
		return ResponseEntity.status(Status.UNAUTHORIZED.getStatusCode()).body(problem);
	}

	@ExceptionHandler(GraphConnectionException.class)
	public ResponseEntity<Problem> handleGraphConnection(GraphConnectionException ex) {
		Problem problem = Problem.builder()
			.withType(BASE_URI.resolve("/graph-error"))
			.withTitle("Graph API Connection Error")
			.withStatus(Status.SERVICE_UNAVAILABLE)
			.withDetail(ex.getMessage())
			.build();
		return ResponseEntity.status(Status.SERVICE_UNAVAILABLE.getStatusCode()).body(problem);
	}

	@Override
	public ResponseEntity<Problem> handleThrowable(final Throwable throwable,
		final NativeWebRequest request) {
		Problem problem = Problem.builder()
			.withType(BASE_URI.resolve("/internal-error"))
			.withTitle("Unexpected Internal Server Error")
			.withStatus(Status.INTERNAL_SERVER_ERROR)
			.withDetail(throwable.getMessage())
			.build();
		return ResponseEntity.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).body(problem);
	}
}
