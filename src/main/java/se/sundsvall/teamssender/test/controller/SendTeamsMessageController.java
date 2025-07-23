package se.sundsvall.teamssender.test.controller;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AuthorizationCodeCredential;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.teamssender.service.OboTokenService;
import se.sundsvall.teamssender.service.TeamsService;
import se.sundsvall.teamssender.test.DatabaseTokenCache;
import se.sundsvall.teamssender.test.StaticTokenCredential;
import se.sundsvall.teamssender.test.config.AzureAuthConfig;
import se.sundsvall.teamssender.test.service.GraphService;
import se.sundsvall.teamssender.test.service.TokenService;

import java.util.Set;

@RestController
public class SendTeamsMessageController {

	@Value("${azure.ad.systemUser}")
	private String systemUser;

	private final GraphService graphService;
	private final OboTokenService oboTokenService;
	private final AzureAuthConfig authConfig;
	private final TokenService tokenService;
	TeamsService teamsService;

	public SendTeamsMessageController(GraphService graphService, OboTokenService oboTokenService, AzureAuthConfig authConfig, TeamsService teamsService, TokenService tokenService) {
		this.graphService = graphService;
		this.oboTokenService = oboTokenService;
		this.authConfig = authConfig;
		this.teamsService = teamsService;
		this.tokenService = tokenService;
	}

//	@PostMapping("/sendTeamsMessage")
//	public String send(@RequestParam String to, @RequestParam String message, HttpSession session) throws Exception {
//		AuthorizationCodeCredential authorizationCodeCredential = (AuthorizationCodeCredential) session.getAttribute("graphCredential");
//
//		if(authorizationCodeCredential == null) {
//			return ("Authorization code not provided");
//		}
//
//		GraphServiceClient client = new GraphServiceClient(authorizationCodeCredential);

//		GraphServiceClient client = clientFactory.createClient();
//		graphService.sendTeamsMessage(client, to, message);
//		String userId = "maria.wiklund@sundsvall.se";
//
//		String token = oboTokenService.getAccessTokenForUser(userId);
//
//		// 3. Hämta Azure AD ID för användarna
//		String fromUserId = teamsService.getUserId(token, from);
//		String toUserId = teamsService.getUserId(token, to);
//
//		// 4. Skapa eller hämta chat mellan användare
//		String chatId = teamsService.createOneOnOneChat(token, fromUserId, toUserId);
//
//		// 5. Skicka meddelande
//		teamsService.sendMessage(token, chatId, message);
//
//		return "Meddelandet skickades!";
//	}

	@PostMapping("/sendTeamsMessage")
	public ResponseEntity<String> sendNotification(HttpSession session, @RequestParam String recipient, String message) throws Exception {
		String accessToken = oboTokenService.getAccessTokenForUser(systemUser);

		// TokenCredential-baserad GraphClient
		TokenCredential credential = new StaticTokenCredential(accessToken);
		GraphServiceClient graphClient = new GraphServiceClient(credential);

		graphService.sendTeamsMessage(graphClient, systemUser, recipient, message);

		return ResponseEntity.ok("Message sent!");
	}

}
