using DotNetEnv;
using Microsoft.Azure.Management.ApplicationInsights.Management;
using Microsoft.Azure.Management.ApplicationInsights.Management.Models;
using Microsoft.Azure.Management.ResourceManager;
using Microsoft.Azure.Management.ResourceManager.Models;
using System;
using Xunit;

namespace JonGallant.Azure.Identity.Extensions.Tests.Mgmt
{
    public class AppInsightsTests
    {
        [Fact]
        public async void CreateAndDeleteAppInsightsTest()
        {
            Env.Load("../../../../../.env");

            var baseName = Environment.GetEnvironmentVariable("AZURE_BASE_NAME");
            var rgName = string.Format("{0}rg", baseName);

            // App Insights
            var client = new ApplicationInsightsManagementClient(new AzureIdentityCredentialAdapter());
            client.SubscriptionId = Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID");

            var component = new ApplicationInsightsComponent("westus", "web", "web");
            var aiName = "appinsightsname" + Guid.NewGuid().ToString("n").Substring(0, 8);

            component = await client.Components.CreateOrUpdateAsync(rgName, aiName, component);

            Assert.NotNull(component.CreationDate);

            await client.Components.DeleteAsync(rgName, aiName);


        }
    }
}
