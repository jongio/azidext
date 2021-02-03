package com.azure.identity.extensions;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import org.junit.Assert;
import org.junit.Test;

import java.time.OffsetDateTime;

public class StaticTokenCredentialTests {

    @Test
    public void testValidStaticTokenString() {
        String token = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.MIN;
        // test
        StaticTokenCredential credential =
            new StaticTokenCredentialBuilder().tokenString(token).build();
        AccessToken accessToken = credential.getToken(request).block();
        Assert.assertEquals(token, accessToken.getToken());
        Assert.assertEquals(expiresAt, accessToken.getExpiresAt());
    }

    @Test
    public void testValidStaticAccessToken() {
        AccessToken token = new AccessToken("token1", OffsetDateTime.MIN);
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        // test
        StaticTokenCredential credential =
            new StaticTokenCredentialBuilder().accessToken(token).build();
        AccessToken accessToken = credential.getToken(request).block();
        Assert.assertEquals(token.getToken(), accessToken.getToken());
        Assert.assertEquals(token.getExpiresAt(), accessToken.getExpiresAt());
    }

}
