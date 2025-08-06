package se.sundsvall.teamssender.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OpenApiConfig {

	@Value("${azure.ad.tenant-id}")
	private String tenantId;

	@Value("${azure.ad.client-id}")
	private String clientId;

	@Primary
	@Bean
	OpenAPI customizeOpenAPI() {

		Scopes scopes = new Scopes()
			.addString("api://" + clientId + "/access_as_user", "Access my API")
			.addString("User.Read", "Read user profile")
			.addString("Chat.ReadWrite", "Read and write chat messages");

		OAuthFlow authorizationCodeFlow = new OAuthFlow()
			.authorizationUrl("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/authorize")
			.tokenUrl("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token")
			.scopes(scopes);

		SecurityScheme oauth2Scheme = new SecurityScheme()
			.type(SecurityScheme.Type.OAUTH2)
			.description("OAuth2 Authorization Code flow with PKCE")
			.flows(new OAuthFlows().authorizationCode(authorizationCodeFlow));

		return new OpenAPI()
			.components(new Components()
				.addSecuritySchemes("oauth2", oauth2Scheme))
			.addSecurityItem(new SecurityRequirement().addList("oauth2"));
	}
}
