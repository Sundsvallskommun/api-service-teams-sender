package se.sundsvall.teamssender.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.microsoft.graph.chats.ChatsRequestBuilder;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.auth.service.TokenService;
import se.sundsvall.teamssender.exceptions.*;

class TeamsSenderServiceFailureTests {
	private TokenService tokenService;
	private GraphServiceClient graphClient;
	private TeamsSenderService service;

	@BeforeEach
	void setup() {
		tokenService = mock(TokenService.class);
		graphClient = mock(GraphServiceClient.class, RETURNS_DEEP_STUBS);
		service = new TeamsSenderService(tokenService);
	}

	@Test
	void sendTeamsMessage_shouldThrowMessageSendException_whenPostMessageFails() throws Exception {
		TeamsSenderService serviceSpy = spy(service);

		User sender = new User();
		sender.setUserPrincipalName("sender@example.com");
		when(graphClient.me().get()).thenReturn(sender);

		User recipient = new User();
		recipient.setId("recipient-123");
		when(graphClient.users().byUserId(anyString()).get()).thenReturn(recipient);

		Chat chat = new Chat();
		chat.setId("chat-456");
		when(graphClient.chats().post(any(Chat.class))).thenReturn(chat);

		when(graphClient.chats()
			.byChatId("chat-456")
			.messages()
			.post(any(ChatMessage.class)))
			.thenThrow(new RuntimeException("Graph API error"));

		when(serviceSpy.tokenService.initializeGraphServiceClient(anyString())).thenReturn(graphClient);

		SendTeamsMessageRequest request = new SendTeamsMessageRequest("recipient@example.com", "Hello!");

		assertThrows(MessageSendException.class, () -> serviceSpy.sendTeamsMessage(request, "2281"));
	}

	@Test
	void sendTeamsMessage_shouldThrowGraphConnectionException_whenGraphClientFails() throws Exception {
		when(tokenService.initializeGraphServiceClient("2281"))
			.thenThrow(new RuntimeException("Connection error"));

		SendTeamsMessageRequest request = new SendTeamsMessageRequest("user@test.com", "Hello");

		assertThrows(GraphConnectionException.class, () -> service.sendTeamsMessage(request, "2281"));
	}

	@Test
	void sendTeamsMessage_shouldThrowAuthenticationException_whenSenderIsNull() throws Exception {
		when(tokenService.initializeGraphServiceClient("2281")).thenReturn(graphClient);
		when(graphClient.me().get()).thenReturn(null);

		SendTeamsMessageRequest request = new SendTeamsMessageRequest("user@test.com", "Hello");

		assertThrows(AuthenticationException.class, () -> service.sendTeamsMessage(request, "2281"));
	}

	@Test
	void createChat_shouldThrowChatNotCreatedException_whenChatIsNull() {
		TeamsSenderService serviceSpy = spy(new TeamsSenderService(mock(TokenService.class)));

		GraphServiceClient mockClient = mock(GraphServiceClient.class);

		AadUserConversationMember fakeMember = new AadUserConversationMember();
		fakeMember.setOdataType("#microsoft.graph.aadUserConversationMember");
		fakeMember.setRoles(List.of("owner"));
		doReturn(fakeMember).when(serviceSpy).createMember(any(), anyString());

		ChatsRequestBuilder chatsBuilder = mock(ChatsRequestBuilder.class);
		when(mockClient.chats()).thenReturn(chatsBuilder);
		when(chatsBuilder.post(any(Chat.class))).thenReturn(null);

		assertThrows(ChatNotCreatedException.class, () -> serviceSpy.createChat(mockClient, "sender@example.com", "recipient@example.com"));
	}

	@Test
	void createMember_shouldThrowRecipientException_whenUserNotFound() {
		when(graphClient.users().byUserId("unknown@test.com").get()).thenReturn(null);

		assertThrows(RecipientException.class, () -> service.createMember(graphClient, "unknown@test.com"));
	}

	@Test
	void createMember_shouldThrowRecipientException_whenUserHasNoId() {
		User mockUser = new User();
		when(graphClient.users().byUserId("bad@test.com").get()).thenReturn(mockUser);

		assertThrows(RecipientException.class, () -> service.createMember(graphClient, "bad@test.com"));
	}
}
