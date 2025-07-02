package se.sundsvall.teamssender.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

@Configuration
public class OpenApiConfig {

	@Value("${azure.ad.tenant-id}")
	private String tenantId;;  // Replace with your Azure AD tenant id


	Scopes scopes = new Scopes()
			.addString("User.Read", "Read user profile")
			.addString("Chat.ReadWrite", "Read and write chat messages");

	@Primary
	@Bean
	public OpenAPI customizeOpenAPI() {
		OAuthFlow authorizationCodeFlow = new OAuthFlow()
				.authorizationUrl("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/authorize")
				.tokenUrl("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token")
				.scopes(scopes);

		OAuthFlows oauthFlows = new OAuthFlows()
				.authorizationCode(authorizationCodeFlow);
		System.out.println(@Value(""));

		return new OpenAPI()
				.components(new Components()
						.addSecuritySchemes("bearerAuth",
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT"))
						.addSecuritySchemes("oauth2",
								new SecurityScheme()
										.type(SecurityScheme.Type.OAUTH2)
										.flows(oauthFlows)))
				.addSecurityItem(new SecurityRequirement().addList("oauth2"))
				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
	}
}
