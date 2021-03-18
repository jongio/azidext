// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azidext

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"net/url"
	"path"
	"strings"
	"time"

	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
)

const (
	clientAssertionType = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
	onBehalfGrantType   = "ietf:params:oauth:grant-type:jwt-bearer"
)

const (
	qbAssertion           = "assertion"
	qpClientAssertionType = "client_assertion_type"
	qpClientAssertion     = "client_assertion"
	qpClientID            = "client_id"
	qpClientSecret        = "client_secret"
	qpCode                = "code"
	qpDeviceCode          = "device_code"
	qpGrantType           = "grant_type"
	qpPassword            = "password"
	qpRedirectURI         = "redirect_uri"
	qpRefreshToken        = "refresh_token"
	qbRequestToken        = "requested_token_use"
	qpResponseType        = "response_type"
	qpScope               = "scope"
	qpUsername            = "username"
)

// interactiveConfig stores the authorization code obtained from the interactive browser and redirect URI used in the initial request
type interactiveConfig struct {
	authCode     string
	codeVerifier string
	redirectURI  string
}

// aadIdentityClient provides the base for authenticating with Client Secret Credentials, Client Certificate Credentials
// and Environment Credentials. This type includes an azcore.Pipeline and TokenCredentialOptions.
type aadIdentityClient struct {
	authorityHost string
	pipeline      azcore.Pipeline
}

// newAADIdentityClient creates a new instance of the aadIdentityClient with the TokenCredentialOptions
// that are passed into it along with a default pipeline.
// options: TokenCredentialOptions that can configure policies for the pipeline and the authority host that
// will be used to retrieve tokens and authenticate
func newAADIdentityClient(authorityHost string, options pipelineOptions) (*aadIdentityClient, error) {
	logEnvVars()
	if options.Telemetry.Value == "" {
		options.Telemetry.Value = UserAgent
	} else {
		options.Telemetry.Value += " " + UserAgent
	}
	return &aadIdentityClient{authorityHost: authorityHost, pipeline: newDefaultPipeline(options)}, nil
}

// refreshAccessToken creates a refresh token request and returns the resulting Access Token or
// an error in case of an authentication failure.
// ctx: The current request context
// tenantID: The Azure Active Directory tenant (directory) ID of the service principal
// clientID: The client (application) ID of the service principal
// clientSecret: A client secret that was generated for the App Registration used to authenticate the client
// scopes: The scopes for the given access token
func (c *aadIdentityClient) refreshAccessToken(ctx context.Context, tenantID string, clientID string, clientSecret string, refreshToken string, scopes []string) (*tokenResponse, error) {
	req, err := c.createRefreshTokenRequest(ctx, tenantID, clientID, clientSecret, refreshToken, scopes)
	if err != nil {
		return nil, err
	}

	resp, err := c.pipeline.Do(req)
	if err != nil {
		return nil, err
	}

	if resp.HasStatusCode(successStatusCodes[:]...) {
		return c.createRefreshAccessToken(resp)
	}

	return nil, &AuthenticationFailedError{inner: newAADAuthenticationFailedError(resp)}
}

// authenticate creates a client secret authentication request and returns the resulting Access Token or
// an error in case of authentication failure.
// ctx: The current request context
// tenantID: The Azure Active Directory tenant (directory) ID of the service principal
// clientID: The client (application) ID of the service principal
// clientSecret: A client secret that was generated for the App Registration used to authenticate the client
// scopes: The scopes required for the token
func (c *aadIdentityClient) authenticate(ctx context.Context, tenantID string, clientID string, clientSecret string, scopes []string) (*azcore.AccessToken, error) {
	req, err := c.createClientSecretAuthRequest(ctx, tenantID, clientID, clientSecret, scopes)
	if err != nil {
		return nil, err
	}

	resp, err := c.pipeline.Do(req)
	if err != nil {
		return nil, err
	}

	if resp.HasStatusCode(successStatusCodes[:]...) {
		return c.createAccessToken(resp)
	}

	return nil, &AuthenticationFailedError{inner: newAADAuthenticationFailedError(resp)}
}

// authenticate creates a client secret authentication request and returns the resulting Access Token or
// an error in case of authentication failure.
// ctx: The current request context
// tenantID: The Azure Active Directory tenant (directory) ID of the service principal
// clientID: The client (application) ID of the service principal
// clientSecret: A client secret that was generated for the App Registration used to authenticate the client
// scopes: The scopes required for the token
func (c *aadIdentityClient) authenticateonbehalf(ctx context.Context, tenantID string, clientID string, clientSecret string, token string, scopes []string) (*azcore.AccessToken, error) {
	req, err := c.createonbehalfAuthRequest(ctx, tenantID, clientID, clientSecret, token, scopes)
	if err != nil {
		return nil, err
	}

	resp, err := c.pipeline.Do(req)
	if err != nil {
		return nil, err
	}

	if resp.HasStatusCode(successStatusCodes[:]...) {
		return c.createAccessToken(resp)
	}

	return nil, &AuthenticationFailedError{inner: newAADAuthenticationFailedError(resp)}
}

