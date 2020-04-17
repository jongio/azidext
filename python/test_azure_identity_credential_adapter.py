# ------------------------------------
# Copyright (c) Microsoft Corporation.
# Licensed under the MIT License.
# ------------------------------------
import pytest


@pytest.fixture(scope="session", autouse=True)
def load_env():
    import os.path
    root_dir = os.path.abspath(os.path.join(os.path.abspath(__file__), "..", ".."))
    env_path = os.path.join(root_dir, ".env")
    if not os.path.exists(env_path):
        raise ValueError("Please create a .env file at the root of this repo using the .env.temp template: {}".format(root_dir))
    from dotenv import load_dotenv
    load_dotenv(dotenv_path=env_path)


def test_list_resource_group():
    from azure_identity_credential_adapter import AzureIdentityCredentialAdapter

    import os
    subscription_id = os.getenv("AZURE_SUBSCRIPTION_ID")

    credentials = AzureIdentityCredentialAdapter()

    from azure.mgmt.resource import ResourceManagementClient
    client = ResourceManagementClient(credentials, subscription_id)
    # Not raising any exception means we were able to do it
    rg_list = list(client.resource_groups.list())
