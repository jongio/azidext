using DotNetEnv;
using Microsoft.Azure.Management.CosmosDB;
using Microsoft.Azure.Management.CosmosDB.Models;
using System;
using System.Collections.Generic;
using Xunit;

namespace JonGallant.Azure.Identity.Extensions.Tests.Mgmt
{
    public class CosmosDBTests
    {
        [Fact]
        public async void CheckCosmosNameExistsTest()
        {
            Env.Load("../../../../../.env");


            var client = new CosmosDBManagementClient(new AzureIdentityCredentialAdapter());
            client.SubscriptionId = Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID");

            var name = "cosmos" + Guid.NewGuid().ToString("n").Substring(0, 8);

            var results = await client.DatabaseAccounts.CheckNameExistsAsync(name);
            
            Assert.False(results);
        }
    }
}
