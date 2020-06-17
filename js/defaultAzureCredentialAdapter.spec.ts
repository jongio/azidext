import assert from "assert";
import * as dotenv from "dotenv";
import { isNode } from "@azure/core-http";
import { KeyVaultManagementClient } from "@azure/arm-keyvault";
import { DefaultAzureCredentialAdapter } from "./defaultAzureCredentialAdapter";

dotenv.config();

const subscriptionId = process.env.AZURE_SUBSCRIPTION_ID;

describe("DefaultAzureCredentialAdapter", function() {
  it("get exsit key vault ", async () => {
    try {
      const cred = new DefaultAzureCredentialAdapter();
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
