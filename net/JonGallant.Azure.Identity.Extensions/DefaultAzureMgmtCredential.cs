using Azure.Identity;
using Microsoft.Rest;
using System;

namespace JonGallant.Azure.Identity.Extensions
{
    public class DefaultAzureMgmtCredential : TokenCredentials
    {
        public DefaultAzureMgmtCredential(bool includeInteractiveCredentials = false, string[] scopes = null) : base(new DefaultAzureCredentialTokenProvider(includeInteractiveCredentials, scopes))
        {
        }

        public DefaultAzureMgmtCredential(DefaultAzureCredentialOptions options, string[] scopes = null) : base(new DefaultAzureCredentialTokenProvider(options, scopes))
        {
        }

        public DefaultAzureMgmtCredential(DefaultAzureCredential defaultAzureCredential, string[] scopes = null) : base(new DefaultAzureCredentialTokenProvider(defaultAzureCredential, scopes)) { }
    }
}