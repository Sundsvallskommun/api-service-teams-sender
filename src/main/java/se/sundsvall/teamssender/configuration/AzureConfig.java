package se.sundsvall.teamssender.configuration;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
		private String user;
		private String clientId;
		private String tenantId;
		private String redirectUri;
		private String scopes;
		private String authorityUrl;
		private String clientSecret;
		private String loginUrl;

		public String getUser() {
			return user;
		}

		public void setUser(final String user) {
			this.user = user;
		}

		public String getClientId() {
			return clientId;
		}

		public void setClientId(final String clientId) {
			this.clientId = clientId;
		}

		public String getTenantId() {
			return tenantId;
		}

		public void setTenantId(final String tenantId) {
			this.tenantId = tenantId;
		}

		public String getRedirectUri() {
			return redirectUri;
		}

		public void setRedirectUri(final String redirectUri) {
			this.redirectUri = redirectUri;
		}

		public String getScopes() {
			return scopes;
		}

		public void setScopes(final String scopes) {
			this.scopes = scopes;
		}

		public String getAuthorityUrl() {
			return authorityUrl;
		}

		public void setAuthorityUrl(final String authorityUrl) {
			this.authorityUrl = authorityUrl;
		}

		public String getClientSecret() {
			return clientSecret;
		}

		public void setClientSecret(final String clientSecret) {
			this.clientSecret = clientSecret;
		}

		public String getLoginUrl() {
			return loginUrl;
		}

		public void setLoginUrl(final String loginUrl) {
			this.loginUrl = loginUrl;
		}
	}
}
