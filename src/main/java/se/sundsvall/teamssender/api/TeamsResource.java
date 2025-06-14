package se.sundsvall.teamssender.api;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Teams resource", description = "Resource for sending message in Teams")
@Validated
class TeamsResource {

	@PostMapping("{municipalityId}/teams/message")

	@Operation(summary = "Send a message in Teams", responses = {
			@ApiResponse(
					responseCode = "200",
					description = "Successful Operation",
					useReturnTypeSchema = true),
	})

	ResponseEntity<String> sendTeamsMessage() {
		return ResponseEntity.ok("Sent Successfully");
	}
}
