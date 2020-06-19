using DotNetEnv;
using Microsoft.Azure.Management.ResourceManager;
using Microsoft.Azure.Management.ResourceManager.Models;
using System;
using Xunit;

namespace JonGallant.Azure.Identity.Extensions.Tests.Mgmt
{
    public class ResourceGroupTests
    {
        [Fact]
        public async void CreateAndDeleteResourceGroupTest()
        {
            Env.Load("../../../../../.env");

            var client = new ResourceManagementClient(new AzureIdentityCredentialAdapter());
            client.SubscriptionId = Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID");

            var name = "azidextrg" + Guid.NewGuid().ToString("n").Substring(0, 8);

            var rg = new ResourceGroup(location:"westus", name:name);

            var result = await client.ResourceGroups.CreateOrUpdateAsync(name, rg);
            
            Assert.Equal(result.Name, name);

            await client.ResourceGroups.DeleteAsync(name);
        }
    }
}
