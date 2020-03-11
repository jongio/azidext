import os
import sys
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
import warnings
import unittest
from azure.mgmt.storage import StorageManagementClient
from JonGallantAzureIdentityExtensions.default_azure_mgmt_credential import DefaultAzureMgmtCredential
from azure.mgmt.storage.models import (
    StorageAccountCreateParameters,
    Sku,
    SkuName,
    Kind
)


class TestDefaultAzureMgmtCredential(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        warnings.simplefilter('ignore', ResourceWarning)
        subscription_id =  os.environ['AZURE_SUBSCRIPTION_ID']
        credential = DefaultAzureMgmtCredential()
        cls.storage_client = StorageManagementClient(credential, subscription_id)
        cls.RESOURCE_GROUP_NAME = os.environ['AZURE_RESOURCE_GROUP']
        cls.STORAGE_ACCOUNT_NAME = os.environ['AZURE_STORAGE_ACCOUNT_NAME']

    def test_storage_accounts(self):
        # -------------------------Create a storage account---------------------
        print('Create a storage account')
        result_create = self.storage_client.storage_accounts.create(
            self.RESOURCE_GROUP_NAME,
            self.STORAGE_ACCOUNT_NAME,
            StorageAccountCreateParameters(
                sku=Sku(name=SkuName.standard_ragrs),
                kind=Kind.storage,
                location='westus'
            )
        )
        storage_account = result_create.result()
        self.assertEqual(storage_account.name, self.STORAGE_ACCOUNT_NAME)

        # -----------------------List storage accounts by resource group----------
        print('List storage accounts by resource group')
        list_storage_account = self.storage_client.storage_accounts.list_by_resource_group(self.RESOURCE_GROUP_NAME)
        storage_account_id_list = [item.id for item in list_storage_account]
        self.assertIn(storage_account.id, storage_account_id_list)

        # -----------------------Delete the storage account------------------------
        print('Delete the storage account')
        self.storage_client.storage_accounts.delete(self.RESOURCE_GROUP_NAME, self.STORAGE_ACCOUNT_NAME)

        # -----------------------verify storage account exists---------------------
        print('list all storage accounts and verify that the test storage account no longer exists')
        list_storage_account = self.storage_client.storage_accounts.list_by_resource_group(self.RESOURCE_GROUP_NAME)
        storage_account_id_list = [item.id for item in list_storage_account]
        self.assertNotIn(storage_account.id, storage_account_id_list)


if __name__ == '__main__':
    unittest.main()