// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azidext

import (
	"context"

	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
	"github.com/Azure/azure-sdk-for-go/sdk/azidentity"
	"github.com/Azure/go-autorest/autorest"
)

type tokenAdapter struct {
	Token string `json:"access_token"`
}

// NewAzureIdentityCredentialAdapter is used to adapt an azcore.Credential to an autorest.Authorizer
func NewAzureIdentityCredentialAdapter(credential azcore.TokenCredential, options azcore.TokenRequestOptions) (autorest.Authorizer, error) {

	token, err := credential.GetToken(context.Background(), options)
	if err != nil {
		return nil, err
	}
	return &tokenAdapter{Token: token.Token}, nil
}

// WithAuthorization implements the autorest.Authorizer interface for type policyAdapter.
func (ca *tokenAdapter) WithAuthorization() autorest.PrepareDecorator {
	return autorest.WithBearerAuthorization(ca.Token)
}

//DefaultManagementScope defined the default Scopes
const DefaultManagementScope = "https://management.azure.com//.default"

// DefaultAzureCredentialOptions contains credential and authentication policy options.
type DefaultAzureCredentialOptions struct {
	// DefaultCredential contains configuration options passed to azidentity.NewDefaultAzureCredential().
	// Set this to nil to accept the underlying default behavior.
	DefaultCredential *azidentity.DefaultAzureCredentialOptions

	// TokenRequestOptions contain specific parameter that may be used by credentials types when attempting to get a token.
	// Setting this to nil will use the DefaultManagementScope when acquiring a token.
	TokenRequest *azcore.TokenRequestOptions
}

// NewDefaultAzureCredentialAdapter adapts azcore.NewDefaultAzureCredential to an autorest.Authorizer.
func NewDefaultAzureCredentialAdapter(options *DefaultAzureCredentialOptions) (autorest.Authorizer, error) {
	if options == nil {
		options = &DefaultAzureCredentialOptions{
			TokenRequest: &azcore.TokenRequestOptions{
				Scopes: []string{DefaultManagementScope},
			},
		}
	}
	chain, err := azidentity.NewDefaultAzureCredential(options.DefaultCredential)
	if err != nil {
		return nil, err
	}
	azureIdentityCredentialAdapter, err := NewAzureIdentityCredentialAdapter(chain, *options.TokenRequest)
	if err != nil {
		return nil, err
	}
	return azureIdentityCredentialAdapter, nil
}
