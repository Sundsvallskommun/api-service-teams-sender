package se.sundsvall.teamssender.service;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public class SimpleAuthProvider implements TokenCredential {

    private final String accessToken;

    public SimpleAuthProvider(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.just(new AccessToken(accessToken, OffsetDateTime.now().plusHours(1)));
    }
}
