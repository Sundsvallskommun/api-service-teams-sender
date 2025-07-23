package se.sundsvall.teamssender.test.controller;

import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.RedirectView;
import se.sundsvall.teamssender.service.OboTokenService;
import se.sundsvall.teamssender.test.config.AzureAuthConfig;
import se.sundsvall.teamssender.test.service.GraphService;
import se.sundsvall.teamssender.test.service.TokenService;

@RestController
public class GraphController {

	private final AzureAuthConfig config;
	private final OboTokenService oboTokenService;
	private final GraphService graphService;
	private final TokenService tokenService;
//	private final OboTokenService tokenService;

	public GraphController(AzureAuthConfig config, OboTokenService oboTokenService, GraphService graphService, TokenService tokenService) {
		this.config = config;
		this.oboTokenService = oboTokenService;
		this.graphService = graphService;
		this.tokenService = tokenService;
	}

	@GetMapping("/login")
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String authorizeUrl = String.format(
			"https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?client_id=%s&response_type=code&redirect_uri=%s&response_mode=query&scope=%s",
			config.getTenantId(),
			config.getClientId(),
			URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8),
			URLEncoder.encode(config.getScopes(), StandardCharsets.UTF_8));

		response.sendRedirect(authorizeUrl);

		// 1. Generera en code_verifier
//		SecureRandom sr = new SecureRandom();
//		byte[] codeVerifierBytes = new byte[32];
//		sr.nextBytes(codeVerifierBytes);
//		String codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
//
//		// Spara verifier, t.ex. i sessionen, för senare användning vid tokenutbyte
//		request.getSession().setAttribute("pkce_code_verifier", codeVerifier);
//
//		// 2. Skapa code_challenge baserat på S256 (SHA‑256 + Base64‑url)
//		MessageDigest md = MessageDigest.getInstance("SHA-256");
//		byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
//		String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
//
//		// 3. Bygg authorize-url med PKCE parametrar
//		String authorizeUrl = String.format(
//				"https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?" +
//						"client_id=%s&response_type=code&redirect_uri=%s&response_mode=query&scope=%s" +
//						"&state=12345&code_challenge=%s&code_challenge_method=S256",
//				config.getTenantId(),
//				config.getClientId(),
//				URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8),
//				URLEncoder.encode(config.getScopes(), StandardCharsets.UTF_8),
//				URLEncoder.encode(codeChallenge, StandardCharsets.US_ASCII)
//		);
	}

	@GetMapping("/swagger-ui/oauth2-redirect.html")
	public ResponseEntity<String> callback(HttpServletRequest request) throws Exception {
		System.out.println("inne i callbackmetoden");
		String code = request.getParameter("code");
		return oboTokenService.exchangeAuthCodeForToken(code);
	}

//	@GetMapping("/swagger-ui/oauth2-redirect.html")
//	public RedirectView callback(HttpServletRequest request, HttpSession session) throws Exception {
//		System.out.println("inne i callbackmetoden");
//		String code = request.getParameter("code");
////		String codeVerifier = (String) request.getSession().getAttribute("pkce_code_verifier");
////		String userId = "maria.wiklund@sundsvall.se";
////
////		oboTokenService.acquireAccessToken(code, codeVerifier, userId);
//
//		System.out.println(code);
//
//		AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
//			.clientId(config.getClientId())
//			.tenantId(config.getTenantId())
//			.clientSecret(config.getClientSecret())
//			.authorizationCode(code)
//			.redirectUrl(config.getRedirectUri())
//			.build();
//
//		session.setAttribute("graphCredential", credential);

//		GraphServiceClient client = new GraphServiceClient(credential);
//
//		graphService.sendTeamsMessage(client, "per-erik.brorsson@sundsvall.se", "hej");

//		// Hämta token omedelbart
//		TokenRequestContext context = new TokenRequestContext()
//			.addScopes(config.getScopes().split(" "));
//
//		AccessToken token = credential.getToken(context).block();

		// Spara access/refresh-token (här: i minnesstore)
//		credentialStorage.setCredential(credential);
//
//		return new RedirectView("/swagger-ui/index.html");
//	}


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


