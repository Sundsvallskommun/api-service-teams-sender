package se.sundsvall.teamssender.integration.microsoftgraph;

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
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

@Component
public class MicrosoftGraphIntegration {

	private final AzureAdTokenService tokenService;

	public MicrosoftGraphIntegration(final AzureAdTokenService tokenService) {
		this.tokenService = tokenService;
	}

	/**
	 * Sends a one-on-one Teams chat message from the authenticated user to the given recipient, creating the chat if it
	 * does not already exist.
	 */
	public void sendChatMessage(final String municipalityId, final String recipient, final String message) {
		final GraphServiceClient graphClient = tokenService.initializeGraphServiceClient(municipalityId);

		final String senderId = resolveSenderId(graphClient);
		final String recipientId = resolveUserId(graphClient, recipient);
		final String chatId = createOneOnOneChat(graphClient, senderId, recipientId);

		postMessage(graphClient, chatId, message);
	}

	private String resolveSenderId(final GraphServiceClient graphClient) {
		final User sender;
		try {
			sender = graphClient.me().get();
		} catch (final Exception e) {
			throw Problem.valueOf(BAD_GATEWAY, "Failed to resolve the authenticated sender from Microsoft Graph: " + e.getMessage());
		}
		return Optional.ofNullable(sender).map(User::getId)
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY, "Microsoft Graph returned the authenticated sender without an id"));
	}

	private String resolveUserId(final GraphServiceClient graphClient, final String identifier) {
		final User user;
		try {
			user = graphClient.users().byUserId(identifier).get();
		} catch (final Exception e) {
			throw Problem.valueOf(NOT_FOUND, "Teams user '%s' could not be found: %s".formatted(identifier, e.getMessage()));
		}
		return Optional.ofNullable(user).map(User::getId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Teams user '%s' could not be found".formatted(identifier)));
	}

	private String createOneOnOneChat(final GraphServiceClient graphClient, final String senderId, final String recipientId) {
		final Chat chat = new Chat();
		chat.setChatType(ChatType.OneOnOne);
		chat.setMembers(List.<ConversationMember>of(asMember(senderId), asMember(recipientId)));

		final Chat createdChat;
		try {
			createdChat = graphClient.chats().post(chat);
		} catch (final Exception e) {
			throw Problem.valueOf(UNPROCESSABLE_CONTENT, "Failed to create the Teams chat: " + e.getMessage());
		}
		return Optional.ofNullable(createdChat).map(Chat::getId)
			.orElseThrow(() -> Problem.valueOf(UNPROCESSABLE_CONTENT, "Microsoft Graph did not return a usable chat"));
	}

	private void postMessage(final GraphServiceClient graphClient, final String chatId, final String message) {
		final ItemBody body = new ItemBody();
		body.setContentType(BodyType.Text);
		body.setContent(message);

		final ChatMessage chatMessage = new ChatMessage();
		chatMessage.setBody(body);

		try {
			graphClient.chats().byChatId(chatId).messages().post(chatMessage);
		} catch (final Exception e) {
			throw Problem.valueOf(UNPROCESSABLE_CONTENT, "Failed to post the Teams message: " + e.getMessage());
		}
	}

	private static AadUserConversationMember asMember(final String userId) {
		final AadUserConversationMember member = new AadUserConversationMember();
		member.setOdataType("#microsoft.graph.aadUserConversationMember");
		member.setRoles(List.of("owner"));
		member.setAdditionalData(Map.of("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + userId + "')"));

		return member;
	}
}
