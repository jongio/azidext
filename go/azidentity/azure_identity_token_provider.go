package azidentity

import (
	"context"

	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
)

//AzureIdentityTokenProvider provides a simple extension to use DefaultAzureCredential to create BearerAuthorizer to
//use Authorizer for authentication.
type AzureIdentityTokenProvider struct {
	AccessToken string `json:"access_token"`
}

// NewAzureIdentityTokenProvider return AzureIdentityTokenProvider.
//azureCredential: TokenCredential
//scopes: The list of scopes for which the token will have access
func NewAzureIdentityTokenProvider(azureCredential azcore.TokenCredential, scopes []string) (*AzureIdentityTokenProvider, error) {
	if scopes == nil {
		scopes = []string{"https://management.azure.com/.default"}
	}
	accesstoken, err := azureCredential.GetToken(context.Background(), azcore.TokenRequestOptions{Scopes: scopes})
	if err != nil {
		return nil, err
	}
	return &AzureIdentityTokenProvider{AccessToken: accesstoken.Token}, nil
}

// OAuthToken return the current access token.
func (c *AzureIdentityTokenProvider) OAuthToken() string {
	return c.AccessToken
}
