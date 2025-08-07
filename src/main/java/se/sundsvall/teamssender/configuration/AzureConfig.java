package se.sundsvall.teamssender.configuration;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "azure")
@Getter
@Setter
public class AzureConfig {

	private Map<String, Azure> ad;

	@Getter
	@Setter
	public static class Azure {
		private String user;
		private String clientId;
		private String tenantId;
		private String redirectUri;
		private String scopes;
		private String authorityUrl;
		private String clientSecret;
	}
}
