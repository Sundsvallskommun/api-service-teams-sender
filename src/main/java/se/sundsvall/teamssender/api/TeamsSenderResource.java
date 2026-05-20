package se.sundsvall.teamssender.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.service.TeamsSenderService;

@RestController
@Validated
class TeamsSenderResource {

	private final TeamsSenderService teamsSenderService;

	public TeamsSenderResource(final TeamsSenderService teamsSenderService) {
		this.teamsSenderService = teamsSenderService;
	}

	@PostMapping("/{municipalityId}/teams/messages")
	@Operation(summary = "Send a message in Microsoft Teams",
		responses = {
			@ApiResponse(responseCode = "204", description = "Message sent successfully", useReturnTypeSchema = true),
			@ApiResponse(responseCode = "400", description = "Incorrect or malformed request", content = @Content(schema = @Schema(oneOf = {
				Problem.class, ConstraintViolationProblem.class
			}))),
			@ApiResponse(responseCode = "404", description = "Recipient not found in Microsoft Graph", content = @Content(schema = @Schema(implementation = Problem.class))),
			@ApiResponse(responseCode = "401", description = "Microsoft Graph rejected the credentials", content = @Content(schema = @Schema(implementation = Problem.class))),
			@ApiResponse(responseCode = "403", description = "Microsoft Graph rejected the request (missing permission or activity type not registered)", content = @Content(schema = @Schema(implementation = Problem.class))),
			@ApiResponse(responseCode = "502", description = "Bad gateway when communicating with Microsoft Graph", content = @Content(schema = @Schema(implementation = Problem.class))),
			@ApiResponse(responseCode = "500", description = "Unexpected internal server error", content = @Content(schema = @Schema(implementation = Problem.class)))
		})
	ResponseEntity<Void> sendTeamsMessage(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody @Valid final SendTeamsMessageRequest request) {

		teamsSenderService.sendTeamsMessage(request, municipalityId);

		return ResponseEntity.noContent().build();
	}
}
