//package se.sundsvall.teamssender.auth;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import java.util.Map;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//import se.sundsvall.teamssender.auth.service.TokenService;
//import se.sundsvall.teamssender.configuration.AzureConfig;
//
//@WebMvcTest(AuthResource.class)
//class AuthResourceIntegrationTest {
//
//	@Autowired
//	private MockMvc mockMvc;
//
//	@MockitoBean
//	private AzureConfig azureConfig;
//
//	@MockitoBean
//	private TokenService tokenService;
//
//	@Test
//	void login_validMunicipality_redirects() throws Exception {
//		var azure = new AzureConfig.Azure();
//		azure.setLoginUrl("https://login.microsoftonline.com/example");
//
//		when(azureConfig.getAd()).thenReturn(Map.of("2281", azure));
//
//		mockMvc.perform(get("/2281/login"))
//			.andExpect(status().is3xxRedirection())
//			.andExpect(redirectedUrl("https://login.microsoftonline.com/example"));
//	}
//
//	@Test
//	void login_invalidMunicipality_returns404() throws Exception {
//		when(azureConfig.getAd()).thenReturn(Map.of());
//
//		mockMvc.perform(get("/0000/login"))
//			.andExpect(status().isNotFound());
//	}
//
//	@Test
//	void callback_successfulTokenExchange() throws Exception {
//		when(tokenService.exchangeAuthCodeForToken("abc123", "2281"))
//			.thenReturn(org.springframework.http.ResponseEntity.ok("token"));
//
//		mockMvc.perform(get("/callback")
//			.param("code", "abc123")
//			.param("state", "2281"))
//			.andExpect(status().isOk())
//			.andExpect(content().string("token"));
//	}
//}
