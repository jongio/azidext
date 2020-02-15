//dotnet add package Microsoft.Azure.Management.Authorization --version 2.11.0-preview
using System;

using Microsoft.Azure.Management.Authorization;
using Microsoft.Azure.Management.Authorization.Models;

using DotNetEnv;

using Xunit;

namespace JonGallant.Azure.Identity.Extensions.Tests.Mgmt
{
    public class RoleAssignmentTests
    {
        [Fact]
        public async void RoleAssignmentTest()
        {
            Env.Load("../../../../../.env");

            var azurePrincipalId = Environment.GetEnvironmentVariable("AZURE_PRINCIPAL_ID"); 
            // Needs to be in Owner role in Azure Subscription, az role assignment create --assignee AZURE_CLIENT_ID --role 8e3af657-a8ff-443c-a75c-2fe8c4bcb635
            // You can get the principalId from the AZURE_CLIENT_ID with this command, az ad sp show --id AZURE_CLIENT_ID --query objectId
            var azureSubId = Environment.GetEnvironmentVariable("AZURE_SUBSCRIPTION_ID");
            var azureRoleDefinitionId = "673868aa-7521-48a0-acc6-0f60742d39f5"; // Data Factory Contributor
            var roleAssignmentName = Guid.NewGuid().ToString();
            var roleAssignmentScope = $"/subscriptions/{azureSubId}/";

            var client = new AuthorizationManagementClient(new DefaultAzureMgmtCredential());
            client.SubscriptionId = azureSubId;

            var roleDefinitionId = $"/subscriptions/{client.SubscriptionId}/providers/Microsoft.Authorization/roleDefinitions/{azureRoleDefinitionId}";

            var roleAssignmentParameters = new RoleAssignmentCreateParameters(roleDefinitionId, azurePrincipalId);

            var roleAssignment = await client.RoleAssignments.CreateAsync(roleAssignmentScope, roleAssignmentName, roleAssignmentParameters);

            Assert.Equal(roleAssignment.RoleDefinitionId, roleDefinitionId);

            var roleAssignmentDelete = await client.RoleAssignments.DeleteAsync(roleAssignmentScope, roleAssignmentName);

            Exception ex = await Assert.ThrowsAsync<Microsoft.Rest.Azure.CloudException>(async () => await client.RoleAssignments.GetAsync(roleAssignmentScope, roleAssignmentName));

            Assert.EndsWith("is not found.", ex.Message);
        }
    }
}
