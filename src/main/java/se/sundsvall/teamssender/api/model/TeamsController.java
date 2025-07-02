package se.sundsvall.teamssender.api.model;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.sundsvall.teamssender.service.OboTokenService;
import se.sundsvall.teamssender.service.TeamsService;

@RestController
@RequestMapping("/teams")
public class TeamsController {

	private final OboTokenService oboTokenService;
	private final TeamsService teamsService;

	public TeamsController(OboTokenService oboTokenService, TeamsService teamsService) {
		this.oboTokenService = oboTokenService;
		this.teamsService = teamsService;
	}

	@PostMapping("/send-message")
	public ResponseEntity<String> sendMessage(
			@RequestHeader("Authorization") String authHeader,
			@RequestParam String fromUser,
			@RequestParam String toUser,
			@RequestBody String message) {

		try {
			// Plocka ut token från header, ta bort "Bearer "
			String userToken = authHeader.replaceFirst("Bearer ", "").trim();

			// 1. Byt ut user token mot OBO token (MS Graph token)
			oboTokenService.acquireOboToken(userToken, fromUser);

			// 2. Hämta token från cache
			String graphToken = oboTokenService.getAccessTokenForUser(fromUser);

			// 3. Hämta Azure AD ID för användarna
			String fromUserId = teamsService.getUserId(graphToken, fromUser);
			String toUserId = teamsService.getUserId(graphToken, toUser);

			// 4. Skapa eller hämta chat mellan användare
			String chatId = teamsService.createOneOnOneChat(graphToken, fromUserId, toUserId);

			// 5. Skicka meddelande
			teamsService.sendMessage(graphToken, chatId, message);

			return ResponseEntity.ok("Message sent in chat: " + chatId);

		} catch (Exception e) {
			// Returnera 500 med felmeddelande vid problem
			return ResponseEntity.status(500).body("Error sending message: " + e.getMessage());
		}
	}
}