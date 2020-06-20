using System;
using Microsoft.Azure.Management.ResourceManager.Fluent;
using DotNetEnv;
using Azure.Identity.Extensions;
using Xunit;
using System.Threading.Tasks;
using static DotNetEnv.Env;

namespace Azure.Identity.Extensions.Tests.Fluent
{
    public class ResourceGroupTests
    {
        [Fact]
        public async Task CheckIfResourceGroupExistsTest()
        {
            Env.Load("../../../../../.env");

            var creds = new AzureIdentityFluentCredentialAdapter(
                Environment.GetEnvironmentVariable("AZURE_TENANT_ID"),
                AzureEnvironment.AzureGlobalCloud);

            var name = Guid.NewGuid().ToString("n").Substring(0, 8);

            var resourceGroupExists = await Microsoft.Azure.Management.Fluent.Azure.Authenticate(creds).
                    WithSubscription(Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID")).
                    ResourceGroups.ContainAsync(name);

            Assert.False(resourceGroupExists);
        }
    }
}
