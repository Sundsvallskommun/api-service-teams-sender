package se.sundsvall.teamssender.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.ad")
@Getter
@Setter
public class AzureConfig {
	private String clientId;
	private String tenantId;
	private String redirectUri;
	private String scopes;
	private String authorityUrl;
	private String certificatePath;
	private String certificateKey;
}
