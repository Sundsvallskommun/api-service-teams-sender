package se.sundsvall.teamssender.service;

import com.microsoft.aad.msal4j.*;
import jakarta.annotation.PostConstruct;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.sundsvall.teamssender.test.DatabaseTokenCache;
import se.sundsvall.teamssender.test.config.AzureAuthConfig;
import se.sundsvall.teamssender.test.repository.JPATokenCacheRepository;

@Service
public class OboTokenService {

	@Value("${azure.ad.tenant-id}")
	private String tenantId;

	@Value("${azure.ad.client-id}")
	private String clientId;

	@Value("${azure.ad.certificate-path}")
	private String certificatePath; // path to .pfx or .pem

	@Value("${azure.ad.certificate-key}")
	private String certificateKey; //

	private ConfidentialClientApplication app;

	private final String scope = "https://graph.microsoft.com/.default";

	private final Map<String, TokenResponse> tokenCache = new ConcurrentHashMap<>();

	private final AzureAuthConfig config;

	private final JPATokenCacheRepository jpaTokenCacheRepository;

	@Value("${azure.ad.systemUser}")
	private String systemUser;

	@Autowired
	public OboTokenService(AzureAuthConfig config, JPATokenCacheRepository jpaTokenCacheRepository) {
		this.config = config;
		this.jpaTokenCacheRepository = jpaTokenCacheRepository;
	}

//	@PostConstruct
//	public void init() throws Exception {
//
//		if (Security.getProvider("BC") == null) {
//			Security.addProvider(new BouncyCastleProvider());
//		}
//
//		// Load private key and cert from PFX file (or PEM)
//		CertificateAndKey certAndKey = loadCertificateAndKey(certificatePath, certificateKey);
//		// 🔍 Logga thumbprint från certifikatet som faktiskt används
//		byte[] encoded = certAndKey.certificate.getEncoded();
//		MessageDigest md = MessageDigest.getInstance("SHA-1");
//		byte[] digest = md.digest(encoded);
//		String thumbprint = DatatypeConverter.printHexBinary(digest).toLowerCase();
//		System.out.println("🔍 Using cert thumbprint (calculated): " + thumbprint);
//
//		System.out.println("🔍 Genererar JWT manuellt...");
//
//		String jwt = JwtDebugUtil.createClientAssertion(
//			clientId,
//			tenantId,
//			certAndKey.certificate,
//			(RSAPrivateKey) certAndKey.privateKey);
//
//		System.out.println("🔐 JWT client_assertion:\n" + jwt);
//
//		app = ConfidentialClientApplication.builder(
//			clientId,
//			ClientCredentialFactory.createFromCertificate(certAndKey.privateKey, certAndKey.certificate))
//			.authority("https://login.microsoftonline.com/" + tenantId)
//			.build();
//	}

	/**
	 * Simple container for private key and certificate
	 */
	private static class CertificateAndKey {
		PrivateKey privateKey;
		X509Certificate certificate;
	}

	private CertificateAndKey loadCertificateAndKey(String path, String password) throws Exception {
		// Här antar vi att path är en mapp eller basnamn och att du har:
		// certifikat i PEM: path + ".crt" eller ".pem"
		// privat nyckel i PEM: path + ".key" eller ".pem"
		String certPath = certificatePath; // eller .crt
		String keyPath = certificateKey;

		CertificateAndKey cak = new CertificateAndKey();
		cak.privateKey = PemUtils.readPrivateKey(keyPath);
		cak.certificate = PemUtils.readCertificate(certPath);
		return cak;
	}

	public static class TokenResponse {
		public String accessToken;
		public long expiresAt; // millis epoch

		public boolean isExpired() {
			return System.currentTimeMillis() > expiresAt;
		}
	}

