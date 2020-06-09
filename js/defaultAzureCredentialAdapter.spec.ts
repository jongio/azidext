import assert from "assert";
import * as dotenv from "dotenv";
import { isNode } from "@azure/core-http";
import { KeyVaultManagementClient } from "@azure/arm-keyvault";
import { DefaultAzureCredentialAdapter } from "./defaultAzureCredentialAdapter";

if (isNode) {
  dotenv.config();
}

const subscriptionId = process.env.SUBSCRIPTION_ID;

describe("DefaultAzureCredentialAdapter", function() {
  it("get exsit key vault ", async () => {
    try {
      const cred = new DefaultAzureCredentialAdapter();
      const client = new KeyVaultManagementClient(cred, subscriptionId);
      const resourceGroupName = process.env.AZURE_RESOURCE_GROUP_NAME;
      const vaultName = process.env.AZURE_EXSIT_KEY_VAULT_NAME;
      await client.vaults.get(resourceGroupName, vaultName).then((result) => {
        console.log("The result is:");
        console.log(result);
      })
    } catch (err) {
      throw err
    }
  })
});
