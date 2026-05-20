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
import static se.sundsvall.teamssender.api.model.SendTeamsMessageRequest.MESSAGE_TYPE_NOTIFICATION;

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
		final var messageType = MESSAGE_TYPE_NOTIFICATION;
		final var recipient = "first.last@example.com";
		final var message = "Hello, world!";
		final var targetUrl = "https://status.example.com/123";

		final var bean = SendTeamsMessageRequest.create()
			.withMessageType(messageType)
			.withRecipient(recipient)
			.withMessage(message)
			.withTargetUrl(targetUrl);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getMessageType()).isEqualTo(messageType);
		assertThat(bean.getRecipient()).isEqualTo(recipient);
		assertThat(bean.getMessage()).isEqualTo(message);
		assertThat(bean.getTargetUrl()).isEqualTo(targetUrl);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new SendTeamsMessageRequest()).hasAllNullFieldsOrProperties();
		assertThat(SendTeamsMessageRequest.create()).hasAllNullFieldsOrProperties();
	}
}
