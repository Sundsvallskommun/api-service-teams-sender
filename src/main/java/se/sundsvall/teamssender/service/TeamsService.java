package se.sundsvall.teamssender.service;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import jakarta.validation.Valid;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;

@Service
@Data

public class TeamsService {

	private GraphServiceClient graphClient;

	public void setGraphClient(GraphServiceClient graphClient) {
		this.graphClient = graphClient;
	}

	public Chat createChat(String user1Id, String user2Id) {
		Chat chat = new Chat();
		chat.setChatType(ChatType.OneOnOne);

		LinkedList<ConversationMember> members = new LinkedList<>();
		members.add(createMember(user1Id));
		members.add(createMember(user2Id));
		chat.setMembers(members);

		return graphClient.chats().post(chat);
	}

	public void sendTeamsMessage(SendTeamsMessageRequest request) {
		var createdChat = createChat(request.getUser(), request.getSender());
		var chatMessage = createMessage(request.getMessage());

		graphClient.chats()
			.byChatId(Objects.requireNonNull(createdChat.getId()))
			.messages()
			.post(chatMessage);
	}

	public ChatMessage createMessage(String message) {
		ItemBody body = new ItemBody();
		body.setContent(message);

		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setBody(body);

		return chatMessage;
	}

	private AadUserConversationMember createMember(String userId) {
		AadUserConversationMember member = new AadUserConversationMember();
		member.setOdataType("#microsoft.graph.aadUserConversationMember");
		member.setRoles(List.of("owner"));

		HashMap<String, Object> additionalData = new HashMap<>();
		additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + userId + "')");
		member.setAdditionalData(additionalData);

		return member;
	}

	private final Map<String, TeamsSender> chatSenders;

	public TeamsService(final List<TeamsSender> chatSenders) {
		this.chatSenders = chatSenders.stream()
			.collect(toMap(TeamsSender::getMunicipalityId, Function.identity()));
	}

	public void initiateMessage(final String municipalityId, final se.sundsvall.teamssender.api.model.@Valid SendTeamsMessageRequest request, GraphServiceClient graphClient) {
		setGraphClient(graphClient);
		var chatSender = chatSenders.get(municipalityId);
		if (isNull(chatSender)) {
			throw Problem.valueOf(Status.BAD_GATEWAY, "No mail sender exists for municipalityId " + municipalityId);
		}

		chatSender.sendTeamsMessage(request);
	}
}
