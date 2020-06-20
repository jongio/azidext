using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Azure.Management.ResourceManager.Fluent;
using Microsoft.Azure.Management.ResourceManager.Fluent.Authentication;
using Microsoft.Rest;
using Microsoft.Rest.Azure.Authentication;
using Azure.Core;
using Azure.Identity;


namespace Azure.Identity.Extensions
{
    public class AzureIdentityFluentCredentialAdapter : AzureCredentials
    {
        private IDictionary<Uri, ServiceClientCredentials> credentialsCache = new ConcurrentDictionary<Uri, ServiceClientCredentials>();
        private TokenCredential tokenCredential;

        public AzureIdentityFluentCredentialAdapter(TokenCredential tokenCredential, string tenantId, AzureEnvironment environment) : base(default(DeviceCredentialInformation), tenantId, environment)
        {
            this.tokenCredential = tokenCredential;
        }

        public AzureIdentityFluentCredentialAdapter(string tenantId, AzureEnvironment environment) : base(default(DeviceCredentialInformation), tenantId, environment)
        {
            this.tokenCredential = new DefaultAzureCredential();
        }

        public async override Task ProcessHttpRequestAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {

            // BEING COPY FROM FLUENT
            var adSettings = new ActiveDirectoryServiceSettings
            {
                AuthenticationEndpoint = new Uri(Environment.AuthenticationEndpoint),
                TokenAudience = new Uri(Environment.ManagementEndpoint),
                ValidateAuthority = true
            };

            string url = request.RequestUri.ToString();
            if (url.StartsWith(Environment.GraphEndpoint, StringComparison.OrdinalIgnoreCase))
            {
                adSettings.TokenAudience = new Uri(Environment.GraphEndpoint);
            }

            string host = request.RequestUri.Host;
            if (host.EndsWith(Environment.KeyVaultSuffix, StringComparison.OrdinalIgnoreCase))
            {
                var resource = new Uri(Regex.Replace(Environment.KeyVaultSuffix, "^.", "https://"));
                if (credentialsCache.ContainsKey(new Uri(Regex.Replace(Environment.KeyVaultSuffix, "^.", "https://"))))
                {
                    adSettings.TokenAudience = resource;
                }
                else
                {
                    using (var r = new HttpRequestMessage(request.Method, url))
                    {
                        var response = await new HttpClient().SendAsync(r).ConfigureAwait(false);

                        if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized && response.Headers.WwwAuthenticate != null)
                        {
                            var header = response.Headers.WwwAuthenticate.ElementAt(0).ToString();
                            var regex = new Regex("authorization=\"([^\"]+)\"");
                            var match = regex.Match(header);
                            adSettings.AuthenticationEndpoint = new Uri(match.Groups[1].Value);
                            regex = new Regex("resource=\"([^\"]+)\"");
                            match = regex.Match(header);
                            adSettings.TokenAudience = new Uri(match.Groups[1].Value);
                        }
                    }
                }
            }

            // END COPY FROM FLUENT

            if (!credentialsCache.ContainsKey(adSettings.TokenAudience))
            {
                credentialsCache[adSettings.TokenAudience] = new AzureIdentityCredentialAdapter(this.tokenCredential);
            }
            await credentialsCache[adSettings.TokenAudience].ProcessHttpRequestAsync(request, cancellationToken);
        }
    }
}