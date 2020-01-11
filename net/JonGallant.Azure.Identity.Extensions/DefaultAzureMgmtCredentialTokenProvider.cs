using Azure.Core;
using Azure.Identity;
using Microsoft.Rest;
using System;
using System.Net.Http.Headers;
using System.Threading;
using System.Threading.Tasks;

namespace JonGallant.Azure.Identity.Extensions
{
    public class DefaultAzureMgmtCredentialTokenProvider : ITokenProvider

    {
        private string accessToken;
        private DateTimeOffset expiration;
        private static readonly TimeSpan ExpirationThreshold = TimeSpan.FromMinutes(5);
        private string[] scopes;

        public DefaultAzureMgmtCredentialTokenProvider(string[] scopes = null)
        {
            if (scopes == null || scopes.Length == 0)
            {
                scopes = new string[] { "https://management.azure.com/.default" };
            }

            this.scopes = scopes;
        }

        public virtual async Task<AuthenticationHeaderValue> GetAuthenticationHeaderAsync(CancellationToken cancellationToken)
        {

            if (AccessTokenExpired)
            {
                var tokenResult = await new DefaultAzureCredential().GetTokenAsync(new TokenRequestContext(this.scopes)).ConfigureAwait(false);
                this.accessToken = tokenResult.Token;
                this.expiration = tokenResult.ExpiresOn;
            }

            return new AuthenticationHeaderValue("Bearer", this.accessToken);

        }

        protected virtual bool AccessTokenExpired
        {
            get { return DateTime.UtcNow + ExpirationThreshold >= this.expiration; }
        }
    }
}