package se.sundsvall.teamssender.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.teamssender.TestDataFactory.createValidSendTeamsMessageRequest;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SendTeamsMessageRequestValidationTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	void validationWithValidRequest() {
		var request = createValidSendTeamsMessageRequest();
		var constraintViolations = validator.validate(request);

		assertThat(constraintViolations).isEmpty();
	}

	@ParameterizedTest
	@MethodSource({
		"requestInvalidRecipientArguments",
		"requestInvalidMessageArguments"
	})
	void testSendEmailRequestValidation(final SendTeamsMessageRequest request, final String constraintField, final String constraintMessage) {
		var constraintViolations = List.copyOf(validator.validate(request));

		assertThat(constraintViolations).hasSize(1);
		assertThat(constraintViolations.getFirst().getPropertyPath()).hasToString(constraintField);
		assertThat(constraintViolations.getFirst().getMessage()).isEqualTo(constraintMessage);
	}

	private static Stream<Arguments> requestInvalidRecipientArguments() {
		return Stream.of(
			Arguments.of(new SendTeamsMessageRequest(null, "message"), "recipient", "must not be blank"),
			Arguments.of(new SendTeamsMessageRequest("", "message"), "recipient", "must not be blank"),
			Arguments.of(new SendTeamsMessageRequest("not a valid email address", "message"), "recipient", "must be a well-formed email address"));
	}

	private static Stream<Arguments> requestInvalidMessageArguments() {
		return Stream.of(
			Arguments.of(new SendTeamsMessageRequest("recipient@example.com", null), "message", "must not be blank"),
			Arguments.of(new SendTeamsMessageRequest("recipient@example.com", ""), "message", "must not be blank"));
	}
}
