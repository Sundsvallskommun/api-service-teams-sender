package se.sundsvall.teamssender.test;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraphController {

	private final AzureAuthConfig config;
	private final GraphCredentialStorage credentialStorage;
//	private final OboTokenService tokenService;

	public GraphController(AzureAuthConfig config, GraphCredentialStorage credentialStorage) {
		this.config = config;
		this.credentialStorage = credentialStorage;
	}

	@GetMapping("/login")
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String codeVerifier = PkceUtil.generateCodeVerifier();
		String codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier);

		// Spara verifier temporärt (t.ex. i session)
		request.getSession().setAttribute("code_verifier", codeVerifier);

		String authorizeUrl = String.format(
			"https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?client_id=%s&response_type=code&redirect_uri=%s&response_mode=query&scope=%s&code_challenge=%s&code_challenge_method=S256",
			config.getTenantId(),
			config.getClientId(),
			URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8),
			URLEncoder.encode(config.getScopes(), StandardCharsets.UTF_8),
			URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8));

		response.sendRedirect(authorizeUrl);
	}

	@GetMapping("/swagger-ui/oauth2-redirect")
	public String callback(HttpServletRequest request) {
		System.out.println("inne i callbackmetoden");
		String code = request.getParameter("code");
		String codeVerifier = (String) request.getSession().getAttribute("code_verifier");

		AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
			.clientId(config.getClientId())
			.tenantId(config.getTenantId())
			.clientSecret(config.getClientSecret())
			.authorizationCode(code)
			.redirectUrl(config.getRedirectUri())
			.build();

		// Hämta token omedelbart
		TokenRequestContext context = new TokenRequestContext()
			.addScopes(config.getScopes().split(" "));

		AccessToken token = credential.getToken(context).block();

		// Spara access/refresh-token (här: i minnesstore)
		credentialStorage.setCredential(credential);

		return "Inloggning lyckades. Access token gäller till: " + token.getExpiresAt();
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


