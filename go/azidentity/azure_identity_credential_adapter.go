// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azidentity

import (
	"github.com/Azure/azure-sdk-for-go/sdk/azidentity"
	"github.com/Azure/go-autorest/autorest"
	"github.com/Azure/go-autorest/autorest/adal"
)

//defaultScope defined the default Scopes
const defaultScope = "https://management.azure.com/.default"

//AzureIdentityCredentialAdapter is the adapter interface
type AzureIdentityCredentialAdapter interface {
	NewBearerAuthorizer() *autorest.BearerAuthorizer
}

//AzureIdentityCredentialAdapterBase is the base adapter
type AzureIdentityCredentialAdapterBase struct {
	Provider adal.OAuthTokenProvider
}

//NewAzureIdentityCredentialAdapterBase returns AzureIdentityCredentialAdapterBase
//provider: a type that implements OAuthTokenProvider interface
func NewAzureIdentityCredentialAdapterBase(provider adal.OAuthTokenProvider) *AzureIdentityCredentialAdapterBase {
	return &AzureIdentityCredentialAdapterBase{
		Provider: provider,
	}
}

func (adapter *AzureIdentityCredentialAdapterBase) setDefaultScopes(scopes []string) []string {
	if scopes == nil || len(scopes) == 0 {
		scopes = []string{defaultScope}
	}
	return scopes
}

//NewBearerAuthorizer returns a new BearerAuthorizer
func (adapter *AzureIdentityCredentialAdapterBase) NewBearerAuthorizer() *autorest.BearerAuthorizer {
	bearerAuthorizer := autorest.NewBearerAuthorizer(adapter.Provider)
	return bearerAuthorizer
}

//DefaultAzureCredentialAdapter is DefaultAzureCredential adapter
type DefaultAzureCredentialAdapter struct {
	*AzureIdentityCredentialAdapterBase
}

// NewDefaultAzureCredentialAdapter returns AzureDefaultAzureCredentialAdapter
// scopes: The list of scopes for which the token will have access
func NewDefaultAzureCredentialAdapter(scopes []string) (AzureIdentityCredentialAdapter, error) {
	tokenCredential, err := azidentity.NewDefaultAzureCredential(nil)
	if err != nil {
		return nil, err
	}
	adapter := &DefaultAzureCredentialAdapter{}
	scopes = adapter.setDefaultScopes(scopes)
	provider, err := NewAzureIdentityTokenProvider(tokenCredential, scopes)
	if err != nil {
		return nil, err
	}
	baseAdapter := NewAzureIdentityCredentialAdapterBase(provider)
	adapter.AzureIdentityCredentialAdapterBase = baseAdapter
	return adapter, nil
}
