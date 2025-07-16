package se.sundsvall.teamssender.test;

import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SendTeamsMessageController {

	private final GraphClientFactory clientFactory;
	private final GraphService graphService;

	public SendTeamsMessageController(GraphClientFactory clientFactory, GraphService graphService) {
		this.clientFactory = clientFactory;
		this.graphService = graphService;
	}

	@PostMapping("/sendTeamsMessage")
	public String send(@RequestParam String to, @RequestParam String message) {
		GraphServiceClient client = clientFactory.createClient();
		graphService.sendTeamsMessage(client, to, message);
		return "Meddelandet skickades!";
	}
}
