package se.sundsvall.teamssender.auth.pojo;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public class StaticTokenCredential implements TokenCredential {

    private final String accessToken;

    public StaticTokenCredential(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.just(new AccessToken(accessToken, OffsetDateTime.now().plusHours(1)));
    }
}
