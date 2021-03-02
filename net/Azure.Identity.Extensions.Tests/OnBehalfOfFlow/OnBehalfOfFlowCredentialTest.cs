using Azure.Core;
using DotNetEnv;
using System;
using Xunit;


namespace Azure.Identity.Extensions.Tests.OnBehalfOfFlow
{
    public class OnBehalfOfFlowCredentialTest
    {
        [Fact]
        public async void TestValidStaticTokenString()
        {
            Env.Load("../../../../../.env");
            var expectedToken = Environment.GetEnvironmentVariable("AZURE_TOKEN_STRING");
            var clientId = Environment.GetEnvironmentVariable("AZURE_CLIENT_ID");
            var clientSecret = Environment.GetEnvironmentVariable("AZURE_CLIENT_SECRET");
            var tenantId = Environment.GetEnvironmentVariable("AZURE_TENANT_ID");
            OnBehalfOfFlowCredential credential = new OnBehalfOfFlowCredential(tenantId, clientId, clientSecret, expectedToken);
            AccessToken actualToken = await credential.GetTokenAsync(new TokenRequestContext(new string[] { "https://vault.azure.net/.default" }));
            Assert.NotNull(actualToken.Token);
        }
    }
}
