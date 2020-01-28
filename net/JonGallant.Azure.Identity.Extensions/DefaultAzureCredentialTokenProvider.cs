using Azure.Core;
using Azure.Identity;
using Microsoft.Rest;
using System;
using System.Net.Http.Headers;
using System.Threading;
using System.Threading.Tasks;

namespace JonGallant.Azure.Identity.Extensions
{
    public class DefaultAzureCredentialTokenProvider : ITokenProvider

    {
        private string accessToken;
        private DateTimeOffset expiration;
        private static readonly TimeSpan ExpirationThreshold = TimeSpan.FromMinutes(5);
        private string[] scopes;

        private DefaultAzureCredential defaultAzureCredential;


        public DefaultAzureCredentialTokenProvider(string[] scopes = null) : this(new DefaultAzureCredential(), scopes)
        {
        }

        public DefaultAzureCredentialTokenProvider(DefaultAzureCredential defaultAzureCredential, string[] scopes = null)
        {
            if (scopes == null || scopes.Length == 0)
            {
                scopes = new string[] { "https://management.azure.com/.default" };
            }

            this.scopes = scopes;
            this.defaultAzureCredential = defaultAzureCredential;
        }

        public virtual async Task<AuthenticationHeaderValue> GetAuthenticationHeaderAsync(CancellationToken cancellationToken)
        {
            var accessToken = await GetTokenAsync(cancellationToken);
            return new AuthenticationHeaderValue("Bearer", accessToken.Token);
        }

        public virtual async Task<AccessToken> GetTokenAsync(CancellationToken cancellationToken)
        {
            if (AccessTokenExpired)
            {
                var tokenResult = await this.defaultAzureCredential.GetTokenAsync(new TokenRequestContext(this.scopes), cancellationToken).ConfigureAwait(false);
                this.accessToken = tokenResult.Token;
                this.expiration = tokenResult.ExpiresOn;
            }

            return new AccessToken(this.accessToken, this.expiration);
        }

        protected virtual bool AccessTokenExpired
        {
            get { return DateTime.UtcNow + ExpirationThreshold >= this.expiration; }
        }
    }
}