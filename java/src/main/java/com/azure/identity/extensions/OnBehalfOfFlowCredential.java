package com.azure.identity.extensions;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.extensions.implementation.IdentityClient;
import com.azure.identity.extensions.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.util.LoggingUtil;
import com.microsoft.aad.msal4j.UserAssertion;
import reactor.core.publisher.Mono;

/**
 * An AAD credential that via the On Behalf Of flow for an AAD application.
 */
@Immutable
public class OnBehalfOfFlowCredential implements TokenCredential {
    private final IdentityClient identityClient;
    private final UserAssertion userAssertion;
    private final ClientLogger logger = new ClientLogger(StaticTokenCredential.class);

    /**
     * Creates a OnBehalfOfCredential
     *
     * @param tokenString The string of prefetched token
     * @param accessToken The prefetched token
     */
    OnBehalfOfFlowCredential(String tenantId, String clientId,
                             String clientSecret, String tokenString,
                             AccessToken accessToken) {
        identityClient = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
        userAssertion = new UserAssertion(tokenString != null ? tokenString : accessToken.getToken());
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return identityClient.authenticateWithOnBehalfOfCredentialCache(request, this.userAssertion)
            .onErrorResume(t -> Mono.empty())
            .switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithOnBehalfOfCredential(request, this.userAssertion)))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(logger, request))
            .doOnError(error -> LoggingUtil.logTokenError(logger, request, error));
    }
}