func (c *aadIdentityClient) createAccessToken(res *azcore.Response) (*azcore.AccessToken, error) {
	value := struct {
		Token     string      `json:"access_token"`
		ExpiresIn json.Number `json:"expires_in"`
		ExpiresOn string      `json:"expires_on"`
	}{}
	if err := res.UnmarshalAsJSON(&value); err != nil {
		return nil, fmt.Errorf("internal AccessToken: %w", err)
	}
	t, err := value.ExpiresIn.Int64()
	if err != nil {
		return nil, err
	}
	return &azcore.AccessToken{
		Token:     value.Token,
		ExpiresOn: time.Now().Add(time.Second * time.Duration(t)).UTC(),
	}, nil
}

func (c *aadIdentityClient) createRefreshAccessToken(res *azcore.Response) (*tokenResponse, error) {
	// To know more about refreshing access tokens please see: https://docs.microsoft.com/en-us/azure/active-directory/develop/v1-protocols-oauth-code#refreshing-the-access-tokens
	// DeviceCodeCredential uses refresh token, please see the authentication flow here: https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-device-code
	value := struct {
		Token        string      `json:"access_token"`
		RefreshToken string      `json:"refresh_token"`
		ExpiresIn    json.Number `json:"expires_in"`
		ExpiresOn    string      `json:"expires_on"`
	}{}
	if err := res.UnmarshalAsJSON(&value); err != nil {
		return nil, fmt.Errorf("internal AccessToken: %w", err)
	}
	t, err := value.ExpiresIn.Int64()
	if err != nil {
		return nil, err
	}
	accessToken := &azcore.AccessToken{
		Token:     value.Token,
		ExpiresOn: time.Now().Add(time.Second * time.Duration(t)).UTC(),
	}
	return &tokenResponse{token: accessToken, refreshToken: value.RefreshToken}, nil
}

func (c *aadIdentityClient) createRefreshTokenRequest(ctx context.Context, tenantID, clientID, clientSecret, refreshToken string, scopes []string) (*azcore.Request, error) {
	data := url.Values{}
	data.Set(qpGrantType, "refresh_token")
	data.Set(qpClientID, clientID)
	// clientSecret is only required for web apps. To know more about refreshing access tokens please see: https://docs.microsoft.com/en-us/azure/active-directory/develop/v1-protocols-oauth-code#refreshing-the-access-tokens
	if len(clientSecret) != 0 {
		data.Set(qpClientSecret, clientSecret)
	}
	data.Set(qpRefreshToken, refreshToken)
	data.Set(qpScope, strings.Join(scopes, " "))
	dataEncoded := data.Encode()
	body := azcore.NopCloser(strings.NewReader(dataEncoded))
	req, err := azcore.NewRequest(ctx, http.MethodPost, azcore.JoinPaths(c.authorityHost, tenantID, tokenEndpoint(oauthPath(tenantID))))
	if err != nil {
		return nil, err
	}
	if err := req.SetBody(body, azcore.HeaderURLEncoded); err != nil {
		return nil, err
	}
	return req, nil
}

func (c *aadIdentityClient) createClientSecretAuthRequest(ctx context.Context, tenantID string, clientID string, clientSecret string, scopes []string) (*azcore.Request, error) {
	data := url.Values{}
	data.Set(qpGrantType, "client_credentials")
	data.Set(qpClientID, clientID)
	data.Set(qpClientSecret, clientSecret)
	data.Set(qpScope, strings.Join(scopes, " "))
	dataEncoded := data.Encode()
	body := azcore.NopCloser(strings.NewReader(dataEncoded))
	req, err := azcore.NewRequest(ctx, http.MethodPost, azcore.JoinPaths(c.authorityHost, tenantID, tokenEndpoint(oauthPath(tenantID))))
	if err != nil {
		return nil, err
	}
	if err := req.SetBody(body, azcore.HeaderURLEncoded); err != nil {
		return nil, err
	}

	return req, nil
}

func (c *aadIdentityClient) createonbehalfAuthRequest(ctx context.Context, tenantID string, clientID string, clientSecret string, token string, scopes []string) (*azcore.Request, error) {
	data := url.Values{}
	data.Set(qpGrantType, onBehalfGrantType)
	data.Set(qpClientID, clientID)
	data.Set(qpClientSecret, clientSecret)
	data.Set(qbAssertion, token)
	data.Set(qpScope, strings.Join(scopes, " "))
	data.Set(qbRequestToken, "on_behalf_of")
	dataEncoded := data.Encode()
	body := azcore.NopCloser(strings.NewReader(dataEncoded))
	req, err := azcore.NewRequest(ctx, http.MethodPost, azcore.JoinPaths(c.authorityHost, tenantID, tokenEndpoint(oauthPath(tenantID))))
	if err != nil {
		return nil, err
	}
	if err := req.SetBody(body, azcore.HeaderURLEncoded); err != nil {
		return nil, err
	}

	return req, nil
}

