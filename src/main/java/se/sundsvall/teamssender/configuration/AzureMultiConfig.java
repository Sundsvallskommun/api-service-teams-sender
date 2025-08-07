package se.sundsvall.teamssender.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "azure")
@Getter
@Setter
public class AzureMultiConfig {

	private Map<String, AzureConfig> ad;

	@Getter
	@Setter
	public static class AzureConfig {
		private String user;
		private String clientId;
		private String tenantId;
		private String redirectUri;
		private String scopes;
		private String authorityUrl;
		private String clientSecret;
	}
}
