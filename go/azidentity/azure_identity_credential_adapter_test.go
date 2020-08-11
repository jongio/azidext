package azidentity

import (
	"context"
	"math/rand"
	"os"
	"strconv"
	"testing"
	"time"

	"github.com/Azure/azure-sdk-for-go/sdk/to"
	"github.com/Azure/azure-sdk-for-go/services/resources/mgmt/2019-05-01/resources"
	"github.com/joho/godotenv"
)

func Test_testCreateResouceGroup(t *testing.T) {
	err := godotenv.Load("../../.env")
	if err != nil {
		t.Fatalf("Loading environment variable from .env fail, error: %v", err)
	}
	subscriptionID := os.Getenv("AZURE_SUBSCRIPTION_ID")
	if subscriptionID == "" {
		t.Fatalf("Missing environment variable AZURE_SUBSCRIPTION_ID")
	}
	groupsClient := resources.NewGroupsClient(subscriptionID)
	a, err := NewAzureIdentityCredentialAapter(nil, nil)
	if err != nil {
		t.Fatalf("Create AzureIdentityTokenAapter fail, error: %v", err)
	}
	groupsClient.Authorizer = a
	resourceGroupname := "azidextrg" + strconv.FormatInt(int64(rand.New(rand.NewSource(time.Now().UnixNano())).Int31n(100000000)), 10)
	defer func() {
		if err := recover(); err != nil {
			t.Fatalf("Create ResourceGroup fail, error: %v", err)
		}
	}()
	_, err = groupsClient.CreateOrUpdate(
		context.Background(),
		resourceGroupname,
		resources.Group{
			Location: to.StringPtr("Central US"),
		})
	if err != nil {
		t.Fatalf("Create ResourceGroup fail, error: %v", err)
	}
}
