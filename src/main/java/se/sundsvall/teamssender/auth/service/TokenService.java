package se.sundsvall.teamssender.auth.service;

import com.azure.core.credential.TokenCredential;
import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.sundsvall.teamssender.auth.pojo.DatabaseTokenCache;
import se.sundsvall.teamssender.auth.pojo.StaticTokenCredential;
import se.sundsvall.teamssender.auth.repository.TokenCacheRepository;
import se.sundsvall.teamssender.configuration.AzureConfig;

@Service
public class TokenService {

	private final AzureConfig config;

	private final TokenCacheRepository tokenCacheRepository;

	private CertificateAndKey certificateAndKey;

	@Value("${teams.sender}")
	private String systemUser;

	@Autowired
	public TokenService(AzureConfig config, TokenCacheRepository tokenCacheRepository) {
		this.config = config;
		this.tokenCacheRepository = tokenCacheRepository;
	}

	@PostConstruct
	public void init() throws Exception {
		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		certificateAndKey = loadCertificateAndKey();
	}

	private static class CertificateAndKey {
		PrivateKey privateKey;
		X509Certificate certificate;
	}

	private CertificateAndKey loadCertificateAndKey() throws Exception {
		CertificateAndKey cak = new CertificateAndKey();
		cak.privateKey = PemUtils.readPrivateKey(config.getCertificateKey());
		cak.certificate = PemUtils.readCertificate(config.getCertificatePath());
		return cak;
	}

	public ResponseEntity<String> exchangeAuthCodeForToken(String authCode) throws Exception {
		ConfidentialClientApplication app = ConfidentialClientApplication.builder(config.getClientId(), ClientCredentialFactory.createFromCertificate(certificateAndKey.privateKey, certificateAndKey.certificate))
			.authority(config.getAuthorityUrl())
			.setTokenCacheAccessAspect(new DatabaseTokenCache(systemUser, tokenCacheRepository))
			.build();

		AuthorizationCodeParameters parameters = AuthorizationCodeParameters
			.builder(authCode, new URI(config.getRedirectUri()))
			.scopes(Collections.singleton(config.getScopes()))
			.build();

		IAuthenticationResult result = app.acquireToken(parameters).get();
		String userId = result.account().username(); // Kan nog tas bort

		ConfidentialClientApplication cachedApp = ConfidentialClientApplication.builder(config.getClientId(), ClientCredentialFactory.createFromCertificate(certificateAndKey.privateKey, certificateAndKey.certificate))
			.authority(config.getAuthorityUrl())
			.setTokenCacheAccessAspect(new DatabaseTokenCache(systemUser, tokenCacheRepository))
			.build();

		SilentParameters silentParameters = SilentParameters.builder(Collections.singleton(config.getScopes()))
			.account(result.account())
			.build();

		cachedApp.acquireTokenSilently(silentParameters).get();

		return ResponseEntity.ok("Token successfully saved");
	}

	public String getAccessTokenForUser(String sender) throws Exception {
		ConfidentialClientApplication confApp = ConfidentialClientApplication.builder(config.getClientId(), ClientCredentialFactory.createFromCertificate(certificateAndKey.privateKey, certificateAndKey.certificate))
			.authority(config.getAuthorityUrl())
			.setTokenCacheAccessAspect(new DatabaseTokenCache(sender, tokenCacheRepository))
			.build();

		Set<IAccount> accounts = confApp.getAccounts().join();
		Optional<IAccount> account = accounts.stream().filter(a -> a.username().equals(sender)).findFirst();

		IAccount user = account.orElse(null);

		SilentParameters silentParameters = SilentParameters.builder(Collections.singleton(config.getScopes())).account(user).build();
		IAuthenticationResult result = confApp.acquireTokenSilently(silentParameters).get();

		return result.accessToken();
	}

	public GraphServiceClient initializeGraphServiceClient() throws Exception {
		String accessToken = getAccessTokenForUser(systemUser);
		TokenCredential credential = new StaticTokenCredential(accessToken);

		return new GraphServiceClient(credential);
	}
}
