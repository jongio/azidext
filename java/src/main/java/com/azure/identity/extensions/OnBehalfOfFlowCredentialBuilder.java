package com.azure.identity.extensions;

import com.azure.core.credential.AccessToken;
import com.azure.identity.extensions.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link OnBehalfOfFlowCredential}.
 *
 * @see OnBehalfOfFlowCredential
 */
public class OnBehalfOfFlowCredentialBuilder extends CredentialBuilderBase<OnBehalfOfFlowCredentialBuilder> {

    private String tenantId;

    private String clientId;

    private String clientSecret;

    private String tokenString;

    private AccessToken accessToken;

    public OnBehalfOfFlowCredentialBuilder tenantId(String tenantId){
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the prefetched token string for the token.
     *
     * @param clientId The id of the client
     *
     * @return The updated OnBehalfOfCredentialBuilder object.
     */
    public OnBehalfOfFlowCredentialBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets the prefetched token string for the token.
     *
     * @param clientSecret The secret of the client
     *
     * @return The updated OnBehalfOfCredentialBuilder object.
     */
    public OnBehalfOfFlowCredentialBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }


    /**
     * Sets the On behalf of Flow token string for the token.
     *
     * @param tokenString The On behalf of Flow token string of prefetched token
     *
     * @return The updated OnBehalfOfCredentialBuilder object.
     */
    public OnBehalfOfFlowCredentialBuilder tokenString(String tokenString) {
        this.tokenString = tokenString;
        return this;
    }

    /**
     * Sets the On behalf of Flow token for the token.
     *
     * @param accessToken The On behalf of Flow token of prefetched token
     *
     * @return The updated OnBehalfOfCredentialBuilder object.
     */
    public OnBehalfOfFlowCredentialBuilder accessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public OnBehalfOfFlowCredential build() {
        com.azure.identity.implementation.util.ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
            put("tenantId", tenantId);
            put("clientId", clientId);
            put("clientSecret", clientSecret);
        }});
        ValidationUtil.validateAllEmpty(getClass().getSimpleName(), new HashMap<String, Object>() {{
            put("tokenString", tokenString);
            put("accessToken", accessToken);
        }});
        return new OnBehalfOfFlowCredential(tenantId, clientId, clientSecret, tokenString, accessToken);
    }

}
