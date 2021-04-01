import { StaticTokenCredential } from "../src/credentials/staticTokenCredential"
import { AccessToken } from "@azure/core-http"
import * as assert from "assert"


describe("StaticTokenCredential", function () {
    it("authenticates with valid token string", async () => {
        const expectedToken = "token";
        const credential = new StaticTokenCredential(expectedToken);
        const actualToken = await credential.getToken();
        assert.strictEqual(expectedToken, actualToken.token);
    });

    it("authenticates with valid access token", async () => {
        const expectedToken: AccessToken = {
            token: "token",
            expiresOnTimestamp: new Date().setDate(new Date().getDate() + 1)
        }
        const credential = new StaticTokenCredential(expectedToken);
        const actualToken = await credential.getToken();
        assert.strictEqual(expectedToken.token, actualToken.token);
        assert.strictEqual(expectedToken.expiresOnTimestamp, actualToken.expiresOnTimestamp);
    });
})