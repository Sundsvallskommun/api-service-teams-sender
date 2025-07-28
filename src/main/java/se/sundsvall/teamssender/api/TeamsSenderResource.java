package se.sundsvall.teamssender.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.service.TeamsSenderService;

@RestController
public class TeamsSenderResource {

	private final TeamsSenderService teamsSenderService;

	public TeamsSenderResource(TeamsSenderService teamsSenderService) {
		this.teamsSenderService = teamsSenderService;
	}

	@PostMapping("/sendTeamsMessage")
	public ResponseEntity<String> sendNotification(@RequestBody SendTeamsMessageRequest request) throws Exception {

		teamsSenderService.sendTeamsMessage(request);

		return ResponseEntity.status(HttpStatus.CREATED).body("Message sent");
	}

}
