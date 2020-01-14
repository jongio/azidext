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

            var client = new ApplicationInsightsManagementClient(new DefaultAzureMgmtCredential());
            client.SubscriptionId = Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID");

            var component = new ApplicationInsightsComponent(Environment.GetEnvironmentVariable("AZURE_REGION"), "web", "web");
            var name = Environment.GetEnvironmentVariable("APPINSIGHTS_NAME") + Guid.NewGuid().ToString("n").Substring(0, 8);

            component = client.Components.CreateOrUpdate(Environment.GetEnvironmentVariable("AZURE_RESOURCE_GROUP"), name, component);

            Assert.NotNull(component.CreationDate);
        }
    }
}
