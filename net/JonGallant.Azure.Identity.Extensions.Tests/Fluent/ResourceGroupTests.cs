using DotNetEnv;
using Microsoft.Azure.Management.ResourceManager.Fluent;
using System;
using Xunit;

namespace JonGallant.Azure.Identity.Extensions.Tests.Fluent
{
    public class ResourceGroupTests
    {
        [Fact]
        public void CreateAzCredsTest()
        {
            Env.Load("../../../.env");

            var creds = new DefaultAzureFluentCredential(Environment.GetEnvironmentVariable("AZURE_TENANT_ID"), AzureEnvironment.AzureGlobalCloud);
            
            var name = Environment.GetEnvironmentVariable("AZURE_RESOURCE_GROUP") + Guid.NewGuid().ToString("n").Substring(0, 8);
            
            var resourceGroup = Microsoft.Azure.Management.Fluent.Azure.Authenticate(creds).
                    WithSubscription(Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID")).
                    ResourceGroups.Define(name).WithRegion(Environment.GetEnvironmentVariable("AZURE_REGION")).Create();

            Assert.Equal(resourceGroup.Name, name);
        }
    }
}
