package se.sundsvall.teamssender.test;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.teamssender.service.OboTokenService;

@RestController
@CrossOrigin(origins = "http://localhost:8080",
		allowedHeaders = {"Authorization","Content-Type"})
public class GraphController {

	private final AzureAuthConfig config;
	private final GraphCredentialStorage credentialStorage;
	private final OboTokenService oboTokenService;
//	private final OboTokenService tokenService;

	public GraphController(AzureAuthConfig config, GraphCredentialStorage credentialStorage, OboTokenService oboTokenService) {
		this.config = config;
		this.credentialStorage = credentialStorage;
		this.oboTokenService = oboTokenService;
	}

	@GetMapping("/login")
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		String authorizeUrl = String.format(
//			"https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?client_id=%s&response_type=code&redirect_uri=%s&response_mode=query&scope=%s",
//			config.getTenantId(),
//			config.getClientId(),
//			URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8),
//			URLEncoder.encode(config.getScopes(), StandardCharsets.UTF_8));
//
//		response.sendRedirect(authorizeUrl);

		// 1. Generera en code_verifier
		SecureRandom sr = new SecureRandom();
		byte[] codeVerifierBytes = new byte[32];
		sr.nextBytes(codeVerifierBytes);
		String codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);

		// Spara verifier, t.ex. i sessionen, för senare användning vid tokenutbyte
		request.getSession().setAttribute("pkce_code_verifier", codeVerifier);

		// 2. Skapa code_challenge baserat på S256 (SHA‑256 + Base64‑url)
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
		String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);

		// 3. Bygg authorize-url med PKCE parametrar
		String authorizeUrl = String.format(
				"https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?" +
						"client_id=%s&response_type=code&redirect_uri=%s&response_mode=query&scope=%s" +
						"&code_challenge=%s&code_challenge_method=S256",
				config.getTenantId(),
				config.getClientId(),
				URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8),
				URLEncoder.encode(config.getScopes(), StandardCharsets.UTF_8),
				URLEncoder.encode(codeChallenge, StandardCharsets.US_ASCII)
		);

		response.sendRedirect(authorizeUrl);
	}

	@GetMapping("/swagger-ui/oauth2-redirect.html")
	public String callback(HttpServletRequest request) throws Exception {
		System.out.println("inne i callbackmetoden");
		String code = request.getParameter("code");
		String codeVerifier = (String) request.getSession().getAttribute("pkce_code_verifier");
		String userId = "maria.wiklund@sundsvall.se";

		oboTokenService.acquireAccessToken(code, codeVerifier, userId);

//		AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
//			.clientId(config.getClientId())
//			.tenantId(config.getTenantId())
//			.clientSecret(config.getClientSecret())
//			.authorizationCode(code)
//			.redirectUrl(config.getRedirectUri())
//			.build();
//
//		// Hämta token omedelbart
//		TokenRequestContext context = new TokenRequestContext()
//			.addScopes(config.getScopes().split(" "));
//
//		AccessToken token = credential.getToken(context).block();
//
//		// Spara access/refresh-token (här: i minnesstore)
//		credentialStorage.setCredential(credential);

		return "Inloggning lyckades";
	}


//	@GetMapping("/auth-url")
//	public String getAuthUrl() throws Exception {
//		return tokenService.buildAuthUrl();
//	}
//
//	@GetMapping("/redirect")
//	public String handleRedirect(@RequestParam("code") String code) {
//		try {
//			IAuthenticationResult result = tokenService.redeemCode(code);
//			System.out.println("🔐 AccessToken: " + result.accessToken());
//			return "Inloggad! Access token erhållen.";
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "Fel vid inlösen av kod: " + e.getMessage();
//		}
//	}
}


