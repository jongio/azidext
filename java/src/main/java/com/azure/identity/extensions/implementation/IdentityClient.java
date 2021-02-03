// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.MsalToken;
import com.azure.identity.implementation.SynchronizedAccessor;
import com.azure.identity.implementation.util.CertificateUtil;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import com.microsoft.aad.msal4jextensions.PersistenceSettings;
import com.microsoft.aad.msal4jextensions.PersistenceTokenCacheAccessAspect;
import com.microsoft.aad.msal4jextensions.persistence.linux.KeyRingAccessException;
import com.sun.jna.Platform;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The identity client that contains APIs to retrieve access tokens
 * from various configurations.
 * Added two authenticate methods in this class.
 */
public class IdentityClient {
    private static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    private static final String DEFAULT_PUBLIC_CACHE_FILE_NAME = "msal.cache";
    private static final String DEFAULT_CONFIDENTIAL_CACHE_FILE_NAME = "msal.confidential.cache";
    private static final Path DEFAULT_CACHE_FILE_PATH = Platform.isWindows()
        ? Paths.get(System.getProperty("user.home"), "AppData", "Local", ".IdentityService")
        : Paths.get(System.getProperty("user.home"), ".IdentityService");
    private static final String DEFAULT_KEYCHAIN_SERVICE = "Microsoft.Developer.IdentityService";
    private static final String DEFAULT_PUBLIC_KEYCHAIN_ACCOUNT = "MSALCache";
    private static final String DEFAULT_CONFIDENTIAL_KEYCHAIN_ACCOUNT = "MSALConfidentialCache";
    private static final String DEFAULT_KEYRING_NAME = "default";
    private static final String DEFAULT_KEYRING_SCHEMA = "msal.cache";
    private static final String DEFAULT_PUBLIC_KEYRING_ITEM_NAME = DEFAULT_PUBLIC_KEYCHAIN_ACCOUNT;
    private static final String DEFAULT_CONFIDENTIAL_KEYRING_ITEM_NAME = DEFAULT_CONFIDENTIAL_KEYCHAIN_ACCOUNT;
    private static final String DEFAULT_KEYRING_ATTR_NAME = "MsalClientID";
    private static final String DEFAULT_KEYRING_ATTR_VALUE = "Microsoft.Developer.IdentityService";
    private final ClientLogger logger = new ClientLogger(IdentityClient.class);

    private final IdentityClientOptions options;
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final InputStream certificate;
    private final String certificatePath;
    private final String certificatePassword;
    private HttpPipelineAdapter httpPipelineAdapter;
    private final SynchronizedAccessor<PublicClientApplication> publicClientApplicationAccessor;
    private final SynchronizedAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessor;

    /**
     * Creates an IdentityClient with the given options.
     *
     * @param tenantId the tenant ID of the application.
     * @param clientId the client ID of the application.
     * @param clientSecret the client secret of the application.
     * @param certificatePath the path to the PKCS12 or PEM certificate of the application.
     * @param certificate the PKCS12 or PEM certificate of the application.
     * @param certificatePassword the password protecting the PFX certificate.
     * @param isSharedTokenCacheCredential Indicate whether the credential is
     * {@link com.azure.identity.SharedTokenCacheCredential} or not.
     * @param options the options configuring the client.
     */
    IdentityClient(String tenantId, String clientId, String clientSecret, String certificatePath,
                   InputStream certificate, String certificatePassword, boolean isSharedTokenCacheCredential,
                   IdentityClientOptions options) {
        if (tenantId == null) {
            tenantId = "organizations";
        }
        if (options == null) {
            options = new IdentityClientOptions();
        }
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.certificatePath = certificatePath;
        this.certificate = certificate;
        this.certificatePassword = certificatePassword;
        this.options = options;

        this.publicClientApplicationAccessor = new SynchronizedAccessor<PublicClientApplication>(() ->
            getPublicClientApplication(isSharedTokenCacheCredential));

        this.confidentialClientApplicationAccessor = new SynchronizedAccessor<ConfidentialClientApplication>(() ->
            getConfidentialClientApplication());
    }

