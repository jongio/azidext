using Microsoft.Rest;
using System;

namespace JonGallant.Azure.Identity.Extensions
{
    public class DefaultAzureMgmtCredential : TokenCredentials
    {
        public DefaultAzureMgmtCredential(string[] scopes = null) : base(new DefaultAzureMgmtCredentialTokenProvider(scopes))
        {
        }
    }
}