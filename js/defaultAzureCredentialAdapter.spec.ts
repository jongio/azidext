import assert from "assert";
import { KeyVaultManagementClient } from "@azure/arm-keyvault";
import { DefaultAzureCredentialAdapter } from "./DefaultAzureCredentialAdapter";
const subscriptionId = "AZUER_SUBSCRIPTION_ID";

describe("DefaultAzureCredentialAdapter", function() {
  it("get exsit key vault ", async () => {
    const cred = new DefaultAzureCredentialAdapter();
    const client = new KeyVaultManagementClient(cred, subscriptionId);
    const resourceGroupName = "AZURE_RESOURCE_GROUP_NAME";
    const vaultName = "AZURE_EXSIT_KEY_VAULT_NAME";
    client.vaults.get(resourceGroupName, vaultName).then((result) => {
     assert.equal(result.name, vaultName)
    });
  });
});
