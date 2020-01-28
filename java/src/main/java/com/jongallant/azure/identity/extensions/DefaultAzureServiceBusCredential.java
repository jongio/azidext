package com.jongallant.azure.identity.extensions;

import com.microsoft.azure.servicebus.security.SecurityToken;
import com.microsoft.azure.servicebus.security.SecurityTokenType;
import com.microsoft.azure.servicebus.security.TokenProvider;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * A Service Bus {@link TokenProvider} that uses {@link DefaultAzureCredential} that is available in com
 * .azure:azure-identity module. This class provides a convenient mechanism to authenticate service bus using the latest
 * Azure Identity SDK.
 */
public class DefaultAzureServiceBusCredential extends TokenProvider {

  private static final String SERVICEBUS_SCOPE = "https://servicebus.azure.net/.default";
  private final DefaultAzureCredential defaultAzureCredential;

  /**
   * Creates an instance of DefaultAzureServiceBusCredential.
   */
  public DefaultAzureServiceBusCredential() {
    this(new DefaultAzureCredentialBuilder().build());
  }

  /**
   * Creates an instance of DefaultAzureServiceBusCredential using the provided {@link DefaultAzureCredential}.
   *
   * @param defaultAzureCredential The {@link DefaultAzureCredential} to use.
   */
  public DefaultAzureServiceBusCredential(DefaultAzureCredential defaultAzureCredential) {
    this.defaultAzureCredential = defaultAzureCredential;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience) {
    TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes(SERVICEBUS_SCOPE);
    return defaultAzureCredential
        .getToken(tokenRequestContext)
        .flatMap(accessToken -> Mono.just(new SecurityToken(SecurityTokenType.JWT, audience, accessToken.getToken(),
            Instant.now(), accessToken.getExpiresAt().toInstant()))).toFuture();
  }

}
