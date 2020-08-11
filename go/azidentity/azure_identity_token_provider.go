package azidentity

import (
	"context"

	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
)

// AzureIdentityTokenProvider provides a simple extension to use DefaultAzureCredential
// to create BearerAuthorizer to use Authorizer for authentication.
type AzureIdentityTokenProvider struct {
	TokenCredential azcore.TokenCredential
	Scopes          []string
}

type azureIdentityToken struct {
	AccessToken string `json:"access_token"`
}

// NewAzureIdentityTokenProvider return AzureIdentityTokenProvider.
// azureCredential: TokenCredential
// scopes: The list of scopes for which the token will have access
func NewAzureIdentityTokenProvider(tokenCredential azcore.TokenCredential, scopes []string) *AzureIdentityTokenProvider {
	if scopes == nil {
		scopes = []string{"https://management.azure.com/.default"}
	}
	return &AzureIdentityTokenProvider{TokenCredential: tokenCredential, Scopes: scopes}
}

// OAuthToken return the current access token.
func (c *AzureIdentityTokenProvider) OAuthToken() string {
	token, err := c.getToken()
	if err != nil {
		panic("TokenCredential get token failed, error:" + err.Error())
	}
	return token.AccessToken
}
func (c *AzureIdentityTokenProvider) getToken() (*azureIdentityToken, error) {
	accesstoken, err := c.TokenCredential.GetToken(context.Background(), azcore.TokenRequestOptions{Scopes: c.Scopes})
	if err != nil {
		return nil, err
	}
	return &azureIdentityToken{AccessToken: accesstoken.Token}, nil
}
