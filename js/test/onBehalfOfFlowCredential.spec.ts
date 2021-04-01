import { OnBehalfOfFlowCredential } from "../src/credentials/onBehalfOfFlowCredential"
import * as assert from "assert"

describe("OnBehalfOfFlowCredential", function () {
    it("authenticates with valid token string", async () => {
        const expectedToken = process.env.AZURE_TOKEN_STRING;
        const clientId = process.env.AZURE_CLIENT_ID;
        const clientSecret = process.env.AZURE_CLIENT_SECRET;
        const tenantId = process.env.AZURE_TENANT_ID;
        const credential = new OnBehalfOfFlowCredential(tenantId, clientId, clientSecret, expectedToken);
        const actualToken = await credential.getToken("https://vault.azure.net/.default");
        assert.ok(actualToken);
    });
});