package se.sundsvall.teamssender.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
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
	@Operation(summary = "OAuth2 redirect endpoint",
		description = "Exchanges the authorization code issued by Azure AD for tokens and persists them. "
			+ "This path is registered as the redirect URI in the Azure app registration - do not rename it.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Token successfully acquired and cached"),
		@ApiResponse(responseCode = "400", description = "Missing or malformed code/state parameter", content = @Content(schema = @Schema(oneOf = {
			Problem.class, ConstraintViolationProblem.class
		}))),
		@ApiResponse(responseCode = "401", description = "Azure AD rejected the authorization code", content = @Content(schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "404", description = "Unknown municipality in the state parameter", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<String> callback(
		@Parameter(name = "code", description = "Authorization code issued by Azure AD") @RequestParam @NotBlank final String code,
		@Parameter(name = "state", description = "Municipality id, round-tripped via the OAuth2 state parameter", example = "2281") @RequestParam @ValidMunicipalityId final String state) {

		return tokenService.exchangeAuthCodeForToken(code, state);
	}
}
