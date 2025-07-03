package se.sundsvall.teamssender.service;

import com.microsoft.aad.msal4j.*;
import jakarta.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OboTokenService {

	@Value("${azure.ad.tenant-id}")
	private String tenantId;

	@Value("${azure.ad.client-id}")
	private String clientId;

	@Value("${azure.ad.certificate-path}")
	private String certificatePath; // path to .pfx or .pem

	@Value("${azure.ad.certificate-password}")
	private String certificatePassword; // if .pfx is password protected

	private ConfidentialClientApplication app;

	private final String scope = "https://graph.microsoft.com/.default";

	private final Map<String, TokenResponse> tokenCache = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() throws Exception {
		// Load private key and cert from PFX file (or PEM)
		CertificateAndKey certAndKey = loadCertificateAndKey(certificatePath, certificatePassword);

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
		// Implement loading .pfx or .pem here,
		// For .pfx you can use KeyStore and CertificateFactory to extract PrivateKey and X509Certificate
		// Example for PFX:
		/*
		 * KeyStore keystore = KeyStore.getInstance("PKCS12");
		 * try (FileInputStream fis = new FileInputStream(path)) {
		 * keystore.load(fis, password.toCharArray());
		 * }
		 * String alias = keystore.aliases().nextElement();
		 * PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, password.toCharArray());
		 * X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);
		 * 
		 * CertificateAndKey cak = new CertificateAndKey();
		 * cak.privateKey = privateKey;
		 * cak.certificate = cert;
		 * return cak;
		 */
		throw new UnsupportedOperationException("Please implement certificate loading");
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
