package se.sundsvall.teamssender.api;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.models.security.Scopes;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.service.MicrosoftGraphTeamsSender;
import se.sundsvall.teamssender.service.TokenService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

//package se.sundsvall.teamssender.api;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.zalando.problem.Problem;
//import org.zalando.problem.violations.ConstraintViolationProblem;
//import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
//import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
//import se.sundsvall.teamssender.service.OboTokenService;
//import se.sundsvall.teamssender.service.TeamsService;
//
//@RestController
//@RequestMapping("/teams")
//public class TeamsController {
//
//	private final OboTokenService oboTokenService;
//	private final TeamsService teamsService;
//
//	public TeamsController(OboTokenService oboTokenService, TeamsService teamsService) {
//		this.oboTokenService = oboTokenService;
//		this.teamsService = teamsService;
//	}
//
//	@PostMapping("{municipalityId}/teams/messages")
//	@Operation(summary = "Send a message in Microsoft Teams", responses = {
//		@ApiResponse(
//			responseCode = "200",
//			description = "Message sent successfully",
//			useReturnTypeSchema = true),
//
//		@ApiResponse(
//			responseCode = "400",
//			description = "Invalid request payload or parameters",
//			content = @Content(schema = @Schema(oneOf = {
//				Problem.class,
//				ConstraintViolationProblem.class
//			}))),
//
//		@ApiResponse(
//			responseCode = "404",
//			description = "Chat could not be found or created",
//			content = @Content(schema = @Schema(implementation = Problem.class))),
//
//		@ApiResponse(
//			responseCode = "422",
//			description = "Message could not be created or sent",
//			content = @Content(schema = @Schema(implementation = Problem.class))),
//
//		@ApiResponse(
//			responseCode = "401",
//			description = "Authentication or authorization issue",
//			content = @Content(schema = @Schema(implementation = Problem.class))),
//
//		@ApiResponse(
//			responseCode = "503",
//			description = "Connection issue to Microsoft Graph API",
//			content = @Content(schema = @Schema(implementation = Problem.class))),
//
//		@ApiResponse(
//			responseCode = "500",
//			description = "Unexpected internal server error",
//			content = @Content(schema = @Schema(implementation = Problem.class)))
//	})
//	public ResponseEntity<String> sendMessage(
//		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
//		@Valid @RequestBody final SendTeamsMessageRequest request) {
//
//		try {
//			// Plocka ut token från header, ta bort "Bearer "
//			String userToken = request.getToken().replaceFirst("Bearer ", "").trim();
//
//			// 1. Byt ut user token mot OBO token (MS Graph token)
//			oboTokenService.acquireOboToken(userToken, request.getSender());
//
//			// 2. Hämta token från cache
//			String graphToken = oboTokenService.getAccessTokenForUser(request.getSender());
//
//			// 3. Hämta Azure AD ID för användarna
//			String senderId = teamsService.getUserId(graphToken, request.getSender());
//			String recipientId = teamsService.getUserId(graphToken, request.getUser());
//
//			// 4. Skapa eller hämta chat mellan användare
//			String chatId = teamsService.createOneOnOneChat(graphToken, senderId, recipientId);
//
//			// 5. Skicka meddelande
//			teamsService.sendMessage(graphToken, chatId, request.getMessage());
//
//			return ResponseEntity.ok("Message sent in chat: " + chatId);
//
//		} catch (Exception e) {
//			// Returnera 500 med felmeddelande vid problem
//			return ResponseEntity.status(500).body("Error sending message: " + e.getMessage());
//		}
//	}
//}
@RestController
@RequestMapping("/api/teams")
public class TeamsController {

	private final MicrosoftGraphTeamsSender teamsSender;

	// Konstruktorinjektion (Autowired fungerar också men constructor preferred)
	public TeamsController(MicrosoftGraphTeamsSender teamsSender) {
		this.teamsSender = teamsSender;
	}

	// POST endpoint för att skicka Teams-meddelande
	@PostMapping("/send")
	public ResponseEntity<String> sendTeamsMessage(@RequestBody SendTeamsMessageRequest request) {
		try {
			// Väntar på att avsändarens token finns tillgänglig innan vi skickar
			teamsSender.waitForAndInitializeClient(request.getSender(), 30_000, 1_000);

			teamsSender.sendTeamsMessage(request);
			return ResponseEntity.ok("Message sent successfully");
		} catch (IllegalStateException e) {
			return ResponseEntity.status(408).body("Authorization code not received in time: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Failed to send message: " + e.getMessage());
		}
	}
}
