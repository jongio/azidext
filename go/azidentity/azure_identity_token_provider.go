// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azidentity

import (
	"context"

	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
)

// AzureIdentityTokenProvider provides a simple extension to use DefaultAzureCredential
// to create BearerAuthorizer to use Authorizer for authentication.
type AzureIdentityTokenProvider struct {
	AccessToken string `json:"access_token"`
}

// NewAzureIdentityTokenProvider return AzureIdentityTokenProvider.
// azureCredential: TokenCredential
// scopes: The list of scopes for which the token will have access
func NewAzureIdentityTokenProvider(tokenCredential azcore.TokenCredential, option *scopeOption) (*AzureIdentityTokenProvider, error) {
	option = option.setDefaultOption()
	accesstoken, err := tokenCredential.GetToken(context.Background(), azcore.TokenRequestOptions{Scopes: option.Scopes})
	if err != nil {
		return nil, err
	}
	return &AzureIdentityTokenProvider{AccessToken: accesstoken.Token}, nil
}

// OAuthToken return the current access token.
func (c *AzureIdentityTokenProvider) OAuthToken() string {
	return c.AccessToken
}

var defaultScopes []string = []string{"https://management.azure.com/.default"}

type scopeOption struct {
	Scopes []string
}

func (s *scopeOption) setDefaultOption() *scopeOption {
	if s == nil {
		s = &scopeOption{Scopes: defaultScopes}
	}
	return s
}

func newScopeOption(scopes []string) *scopeOption {
	if scopes == nil {
		return nil
	}
	return &scopeOption{Scopes: scopes}
}