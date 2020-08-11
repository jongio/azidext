package azidentity

import (
	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
	"github.com/Azure/azure-sdk-for-go/sdk/azidentity"
	"github.com/Azure/go-autorest/autorest"
)

// NewAzureIdentityCredentialAapter returns BearerAuthorizer.
// azureCredential: TokenCredential
// scopes: The list of scopes for which the token will have access
func NewAzureIdentityCredentialAapter(tokenCredential azcore.TokenCredential, scopes []string) (*autorest.BearerAuthorizer, error) {
	var err error
	if tokenCredential == nil {
		tokenCredential, err = azidentity.NewDefaultAzureCredential(nil)
		if err != nil {
			return nil, err
		}
	}
	AzureIdentityTokenProvider := NewAzureIdentityTokenProvider(tokenCredential, scopes)
	bearerAuthorizer := autorest.NewBearerAuthorizer(AzureIdentityTokenProvider)
	return bearerAuthorizer, nil
}
