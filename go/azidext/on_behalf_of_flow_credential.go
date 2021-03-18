// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azidext

import (
	"context"

	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
)

// OnBehalfOfFlowCredentialOptions configures the OnBehalfOfFlowCredential with optional parameters.
// All zero-value fields will be initialized with their default values.
type OnBehalfOfFlowCredentialOptions struct {
	// The host of the Azure Active Directory authority. The default is AzurePublicCloud.
	// Leave empty to allow overriding the value from the AZURE_AUTHORITY_HOST environment variable.
	AuthorityHost string
	// HTTPClient sets the transport for making HTTP requests
	// Leave this as nil to use the default HTTP transport
	HTTPClient azcore.Transport
	// Retry configures the built-in retry policy behavior
	Retry azcore.RetryOptions
	// Telemetry configures the built-in telemetry policy behavior
	Telemetry azcore.TelemetryOptions
	// Logging configures the built-in logging policy behavior.
	Logging azcore.LogOptions
}

// OnBehalfOfFlowCredential enables authentication.
type OnBehalfOfFlowCredential struct {
	client       *aadIdentityClient
	tenantID     string // Gets the Azure Active Directory tenant (directory) ID of the service principal
	clientID     string // Gets the client (application) ID of the service principal
	clientSecret string // Gets the client secret that was generated for the App Registration used to authenticate the client.
	accessToken  string
}

// NewOnBehalfOfFlowCredential constructs a new OnBehalfOfFlowCredential.
// tenantID: The Azure Active Directory tenant (directory) ID of the service principal.
// clientID: The client (application) ID of the service principal.
// clientSecret: A client secret that was generated for the App Registration used to authenticate the client.
// accessToken: The string of prefetched token.
// options: allow to configure the management of the requests sent to Azure Active Directory.
func NewOnBehalfOfFlowCredential(tenantID string, clientID string, clientSecret string, accessToken string, options *OnBehalfOfFlowCredentialOptions) (*OnBehalfOfFlowCredential, error) {
	if !validTenantID(tenantID) {
		return nil, &CredentialUnavailableError{credentialType: "On Behalf Of Flow Credential", message: tenantIDValidationErr}
	}
	if options == nil {
		options = &OnBehalfOfFlowCredentialOptions{}
	}
	authorityHost, err := setAuthorityHost(options.AuthorityHost)
	if err != nil {
		return nil, err
	}
	c, err := newAADIdentityClient(authorityHost, pipelineOptions{HTTPClient: options.HTTPClient, Retry: options.Retry, Telemetry: options.Telemetry, Logging: options.Logging})
	if err != nil {
		return nil, err
	}
	return &OnBehalfOfFlowCredential{tenantID: tenantID, clientID: clientID, clientSecret: clientSecret, accessToken: accessToken, client: c}, nil
}

// GetToken obtains a token from Azure Active Directory, using the specified client secret to authenticate.
// ctx: Context used to control the request lifetime.
// opts: TokenRequestOptions contains the list of scopes for which the token will have access.
// Returns an AccessToken which can be used to authenticate service client calls.
func (c *OnBehalfOfFlowCredential) GetToken(ctx context.Context, opts azcore.TokenRequestOptions) (*azcore.AccessToken, error) {
	tk, err := c.client.authenticateonbehalf(ctx, c.tenantID, c.clientID, c.clientSecret, c.accessToken, opts.Scopes)
	if err != nil {
		addGetTokenFailureLogs("On Behalf Of Flow Credential", err, true)
		return nil, err
	}
	logGetTokenSuccess(c, opts)
	return tk, nil
}

// AuthenticationPolicy implements the azcore.Credential interface on ClientSecretCredential and calls the Bearer Token policy
// to get the bearer token.
func (c *OnBehalfOfFlowCredential) AuthenticationPolicy(options azcore.AuthenticationPolicyOptions) azcore.Policy {
	return newBearerTokenPolicy(c, options)
}
