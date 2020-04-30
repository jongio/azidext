import { ServiceClientCredentials } from "@azure/ms-rest-js";
import {  DefaultAzureCredential } from "@azure/identity";
import { Constants as MSRestConstants, WebResource } from "@azure/ms-rest-js";
import { TokenResponse } from "@azure/ms-rest-nodeauth/dist/lib/credentials/tokenClientCredentials";

const DEFAULT_AUTHORIZATION_SCHEME = "Bearer";
/**
 * This class provides a simple extension to use {@link DefaultAzureCredential} from com.azure:azure-identity library to
 * use with legacy Azure SDKs that accept {@link ServiceClientCredentials} family of credentials for authentication.
 */
export class DefaultAzureCredentialAdapter implements ServiceClientCredentials {
    private defaultAzureCredential: DefaultAzureCredential;
    private scopes: string | string[];
    constructor(
        defaultAzureCredential = new DefaultAzureCredential(),
        scopes: string | string[] = "https://management.azure.com/.default",
      ) { 
        this.defaultAzureCredential = defaultAzureCredential;
        this.scopes = scopes;
      }
 
      public async getToken(): Promise<TokenResponse>{

        const accessToken = (await this.defaultAzureCredential.getToken(this.scopes));
        const result: TokenResponse = {
          accessToken: accessToken.token,
          tokenType: DEFAULT_AUTHORIZATION_SCHEME,
          expiresOn: accessToken.expiresOnTimestamp,
        };
        return result;
      }

      public async signRequest(webResource: WebResource) {
        const tokenResponse = await this.getToken();
        webResource.headers.set(MSRestConstants.HeaderConstants.AUTHORIZATION, `${tokenResponse.tokenType} ${tokenResponse.accessToken}`);
        return Promise.resolve(webResource);
      }
  }