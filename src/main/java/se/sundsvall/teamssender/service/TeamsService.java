package se.sundsvall.teamssender.service;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

@Service
@Data

public class TeamsService {

	private final Map<String, TeamsSender> chatSenders;

	public TeamsService(final List<TeamsSender> chatSenders) {
		this.chatSenders = chatSenders.stream()
			.collect(toMap(TeamsSender::getMunicipalityId, Function.identity()));
	}

	public void sendTeamsMessage(final String municipalityId, final se.sundsvall.teamssender.api.model.@Valid SendTeamsMessageRequest request) {
		var chatSender = chatSenders.get(municipalityId);
		if (isNull(chatSender)) {
			throw Problem.valueOf(Status.BAD_GATEWAY, "No mail sender exists for municipalityId " + municipalityId);
		}

		chatSender.sendTeamsMessage(request);
	}
}
