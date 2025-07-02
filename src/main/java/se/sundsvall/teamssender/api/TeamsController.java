//package se.sundsvall.teamssender.api;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import se.sundsvall.teamssender.service.OboTokenService;
//import se.sundsvall.teamssender.service.TeamsService;
//
//@RestController
//@RequestMapping("/teams")
//public class TeamsController {
//
//    private final OboTokenService oboTokenService;
//    private final TeamsService teamsService;
//
//    public TeamsController(OboTokenService oboTokenService, TeamsService teamsService) {
//        this.oboTokenService = oboTokenService;
//        this.teamsService = teamsService;
//    }
//
//    @PostMapping("/send-message")
//    public ResponseEntity<String> sendMessage(@RequestHeader("Authorization") String authHeader, //Varför skickar vi in denna?
//                                              @RequestParam String userId, //Varför skickar vi in denna?
//                                              @RequestBody String message) {
//        try {
//            // Extract token from "Bearer xyz..."
//            String userToken = authHeader.substring("Bearer ".length());
//
//            // Exchange or get cached token
//            oboTokenService.acquireOboToken(userToken, userId);
//            String graphToken = oboTokenService.getAccessTokenForUser(userId);
//
//            // Call your method to send Teams message with graphToken
//            teamsService.sendTeamsMessage(graphToken, message);
//
//            return ResponseEntity.ok("Message sent!");
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Error: " + e.getMessage());
//        }
//    }
//}
