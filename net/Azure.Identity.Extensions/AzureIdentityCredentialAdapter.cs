using Microsoft.Rest;
using Azure.Core;

namespace Azure.Identity.Extensions
{
    public class AzureIdentityCredentialAdapter : TokenCredentials
    {
        public AzureIdentityCredentialAdapter(string[] scopes = null) : base(new AzureIdentityTokenProvider(scopes))
        {
        }

        public AzureIdentityCredentialAdapter(TokenCredential tokenCredential, string[] scopes = null) : base(new AzureIdentityTokenProvider(tokenCredential, scopes))
        {

        }
    }
}