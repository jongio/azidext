import { TokenCredential, AccessToken } from "@azure/core-http"
import { IdentityClient, TokenCredentialOptions } from "../client/identityClient";
import qs from "qs";
import { getIdentityTokenEndpointSuffix } from "../util/identityTokenEndpoint";


export class OnBehalfOfFlowCredential implements TokenCredential {
    private identityClient: IdentityClient;
    private tenantId: string;
    private clientId: string;
    private clientSecret: string;
    private accessToken: string | AccessToken;

    constructor(tenantId: string,
        clientId: string,
        clientSecret: string,
        accessToken: string | AccessToken,
        options?: TokenCredentialOptions) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.identityClient = new IdentityClient(options);
        this.accessToken = (typeof accessToken == "string") ? accessToken : accessToken.token;
    }

    public async getToken(scopes: string | string[]): Promise<AccessToken | null> {
        const urlSuffix = getIdentityTokenEndpointSuffix(this.tenantId);
        const webResource = this.identityClient.createWebResource({
            url: `${this.identityClient.authorityHost}/${this.tenantId}/${urlSuffix}`,
            method: "POST",
            disableJsonStringifyOnBody: true,
            deserializationMapper: undefined,
            body: qs.stringify({
                grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
                client_id: this.clientId,
                client_secret: this.clientSecret,
                assertion: this.accessToken,
                scope: typeof scopes === "string" ? scopes : scopes.join(" "),
                requested_token_use: "on_behalf_of"
            }),
            headers: {
                Accept: "application/json",
                "Content-Type": "application/x-www-form-urlencoded"
            }
        });
        const tokenResponse = await this.identityClient.sendTokenRequest(webResource);
        return (tokenResponse && tokenResponse.accessToken) || null;
    }
}