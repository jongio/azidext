using Azure.Identity;
using Microsoft.Azure.ServiceBus;
using Microsoft.Azure.ServiceBus.Primitives;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace JonGallant.Azure.Identity.Extensions
{
    public class DefaultAzureServiceBusCredential : ITokenProvider
    {
        private DefaultAzureCredential defaultAzureCredential;

        public DefaultAzureServiceBusCredential() : this(new DefaultAzureCredential())
        {
        }
        public DefaultAzureServiceBusCredential(DefaultAzureCredential defaultAzureCredential)
        {
            this.defaultAzureCredential = defaultAzureCredential;
        }

        public async Task<SecurityToken> GetTokenAsync(string appliesTo, TimeSpan timeout)
        {
            var cts = new CancellationTokenSource();
            cts.CancelAfter((int)timeout.TotalMilliseconds);

            var token = await new DefaultAzureCredentialTokenProvider(
                this.defaultAzureCredential, 
                new string[] { "https://servicebus.azure.net/.default" })
                .GetTokenAsync(cts.Token);

            var appliesToUri = new Uri(appliesTo);

            return new JsonSecurityToken(token.Token, appliesToUri.Host);
        }
    }
}