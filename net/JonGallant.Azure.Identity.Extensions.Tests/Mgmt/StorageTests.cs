using DotNetEnv;
using Microsoft.Azure.Management.Storage;
using System;
using Xunit;

namespace JonGallant.Azure.Identity.Extensions.Tests.Mgmt
{
    public class StorageTests
    {
        [Fact]
        public void ReadStorageAccountLocationTest()
        {
            // Pre-req: Storage account created.
            Env.Load("../../../.env");

            var client = new StorageManagementClient(new DefaultAzureMgmtCredential());

            client.SubscriptionId = Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID");

            var props = client.StorageAccounts.GetProperties(
                Environment.GetEnvironmentVariable("AZURE_RESOURCE_GROUP"), 
                Environment.GetEnvironmentVariable("AZURE_STORAGE_ACCOUNT_NAME"));

            Assert.Equal(props.Location, Environment.GetEnvironmentVariable("AZURE_REGION"));
        }
    }
}
