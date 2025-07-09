package se.sundsvall.teamssender.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.UUID;

public class JwtDebugUtil {

	public static String createClientAssertion(String clientId, String tenantId, X509Certificate cert, RSAPrivateKey privateKey) throws Exception {
		String thumbprint = calculateX5t(cert);
		String headerJson = "{\"alg\":\"RS256\",\"typ\":\"JWT\",\"x5t\":\"" + thumbprint + "\"}";
		String audience = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";

		long now = System.currentTimeMillis() / 1000;
		long exp = now + 600;

		String payloadJson = "{"
			+ "\"aud\":\"" + audience + "\","
			+ "\"exp\":" + exp + ","
			+ "\"iss\":\"" + clientId + "\","
			+ "\"jti\":\"" + UUID.randomUUID() + "\","
			+ "\"nbf\":" + now + ","
			+ "\"sub\":\"" + clientId + "\""
			+ "}";

		String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
		String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

		String unsignedJwt = headerB64 + "." + payloadB64;

		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(unsignedJwt.getBytes(StandardCharsets.UTF_8));
		byte[] sig = signature.sign();

		String signedJwt = unsignedJwt + "." + base64UrlEncode(sig);

		return signedJwt;
	}

	private static String base64UrlEncode(byte[] input) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
	}

	private static String calculateX5t(X509Certificate cert) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] digest = md.digest(cert.getEncoded());
		return base64UrlEncode(digest);
	}
}
