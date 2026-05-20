package se.sundsvall.teamssender.integration.microsoftgraph;

import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.TeamworkActivityTopic;
import com.microsoft.graph.models.TeamworkActivityTopicSource;
import com.microsoft.graph.users.item.teamwork.sendactivitynotification.SendActivityNotificationPostRequestBody;
import com.microsoft.kiota.ApiException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.teamssender.configuration.TeamsProperties;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Sends Teams activity-feed notifications (the bell icon) on behalf of a per-municipality Azure AD app.
 *
 * <p>
 * The recipient is encoded in the request URL ({@code /users/{recipient}/teamwork/sendActivityNotification}), so the
 * body only carries the topic, the activity type, and the preview text. {@code activityType} must match an entry
 * registered tenant-side in the Teams app manifest; see {@code TODO.md} and the project plan for the tenant
 * prerequisite.
 */
@Component
public class MicrosoftGraphIntegration {

	private final GraphServiceClientFactory graphServiceClientFactory;
	private final TeamsProperties teamsProperties;

	public MicrosoftGraphIntegration(final GraphServiceClientFactory graphServiceClientFactory, final TeamsProperties teamsProperties) {
		this.graphServiceClientFactory = graphServiceClientFactory;
		this.teamsProperties = teamsProperties;
	}

	public void sendNotification(final String municipalityId, final String recipient, final String message, final String targetUrl) {
		final var graphClient = graphServiceClientFactory.forMunicipality(municipalityId);
		final var body = buildPostRequestBody(message, targetUrl);
		try {
			graphClient.users().byUserId(recipient).teamwork().sendActivityNotification().post(body);
		} catch (final ThrowableProblem e) {
			throw e;
		} catch (final ApiException e) {
			throw mapGraphException(e, recipient);
		} catch (final Exception e) {
			throw Problem.valueOf(BAD_GATEWAY, "Failed to send Teams notification: " + e.getMessage());
		}
	}

	private SendActivityNotificationPostRequestBody buildPostRequestBody(final String message, final String targetUrl) {
		final var resolvedTargetUrl = Optional.ofNullable(targetUrl).orElseGet(teamsProperties::getDefaultTargetUrl);

		final var topic = new TeamworkActivityTopic();
		topic.setSource(TeamworkActivityTopicSource.Text);
		topic.setValue(teamsProperties.getTopicLabel());
		Optional.ofNullable(resolvedTargetUrl).ifPresent(topic::setWebUrl);

		final var previewText = new ItemBody();
		previewText.setContent(message);

		final var requestBody = new SendActivityNotificationPostRequestBody();
		requestBody.setTopic(topic);
		requestBody.setActivityType(teamsProperties.getActivityType());
		requestBody.setPreviewText(previewText);
		requestBody.setTemplateParameters(List.of());
		return requestBody;
	}

	private ThrowableProblem mapGraphException(final ApiException e, final String recipient) {
		return switch (e.getResponseStatusCode()) {
			case 401 -> Problem.valueOf(UNAUTHORIZED, "Microsoft Graph rejected the request as unauthorized: " + e.getMessage());
			case 403 -> Problem.valueOf(FORBIDDEN, "Microsoft Graph rejected the request as forbidden (missing TeamsActivity.Send permission, or activity type not registered in tenant): " + e.getMessage());
			case 404 -> Problem.valueOf(NOT_FOUND, "Teams user '%s' could not be found".formatted(recipient));
			default -> Problem.valueOf(BAD_GATEWAY, "Microsoft Graph returned status %d: %s".formatted(e.getResponseStatusCode(), e.getMessage()));
		};
	}
}