	/**
	 * Acquire OBO token using MSAL4J client certificate flow
	 */
	public TokenResponse acquireOboToken(String userAccessToken, String userId) throws Exception {
		System.out.println("🛂 Incoming user token (from Swagger): " + userAccessToken);
		OnBehalfOfParameters parameters = OnBehalfOfParameters.builder(
			Collections.singleton(scope),
			new UserAssertion(userAccessToken))
			.build();

		IAuthenticationResult result;
		try {
			result = app.acquireToken(parameters).get();
			// Här loggar du token för att se vad du får tillbaka:
			System.out.println("🔐 Access token:\n" + result.accessToken());
			System.out.println("🔐 ID token:\n" + result.idToken()); // Kan vara null i OBO-flödet
		} catch (ExecutionException ee) {
			System.err.println("OBO token request failed: " + ee.getCause().getMessage());
			ee.getCause().printStackTrace();
			throw ee;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.accessToken = result.accessToken();
		tokenResponse.expiresAt = System.currentTimeMillis() + (result.expiresOnDate().getTime() - System.currentTimeMillis()) - 60000; // 1 min early

		tokenCache.put(userId, tokenResponse);

		return tokenResponse;
	}

	public void acquireAccessToken(String code, String codeVerifier, String userId) throws Exception {
		System.out.println("före pca");
		PublicClientApplication pca = PublicClientApplication.builder(config.getClientId())
				.authority("https://login.microsoftonline.com/" + tenantId).build();

		System.out.println("före authcodeparam");
		AuthorizationCodeParameters parameters = AuthorizationCodeParameters.builder(code, new URI(config.getRedirectUri()))
				.scopes(Collections.singleton(config.getScopes()))
				.codeVerifier(codeVerifier).build();

		System.out.println("före authenticationresult");
//		IAuthenticationResult authenticationResult = pca.acquireToken(parameters).join();

		IAuthenticationResult authenticationResult;
		try {
			authenticationResult = app.acquireToken(parameters).get();
			// Här loggar du token för att se vad du får tillbaka:
			System.out.println("🔐 Access token:\n" + authenticationResult.accessToken());
			System.out.println("🔐 ID token:\n" + authenticationResult.idToken()); // Kan vara null i OBO-flödet
		} catch (ExecutionException ee) {
			System.err.println("AuthCode token request failed: " + ee.getCause().getMessage());
			ee.getCause().printStackTrace();
			throw ee;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		System.out.println("före tokencache");
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.accessToken = authenticationResult.accessToken();
		tokenResponse.expiresAt = System.currentTimeMillis() + (authenticationResult.expiresOnDate().getTime() - System.currentTimeMillis()) - 60000; // 1 min early

		tokenCache.put(userId, tokenResponse);
	}

	public ResponseEntity<String> exchangeAuthCodeForToken(String authCode) throws Exception {
//		PublicClientApplication app = PublicClientApplication.builder(config.getClientId())
//				.authority("https://login.microsoftonline.com/" + config.getTenantId())
//				.build();
//
//		ConfidentialClientApplication confApp = ConfidentialClientApplication.builder(config.getClientId())
//				.authority("https://login.microsoftonline.com/" + config.getTenantId())
//				.build();

//		if (Security.getProvider("BC") == null) {
//			Security.addProvider(new BouncyCastleProvider());
//		}
//
//		// Load private key and cert from PFX file (or PEM)
//		CertificateAndKey certAndKey = loadCertificateAndKey(certificatePath, certificateKey);
//		// 🔍 Logga thumbprint från certifikatet som faktiskt används
//		byte[] encoded = certAndKey.certificate.getEncoded();
//		MessageDigest md = MessageDigest.getInstance("SHA-1");
//		byte[] digest = md.digest(encoded);
//		String thumbprint = DatatypeConverter.printHexBinary(digest).toLowerCase();
//		System.out.println("🔍 Using cert thumbprint (calculated): " + thumbprint);
//
//		System.out.println("🔍 Genererar JWT manuellt...");

		System.out.println("före app");

		IClientCredential cred = ClientCredentialFactory.createFromSecret(config.getClientSecret());
		ConfidentialClientApplication app = ConfidentialClientApplication.builder(clientId, cred)
				.authority("https://login.microsoftonline.com/" + tenantId)
				.setTokenCacheAccessAspect(new DatabaseTokenCache(systemUser, jpaTokenCacheRepository))
				.build();

		System.out.println("efter app");

		AuthorizationCodeParameters parameters = AuthorizationCodeParameters
				.builder(authCode, new URI(config.getRedirectUri()))
				.scopes(Collections.singleton(config.getScopes()))
				.build();

		System.out.println("efter authcode parameters");

		IAuthenticationResult result = app.acquireToken(parameters).get();

		System.out.println("efter app.acquireToken");

		String userId = result.account().username();

		System.out.println("före cachedApp");

		ConfidentialClientApplication cachedApp = ConfidentialClientApplication.builder(clientId, cred)
				.authority("https://login.microsoftonline.com/" + tenantId)
				.setTokenCacheAccessAspect(new DatabaseTokenCache(systemUser, jpaTokenCacheRepository))
				.build();

		System.out.println("efter cachedApp");

		System.out.println("före silentParam");
		SilentParameters silentParameters = SilentParameters.builder(Collections.singleton(config.getScopes()))
				.account(result.account())
				.build();

		System.out.println("efter silentParam");

		cachedApp.acquireTokenSilently(silentParameters).get(); // triggar cache-sparning

		System.out.println("efter acquireTokenSilently");

		return ResponseEntity.ok("Token successfully saved");
	}

	/**
	 * Get cached access token or throw if missing/expired
	 */
//	public String getAccessTokenForUser(String userId) throws Exception {
//		TokenResponse tokenResponse = tokenCache.get(userId);
//		System.out.println(tokenResponse.accessToken);
//		if (tokenResponse == null || tokenResponse.isExpired()) {
//			throw new IllegalStateException("No valid cached token for user: " + userId);
//		}
//		return tokenResponse.accessToken;
//	}

	public String getAccessTokenForUser(String userId) throws Exception {
//		PublicClientApplication app = PublicClientApplication.builder(config.getClientId())
//				.authority("https://login.microsoftonline.com/" + config.getTenantId())
//				.setTokenCacheAccessAspect(new DatabaseTokenCache(userId, jpaTokenCacheRepository))
//				.build();
//
//		if (Security.getProvider("BC") == null) {
//			Security.addProvider(new BouncyCastleProvider());
//		}
//
//		// Load private key and cert from PFX file (or PEM)
//		CertificateAndKey certAndKey = loadCertificateAndKey(certificatePath, certificateKey);
//		// 🔍 Logga thumbprint från certifikatet som faktiskt används
//		byte[] encoded = certAndKey.certificate.getEncoded();
//		MessageDigest md = MessageDigest.getInstance("SHA-1");
//		byte[] digest = md.digest(encoded);
//		String thumbprint = DatatypeConverter.printHexBinary(digest).toLowerCase();
//		System.out.println("🔍 Using cert thumbprint (calculated): " + thumbprint);
//
//		System.out.println("🔍 Genererar JWT manuellt...");
		IClientCredential cred = ClientCredentialFactory.createFromSecret(config.getClientSecret());

		ConfidentialClientApplication confApp = ConfidentialClientApplication.builder(clientId, cred)
				.authority("https://login.microsoftonline.com/" + tenantId)
				.setTokenCacheAccessAspect(new DatabaseTokenCache(systemUser, jpaTokenCacheRepository))
				.build();

		Set<IAccount> accounts = confApp.getAccounts().join();
		Optional<IAccount> account = accounts.stream().filter(a -> a.username().equals(systemUser)).findFirst();

		IAccount user = account.orElse(null);

		SilentParameters silentParameters = SilentParameters.builder(Collections.singleton(config.getScopes())).account(user).build();
		IAuthenticationResult result = confApp.acquireTokenSilently(silentParameters).get();

		return result.accessToken();
	}
}
