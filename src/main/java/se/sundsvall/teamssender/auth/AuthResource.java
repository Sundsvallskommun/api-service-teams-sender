package se.sundsvall.teamssender.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.teamssender.auth.service.TokenService;
import se.sundsvall.teamssender.configuration.AzureMultiConfig;

@RestController
class AuthResource {

	private final AzureMultiConfig azureMultiConfig;
	private final TokenService tokenService;

	public AuthResource(AzureMultiConfig azureMultiConfig, TokenService tokenService) {
		this.azureMultiConfig = azureMultiConfig;
		this.tokenService = tokenService;
	}

	@GetMapping("/{municipalityId}/login")
	void login(@PathVariable String municipalityId, HttpServletResponse response) throws Exception {
		AzureMultiConfig.AzureConfig config = azureMultiConfig.getAd().get(municipalityId);
		if (config == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid municipality ID");
			return;
		}

		// Lägg till municipalityId som query-param i redirect_uri
		String redirectUriWithId = String.format("%s?municipalityId=%s", config.getRedirectUri(), municipalityId);

		String authorizeUrl = String.format(
				"https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?client_id=%s&response_type=code&redirect_uri=%s&response_mode=query&scope=%s",
				config.getTenantId(),
				config.getClientId(),
				URLEncoder.encode(redirectUriWithId, StandardCharsets.UTF_8),
				URLEncoder.encode(config.getScopes(), StandardCharsets.UTF_8));

		response.sendRedirect(authorizeUrl);
	}
	@GetMapping("/swagger-ui/oauth2-redirect.html")
	ResponseEntity<String> callback(
			@RequestParam("code") String code,
			@RequestParam("municipalityId") String municipalityId) throws Exception {

		return tokenService.exchangeAuthCodeForToken(code, municipalityId);
	}
}
