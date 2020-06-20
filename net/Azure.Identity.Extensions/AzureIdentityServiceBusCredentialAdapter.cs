using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Azure.ServiceBus;
using Microsoft.Azure.ServiceBus.Primitives;
using Azure.Core;
using Azure.Identity;


namespace Azure.Identity.Extensions
{
    public class AzureIdentityServiceBusCredentialAdapter : ITokenProvider
    {
        private TokenCredential tokenCredential;

        public AzureIdentityServiceBusCredentialAdapter() : this(new DefaultAzureCredential())
        {
        }

        public AzureIdentityServiceBusCredentialAdapter(TokenCredential tokenCredential)
        {
            this.tokenCredential = tokenCredential;
        }

        public async Task<SecurityToken> GetTokenAsync(string appliesTo, TimeSpan timeout)
        {
            var cts = new CancellationTokenSource();
            cts.CancelAfter((int)timeout.TotalMilliseconds);

            var token = await new AzureIdentityTokenProvider(
                this.tokenCredential,
                new string[] { "https://servicebus.azure.net/.default" })
                .GetTokenAsync(cts.Token);

            var appliesToUri = new Uri(appliesTo);

            return new JsonSecurityToken(token.Token, appliesToUri.Host);
        }
    }
}