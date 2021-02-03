package com.azure.identity.extensions;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.aad.msal4j.UserAssertion;
import net.minidev.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class OnBehalfOfFlowCredentialTests {

    private OffsetDateTime expiresAt = OffsetDateTime.MIN;
    private static Base64.Encoder encoder = Base64.getEncoder();
    private String token1 = createTokenString();
    private AccessToken accessToken = new AccessToken(token1, expiresAt);
    private String clientId = UUID.randomUUID().toString();
    private String tenantId = UUID.randomUUID().toString();
    private String clientSecret = "clientSecret";
    private TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");

    @Test
    public void testValidCacheStaticTokenString() throws Exception {

        // mock
        com.azure.identity.extensions.IdentityClient identityClient = PowerMockito.mock(com.azure.identity.extensions.IdentityClient.class);
        when(identityClient.authenticateWithOnBehalfOfCredentialCache(any(TokenRequestContext.class), any(UserAssertion.class)))
            .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        PowerMockito.whenNew(com.azure.identity.extensions.IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        OnBehalfOfFlowCredential credential =
            new OnBehalfOfFlowCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tokenString(token1).build();
        StepVerifier.create(credential.getToken(request1)).expectNext()
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken()))
            .verifyComplete();
    }

    @Test
    public void testValidStaticTokenString() throws Exception {

        // mock
        com.azure.identity.extensions.IdentityClient identityClient = PowerMockito.mock(com.azure.identity.extensions.IdentityClient.class);
        when(identityClient.authenticateWithOnBehalfOfCredentialCache(any(TokenRequestContext.class), any(UserAssertion.class)))
            .thenReturn(Mono.empty());
        when(identityClient.authenticateWithOnBehalfOfCredential(any(TokenRequestContext.class), any(UserAssertion.class)))
            .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        PowerMockito.whenNew(com.azure.identity.extensions.IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        OnBehalfOfFlowCredential credential =
            new OnBehalfOfFlowCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tokenString(token1).build();
        StepVerifier.create(credential.getToken(request1)).expectNext()
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken()))
            .verifyComplete();
    }

    @Test
    public void testValidCacheStaticAccessToken() throws Exception {
        // mock
        com.azure.identity.extensions.IdentityClient identityClient = PowerMockito.mock(com.azure.identity.extensions.IdentityClient.class);
        when(identityClient.authenticateWithOnBehalfOfCredentialCache(any(TokenRequestContext.class), any(UserAssertion.class)))
            .thenReturn(TestUtils.getMockAccessToken(accessToken.getToken(), accessToken.getExpiresAt()));
        PowerMockito.whenNew(com.azure.identity.extensions.IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        OnBehalfOfFlowCredential credential =
            new OnBehalfOfFlowCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .accessToken(accessToken).build();
        StepVerifier.create(credential.getToken(request1))
            .expectNextMatches(accessToken -> accessToken.getToken().equals(accessToken.getToken()))
            .verifyComplete();
    }

    @Test
    public void testValidStaticAccessToken() throws Exception {
        // mock
        com.azure.identity.extensions.IdentityClient identityClient = PowerMockito.mock(com.azure.identity.extensions.IdentityClient.class);
        when(identityClient.authenticateWithOnBehalfOfCredentialCache(any(TokenRequestContext.class), any(UserAssertion.class)))
            .thenReturn(Mono.empty());
        when(identityClient.authenticateWithOnBehalfOfCredential(any(TokenRequestContext.class), any(UserAssertion.class)))
            .thenReturn(TestUtils.getMockAccessToken(accessToken.getToken(), accessToken.getExpiresAt()));
        PowerMockito.whenNew(com.azure.identity.extensions.IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        OnBehalfOfFlowCredential credential =
            new OnBehalfOfFlowCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .accessToken(accessToken).build();
        StepVerifier.create(credential.getToken(request1))
            .expectNextMatches(accessToken -> accessToken.getToken().equals(accessToken.getToken()))
            .verifyComplete();
    }

    private String createTokenString() {
        JSONObject part1Object = new JSONObject();
        String[] firstPartArray = {"alg", "typ"};
        for (String firstPart : firstPartArray) {
            if ("alg".equals(firstPart)) {
                part1Object.put(firstPart, "HS256");
                continue;
            }
            part1Object.put(firstPart, firstPart);
        }
        return encoder.encodeToString(part1Object.toJSONString().getBytes(StandardCharsets.UTF_8)) + ".parts2.parts3";
    }

}
