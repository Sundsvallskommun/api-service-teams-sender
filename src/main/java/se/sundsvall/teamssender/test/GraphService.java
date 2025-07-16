package se.sundsvall.teamssender.test;

import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class GraphService {

	public void sendTeamsMessage(GraphServiceClient graphClient, String recipientEmail, String message) {
		String user = "maria.wiklund@sundsvall.se";
		var createdChat = createChat(graphClient, user, recipientEmail);
		var chatMessage = createMessage(message);

		graphClient.chats()
			.byChatId(Objects.requireNonNull(createdChat.getId()))
			.messages()
			.post(chatMessage);
	}

//    public void sendTeamsMessage(GraphServiceClient client, String recipientEmail, String message) {
	// Hämta användare
//        User user = client.users(recipientEmail).buildRequest().get();
//
//        // Skapa en ny chatt med mottagaren
//        Chat chat = createChat();
//        ChatMember member = new AadUserConversationMember();
//        member.roles = List.of("owner");
//        member.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users/" + user.id));
//        chat.members = List.of(member);
//
//        Chat createdChat = client.chats().buildRequest().post(chat);
//
//        // Skicka meddelande
//        ChatMessage chatMessage = new ChatMessage();
//        ItemBody body = new ItemBody();
//        body.contentType = BodyType.TEXT;
//        body.content = message;
//        chatMessage.body = body;
//
//        client.chats(createdChat.id)
//                .messages()
//                .buildRequest()
//                .post(chatMessage);
//    }

	public Chat createChat(GraphServiceClient graphClient, String user1Id, String user2Id) {
		Chat chat = new Chat();
		chat.setChatType(ChatType.OneOnOne);

		LinkedList<ConversationMember> members = new LinkedList<>();
		members.add(createMember(graphClient, user1Id));
		members.add(createMember(graphClient, user2Id));
		chat.setMembers(members);

		return graphClient.chats().post(chat);
	}

//    public void sendTeamsMessage(GraphServiceClient graphClient, SendTeamsMessageRequest request) {
//        var createdChat = createChat(request.getUser(), request.getSender());
//        var chatMessage = createMessage(request.getMessage());
//
//        graphClient.chats()
//                .byChatId(Objects.requireNonNull(createdChat.getId()))
//                .messages()
//                .post(chatMessage);
//    }

	public ChatMessage createMessage(String message) {
		ItemBody body = new ItemBody();
		body.setContent(message);

		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setBody(body);

		return chatMessage;
	}

	private AadUserConversationMember createMember(GraphServiceClient graphClient, String userEmail) {
		User user = graphClient.users().byUserId(userEmail).get();
		AadUserConversationMember member = new AadUserConversationMember();
		member.setOdataType("#microsoft.graph.aadUserConversationMember");
		member.setRoles(List.of("owner"));

		HashMap<String, Object> additionalData = new HashMap<>();
		additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + user.getId() + "')");
		member.setAdditionalData(additionalData);

		return member;
	}
}
