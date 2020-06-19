using DotNetEnv;
using Microsoft.Azure.Management.Storage;
using System;
using Xunit;

namespace JonGallant.Azure.Identity.Extensions.Tests.Mgmt
{
    public class StorageTests
    {
        [Fact]
        public async void CheckIfStorageNameAvailableTest()
        {
            // Pre-req: Storage account created.
            Env.Load("../../../../../.env");

            var client = new StorageManagementClient(new AzureIdentityCredentialAdapter());

            client.SubscriptionId = Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID");

            var name = "azidext" + Guid.NewGuid().ToString("n").Substring(0, 8);

            var nameAvailable = await client.StorageAccounts.CheckNameAvailabilityAsync(name);

            Assert.True(nameAvailable.NameAvailable);
        }
    }
}
