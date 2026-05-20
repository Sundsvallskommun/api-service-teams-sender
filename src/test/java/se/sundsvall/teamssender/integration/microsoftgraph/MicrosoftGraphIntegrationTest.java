package se.sundsvall.teamssender.integration.microsoftgraph;

import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.item.teamwork.sendactivitynotification.SendActivityNotificationPostRequestBody;
import com.microsoft.kiota.ApiExceptionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.teamssender.configuration.TeamsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class MicrosoftGraphIntegrationTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String RECIPIENT = "recipient@example.com";
	private static final String MESSAGE = "Datacenter A offline";
	private static final String TARGET_URL = "https://status.example.com/123";

	@Mock
	private GraphServiceClientFactory graphServiceClientFactoryMock;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private GraphServiceClient graphClientMock;

	private TeamsProperties teamsProperties;
	private MicrosoftGraphIntegration integration;

	@BeforeEach
	void setUp() {
		teamsProperties = new TeamsProperties();
		teamsProperties.setActivityType("emergencyAlert");
		teamsProperties.setTopicLabel("Emergency");
		teamsProperties.setDefaultTargetUrl("https://status.default.example.com");
		integration = new MicrosoftGraphIntegration(graphServiceClientFactoryMock, teamsProperties);

		when(graphServiceClientFactoryMock.forMunicipality(MUNICIPALITY_ID)).thenReturn(graphClientMock);
	}

	@Test
	void happyPathSendsExpectedBody() {
		integration.sendNotification(MUNICIPALITY_ID, RECIPIENT, MESSAGE, TARGET_URL);

		final var captor = ArgumentCaptor.forClass(SendActivityNotificationPostRequestBody.class);
		verify(graphClientMock.users().byUserId(RECIPIENT).teamwork().sendActivityNotification()).post(captor.capture());

		final var body = captor.getValue();
		assertThat(body.getActivityType()).isEqualTo("emergencyAlert");
		assertThat(body.getPreviewText().getContent()).isEqualTo(MESSAGE);
		assertThat(body.getTopic().getValue()).isEqualTo("Emergency");
		assertThat(body.getTopic().getWebUrl()).isEqualTo(TARGET_URL);
	}

	@Test
	void missingTargetUrlFallsBackToConfiguredDefault() {
		integration.sendNotification(MUNICIPALITY_ID, RECIPIENT, MESSAGE, null);

		final var captor = ArgumentCaptor.forClass(SendActivityNotificationPostRequestBody.class);
		verify(graphClientMock.users().byUserId(RECIPIENT).teamwork().sendActivityNotification()).post(captor.capture());

		assertThat(captor.getValue().getTopic().getWebUrl()).isEqualTo("https://status.default.example.com");
	}

	@Test
	void graph401MapsToUnauthorized() {
		stubGraphFailureWith(401);

		assertThatThrownBy(() -> integration.sendNotification(MUNICIPALITY_ID, RECIPIENT, MESSAGE, TARGET_URL))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(UNAUTHORIZED));
	}

	@Test
	void graph403MapsToForbidden() {
		stubGraphFailureWith(403);

		assertThatThrownBy(() -> integration.sendNotification(MUNICIPALITY_ID, RECIPIENT, MESSAGE, TARGET_URL))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(FORBIDDEN));
	}

	@Test
	void graph404MapsToNotFound() {
		stubGraphFailureWith(404);

		assertThatThrownBy(() -> integration.sendNotification(MUNICIPALITY_ID, RECIPIENT, MESSAGE, TARGET_URL))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(NOT_FOUND))
			.hasMessageContaining(RECIPIENT);
	}

	@Test
	void graph500MapsToBadGateway() {
		stubGraphFailureWith(500);

		assertThatThrownBy(() -> integration.sendNotification(MUNICIPALITY_ID, RECIPIENT, MESSAGE, TARGET_URL))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(BAD_GATEWAY));
	}

	private void stubGraphFailureWith(final int httpStatus) {
		final var apiException = new ApiExceptionBuilder()
			.withMessage("Graph returned " + httpStatus)
			.withResponseStatusCode(httpStatus)
			.build();
		final var requestBuilder = graphClientMock.users().byUserId(RECIPIENT).teamwork().sendActivityNotification();
		doThrow(apiException).when(requestBuilder).post(org.mockito.ArgumentMatchers.any(SendActivityNotificationPostRequestBody.class));
	}
}
