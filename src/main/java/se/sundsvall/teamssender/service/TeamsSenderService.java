package se.sundsvall.teamssender.service;

import com.microsoft.graph.models.AadUserConversationMember;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.Chat;
import com.microsoft.graph.models.ChatMessage;
import com.microsoft.graph.models.ChatType;
import com.microsoft.graph.models.ConversationMember;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

@Service
public class TeamsSenderService {

	private final TokenService tokenService;

	public TeamsSenderService(final TokenService tokenService) {
		this.tokenService = tokenService;
	}

	public void sendTeamsMessage(final SendTeamsMessageRequest request, final String municipalityId) {
		final GraphServiceClient graphClient = tokenService.initializeGraphServiceClient(municipalityId);

		final String senderId = Optional.ofNullable(resolveSender(graphClient).getId())
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY, "Microsoft Graph returned the authenticated sender without an id"));

		final Chat chat = createChat(graphClient, senderId, request.getRecipient());

		final String chatId = Optional.ofNullable(chat.getId())
			.orElseThrow(() -> Problem.valueOf(UNPROCESSABLE_CONTENT, "Microsoft Graph returned a chat without an id"));
		final ChatMessage chatMessage = createMessage(request.getMessage());

		try {
			graphClient.chats().byChatId(chatId).messages().post(chatMessage);
		} catch (final Exception e) {
			throw Problem.valueOf(UNPROCESSABLE_CONTENT, "Failed to post the Teams message: " + e.getMessage());
		}
	}

	private User resolveSender(final GraphServiceClient graphClient) {
		final User sender;
		try {
			sender = graphClient.me().get();
		} catch (final Exception e) {
			throw Problem.valueOf(BAD_GATEWAY, "Failed to resolve the authenticated sender from Microsoft Graph: " + e.getMessage());
		}
		return Optional.ofNullable(sender)
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY, "Microsoft Graph did not return the authenticated sender"));
	}

	private Chat createChat(final GraphServiceClient graphClient, final String senderId, final String recipient) {
		final Chat chat = new Chat();
		chat.setChatType(ChatType.OneOnOne);

		chat.setMembers(List.<ConversationMember>of(asMember(senderId), asMember(resolveRecipientId(graphClient, recipient))));

		final Chat createdChat;
		try {
			createdChat = graphClient.chats().post(chat);
		} catch (final Exception e) {
			throw Problem.valueOf(UNPROCESSABLE_CONTENT, "Failed to create the Teams chat: " + e.getMessage());
		}
		return Optional.ofNullable(createdChat)
			.orElseThrow(() -> Problem.valueOf(UNPROCESSABLE_CONTENT, "Microsoft Graph returned no chat"));
	}

	private String resolveRecipientId(final GraphServiceClient graphClient, final String recipient) {
		final User user;
		try {
			user = graphClient.users().byUserId(recipient).get();
		} catch (final Exception e) {
			throw Problem.valueOf(NOT_FOUND, "Teams user '%s' could not be found: %s".formatted(recipient, e.getMessage()));
		}
		return Optional.ofNullable(user).map(User::getId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Teams user '%s' could not be found".formatted(recipient)));
	}

	private ChatMessage createMessage(final String message) {
		final ItemBody body = new ItemBody();
		body.setContentType(BodyType.Text);
		body.setContent(message);

		final ChatMessage chatMessage = new ChatMessage();
		chatMessage.setBody(body);

		return chatMessage;
	}

	private static AadUserConversationMember asMember(final String userId) {
		final AadUserConversationMember member = new AadUserConversationMember();
		member.setOdataType("#microsoft.graph.aadUserConversationMember");
		member.setRoles(List.of("owner"));
		member.setAdditionalData(Map.of("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + userId + "')"));

		return member;
	}
}
