using System;
using System.Net.Http.Headers;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Rest;
using Azure.Core;
using Azure.Identity;


namespace Azure.Identity.Extensions
{
    public class AzureIdentityTokenProvider : ITokenProvider
    {
        private AccessToken? accessToken;
        private static readonly TimeSpan ExpirationThreshold = TimeSpan.FromMinutes(5);
        private string[] scopes;

        private TokenCredential tokenCredential;

        public AzureIdentityTokenProvider(string[] scopes = null) : this(new DefaultAzureCredential(), scopes)
        {
        }

        public AzureIdentityTokenProvider(TokenCredential tokenCredential, string[] scopes = null)
        {
            if (scopes == null || scopes.Length == 0)
            {
                scopes = new string[] { "https://management.azure.com/.default" };
            }

            this.scopes = scopes;
            this.tokenCredential = tokenCredential;
        }

        public virtual async Task<AuthenticationHeaderValue> GetAuthenticationHeaderAsync(CancellationToken cancellationToken)
        {
            var accessToken = await GetTokenAsync(cancellationToken);
            return new AuthenticationHeaderValue("Bearer", accessToken.Token);
        }

        public virtual async Task<AccessToken> GetTokenAsync(CancellationToken cancellationToken)
        {
            if (!this.accessToken.HasValue || AccessTokenExpired)
            {
                this.accessToken = await this.tokenCredential.GetTokenAsync(new TokenRequestContext(this.scopes), cancellationToken).ConfigureAwait(false);
            }

            return this.accessToken.Value;
        }

        protected virtual bool AccessTokenExpired
        {
            get { return !this.accessToken.HasValue ? true : (DateTime.UtcNow + ExpirationThreshold >= this.accessToken.Value.ExpiresOn); }
        }
    }
}