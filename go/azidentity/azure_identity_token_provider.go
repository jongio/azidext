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

// NewAzureIdentityTokenProvider returns AzureIdentityTokenProvider.
// tokenCredential: TokenCredential
// scopes: The list of scopes for which the token will have access
func NewAzureIdentityTokenProvider(tokenCredential azcore.TokenCredential, scopes []string) (*AzureIdentityTokenProvider, error) {
	accesstoken, err := tokenCredential.GetToken(context.Background(), azcore.TokenRequestOptions{Scopes: scopes})
	if err != nil {
		return nil, err
	}
	return &AzureIdentityTokenProvider{AccessToken: accesstoken.Token}, nil
}

// OAuthToken return the current access token.
func (c *AzureIdentityTokenProvider) OAuthToken() string {
	return c.AccessToken
}
