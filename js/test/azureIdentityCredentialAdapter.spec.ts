import * as assert from "assert";
import { ResourceManagementClient } from "@azure/arm-resources";
import { AzureIdentityCredentialAdapter } from "../src/credentials/azureIdentityCredentialAdapter";

const subscriptionId = process.env.AZURE_SUBSCRIPTION_ID;

describe("AzureIdentityCredentialAdapter ", function () {
  it("create resoucegroup and delete it", async () => {
    const cred = new AzureIdentityCredentialAdapter();
    const client = new ResourceManagementClient(cred, subscriptionId);
    const name = "azidextrg" + (Math.floor(Math.random() * 89999999) + 10000000);
    const result = await client.resourceGroups.createOrUpdate(name, { location: 'westus', name: name });
    assert.strictEqual(result.name, name);
    await (await client.resourceGroups.beginDeleteMethod(name)).pollUntilFinished;
  });
});
