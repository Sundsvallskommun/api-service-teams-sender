package se.sundsvall.teamssender.service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class OboTokenService {

    private final ConcurrentHashMap<String, TokenCacheEntry> tokenCache = new ConcurrentHashMap<>();

    /**
     * Hämtar ett giltigt Graph-token för användaren, antingen från cache
     * eller genom att hämta ett nytt OBO-token.
     */
    public String getValidGraphToken(String userAccessToken, String senderEmail) {
        TokenCacheEntry cached = tokenCache.get(senderEmail);
        if (cached != null && Instant.now().isBefore(cached.expiresAt.minusSeconds(60))) {
            return cached.token;
        }
        return acquireOboToken(userAccessToken, senderEmail);
    }

    /**
     * Din vanliga OBO-funktionalitet här. Byt ut detta mot ditt faktiska anrop till
     * Azure AD för att byta ut user-token mot OBO-token (Graph-token).
     */
    public String acquireOboToken(String userAccessToken, String senderEmail) {
        // TODO: Lägg in ditt befintliga anrop mot Azure AD här
        String newToken = "..."; // token från Azure AD
        Instant expiresAt = Instant.now().plusSeconds(3600); // ex: 1h giltig
        tokenCache.put(senderEmail, newToken, expiresAt);
        return newToken;
    }

    private static class TokenCacheEntry {
        private final String token;
        private final Instant expiresAt;

        public TokenCacheEntry(String token, Instant expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }
    }
}