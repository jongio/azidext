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
        private TimeSpan timeout;
        private DefaultAzureCredential defaultAzureCredential;

        public DefaultAzureServiceBusCredential() : this(null, null)
        {
        }

        public DefaultAzureServiceBusCredential(TimeSpan timeout) : this(null, timeout)
        {
        }

        public DefaultAzureServiceBusCredential(DefaultAzureCredential defaultAzureCredential) : this(defaultAzureCredential, null) { }
        public DefaultAzureServiceBusCredential(DefaultAzureCredential defaultAzureCredential, TimeSpan? timeout)
        {
            this.defaultAzureCredential = defaultAzureCredential != null ? defaultAzureCredential : new DefaultAzureCredential();
            this.timeout = timeout.HasValue ? timeout.Value : TimeSpan.FromMinutes(2);
        }

        public async Task<SecurityToken> GetTokenAsync(string appliesTo, TimeSpan timeout)
        {
            if (timeout != this.timeout) { this.timeout = timeout; }

            var cts = new CancellationTokenSource();
            cts.CancelAfter((int)this.timeout.TotalMilliseconds);

            var token = await new DefaultAzureCredentialTokenProvider(this.defaultAzureCredential, new string[] { "https://servicebus.azure.net/.default" })
                .GetTokenAsync(cts.Token);

            var appliesToUri = new Uri(appliesTo);

            return new JsonSecurityToken(token.Token, appliesToUri.Host);
        }
    }
}