package azidentity

import (
	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
	"github.com/Azure/azure-sdk-for-go/sdk/azidentity"
	"github.com/Azure/go-autorest/autorest"
)

// NewAzureIdentityCredentialAapter returns BearerAuthorizer.
func NewAzureIdentityCredentialAapter() (*autorest.BearerAuthorizer, error) {
	defaultAzureCredential, err := azidentity.NewDefaultAzureCredential(nil)
	if err != nil {
		return nil, err
	}
	AzureIdentityTokenProvider := NewAzureIdentityTokenProvider(defaultAzureCredential, nil)
	bearerAuthorizer := autorest.NewBearerAuthorizer(AzureIdentityTokenProvider)
	return bearerAuthorizer, nil
}

// NewAzureIdentityCredentialAapterWithTokenCredential returns BearerAuthorizer.
// azureCredential: TokenCredential
// scopes: The list of scopes for which the token will have access
func NewAzureIdentityCredentialAapterWithTokenCredential(tokenCredential azcore.TokenCredential, scopes []string) *autorest.BearerAuthorizer {
	AzureIdentityTokenProvider := NewAzureIdentityTokenProvider(tokenCredential, scopes)
	bearerAuthorizer := autorest.NewBearerAuthorizer(AzureIdentityTokenProvider)
	return bearerAuthorizer
}
