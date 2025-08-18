//package se.sundsvall.teamssender.auth;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.util.Map;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.ResponseEntity;
//import se.sundsvall.teamssender.auth.service.TokenService;
//import se.sundsvall.teamssender.configuration.AzureConfig;
//import se.sundsvall.teamssender.configuration.AzureConfig.Azure;
//
//class AuthResourceTest {
//
//	private AuthResource authResource;
//	private AzureConfig azureConfig;
//	private TokenService tokenService;
//
//	@BeforeEach
//	void setUp() {
//		azureConfig = mock(AzureConfig.class);
//		tokenService = mock(TokenService.class);
//		authResource = new AuthResource(azureConfig, tokenService);
//	}
//
//	@Test
//	void testLogin_validMunicipality_redirectsToLoginUrl() throws Exception {
//		var response = mock(HttpServletResponse.class);
//		var config = mock(Azure.class);
//
//		when(config.getLoginUrl()).thenReturn("https://login.microsoftonline.com/example");
//		when(azureConfig.getAd()).thenReturn(Map.of("2281", config));
//
//		authResource.login("2281", response);
//
//		verify(response).sendRedirect("https://login.microsoftonline.com/example");
//	}
//
//	@Test
//	void testLogin_invalidMunicipality_returns404() throws Exception {
//		var response = mock(HttpServletResponse.class);
//
//		when(azureConfig.getAd()).thenReturn(Map.of()); // Tom karta
//
//		authResource.login("0000", response);
//
//		verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid municipality ID");
//	}
//
//	@Test
//	void testCallback_successfulTokenExchange() throws Exception {
//		var request = mock(HttpServletRequest.class);
//
//		when(request.getParameter("code")).thenReturn("abc123");
//		when(request.getParameter("state")).thenReturn("2281");
//
//		ResponseEntity<String> expectedResponse = ResponseEntity.ok("token");
//		when(tokenService.exchangeAuthCodeForToken("abc123", "2281")).thenReturn(expectedResponse);
//
//		var response = authResource.callback(request);
//
//		assertEquals(expectedResponse, response);
//		verify(tokenService).exchangeAuthCodeForToken("abc123", "2281");
//	}
//}
