package com.jongallant.azure.identity.extensions;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides a simple extension to use {@link TokenCredential} from com.azure:azure-identity library to
 * use with legacy Azure SDKs that accept {@link ServiceClientCredentials} family of credentials for authentication.
 */
public class AzureIdentityCredentialAdapter extends AzureTokenCredentials {

  public static final String MANAGEMENT_SCOPE = "https://management.azure.com/.default";
  private final TokenCredential tokenCredential;
  private final Map<String, AccessToken> accessTokenCache = new ConcurrentHashMap<>();
  private final String[] scopes;

  /**
   * Creates an instance with {@link AzureEnvironment#AZURE} environment.
   *
   * @param tenantId The tenant id for the token credentials.
   */
  public AzureIdentityCredentialAdapter(String tenantId) {
    this(AzureEnvironment.AZURE, tenantId);
  }

  /**
   * Creates an instance with {@link AzureEnvironment#AZURE} environment and given {@link TokenCredential}
   * instance.
   *
   * @param tenantId The tenant id for the token credentials.
   * @param tokenCredential The {@link TokenCredential} instance to use.
   */
  public AzureIdentityCredentialAdapter(String tenantId, TokenCredential tokenCredential) {
    this(AzureEnvironment.AZURE, tenantId, tokenCredential, new String[]{MANAGEMENT_SCOPE});
  }

  /**
   * Creates an instance for the given environment.
   *
   * @param environment The {@link AzureEnvironment} for which the credentials will be created.
   * @param tenantId The tenant id for the token credentials.
   */
  public AzureIdentityCredentialAdapter(AzureEnvironment environment, String tenantId) {
    this(environment, tenantId, new DefaultAzureCredentialBuilder().build(), new String[]{MANAGEMENT_SCOPE});
  }

  /**
   * Creates an instance for the given environment, tenant id and {@link TokenCredential}.
   *
   * @param environment The {@link AzureEnvironment} for which the credentials will be created.
   * @param tenantId The tenant id for the token credentials.
   * @param tokenCredential The {@link TokenCredential} instance to use.
   * @param scopes The scopes for the credential.
   */
  public AzureIdentityCredentialAdapter(AzureEnvironment environment, String tenantId,
      TokenCredential tokenCredential, String[] scopes) {
    super(environment, tenantId);
    this.tokenCredential = tokenCredential;
    this.scopes = scopes;
  }

  @Override
  public String getToken(String endpoint) {
    if (!accessTokenCache.containsKey(endpoint) || accessTokenCache.get(endpoint).isExpired()) {
      accessTokenCache.put(endpoint,
          this.tokenCredential.getToken(new TokenRequestContext().addScopes(scopes)).block());
    }
    return accessTokenCache.get(endpoint).getToken();
  }
}
