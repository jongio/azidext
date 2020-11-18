import * as assert from "assert";
import * as dotenv from "dotenv";
import { ResourceManagementClient } from "@azure/arm-resources";
import { AzureIdentityCredentialAdapter } from "./azureIdentityCredentialAdapter";

dotenv.config({ path: "../.env" });

const subscriptionId = process.env.AZURE_SUBSCRIPTION_ID;

describe("AzureIdentityCredentialAdapter ", function() {
  it("create resoucegroup and delete it", async () => {
      const cred = new AzureIdentityCredentialAdapter();
      const client = new ResourceManagementClient(cred, subscriptionId);
      const name = "azidextrg" + (Math.floor(Math.random()* 89999999) + 10000000);    
      const result = await client.resourceGroups.createOrUpdate(name, { location: 'westus', name : name });
      assert.equal(result.name, name);
      await (await client.resourceGroups.beginDeleteMethod(name)).pollUntilFinished;  
  });
});
