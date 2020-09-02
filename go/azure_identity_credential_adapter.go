// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azidext

import (
	"errors"
	"net/http"

	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
	"github.com/Azure/azure-sdk-for-go/sdk/azidentity"
	"github.com/Azure/go-autorest/autorest"
)

// NewAzureIdentityCredentialAdapter is used to adapt an azcore.Credential to an autorest.Authorizer
func NewAzureIdentityCredentialAdapter(credential azcore.Credential, options azcore.AuthenticationPolicyOptions) autorest.Authorizer {
	policy := credential.AuthenticationPolicy(options)
	return &policyAdapter{p: policy}
}

type policyAdapter struct {
	p azcore.Policy
}

// WithAuthorization implements the autorest.Authorizer interface for type policyAdapter.
func (ca *policyAdapter) WithAuthorization() autorest.PrepareDecorator {
	return func(p autorest.Preparer) autorest.Preparer {
		return autorest.PreparerFunc(func(r *http.Request) (*http.Request, error) {
			r, err := p.Prepare(r)
			if err != nil {
				return r, err
			}
			_, err = ca.p.Do(r.Context(), &azcore.Request{Request: r})
			if errors.Is(err, azcore.ErrNoMorePolicies) {
				return r, nil
			}
			var afe *azidentity.AuthenticationFailedError
			if errors.As(err, &afe) {
				err = &tokenRefreshError{
					inner: afe,
				}
			}
			return r, err
		})
	}
}

type tokenRefreshError struct {
	inner error
}

func (t *tokenRefreshError) Error() string {
	return t.inner.Error()
}

func (t *tokenRefreshError) Response() *http.Response {
	return nil
}

func (t *tokenRefreshError) Unwrap() error {
	return t.inner
}

// DefaultManagementScope is the default credential scope for Azure Resource Management.
const DefaultManagementScope = "https://management.azure.com//.default"

// DefaultAzureCredentialOptions contains credential and authentication policy options.
type DefaultAzureCredentialOptions struct {
	// DefaultCredential contains configuration options passed to azidentity.NewDefaultAzureCredential().
	// Set this to nil to accept the underlying default behavior.
	DefaultCredential *azidentity.DefaultAzureCredentialOptions

	// AuthenticationPolicy contains configuration options passed to the underlying authentication policy.
	// Setting this to nil will use the DefaultManagementScope when acquiring a token.
	AuthenticationPolicy *azcore.AuthenticationPolicyOptions
}

// NewDefaultAzureCredentialAdapter adapts azcore.NewDefaultAzureCredential to an autorest.Authorizer.
func NewDefaultAzureCredentialAdapter(options *DefaultAzureCredentialOptions) (autorest.Authorizer, error) {
	if options == nil {
		options = &DefaultAzureCredentialOptions{
			AuthenticationPolicy: &azcore.AuthenticationPolicyOptions{
				Options: azcore.TokenRequestOptions{
					Scopes: []string{DefaultManagementScope},
				},
			},
		}
	}
	chain, err := azidentity.NewDefaultAzureCredential(options.DefaultCredential)
	if err != nil {
		return nil, err
	}
	return NewAzureIdentityCredentialAdapter(chain, *options.AuthenticationPolicy), nil
}
