package se.sundsvall.teamssender.api;

import com.azure.identity.AuthorizationCodeCredential;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.service.AuthenticationService;
import se.sundsvall.teamssender.service.TeamsService;

import java.io.IOException;

@RestController
@Tag(name = "Teams resource", description = "Resource for sending messages in Microsoft Teams")
@Validated
class TeamsResource {

	@Autowired
	private AuthenticationService authenticationService;

	private final TeamsService teamsService;

	public TeamsResource(final TeamsService teamsService) {
		this.teamsService = teamsService;
	}



	@PostMapping("{municipalityId}/teams/messages")
	@Operation(summary = "Send a message in Microsoft Teams", responses = {

		@ApiResponse(
			responseCode = "200",
			description = "Message sent successfully",
			useReturnTypeSchema = true),

		@ApiResponse(
			responseCode = "400",
			description = "Invalid request payload or parameters",
			content = @Content(schema = @Schema(oneOf = {
				Problem.class,
				ConstraintViolationProblem.class
			}))),

		@ApiResponse(
			responseCode = "404",
			description = "Chat could not be found or created",
			content = @Content(schema = @Schema(implementation = Problem.class))),

		@ApiResponse(
			responseCode = "422",
			description = "Message could not be created or sent",
			content = @Content(schema = @Schema(implementation = Problem.class))),

		@ApiResponse(
			responseCode = "401",
			description = "Authentication or authorization issue",
			content = @Content(schema = @Schema(implementation = Problem.class))),

		@ApiResponse(
			responseCode = "503",
			description = "Connection issue to Microsoft Graph API",
			content = @Content(schema = @Schema(implementation = Problem.class))),

		@ApiResponse(
			responseCode = "500",
			description = "Unexpected internal server error",
			content = @Content(schema = @Schema(implementation = Problem.class)))
	})

	ResponseEntity<String> sendTeamsMessage(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,

		@Valid @RequestBody final SendTeamsMessageRequest request) {
		AuthorizationCodeCredential credential = authenticationService.createCredential(session);

		teamsService.sendTeamsMessage(municipalityId, request);
		return ResponseEntity.ok("Message sent successfully");
	}
	@Value("${integration.teams.instances.2281.azure.tenantId}")
	private String tenantId;

	@Value("${integration.teams.instances.2281.azure.clientId}")
	private String clientId;

	@GetMapping("/login")
	public void login(HttpServletResponse response) throws IOException {
		String authorizationUrl = "https://login.microsoftonline.com/{tenantId}/oauth2/v2.0/authorize" +
				"?client_id={clientId}" +
				"&response_type=code" +
				"&redirect_uri={http://localhost:8080}" +
				"&response_mode=query" +
				"&scope=Chat.ReadWrite ChatMessage.Send offline_access";
		response.sendRedirect(authorizationUrl);
	}

	@GetMapping("/redirect")
	public String redirect(@RequestParam String code, HttpSession session) {
		session.setAttribute("authorizationCode", code);
		return "Inloggning lyckades!";
	}
}
