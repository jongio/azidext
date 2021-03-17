// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azidext

import (
	"context"
	"testing"

	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
)

func Test_ValidTokenString(t *testing.T) {
	expectedToken := "token"
	credential := NewStaticTokenCredential(expectedToken, nil)
	actualToken, _ := credential.GetToken(context.Background(), azcore.TokenRequestOptions{Scopes: []string{"https://default.mock.auth.scope/.default"}})
	if expectedToken != actualToken.Token {
		t.Fatal("the token is not expected")
	}
}
