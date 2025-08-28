package se.sundsvall.teamssender.configuration;

import io.swagger.v3.oas.models.Operation;
import java.util.Optional;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Disable the authentication for the "/login"-endpoint since it needs to be public.
 */
@Configuration
public class OpenApiConfigurationExtension {

	private static final String LOGIN_ENDPOINT = "/{municipalityId}/login";

	@Bean
	public OpenApiCustomizer addNoAuthEndpoint() {
		return openApi -> Optional.ofNullable(openApi.getPaths().get(LOGIN_ENDPOINT))
			.flatMap(openApiPath -> Optional.ofNullable(openApiPath.getGet())).ifPresent(this::extendOperation);
	}

	void extendOperation(Operation operation) {
		operation.addExtension("x-auth-type", "None");
		operation.addExtension("x-throttling-tier", "Unlimited");
		operation.addExtension("x-wso2-mutual-ssl", "Optional");
	}
}
