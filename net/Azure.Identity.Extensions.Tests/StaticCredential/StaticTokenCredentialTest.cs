using Azure.Core;
using DotNetEnv;
using Microsoft.Azure.Management.ServiceBus;
using Microsoft.Azure.ServiceBus;
using System;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Xunit;

namespace Azure.Identity.Extensions.Tests.StaticCredential
{
    public class StaticTokenCredentialTest
    {
        [Fact]
        public async void TestValidStaticTokenString()
        {
            string expectedToken = "token";
            StaticTokenCredential credential = new StaticTokenCredential(expectedToken);

            AccessToken actualToken = await credential.GetTokenAsync(new TokenRequestContext(new string[] { "https://default.mock.auth.scope/.default" }));

            Assert.Equal(expectedToken, actualToken.Token);
        }

        [Fact]
        public async void testValidStaticAccessToken()
        {
            AccessToken expectedToken = new AccessToken("token", DateTimeOffset.MinValue);
            StaticTokenCredential credential = new StaticTokenCredential(expectedToken);

            AccessToken actualToken = await credential.GetTokenAsync(new TokenRequestContext(new string[] { "https://default.mock.auth.scope/.default" }));

            Assert.Equal(expectedToken.Token, actualToken.Token);
            Assert.Equal(expectedToken.ExpiresOn, actualToken.ExpiresOn);
        }
    }
}
