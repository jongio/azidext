import { TokenCredential, AccessToken } from "@azure/core-http"

export class StaticTokenCredential implements TokenCredential {
    private accessToken: AccessToken;

    constructor(token: string | AccessToken) {
        if (typeof token == "string") {
            this.accessToken = {
                token: token,
                expiresOnTimestamp: new Date().setDate(new Date().getDate() + 1)
            }
        } else {
            this.accessToken = {
                token: token.token,
                expiresOnTimestamp: token.expiresOnTimestamp
            }
        }
    }

    public async getToken(): Promise<AccessToken | null> {
        return this.accessToken;
    }
}