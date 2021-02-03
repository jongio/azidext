package com.azure.identity.extensions;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * An AAD credential with a prefetched token for an AAD application.
 */
@Immutable
public class StaticTokenCredential implements TokenCredential {
    private final AccessToken accessToken;

    /**
     * Creates a StaticTokenCredential
     *
     * @param tokenString The string of prefetched token
     * @param accessToken The prefetched token
     */
    StaticTokenCredential(String tokenString, AccessToken accessToken) {
        this.accessToken = new AccessToken(
            tokenString != null ? tokenString : accessToken.getToken(),
            tokenString != null ? OffsetDateTime.MIN : accessToken.getExpiresAt()
        );
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.just(this.accessToken);
    }
}
