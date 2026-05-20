package se.sundsvall.teamssender.configuration;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Per-municipality Azure AD app registration credentials. The service uses OAuth2 client credentials (no user login),
 * so only the three values needed to acquire an application access token are kept.
 */
@Configuration
@ConfigurationProperties(prefix = "azure")
public class AzureConfig {

	private Map<String, Azure> ad;

	public Map<String, Azure> getAd() {
		return ad;
	}

	public void setAd(final Map<String, Azure> ad) {
		this.ad = ad;
	}

	public static class Azure {
		private String tenantId;
		private String clientId;
		private String clientSecret;

		public String getTenantId() {
			return tenantId;
		}

		public void setTenantId(final String tenantId) {
			this.tenantId = tenantId;
		}

		public String getClientId() {
			return clientId;
		}

		public void setClientId(final String clientId) {
			this.clientId = clientId;
		}

		public String getClientSecret() {
			return clientSecret;
		}

		public void setClientSecret(final String clientSecret) {
			this.clientSecret = clientSecret;
		}
	}
}