// authenticateUsernamePassword creates a client username and password authentication request and returns an Access Token or
// an error.
// ctx: The current request context
// tenantID: The Azure Active Directory tenant (directory) ID of the service principal
// clientID: The client (application) ID of the service principal
// username: User's account username
// password: User's account password
// scopes: The scopes required for the token
func (c *aadIdentityClient) authenticateUsernamePassword(ctx context.Context, tenantID string, clientID string, username string, password string, scopes []string) (*azcore.AccessToken, error) {
	req, err := c.createUsernamePasswordAuthRequest(ctx, tenantID, clientID, username, password, scopes)
	if err != nil {
		return nil, err
	}

	resp, err := c.pipeline.Do(req)
	if err != nil {
		return nil, err
	}

	if resp.HasStatusCode(successStatusCodes[:]...) {
		return c.createAccessToken(resp)
	}

	return nil, &AuthenticationFailedError{inner: newAADAuthenticationFailedError(resp)}
}

func (c *aadIdentityClient) createUsernamePasswordAuthRequest(ctx context.Context, tenantID string, clientID string, username string, password string, scopes []string) (*azcore.Request, error) {
	data := url.Values{}
	data.Set(qpResponseType, "token")
	data.Set(qpGrantType, "password")
	data.Set(qpClientID, clientID)
	data.Set(qpUsername, username)
	data.Set(qpPassword, password)
	data.Set(qpScope, strings.Join(scopes, " "))
	dataEncoded := data.Encode()
	body := azcore.NopCloser(strings.NewReader(dataEncoded))
	req, err := azcore.NewRequest(ctx, http.MethodPost, azcore.JoinPaths(c.authorityHost, tenantID, tokenEndpoint(oauthPath(tenantID))))
	if err != nil {
		return nil, err
	}
	if err := req.SetBody(body, azcore.HeaderURLEncoded); err != nil {
		return nil, err
	}
	return req, nil
}

func (c *aadIdentityClient) createDeviceCodeNumberRequest(ctx context.Context, tenantID string, clientID string, scopes []string) (*azcore.Request, error) {
	data := url.Values{}
	data.Set(qpClientID, clientID)
	data.Set(qpScope, strings.Join(scopes, " "))
	dataEncoded := data.Encode()
	body := azcore.NopCloser(strings.NewReader(dataEncoded))
	// endpoint that will return a device code along with the other necessary authentication flow parameters in the DeviceCodeResult struct
	req, err := azcore.NewRequest(ctx, http.MethodPost, azcore.JoinPaths(c.authorityHost, tenantID, path.Join(oauthPath(tenantID), "/devicecode")))
	if err != nil {
		return nil, err
	}
	if err := req.SetBody(body, azcore.HeaderURLEncoded); err != nil {
		return nil, err
	}
	return req, nil
}

// authenticateAuthCode requests an Access Token with the authorization code and returns the token or an error in case of authentication failure.
// ctx: The current request context.
// tenantID: The Azure Active Directory tenant (directory) ID of the service principal.
// clientID: The client (application) ID of the service principal.
// authCode: The authorization code received from the authorization code flow. The authorization code must not have been used to obtain another token.
// clientSecret: Gets the client secret that was generated for the App Registration used to authenticate the client.
// redirectURI: The redirect URI that was used to request the authorization code. Must be the same URI that is configured for the App Registration.
// scopes: The scopes required for the token
func (c *aadIdentityClient) authenticateAuthCode(ctx context.Context, tenantID, clientID, authCode, clientSecret, codeVerifier, redirectURI string, scopes []string) (*azcore.AccessToken, error) {
	req, err := c.createAuthorizationCodeAuthRequest(ctx, tenantID, clientID, authCode, clientSecret, codeVerifier, redirectURI, scopes)
	if err != nil {
		return nil, err
	}

	resp, err := c.pipeline.Do(req)
	if err != nil {
		return nil, err
	}

	if resp.HasStatusCode(successStatusCodes[:]...) {
		return c.createAccessToken(resp)
	}

	return nil, &AuthenticationFailedError{inner: newAADAuthenticationFailedError(resp)}
}

// createAuthorizationCodeAuthRequest creates a request for an Access Token for authorization_code grant types.
func (c *aadIdentityClient) createAuthorizationCodeAuthRequest(ctx context.Context, tenantID, clientID, authCode, clientSecret, codeVerifier, redirectURI string, scopes []string) (*azcore.Request, error) {
	data := url.Values{}
	data.Set(qpGrantType, "authorization_code")
	data.Set(qpClientID, clientID)
	if clientSecret != "" {
		data.Set(qpClientSecret, clientSecret) // only for web apps
	}
	if codeVerifier != "" {
		// used during interactive browser auth
		data.Set("code_verifier", codeVerifier)
	}
	data.Set(qpRedirectURI, redirectURI)
	data.Set(qpScope, strings.Join(scopes, " "))
	data.Set(qpCode, authCode)
	dataEncoded := data.Encode()
	body := azcore.NopCloser(strings.NewReader(dataEncoded))
	req, err := azcore.NewRequest(ctx, http.MethodPost, azcore.JoinPaths(c.authorityHost, tenantID, tokenEndpoint(oauthPath(tenantID))))
	if err != nil {
		return nil, err
	}
	if err := req.SetBody(body, azcore.HeaderURLEncoded); err != nil {
		return nil, err
	}
	return req, nil
}
