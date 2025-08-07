package se.sundsvall.teamssender.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.teamssender.auth.service.TokenService;
import se.sundsvall.teamssender.configuration.AzureConfig;

@RestController
class AuthResource {

	private final AzureConfig azureConfig;
	private final TokenService tokenService;

	public AuthResource(AzureConfig azureConfig, TokenService tokenService) {
		this.azureConfig = azureConfig;
		this.tokenService = tokenService;
	}

	@GetMapping("/{municipalityId}/login")
	void login(@PathVariable String municipalityId, HttpServletResponse response) throws Exception {
		AzureConfig.Azure config = azureConfig.getAd().get(municipalityId);
		if (config == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid municipality ID");
			return;
		}

		String statePayload = Base64.getUrlEncoder()
			.encodeToString(("municipalityId=" + municipalityId).getBytes(StandardCharsets.UTF_8));

		String authorizeUrl = String.format(
			"https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?" +
				"&client_id=%s&response_type=code&redirect_uri=%s&response_mode=query&scope=%s&state=%s",
			config.getTenantId(),
			config.getClientId(),
			URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8),
			URLEncoder.encode(config.getScopes(), StandardCharsets.UTF_8),
			URLEncoder.encode(statePayload, StandardCharsets.UTF_8));
		response.sendRedirect(authorizeUrl);
	}

	@GetMapping("/swagger-ui/oauth2-redirect.html")
	ResponseEntity<String> callback(HttpServletRequest request) throws Exception {
		String code = request.getParameter("code");
		String stateParam = request.getParameter("state");

		String decoded = new String(Base64.getUrlDecoder().decode(stateParam), StandardCharsets.UTF_8);
		String municipalityId = decoded.split("=")[1];

		return tokenService.exchangeAuthCodeForToken(code, municipalityId);
	}
}
