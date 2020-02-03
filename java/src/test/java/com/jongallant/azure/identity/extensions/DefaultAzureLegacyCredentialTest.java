package com.jongallant.azure.identity.extensions;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.StorageAccount;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rx.observers.TestSubscriber;

/**
 * Tests for {@link DefaultAzureLegacyCredential}. These tests run against live Azure services to ensure credentials
 * work correctly.
 */
public class DefaultAzureLegacyCredentialTest {

  private static final String AZURE_STORAGE_ACCOUNT = "AZURE_STORAGE_ACCOUNT_NAME";
  private static final String AZURE_TENANT_ID = "AZURE_TENANT_ID";
  private static final String AZURE_RESOURCE_GROUP = "AZURE_RESOURCE_GROUP";
  private static Dotenv ENVIRONMENT;

  /**
   * Loads environment variables from a properties file named ".env" that should be available in the classpath.
   *
   * Template for this file can be found at the root of this repository name .env.temp
   */
  @BeforeAll
  static void loadEnvironmentProperties() {
    ENVIRONMENT = Dotenv.load();
  }

  /**
   * Tests creating a new storage account, list all accounts and verify that the new account is part of the list and
   * then deletes the newly created account. This test requires the environment variables (either via .env file or
   * through system environment variables) to contain the tenant id, the resource group and storage account name that
   * can be used to create and delete.
   *
   * @throws Exception If there's any error during any of the operations.
   */
  @Test
  public void testStorageAccountCreation() throws Exception {
    String tenantId = ENVIRONMENT.get(AZURE_TENANT_ID);
    // Create an instance of fluent Azure type which can be used for managing various Azure resources
    Azure azure = Azure.authenticate(new DefaultAzureLegacyCredential(tenantId)).withDefaultSubscription();

    // create a test storage account
    StorageAccount createdAccount =
        azure.storageAccounts().define(ENVIRONMENT.get(AZURE_STORAGE_ACCOUNT)).withRegion(Region.US_WEST2)
            .withExistingResourceGroup(ENVIRONMENT.get(AZURE_RESOURCE_GROUP)).create();

    // list all storage accounts and verify that the test account created above is part of the list
    TestSubscriber<String> testCreateSubscriber = new TestSubscriber<>();
    azure.storageAccounts()
        .listAsync()
        .filter(account -> account.id().equals(createdAccount.id()))
        .map(account -> account.id())
        .subscribe(testCreateSubscriber);

    testCreateSubscriber.assertCompleted();
    testCreateSubscriber.assertNoErrors();
    testCreateSubscriber.assertValue(createdAccount.id());

    // delete the test storage account
    azure.storageAccounts().deleteByIdAsync(createdAccount.id()).get();

    // list all storage accounts and verify that the test storage account no longer exists
    TestSubscriber<StorageAccount> testDeleteSubscriber = new TestSubscriber<>();
    azure.storageAccounts()
        .listAsync()
        .filter(account -> account.id().equals(createdAccount.id()))
        .subscribe(testDeleteSubscriber);

    testDeleteSubscriber.assertCompleted();
    testDeleteSubscriber.assertNoErrors();
    testDeleteSubscriber.assertNoValues();
  }
}
