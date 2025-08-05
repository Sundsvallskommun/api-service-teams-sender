package se.sundsvall.teamssender.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.teamssender.auth.service.TokenService;
import se.sundsvall.teamssender.configuration.AzureConfig;

@RestController
class AuthResource {

	private final AzureConfig config;
	private final TokenService tokenService;

	AuthResource(AzureConfig config, TokenService tokenService) {
		this.config = config;
		this.tokenService = tokenService;
	}

	@GetMapping("/login")
	void login(HttpServletResponse response) throws Exception {
		String authorizeUrl = String.format(
			"https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?client_id=%s&response_type=code&redirect_uri=%s&response_mode=query&scope=%s",
			config.getTenantId(),
			config.getClientId(),
			URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8),
			URLEncoder.encode(config.getScopes(), StandardCharsets.UTF_8));

		response.sendRedirect(authorizeUrl);
	}

	@GetMapping("/swagger-ui/oauth2-redirect.html")
	ResponseEntity<String> callback(HttpServletRequest request) throws Exception {
		return tokenService.exchangeAuthCodeForToken(request.getParameter("code"));
	}
}
