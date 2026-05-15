package se.sundsvall.teamssender.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.teamssender.integration.microsoftgraph.AzureAdTokenService;

@RestController
@Validated
@RequestMapping("/api/teamssender")
class AuthResource {

	private final AzureAdTokenService tokenService;

	public AuthResource(final AzureAdTokenService tokenService) {
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
		final HttpServletResponse response) throws IOException {

		response.sendRedirect(tokenService.getLoginUrl(municipalityId));
	}

	@GetMapping("/callback")
	ResponseEntity<String> callback(final HttpServletRequest request) {
		final String code = request.getParameter("code");
		final String municipalityId = request.getParameter("state");

		return tokenService.exchangeAuthCodeForToken(code, municipalityId);
	}
}
