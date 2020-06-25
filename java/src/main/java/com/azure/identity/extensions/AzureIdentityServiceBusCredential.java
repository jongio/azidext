package com.azure.identity.extensions;

import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.servicebus.security.SecurityToken;
import com.microsoft.azure.servicebus.security.SecurityTokenType;
import com.microsoft.azure.servicebus.security.TokenProvider;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * A Service Bus {@link TokenProvider} that uses {@link TokenCredential} that is available in com
 * .azure:azure-identity module. This class provides a convenient mechanism to authenticate service bus using the latest
 * Azure Identity SDK.
 */
public class AzureIdentityServiceBusCredential extends TokenProvider {

  private static final String SERVICEBUS_SCOPE = "https://servicebus.azure.net/.default";
  private final TokenCredential tokenCredential;
  private final Map<String, SecurityToken> tokenCache = new ConcurrentHashMap<>();

  /**
   * Creates an instance of DefaultAzureServiceBusCredential.
   */
  public AzureIdentityServiceBusCredential() {
    this(new DefaultAzureCredentialBuilder().build());
  }

  /**
   * Creates an instance of DefaultAzureServiceBusCredential using the provided {@link TokenCredential}.
   *
   * @param tokenCredential The {@link TokenCredential} to use.
   */
  public AzureIdentityServiceBusCredential(TokenCredential tokenCredential) {
    this.tokenCredential = tokenCredential;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience) {
    TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes(SERVICEBUS_SCOPE);
    if (tokenCache.containsKey(audience) && tokenCache.get(audience).getValidUntil().isAfter(Instant.now())) {
      return Mono.just(tokenCache.get(audience)).toFuture();
    }

    return tokenCredential
        .getToken(tokenRequestContext)
        .flatMap(accessToken -> {
          SecurityToken securityToken = new SecurityToken(SecurityTokenType.JWT, audience, accessToken.getToken(),
              Instant.now(), accessToken.getExpiresAt().toInstant());
          tokenCache.put(audience, securityToken);
          return Mono.just(securityToken);
        }).toFuture();
  }

}
