package se.sundsvall.teamssender.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.sundsvall.teamssender.service.OboTokenService;

@RestController
@RequestMapping("/teams")
public class TeamsController {

    private final OboTokenService oboTokenService;

    public TeamsController(OboTokenService oboTokenService) {
        this.oboTokenService = oboTokenService;
    }

    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessage(@RequestHeader("Authorization") String authHeader,
                                              @RequestParam String userId,
                                              @RequestBody String message) {
        try {
            // Extract token from "Bearer xyz..."
            String userToken = authHeader.substring("Bearer ".length());

            // Exchange or get cached token
            oboTokenService.acquireOboToken(userToken, userId);
            String graphToken = oboTokenService.getAccessTokenForUser(userId);

            // Call your method to send Teams message with graphToken
            // sendTeamsMessage(graphToken, message);

            return ResponseEntity.ok("Message sent!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
