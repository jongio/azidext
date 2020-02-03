package com.jongallant.azure.identity.extensions;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides a simple extension to use {@link DefaultAzureCredential} from com.azure:azure-identity library to
 * use with legacy Azure SDKs that accept {@link ServiceClientCredentials} family of credentials for authentication.
 */
public class DefaultAzureLegacyCredential extends AzureTokenCredentials {

  public static final String MANAGEMENT_SCOPE = "https://management.azure.com/.default";
  private final DefaultAzureCredential defaultAzureCredential;
  private final Map<String, AccessToken> accessTokenCache = new ConcurrentHashMap<>();
  private final String[] scopes;

  /**
   * Creates an instance with {@link AzureEnvironment#AZURE} environment.
   *
   * @param tenantId The tenant id for the token credentials.
   */
  public DefaultAzureLegacyCredential(String tenantId) {
    this(AzureEnvironment.AZURE, tenantId);
  }

  /**
   * Creates an instance with {@link AzureEnvironment#AZURE} environment and given {@link DefaultAzureCredential}
   * instance.
   *
   * @param tenantId The tenant id for the token credentials.
   * @param defaultAzureCredential The {@link DefaultAzureCredential} instance to use.
   */
  public DefaultAzureLegacyCredential(String tenantId, DefaultAzureCredential defaultAzureCredential) {
    this(AzureEnvironment.AZURE, tenantId, defaultAzureCredential, new String[]{MANAGEMENT_SCOPE});
  }

  /**
   * Creates an instance for the given environment.
   *
   * @param environment The {@link AzureEnvironment} for which the credentials will be created.
   * @param tenantId The tenant id for the token credentials.
   */
  public DefaultAzureLegacyCredential(AzureEnvironment environment, String tenantId) {
    this(environment, tenantId, new DefaultAzureCredentialBuilder().build(), new String[]{MANAGEMENT_SCOPE});
  }

  /**
   * Creates an instance for the given environment, tenant id and {@link DefaultAzureCredential}.
   *
   * @param environment The {@link AzureEnvironment} for which the credentials will be created.
   * @param tenantId The tenant id for the token credentials.
   * @param defaultAzureCredential The {@link DefaultAzureCredential} instance to use.
   * @param scopes The scopes for the credential.
   */
  public DefaultAzureLegacyCredential(AzureEnvironment environment, String tenantId,
      DefaultAzureCredential defaultAzureCredential, String[] scopes) {
    super(environment, tenantId);
    this.defaultAzureCredential = defaultAzureCredential;
    this.scopes = scopes;
  }

  @Override
  public String getToken(String endpoint) {
    if (!accessTokenCache.containsKey(endpoint) || accessTokenCache.get(endpoint).isExpired()) {
      accessTokenCache.put(endpoint,
          this.defaultAzureCredential.getToken(new TokenRequestContext().addScopes(scopes)).block());
    }
    return accessTokenCache.get(endpoint).getToken();
  }
}
