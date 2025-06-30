package se.sundsvall.teamssender.service;

import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
public class AuthenticationService {

    public AuthorizationCodeCredential createCredential(HttpSession session) {
        String authorizationCode = (String) session.getAttribute("authorizationCode");
        if (authorizationCode == null) {
            throw new IllegalStateException("Authorization code not found in session.");
        }

        TokenCachePersistenceOptions persistenceOptions = new TokenCachePersistenceOptions()
                .setName("teams-sender-cache");

        return new AuthorizationCodeCredentialBuilder()
                .clientId("din-klient-id")
                .tenantId("ditt-tenant-id")
                .clientSecret("din-klient-hemlighet")
                .authorizationCode(authorizationCode)
                .redirectUrl("http://localhost:8080/redirect")
                .tokenCachePersistenceOptions(persistenceOptions)
                .build();
    }
}
