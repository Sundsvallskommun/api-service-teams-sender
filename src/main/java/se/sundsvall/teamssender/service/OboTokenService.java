package se.sundsvall.teamssender.service;

import com.microsoft.aad.msal4j.*;
import jakarta.annotation.PostConstruct;

import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;

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

	@PostConstruct
	public void init() throws Exception {

		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		// Load private key and cert from PFX file (or PEM)
		CertificateAndKey certAndKey = loadCertificateAndKey(certificatePath, certificateKey);
		// 🔍 Logga thumbprint från certifikatet som faktiskt används
		byte[] encoded = certAndKey.certificate.getEncoded();
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] digest = md.digest(encoded);
		String thumbprint = DatatypeConverter.printHexBinary(digest).toLowerCase();
		System.out.println("🔍 Using cert thumbprint (calculated): " + thumbprint);

		System.out.println("🔍 Genererar JWT manuellt...");

		String jwt = JwtDebugUtil.createClientAssertion(
				clientId,
				tenantId,
				certAndKey.certificate,
				(RSAPrivateKey) certAndKey.privateKey
		);

		System.out.println("🔐 JWT client_assertion:\n" + jwt);

		app = ConfidentialClientApplication.builder(
			clientId,
			ClientCredentialFactory.createFromCertificate(certAndKey.privateKey, certAndKey.certificate))
			.authority("https://login.microsoftonline.com/" + tenantId)
			.build();
	}

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
		OnBehalfOfParameters parameters = OnBehalfOfParameters.builder(
			Collections.singleton(scope),
			new UserAssertion(userAccessToken))
			.build();

		IAuthenticationResult result;
		try {
			result = app.acquireToken(parameters).get();
		} catch (ExecutionException ee) {
			throw new RuntimeException("OBO token request failed: " + ee.getCause().getMessage(), ee);
		}

		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.accessToken = result.accessToken();
		tokenResponse.expiresAt = System.currentTimeMillis() + (result.expiresOnDate().getTime() - System.currentTimeMillis()) - 60000; // 1 min early

		tokenCache.put(userId, tokenResponse);

		return tokenResponse;
	}

	/**
	 * Get cached access token or throw if missing/expired
	 */
	public String getAccessTokenForUser(String userId) throws Exception {
		TokenResponse tokenResponse = tokenCache.get(userId);
		if (tokenResponse == null || tokenResponse.isExpired()) {
			throw new IllegalStateException("No valid cached token for user: " + userId);
		}
		return tokenResponse.accessToken;
	}
}
