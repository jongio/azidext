using DotNetEnv;
using Microsoft.Azure.Management.Storage;
using System;
using Xunit;
using Azure.Identity;

namespace JonGallant.Azure.Identity.Extensions.Tests.Mgmt
{
    public class InteractiveBrowserTests
    {
        [Fact(Skip = "Requires user interaction")]
        public async void CheckIfStorageNameAvailableWithInteractiveBrowserTest()
        {
            // Pre-req: Storage account created.
            Env.Load("../../../../../.env");
            Environment.SetEnvironmentVariable("AZURE_CLIENT_ID", "");
            Environment.SetEnvironmentVariable("AZURE_CLIENT_SECRET", "");
            Environment.SetEnvironmentVariable("AZURE_TENANT_ID", "");

            var client = new StorageManagementClient(new AzureIdentityCredentialAdapter(new DefaultAzureCredential(true)));
            client.SubscriptionId = Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID");

            var name = "azidext" + Guid.NewGuid().ToString("n").Substring(0, 8);

            var nameAvailable = await client.StorageAccounts.CheckNameAvailabilityAsync(name);

            Assert.True(nameAvailable.NameAvailable);
        }
    }
}
