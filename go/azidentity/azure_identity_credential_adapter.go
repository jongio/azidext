package azidentity

import (
	"github.com/Azure/azure-sdk-for-go/sdk/azidentity"
	"github.com/Azure/go-autorest/autorest"
)

// NewAzureIdentityCredentialAapter return NewAzureIdentityTokenAapter.
func NewAzureIdentityCredentialAapter() (*autorest.BearerAuthorizer, error) {
	defaultAzureCredential, err := azidentity.NewDefaultAzureCredential(nil)
	if err != nil {
		return nil, err
	}
	AzureIdentityTokenProvider, err := NewAzureIdentityTokenProvider(defaultAzureCredential, nil)
	if err != nil {
		return nil, err
	}
	bearerAuthorizer := autorest.NewBearerAuthorizer(AzureIdentityTokenProvider)
	return bearerAuthorizer, nil
}
