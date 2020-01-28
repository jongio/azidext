using Azure.Identity;
using Microsoft.Rest;
using System;

namespace JonGallant.Azure.Identity.Extensions
{
    public class DefaultAzureMgmtCredential : TokenCredentials
    {
        public DefaultAzureMgmtCredential(string[] scopes = null) : base(new DefaultAzureCredentialTokenProvider(scopes))
        {
        }

        public DefaultAzureMgmtCredential(DefaultAzureCredential defaultAzureCredential, string[] scopes = null) : base(new DefaultAzureCredentialTokenProvider(defaultAzureCredential, scopes)) { }
    }
}