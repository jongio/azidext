// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azidentity

import (
	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
	"github.com/Azure/azure-sdk-for-go/sdk/azidentity"
	"github.com/Azure/go-autorest/autorest"
)

// NewDefaultAzureIdentityCredentialAdapter returns BearerAuthorizer.
// scopes: The list of scopes for which the token will have access
func NewDefaultAzureIdentityCredentialAdapter() (*autorest.BearerAuthorizer, error) {
	tokenCredential, err := azidentity.NewDefaultAzureCredential(nil)
	if err != nil {
		return nil, err
	}
	bearerAuthorizer, err := NewAzureIdentityCredentialAdapter(tokenCredential, nil)
	if err != nil {
		return nil, err
	}
	return bearerAuthorizer, nil
}

// NewAzureIdentityCredentialAdapter returns BearerAuthorizer.
// azureCredential: TokenCredential
// scopes: The list of scopes for which the token will have access
func NewAzureIdentityCredentialAdapter(tokenCredential azcore.TokenCredential, scopes []string) (*autorest.BearerAuthorizer, error) {
	option := newScopeOption(scopes)
	AzureIdentityTokenProvider, err := NewAzureIdentityTokenProvider(tokenCredential, option)
	if err != nil {
		return nil, err
	}
	bearerAuthorizer := autorest.NewBearerAuthorizer(AzureIdentityTokenProvider)
	return bearerAuthorizer, nil
}
