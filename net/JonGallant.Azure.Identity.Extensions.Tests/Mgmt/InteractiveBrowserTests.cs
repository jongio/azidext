using DotNetEnv;
using Microsoft.Azure.Management.Storage;
using System;
using Xunit;

namespace JonGallant.Azure.Identity.Extensions.Tests.Mgmt
{
    public class InteractiveBrowserTests
    {
        [Fact]
        public async void InteractiveBrowserTest()
        {
            // Pre-req: Storage account created.
            Env.Load("../../../../../.env");
            Environment.SetEnvironmentVariable("AZURE_CLIENT_ID", "");
            Environment.SetEnvironmentVariable("AZURE_CLIENT_SECRET", "");
            Environment.SetEnvironmentVariable("AZURE_TENANT_ID", "");

            var client = new StorageManagementClient(new DefaultAzureMgmtCredential(true));

            client.SubscriptionId = Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID");

            var props = await client.StorageAccounts.GetPropertiesAsync(
                Environment.GetEnvironmentVariable("AZURE_RESOURCE_GROUP"),
                Environment.GetEnvironmentVariable("AZURE_STORAGE_ACCOUNT_NAME"));

            Assert.Equal(props.Location, Environment.GetEnvironmentVariable("AZURE_REGION"));
        }
    }
}
