// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azidext

import (
	"context"
	"time"

	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
)

// StaticTokenCredential enables authentication to Azure Active Directory using a access token.
type StaticTokenCredential struct {
	accessToken *azcore.AccessToken
}

// NewStaticTokenCredential constructs a new StaticTokenCredential with the details needed to authenticate against Azure Active Directory with a access token.
// tenantID: The Azure Active Directory tenant (directory) ID of the service principal.
// clientID: The client (application) ID of the service principal.
// clientSecret: A client secret that was generated for the App Registration used to authenticate the client.
// options: allow to configure the management of the requests sent to Azure Active Directory.
func NewStaticTokenCredential(tokenString string, accessToken *azcore.AccessToken) *StaticTokenCredential {
	if tokenString != "" {
		converted := &azcore.AccessToken{
			Token:     tokenString,
			ExpiresOn: time.Now().AddDate(0, 0, 1).UTC(),
		}
		return &StaticTokenCredential{accessToken: converted}
	}

	return &StaticTokenCredential{accessToken: accessToken}
}

// GetToken obtains a token from Azure Active Directory, using the specified client secret to authenticate.
// ctx: Context used to control the request lifetime.
// opts: TokenRequestOptions contains the list of scopes for which the token will have access.
// Returns an AccessToken which can be used to authenticate service client calls.
func (c *StaticTokenCredential) GetToken(ctx context.Context, opts azcore.TokenRequestOptions) (*azcore.AccessToken, error) {
	logGetTokenSuccess(c, opts)
	return c.accessToken, nil
}

func (c *StaticTokenCredential) AuthenticationPolicy(options azcore.AuthenticationPolicyOptions) azcore.Policy {
	return newBearerTokenPolicy(c, options)
}
