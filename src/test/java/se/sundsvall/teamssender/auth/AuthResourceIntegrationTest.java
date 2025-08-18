package se.sundsvall.teamssender.auth;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import se.sundsvall.teamssender.configuration.AzureConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class AuthResourceIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Mock
	private AzureConfig azureConfig;

	private AutoCloseable mocks;

	@BeforeEach
	void setup() {
		mocks = MockitoAnnotations.openMocks(this);

		AzureConfig.Azure azure = new AzureConfig.Azure();
		azure.setUser("user@example.com");
		azure.setClientId("client-id");
		azure.setTenantId("tenant-id");
		azure.setRedirectUri("http://localhost/redirect");
		azure.setScopes("openid profile");
		azure.setAuthorityUrl("https://login.microsoftonline.com/common");
		azure.setClientSecret("secret");
		azure.setLoginUrl("http://localhost/login");

		when(azureConfig.getAd()).thenReturn(Map.of("municipality1", azure));
	}

	@AfterEach
	void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	void login_validMunicipality_returnsRedirect() throws Exception {
		mockMvc.perform(get("/auth/login?municipality=municipality1"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void login_invalidMunicipality_returns404() throws Exception {
		mockMvc.perform(get("/auth/login?municipality=invalid"))
			.andExpect(status().isNotFound());
	}

	@Test
	void callback_returnsExpectedResponseFromTokenService() throws Exception {
		mockMvc.perform(get("/auth/callback?code=test-code&state=test-state"))
			.andExpect(status().isOk());
	}
}
