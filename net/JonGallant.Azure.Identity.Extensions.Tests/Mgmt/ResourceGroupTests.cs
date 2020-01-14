using DotNetEnv;
using Microsoft.Azure.Management.ResourceManager;
using Microsoft.Azure.Management.ResourceManager.Models;
using System;
using System.Collections.Generic;
using Xunit;

namespace JonGallant.Azure.Identity.Extensions.Tests.Mgmt
{
    public class ResourceGroupTests
    {
        [Fact]
        public void CreateResourceGroupTest()
        {
            Env.Load("../../../.env");

            var client = new ResourceManagementClient(new DefaultAzureMgmtCredential());
            client.SubscriptionId = Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID");

            var name = Environment.GetEnvironmentVariable("AZURE_RESOURCE_GROUP") + Guid.NewGuid().ToString("n").Substring(0, 8);

            var rg = new ResourceGroup(location:Environment.GetEnvironmentVariable("AZURE_REGION"), name:name);

            var result = client.ResourceGroups.CreateOrUpdate(name, rg);
            
            Assert.Equal(result.Name, name);
        }
    }
}
