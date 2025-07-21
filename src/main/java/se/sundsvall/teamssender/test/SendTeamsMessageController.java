package se.sundsvall.teamssender.test;

import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.teamssender.service.OboTokenService;
import se.sundsvall.teamssender.service.TeamsService;

@RestController
public class SendTeamsMessageController {

	private final GraphClientFactory clientFactory;
	private final GraphService graphService;
	private final OboTokenService oboTokenService;
	TeamsService teamsService;

	public SendTeamsMessageController(GraphClientFactory clientFactory, GraphService graphService, OboTokenService oboTokenService, TeamsService teamsService) {
		this.clientFactory = clientFactory;
		this.graphService = graphService;
		this.oboTokenService = oboTokenService;
		this.teamsService = teamsService;
	}

	@PostMapping("/sendTeamsMessage")
	public String send(@RequestParam String to, @RequestParam String from, @RequestParam String message) throws Exception {
		GraphServiceClient client = clientFactory.createClient();
		graphService.sendTeamsMessage(client, to, message);
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

		return "Meddelandet skickades!";
	}
}
