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

	@Value("${azure.ad.tenant-id}")
	private String tenantId;
	@Value("${azure.ad.client-id}")
	private String clientId;
	@Value ("${azure.ad.client-secret}")
	private String clientSecret;
	@Value ("${azure.ad.redirecturi}")
	private String redirectUri;
	private final MicrosoftGraphTeamsSender teamsSender;

	// Konstruktorinjektion (Autowired fungerar också men constructor preferred)
	public TeamsController(MicrosoftGraphTeamsSender teamsSender) {
		this.teamsSender = teamsSender;
	}

	// POST endpoint för att skicka Teams-meddelande
	@PostMapping("/send")
	public ResponseEntity<String> sendTeamsMessage(@RequestBody SendTeamsMessageRequest request) {
		try {
			teamsSender.sendTeamsMessage(request);
			return ResponseEntity.ok("Message sent successfully");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Failed to send message: " + e.getMessage());
		}
	}

	// GET endpoint för test av inloggning/token (exempel)
	@GetMapping("/test-auth")
	public ResponseEntity<String> testAuth() {
		try {
			// Exempel: hämta access token från din TokenService (lägg in TokenService i konstruktorn)
			// String token = tokenService.getValidAccessToken("userId");
			// return ResponseEntity.ok("Token fetched: " + token);
			return ResponseEntity.ok("Auth test endpoint - implementera tokenhämtning");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Auth test failed: " + e.getMessage());
		}
	}

	@RestController
	@RequestMapping("/auth")
	public class AuthController {

		private final TokenService tokenService; // injecta denna via konstruktor

		public AuthController(TokenService tokenService) {
			this.tokenService = tokenService;
		}

		@GetMapping("/login")
		public void login(HttpServletResponse response) throws IOException {


					String scopes = "User.Read Chat.ReadWrite api://<clientId>/access_as_user";


			String url = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/authorize" +
					"?client_id=" + clientId +
					"&response_type=code" +
					"&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
					"&response_mode=query" +
					"&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
					"&state=12345";

			response.sendRedirect(url);
		}
		@GetMapping("/callback")
		public ResponseEntity<String> callback(@RequestParam String code, @RequestParam String state) {
			try {
				// Byt authorization code mot access token och refresh token
				tokenService.exchangeAuthorizationCodeForToken(code);

				return ResponseEntity.ok("Login succeeded, tokens saved!");
			} catch (Exception e) {
				return ResponseEntity.status(500).body("Login failed: " + e.getMessage());
			}
		}
	}
}
