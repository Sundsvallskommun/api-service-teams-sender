package se.sundsvall.teamssender.api;

import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.service.TeamsService;

@RestController
@Tag(name = "Teams resource", description = "Resource for sending messages in Microsoft Teams")
@Validated
class TeamsResource {

	private final TeamsService teamsService;
	private GraphServiceClient graphServiceClient;

	TeamsResource(TeamsService teamsService) {
		this.teamsService = teamsService;
	}

	public void setGraphServiceClient(GraphServiceClient graphServiceClient) {
		this.graphServiceClient = graphServiceClient;
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
		@Valid @RequestBody final SendTeamsMessageRequest request, @ValidMunicipalityId @PathVariable final String municipalityId) throws Exception {

		// skicka in graphtoken i sendteamsmessage
		teamsService.initiateMessage(municipalityId, request, graphServiceClient);
		return ResponseEntity.ok("Message sent successfully");
	}

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

	@GetMapping("/callback")
	public String callback(@RequestParam String code) {
		// Byt auth-kod mot access- och refresh-token
		AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
			.clientId("din-client-id")
			.tenantId("din-tenant-id")
			.authorizationCode(code)
			.redirectUrl("din-redirect-url")
			.build();

		setGraphServiceClient(new GraphServiceClient(credential));
		return "Autentisering lyckades!";
	}
}
