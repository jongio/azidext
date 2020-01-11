using DotNetEnv;
using Microsoft.Azure.Management.ApplicationInsights.Management;
using Microsoft.Azure.Management.ApplicationInsights.Management.Models;
using System;
using Xunit;

namespace JonGallant.Azure.Identity.Extensions.Tests
{
    public class AppInsightsTests
    {
        [Fact]
        public void CreateAppInsightsTest()
        {
            Env.Load("../../../.env");

            var appInsightsClient = new ApplicationInsightsManagementClient(new DefaultAzureMgmtCredential());
            appInsightsClient.SubscriptionId = Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID");

            var appInsightsComponent = new ApplicationInsightsComponent("westus", "web", "web");
            var appInsightsName = Environment.GetEnvironmentVariable("APPINSIGHTS_NAME") + Guid.NewGuid().ToString("n").Substring(0, 8);
            
            appInsightsComponent = appInsightsClient.Components.CreateOrUpdate(Environment.GetEnvironmentVariable("AZURE_RESOURCE_GROUP"), appInsightsName, appInsightsComponent);

            Assert.NotNull(appInsightsComponent.CreationDate);
        }
    }
}
