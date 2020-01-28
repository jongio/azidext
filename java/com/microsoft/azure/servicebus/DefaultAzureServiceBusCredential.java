package com.microsoft.azure.servicebus;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class DefaultAzureServiceBusCredential extends TokenProvider {

    private final DefaultAzureCredential defaultAzureCredential;

    public DefaultAzureServiceBusCredential() {
        this(new DefaultAzureCredentialBuilder().build());
    }

    public DefaultAzureServiceBusCredential(DefaultAzureCredential defaultAzureCredential) {
        this.defaultAzureCredential = defaultAzureCredential;
    }

    @Override
    public CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience) {

        String scope = "https://servicebus.azure.net/.default";
        TokenRequestContext tokenRequestContext = new TokenRequestContext()
            .addScopes(scope);

        return defaultAzureCredential
            .getToken(tokenRequestContext)
            .flatMap(accessToken -> Mono.just(new SecurityToken(SecurityTokenType.JWT, audience, accessToken.getToken(),
                Instant.now(), accessToken.getExpiresAt().toInstant()))).toFuture();
    }

}
