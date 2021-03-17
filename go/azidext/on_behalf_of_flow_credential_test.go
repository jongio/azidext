// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azidext

import (
	"context"
	"os"
	"testing"

	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
	"github.com/joho/godotenv"
)

func Test_ValidToken(t *testing.T) {
	err := godotenv.Load("../../.env")
	if err != nil {
		t.Fatalf("Loading environment variable from .env fail, error: %v", err)
	}
	expectedToken := os.Getenv("AZURE_TOKEN_STRING")
	if expectedToken == "" {
		t.Fatalf("Missing environment variable AZURE_TOKEN_STRING")
	}
	clientId := os.Getenv("AZURE_CLIENT_ID")
	if clientId == "" {
		t.Fatalf("Missing environment variable AZURE_CLIENT_ID")
	}
	clientSecret := os.Getenv("AZURE_CLIENT_SECRET")
	if clientSecret == "" {
		t.Fatalf("Missing environment variable AZURE_CLIENT_SECRET")
	}
	tenantId := os.Getenv("AZURE_TENANT_ID")
	if tenantId == "" {
		t.Fatalf("Missing environment variable AZURE_TENANT_ID")
	}
	credential, err := NewOnBehalfOfFlowCredential(tenantId, clientId, clientSecret, expectedToken, nil)
	if err != nil {
		t.Fatalf("Unable to create credential. Received: %v", err)
	}
	_, err = credential.GetToken(context.Background(), azcore.TokenRequestOptions{Scopes: []string{"https://vault.azure.net/.default"}})
	if err != nil {
		t.Fatalf("Unable to get token. Received: %v", err)
	}
}
