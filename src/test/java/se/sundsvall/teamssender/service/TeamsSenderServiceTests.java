package se.sundsvall.teamssender.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.teamssender.TestDataFactory.createValidSendTeamsMessageRequest;

import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.teamssender.auth.service.TokenService;

@ExtendWith(MockitoExtension.class)
class TeamsSenderServiceTests {

	@Mock
	private TokenService tokenService;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private GraphServiceClient graphClient;

	@InjectMocks
	private TeamsSenderService service;

	@Test
	void sendTeamsMessage_success() throws Exception {
		var request = createValidSendTeamsMessageRequest();

		User mockSender = new User();
		mockSender.setUserPrincipalName("sender@example.com");
		mockSender.setId("sender-id");

		Chat mockChat = new Chat();
		mockChat.setId("chat-123");

		when(tokenService.initializeGraphServiceClient(anyString())).thenReturn(graphClient);
		when(graphClient.me().get()).thenReturn(mockSender);
		when(graphClient.chats().post(any(Chat.class))).thenReturn(mockChat);

		service.sendTeamsMessage(request, "municipal-1");

		verify(tokenService).initializeGraphServiceClient("municipal-1");
		verify(graphClient.me()).get();
		verify(graphClient.chats()).post(any(Chat.class));
		verify(graphClient.chats()
			.byChatId("chat-123")
			.messages())
			.post(any(ChatMessage.class));
	}

	@Test
	void createMessage_shouldReturnChatMessage() {
		ChatMessage result = service.createMessage("message");

		assertThat(result).isNotNull();
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getContent()).isEqualTo("message");
	}

	@Test
	void createChat_shouldReturnChatWithMembers() {
		User user1 = new User();
		user1.setId("u1");
		when(graphClient.users().byUserId("user1").get()).thenReturn(user1);

		User user2 = new User();
		user2.setId("u2");
		when(graphClient.users().byUserId("user2").get()).thenReturn(user2);

		Chat expectedChat = new Chat();
		expectedChat.setId("chat-xyz");
		when(graphClient.chats().post(any(Chat.class))).thenReturn(expectedChat);

		Chat result = service.createChat(graphClient, "user1", "user2");

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("chat-xyz");
		verify(graphClient.chats()).post(any(Chat.class));
	}

	@Test
	void createMember_shouldReturnConversationMember() {
		User user = new User();
		user.setId("some-user-id");
		when(graphClient.users().byUserId("user@example.com").get()).thenReturn(user);

		AadUserConversationMember member = service.createMember(graphClient, "user@example.com");

		assertThat(member.getRoles()).containsExactly("owner");
		assertThat(member.getAdditionalData())
			.containsEntry("user@odata.bind", "https://graph.microsoft.com/v1.0/users('some-user-id')");
	}
}