    private ConfidentialClientApplication getConfidentialClientApplication() {
        if (clientId == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
        }
        String authorityUrl = options.getAuthorityHost().replaceAll("/+$", "") + "/" + tenantId;
        IClientCredential credential;
        if (clientSecret != null) {
            credential = ClientCredentialFactory.createFromSecret(clientSecret);
        } else if (certificate != null || certificatePath != null) {
            try {
                if (certificatePassword == null) {
                    byte[] pemCertificateBytes = getCertificateBytes();

                    List<X509Certificate> x509CertificateList =  CertificateUtil.publicKeyFromPem(pemCertificateBytes);
                    PrivateKey privateKey = CertificateUtil.privateKeyFromPem(pemCertificateBytes);
                    if (x509CertificateList.size() == 1) {
                        credential = ClientCredentialFactory.createFromCertificate(
                            privateKey, x509CertificateList.get(0));
                    } else {
                        credential = ClientCredentialFactory.createFromCertificateChain(
                            privateKey, x509CertificateList);
                    }
                } else {
                    InputStream pfxCertificateStream = getCertificateInputStream();
                    credential = ClientCredentialFactory.createFromCertificate(
                            pfxCertificateStream, certificatePassword);
                }
            } catch (IOException | GeneralSecurityException e) {
                throw logger.logExceptionAsError(new RuntimeException(
                    "Failed to parse the certificate for the credential: " + e.getMessage(), e));
            }
        } else {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Must provide client secret or client certificate path"));
        }

        ConfidentialClientApplication.Builder applicationBuilder =
            ConfidentialClientApplication.builder(clientId, credential);
        try {
            applicationBuilder = applicationBuilder.authority(authorityUrl);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsWarning(new IllegalStateException(e));
        }

        applicationBuilder.sendX5c(options.isIncludeX5c());

        initializeHttpPipelineAdapter();
        if (httpPipelineAdapter != null) {
            applicationBuilder.httpClient(httpPipelineAdapter);
        } else {
            applicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.getProxyOptions()));
        }

        if (options.getExecutorService() != null) {
            applicationBuilder.executorService(options.getExecutorService());
        }
        if (options.isSharedTokenCacheEnabled()) {
            try {
                PersistenceSettings.Builder persistenceSettingsBuilder = PersistenceSettings.builder(
                    DEFAULT_CONFIDENTIAL_CACHE_FILE_NAME, DEFAULT_CACHE_FILE_PATH);
                if (Platform.isMac()) {
                    persistenceSettingsBuilder.setMacKeychain(
                        DEFAULT_KEYCHAIN_SERVICE, DEFAULT_CONFIDENTIAL_KEYCHAIN_ACCOUNT);
                }
                if (Platform.isLinux()) {
                    try {
                        persistenceSettingsBuilder
                            .setLinuxKeyring(DEFAULT_KEYRING_NAME, DEFAULT_KEYRING_SCHEMA,
                                DEFAULT_CONFIDENTIAL_KEYRING_ITEM_NAME, DEFAULT_KEYRING_ATTR_NAME,
                                DEFAULT_KEYRING_ATTR_VALUE, null, null);
                        applicationBuilder.setTokenCacheAccessAspect(
                            new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                    } catch (KeyRingAccessException e) {
                        if (!options.getAllowUnencryptedCache()) {
                            throw logger.logExceptionAsError(e);
                        }
                        persistenceSettingsBuilder.setLinuxUseUnprotectedFileAsCacheStorage(true);
                        applicationBuilder.setTokenCacheAccessAspect(
                            new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                    }
                }
            } catch (Throwable t) {
                throw logger.logExceptionAsError(new ClientAuthenticationException(
                    "Shared token cache is unavailable in this environment.", null, t));
            }
        }
        return applicationBuilder.build();
    }

    private PublicClientApplication getPublicClientApplication(boolean sharedTokenCacheCredential) {
        if (clientId == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
        }
        String authorityUrl = options.getAuthorityHost().replaceAll("/+$", "") + "/" + tenantId;
        PublicClientApplication.Builder publicClientApplicationBuilder = PublicClientApplication.builder(clientId);
        try {
            publicClientApplicationBuilder = publicClientApplicationBuilder.authority(authorityUrl);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsWarning(new IllegalStateException(e));
        }

        initializeHttpPipelineAdapter();
        if (httpPipelineAdapter != null) {
            publicClientApplicationBuilder.httpClient(httpPipelineAdapter);
        } else {
            publicClientApplicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.getProxyOptions()));
        }

        if (options.getExecutorService() != null) {
            publicClientApplicationBuilder.executorService(options.getExecutorService());
        }
        if (options.isSharedTokenCacheEnabled()) {
            try {
                PersistenceSettings.Builder persistenceSettingsBuilder = PersistenceSettings.builder(
                        DEFAULT_PUBLIC_CACHE_FILE_NAME, DEFAULT_CACHE_FILE_PATH);
                if (Platform.isWindows()) {
                    publicClientApplicationBuilder.setTokenCacheAccessAspect(
                        new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                } else if (Platform.isMac()) {
                    persistenceSettingsBuilder.setMacKeychain(
                        DEFAULT_KEYCHAIN_SERVICE, DEFAULT_PUBLIC_KEYCHAIN_ACCOUNT);
                    publicClientApplicationBuilder.setTokenCacheAccessAspect(
                        new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                } else if (Platform.isLinux()) {
                    try {
                        persistenceSettingsBuilder
                            .setLinuxKeyring(DEFAULT_KEYRING_NAME, DEFAULT_KEYRING_SCHEMA,
                                DEFAULT_PUBLIC_KEYRING_ITEM_NAME, DEFAULT_KEYRING_ATTR_NAME, DEFAULT_KEYRING_ATTR_VALUE,
                                null, null);
                        publicClientApplicationBuilder.setTokenCacheAccessAspect(
                            new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                    } catch (KeyRingAccessException e) {
                        if (!options.getAllowUnencryptedCache()) {
                            throw logger.logExceptionAsError(e);
                        }
                        persistenceSettingsBuilder.setLinuxUseUnprotectedFileAsCacheStorage(true);
                        publicClientApplicationBuilder.setTokenCacheAccessAspect(
                            new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                    }
                }
            } catch (Throwable t) {
                String message = "Shared token cache is unavailable in this environment.";
                if (sharedTokenCacheCredential) {
                    throw logger.logExceptionAsError(new CredentialUnavailableException(message, t));
                } else {
                    throw logger.logExceptionAsError(new ClientAuthenticationException(message, null, t));
                }
            }
        }
        return publicClientApplicationBuilder.build();
    }

    public Mono<AccessToken> authenticateWithOnBehalfOfCredentialCache(TokenRequestContext request, UserAssertion userAssertion) {
        return confidentialClientApplicationAccessor.getValue()
            .flatMap(confidentialClient -> Mono.fromFuture(() -> {
                OnBehalfOfParameters.OnBehalfOfParametersBuilder parametersBuilder = OnBehalfOfParameters.builder(
                    new HashSet<>(request.getScopes()), userAssertion);
                return confidentialClient.acquireToken(parametersBuilder.build());
            }).map(MsalToken::new)
                .filter(t -> OffsetDateTime.now().isBefore(t.getExpiresAt().minus(REFRESH_OFFSET))));
    }

    public Mono<AccessToken> authenticateWithOnBehalfOfCredential(TokenRequestContext request, UserAssertion userAssertion) {
        return confidentialClientApplicationAccessor.getValue()
            .flatMap(confidentialClient -> Mono.fromFuture(() -> {
                OnBehalfOfParameters.OnBehalfOfParametersBuilder parametersBuilder = OnBehalfOfParameters.builder(
                    new HashSet<>(request.getScopes()), userAssertion);
                return confidentialClient.acquireToken(parametersBuilder.build());
                })
                .map(MsalToken::new)
            );
    }

    private HttpPipeline setupPipeline(HttpClient httpClient) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        HttpLogOptions httpLogOptions = new HttpLogOptions();
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RetryPolicy());
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        return new HttpPipelineBuilder().httpClient(httpClient)
                   .policies(policies.toArray(new HttpPipelinePolicy[0])).build();
    }

    private static Proxy proxyOptionsToJavaNetProxy(ProxyOptions options) {
        switch (options.getType()) {
            case SOCKS4:
            case SOCKS5:
                return new Proxy(Type.SOCKS, options.getAddress());
            case HTTP:
            default:
                return new Proxy(Type.HTTP, options.getAddress());
        }
    }

    private CompletableFuture<IAuthenticationResult> getFailedCompletableFuture(Exception e) {
        CompletableFuture<IAuthenticationResult> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(e);
        return completableFuture;
    }

    private void initializeHttpPipelineAdapter() {
        // If user supplies the pipeline, then it should override all other properties
        // as they should directly be set on the pipeline.
        HttpPipeline httpPipeline = options.getHttpPipeline();
        if (httpPipeline != null) {
            httpPipelineAdapter = new HttpPipelineAdapter(httpPipeline);
        } else {
            // If http client is set on the credential, then it should override the proxy options if any configured.
            HttpClient httpClient = options.getHttpClient();
            if (httpClient != null) {
                httpPipelineAdapter = new HttpPipelineAdapter(setupPipeline(httpClient));
            } else if (options.getProxyOptions() == null) {
                //Http Client is null, proxy options are not set, use the default client and build the pipeline.
                httpPipelineAdapter = new HttpPipelineAdapter(setupPipeline(HttpClient.createDefault()));
            }
        }
    }

    private byte[] getCertificateBytes() throws IOException {
        if (certificatePath != null) {
            return Files.readAllBytes(Paths.get(certificatePath));
        } else if (certificate != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = certificate.read(buffer, 0, buffer.length);
            while (read != -1) {
                outputStream.write(buffer, 0, read);
                read = certificate.read(buffer, 0, buffer.length);
            }
            return outputStream.toByteArray();
        } else {
            return new byte[0];
        }
    }

    private InputStream getCertificateInputStream() throws IOException {
        if (certificatePath != null) {
            return new FileInputStream(certificatePath);
        } else if (certificate != null) {
            return certificate;
        } else {
            return null;
        }
    }
}
