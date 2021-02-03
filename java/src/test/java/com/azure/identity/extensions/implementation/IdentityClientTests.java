// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.extensions.util.TestUtils;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.CertificateUtil;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "javax.net.ssl.*", "org.xml.*"})
@PrepareForTest({CertificateUtil.class, ClientCredentialFactory.class, Runtime.class, URL.class, ConfidentialClientApplication.class, ConfidentialClientApplication.Builder.class, PublicClientApplication.class, PublicClientApplication.Builder.class, IdentityClient.class})
public class IdentityClientTests {

    private static final String TENANT_ID = "contoso.com";
    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private final ClientLogger logger = new ClientLogger(IdentityClientTests.class);

    @Test
    public void testValidOnBehalfOfFLowCredential() throws Exception {
        // setup
        String accessToken = "token";
        UserAssertion userAssertion = new UserAssertion(accessToken);
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForOnBehalfOfFlowCredential(accessToken, request, expiresOn);

        // test
        IdentityClientOptions options = new IdentityClientOptions();
        IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret("secret").identityClientOptions(options).build();
        AccessToken token = client.authenticateWithOnBehalfOfCredential(request, userAssertion).block();
        Assert.assertEquals(accessToken, token.getToken());
        Assert.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
    }

    private void mockForOnBehalfOfFlowCredential(String token, TokenRequestContext request, OffsetDateTime expiresAt) throws Exception {
        ConfidentialClientApplication application = PowerMockito.mock(ConfidentialClientApplication.class);
        when(application.acquireToken(any(OnBehalfOfParameters.class))).thenAnswer(invocation -> {
            OnBehalfOfParameters argument = (OnBehalfOfParameters) invocation.getArguments()[0];
            if (argument.scopes().size() == 1 && request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                return TestUtils.getMockAuthenticationResult(token, expiresAt);
            } else {
                return CompletableFuture.runAsync(() -> {
                    throw new MsalServiceException("Invalid request", "InvalidScopes");
                });
            }
        });
        ConfidentialClientApplication.Builder builder = PowerMockito.mock(ConfidentialClientApplication.Builder.class);
        when(builder.build()).thenReturn(application);
        when(builder.authority(any())).thenReturn(builder);
        when(builder.httpClient(any())).thenReturn(builder);
        whenNew(ConfidentialClientApplication.Builder.class).withAnyArguments().thenAnswer(invocation -> {
            String cid = (String) invocation.getArguments()[0];
            IClientCredential keyCredential = (IClientCredential) invocation.getArguments()[1];
            if (!CLIENT_ID.equals(cid)) {
                throw new MsalServiceException("Invalid CLIENT_ID", "InvalidClientId");
            }
            if (keyCredential == null) {
                throw new MsalServiceException("Invalid clientCertificate", "InvalidClientCertificate");
            }
            return builder;
        });
    }

}
