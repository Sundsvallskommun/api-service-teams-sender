package se.sundsvall.teamssender.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.api.model.SendTeamsMessageResponse;
import se.sundsvall.teamssender.service.TeamsSenderService;

@RestController
class TeamsSenderResource {

	private final TeamsSenderService teamsSenderService;

	public TeamsSenderResource(TeamsSenderService teamsSenderService) {
		this.teamsSenderService = teamsSenderService;
	}

	@PostMapping("/teams/messages")
	@Operation(summary = "Send a message in Microsoft Teams",
		responses = {
			@ApiResponse(responseCode = "200", description = "Message sent successfully", useReturnTypeSchema = true),
			@ApiResponse(responseCode = "400", description = "Incorrect or malformed request", content = @Content(schema = @Schema(oneOf = {
				Problem.class, ConstraintViolationProblem.class
			}))),
			@ApiResponse(responseCode = "404", description = "Requested resource could not be found", content = @Content(schema = @Schema(implementation = Problem.class))),
			@ApiResponse(responseCode = "422", description = "Message could not be created or sent", content = @Content(schema = @Schema(implementation = Problem.class))),
			@ApiResponse(responseCode = "401", description = "Authentication information is either missing or invalid", content = @Content(schema = @Schema(implementation = Problem.class))),
			@ApiResponse(responseCode = "503", description = "Connection issue to Microsoft Graph API", content = @Content(schema = @Schema(implementation = Problem.class))),
			@ApiResponse(responseCode = "500", description = "Unexpected internal server error", content = @Content(schema = @Schema(implementation = Problem.class)))
		})
	ResponseEntity<SendTeamsMessageResponse> sendTeamsMessage(@RequestBody @Valid SendTeamsMessageRequest request) throws Exception {

		teamsSenderService.sendTeamsMessage(request);

		return ResponseEntity.ok(new SendTeamsMessageResponse("Message sent successfully"));
	}
}
