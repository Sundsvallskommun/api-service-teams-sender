package se.sundsvall.teamssender.api;


import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
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

		teamsService.sendTeamsMessage(municipalityId, request);
		return ResponseEntity.ok("Message sent successfully");
	}
}
