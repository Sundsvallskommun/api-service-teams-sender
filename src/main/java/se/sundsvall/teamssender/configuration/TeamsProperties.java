package se.sundsvall.teamssender.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Service-wide Teams notification settings, separate from per-municipality Azure AD credentials in {@link AzureConfig}.
 *
 * <p>
 * {@code graphBaseUrl} is intentionally nullable: when unset (production), the Graph SDK uses its default base URL
 * ({@code https://graph.microsoft.com/v1.0}). The IT profile sets it to a WireMock URL.
 */
@Configuration
@ConfigurationProperties(prefix = "teams")
public class TeamsProperties {

	private String scope = "https://graph.microsoft.com/.default";
	private String defaultTargetUrl;
	private String graphBaseUrl;
	private String activityType = "systemDefault";
	private String topicLabel = "Notification";
	/**
	 * Test hook: when set, the {@code GraphServiceClientFactory} short-circuits the real Azure AD token acquisition and
	 * uses this value as a static bearer token. Production must leave this unset.
	 */
	private String staticAccessToken;

	public String getScope() {
		return scope;
	}

	public void setScope(final String scope) {
		this.scope = scope;
	}

	public String getDefaultTargetUrl() {
		return defaultTargetUrl;
	}

	public void setDefaultTargetUrl(final String defaultTargetUrl) {
		this.defaultTargetUrl = defaultTargetUrl;
	}

	public String getGraphBaseUrl() {
		return graphBaseUrl;
	}

	public void setGraphBaseUrl(final String graphBaseUrl) {
		this.graphBaseUrl = graphBaseUrl;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(final String activityType) {
		this.activityType = activityType;
	}

	public String getTopicLabel() {
		return topicLabel;
	}

	public void setTopicLabel(final String topicLabel) {
		this.topicLabel = topicLabel;
	}

	public String getStaticAccessToken() {
		return staticAccessToken;
	}

	public void setStaticAccessToken(final String staticAccessToken) {
		this.staticAccessToken = staticAccessToken;
	}
}
