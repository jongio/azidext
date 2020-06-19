import assert from "assert";
import * as dotenv from "dotenv";
import { KeyVaultManagementClient } from "@azure/arm-keyvault";
import { AzureCredentialAdapter } from "./azureCredentialAdapter";

dotenv.config();

const subscriptionId = process.env.AZURE_SUBSCRIPTION_ID;

describe("DefaultAzureCredentialAdapter", function() {
  it("get exsit key vault ", async () => {
    try {
      const cred = new AzureCredentialAdapter();
      const client = new KeyVaultManagementClient(cred, subscriptionId);
      const resourceGroupName = process.env.AZURE_RESOURCE_GROUP;
      const vaultName = process.env.AZURE_KEY_VAULT_NAME;
      await client.vaults.get(resourceGroupName, vaultName).then((result) => {
        console.log("The result is:");
        console.log(result);
      })
    } catch (err) {
      throw err
    }
  })
});
