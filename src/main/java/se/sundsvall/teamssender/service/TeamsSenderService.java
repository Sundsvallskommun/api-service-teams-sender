package se.sundsvall.teamssender.service;

import com.microsoft.graph.models.AadUserConversationMember;
import com.microsoft.graph.models.Chat;
import com.microsoft.graph.models.ChatMessage;
import com.microsoft.graph.models.ChatType;
import com.microsoft.graph.models.ConversationMember;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.auth.service.TokenService;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Service
public class TeamsSenderService {

	private final TokenService tokenService;

	public TeamsSenderService(final TokenService tokenService) {
		this.tokenService = tokenService;
	}

	public void sendTeamsMessage(final SendTeamsMessageRequest request, final String municipalityId) {
		final GraphServiceClient graphClient = tokenService.initializeGraphServiceClient(municipalityId);

		final User sender = resolveSender(graphClient);

		final Chat createdChat = createChat(graphClient, sender.getUserPrincipalName(), request.getRecipient());
		final ChatMessage chatMessage = createMessage(request.getMessage());

		try {
			graphClient.chats()
				.byChatId(Objects.requireNonNull(createdChat.getId(), "Created Teams chat is missing an id"))
				.messages()
				.post(chatMessage);
		} catch (final ThrowableProblem e) {
			throw e;
		} catch (final Exception e) {
			throw Problem.valueOf(UNPROCESSABLE_ENTITY, "Failed to post the Teams message: " + e.getMessage());
		}
	}

	private User resolveSender(final GraphServiceClient graphClient) {
		try {
			return Objects.requireNonNull(graphClient.me().get(), "Could not resolve the authenticated sender");
		} catch (final Exception e) {
			throw Problem.valueOf(BAD_GATEWAY, "Failed to resolve the authenticated sender from Microsoft Graph: " + e.getMessage());
		}
	}

	private Chat createChat(final GraphServiceClient graphClient, final String senderId, final String recipientId) {
		final Chat chat = new Chat();
		chat.setChatType(ChatType.OneOnOne);

		final LinkedList<ConversationMember> members = new LinkedList<>();
		members.add(createMember(graphClient, senderId));
		members.add(createMember(graphClient, recipientId));
		chat.setMembers(members);

		try {
			return Objects.requireNonNull(graphClient.chats().post(chat), "Microsoft Graph returned no chat");
		} catch (final ThrowableProblem e) {
			throw e;
		} catch (final Exception e) {
			throw Problem.valueOf(UNPROCESSABLE_ENTITY, "Failed to create the Teams chat: " + e.getMessage());
		}
	}

	private ChatMessage createMessage(final String message) {
		final ItemBody body = new ItemBody();
		body.setContent(message);

		final ChatMessage chatMessage = new ChatMessage();
		chatMessage.setBody(body);

		return chatMessage;
	}

	private AadUserConversationMember createMember(final GraphServiceClient graphClient, final String userEmail) {
		final User user;
		try {
			user = Objects.requireNonNull(graphClient.users().byUserId(userEmail).get(), "No matching user");
		} catch (final Exception e) {
			throw Problem.valueOf(NOT_FOUND, "Teams user '%s' could not be found: %s".formatted(userEmail, e.getMessage()));
		}

		final AadUserConversationMember member = new AadUserConversationMember();
		member.setOdataType("#microsoft.graph.aadUserConversationMember");
		member.setRoles(List.of("owner"));

		final HashMap<String, Object> additionalData = new HashMap<>();
		additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + Objects.requireNonNull(user.getId()) + "')");
		member.setAdditionalData(additionalData);

		return member;
	}
}
