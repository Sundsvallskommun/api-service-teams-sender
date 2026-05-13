package se.sundsvall.teamssender.api.model;

import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

class SendTeamsMessageRequestTest {

	@Test
	void testBean() {
		assertThat(SendTeamsMessageRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var recipient = "first.last@example.com";
		final var message = "Hello, world!";

		final var bean = SendTeamsMessageRequest.create()
			.withRecipient(recipient)
			.withMessage(message);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getRecipient()).isEqualTo(recipient);
		assertThat(bean.getMessage()).isEqualTo(message);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new SendTeamsMessageRequest()).hasAllNullFieldsOrProperties();
		assertThat(SendTeamsMessageRequest.create()).hasAllNullFieldsOrProperties();
	}
}
