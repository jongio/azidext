// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

using Azure.Core;
using Azure.Core.Pipeline;
using Azure.Identity.Extensions.implementation;
using Microsoft.Identity.Client;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Identity.Extensions
{
    public class OnBehalfOfFlowCredential : TokenCredential
    {
        private readonly MsalConfidentialClient _client;
        private readonly CredentialPipeline _pipeline;
        private readonly UserAssertion _userAssertion;

        /// <summary>
        /// Gets the Azure Active Directory tenant (directory) Id of the service principal
        /// </summary>
        internal string TenantId { get; }

        /// <summary>
        /// Gets the client (application) ID of the service principal
        /// </summary>
        internal string ClientId { get; }

        /// <summary>
        /// Gets the client secret that was generated for the App Registration used to authenticate the client.
        /// </summary>
        internal string ClientSecret { get; }

        /// <summary>
        /// Protected constructor for mocking.
        /// </summary>
        protected OnBehalfOfFlowCredential()
        {
        }

        /// <summary>
        /// Creates an instance of the OnBehalfOfFlowCredential.
        /// </summary>
        /// <param name="tenantId">The Azure Active Directory tenant (directory) Id of the service principal.</param>
        /// <param name="clientId">The client (application) ID of the web api</param>
        /// <param name="clientSecret">A client secret that was generated for the App Registration used to authenticate the client.</param>
        /// <param name="tokenString">tokenString The string of prefetched token.</param>
        public OnBehalfOfFlowCredential(string tenantId, string clientId, string clientSecret, string tokenString)
            : this(tenantId, clientId, clientSecret, tokenString, default, null, null, null)
        {
        }

        /// <summary>
        /// Creates an instance of the OnBehalfOfFlowCredential.
        /// </summary>
        /// <param name="tenantId">The Azure Active Directory tenant (directory) Id of the service principal.</param>
        /// <param name="clientId">The client (application) ID of the web api</param>
        /// <param name="clientSecret">A client secret that was generated for the App Registration used to authenticate the client.</param>
        /// <param name="accessToken">accessToken The prefetched token.</param>
        public OnBehalfOfFlowCredential(string tenantId, string clientId, string clientSecret, AccessToken accessToken)
           : this(tenantId, clientId, clientSecret, null, accessToken, null, null, null)
        {
        }

        /// <summary>
        /// Creates an instance of the ClientSecretCredential with the details needed to authenticate against Azure Active Directory with a client secret.
        /// </summary>
        /// <param name="tenantId">The Azure Active Directory tenant (directory) Id of the service principal.</param>
        /// <param name="clientId">The client (application) ID of the web api</param>
        /// <param name="clientSecret">A client secret that was generated for the App Registration used to authenticate the client.</param>
        /// <param name="tokenString">tokenString The string of prefetched token.</param>
        /// <param name="options">Options that allow to configure the management of the requests sent to the Azure Active Directory service.</param>
        public OnBehalfOfFlowCredential(string tenantId,
            string clientId, string clientSecret, string tokenString, TokenCredentialOptions options)
            : this(tenantId, clientId, clientSecret, tokenString, default, options, null, null)
        {
        }

        /// <summary>
        /// Creates an instance of the ClientSecretCredential with the details needed to authenticate against Azure Active Directory with a client secret.
        /// </summary>
        /// <param name="tenantId">The Azure Active Directory tenant (directory) Id of the service principal.</param>
        /// <param name="clientId">The client (application) ID of the web api</param>
        /// <param name="clientSecret">A client secret that was generated for the App Registration used to authenticate the client.</param>
        /// <param name="accessToken">accessToken The prefetched token.</param>
        /// <param name="options">Options that allow to configure the management of the requests sent to the Azure Active Directory service.</param>
        public OnBehalfOfFlowCredential(string tenantId, string clientId, string clientSecret, AccessToken accessToken, TokenCredentialOptions options)
           : this(tenantId, clientId, clientSecret, null, accessToken, options, null, null)
        {
        }

        internal OnBehalfOfFlowCredential(string tenantId, string clientId, string clientSecret, string tokenString, AccessToken accessToken, TokenCredentialOptions options, CredentialPipeline pipeline, MsalConfidentialClient client)
        {
            TenantId = Validations.ValidateTenantId(tenantId, nameof(tenantId));

            ClientId = clientId ?? throw new ArgumentNullException(nameof(clientId));

            ClientSecret = clientSecret ?? throw new ArgumentNullException(nameof(clientSecret));

            _userAssertion = new UserAssertion(tokenString != null ? tokenString : accessToken.Token);

            _pipeline = pipeline ?? CredentialPipeline.GetInstance(options);

            _client = client ?? new MsalConfidentialClient(_pipeline, tenantId, clientId, clientSecret, options as ITokenCacheOptions);
        }

        /// <summary>
        /// Obtains a token from the Azure Active Directory service, using the specified client secret to authenticate. This method is called automatically by Azure SDK client libraries. You may call this method directly, but you must also handle token caching and token refreshing.
        /// </summary>
        /// <param name="requestContext">The details of the authentication request.</param>
        /// <param name="cancellationToken">A <see cref="CancellationToken"/> controlling the request lifetime.</param>
        /// <returns>An <see cref="AccessToken"/> which can be used to authenticate service client calls.</returns>
        public override async ValueTask<AccessToken> GetTokenAsync(TokenRequestContext requestContext, CancellationToken cancellationToken = default)
        {
            using CredentialDiagnosticScope scope = _pipeline.StartGetTokenScope("OnBehalfOfFlowCredential.GetToken", requestContext);

            try
            {
                AuthenticationResult result = await _client.AcquireTokenForClientAsync(requestContext.Scopes, true, _userAssertion, cancellationToken).ConfigureAwait(false);

                return scope.Succeeded(new AccessToken(result.AccessToken, result.ExpiresOn));
            }
            catch (Exception e)
            {
                throw scope.FailWrapAndThrow(e);
            }
        }

        /// <summary>
        /// Obtains a token from the Azure Active Directory service, using the specified client secret to authenticate. This method is called automatically by Azure SDK client libraries. You may call this method directly, but you must also handle token caching and token refreshing.
        /// </summary>
        /// <param name="requestContext">The details of the authentication request.</param>
        /// <param name="cancellationToken">A <see cref="CancellationToken"/> controlling the request lifetime.</param>
        /// <returns>An <see cref="AccessToken"/> which can be used to authenticate service client calls.</returns>
        public override AccessToken GetToken(TokenRequestContext requestContext, CancellationToken cancellationToken = default)
        {
            using CredentialDiagnosticScope scope = _pipeline.StartGetTokenScope("OnBehalfOfFlowCredential.GetToken", requestContext);

            try
            {
                AuthenticationResult result = _client.AcquireTokenForClientAsync(requestContext.Scopes, false, _userAssertion, cancellationToken).EnsureCompleted();

                return scope.Succeeded(new AccessToken(result.AccessToken, result.ExpiresOn));
            }
            catch (Exception e)
            {
                throw scope.FailWrapAndThrow(e);
            }
        }
    }
}
