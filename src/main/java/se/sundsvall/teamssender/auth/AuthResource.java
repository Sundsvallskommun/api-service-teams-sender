package se.sundsvall.teamssender.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.teamssender.auth.service.TokenService;
import se.sundsvall.teamssender.configuration.AzureConfig;

@RestController
class AuthResource {

	private final AzureConfig azureConfig;
	private final TokenService tokenService;

	public AuthResource(AzureConfig azureConfig, TokenService tokenService) {
		this.azureConfig = azureConfig;
		this.tokenService = tokenService;
	}

	@GetMapping("/{municipalityId}/login")
	@Operation(summary = "Login for user in a specific municipality", description = "Redirects the user to Microsoft loginpage")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "302", description = "Redirects the user to Microsoft loginpage"),
		@ApiResponse(responseCode = "404", description = "Invalid municipality ID")
	})
	void login(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		HttpServletResponse response) throws Exception {

		AzureConfig.Azure config = azureConfig.getAd().get(municipalityId);
		if (config == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid municipality ID");
			return;
		}

		response.sendRedirect(config.getLoginUrl());
	}

	@GetMapping("/callback")
	ResponseEntity<String> callback(HttpServletRequest request) throws Exception {
		String code = request.getParameter("code");
		String municipalityId = request.getParameter("state");

		return tokenService.exchangeAuthCodeForToken(code, municipalityId);
	}
}
