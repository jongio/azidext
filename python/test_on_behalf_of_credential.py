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


def test_on_behalf_of_credential(): 
    from on_behalf_of_credential import OnBehalfOfCredential
    from azure.core.credentials import AccessToken   
    import os

    expected_token = os.getenv("Azure_TOKEN_STRING")
    tenant_id = os.getenv("AZURE_TENANT_ID")
    client_id = os.getenv("AZURE_CLIENT_ID")
    client_secret = os.getenv("AZURE_CLIENT_SECRET")
    credential = OnBehalfOfCredential(tenant_id, client_id, client_secret, expected_token)
    actual_token = credential.get_token("https://vault.azure.net/.default")
    assert actual_token.token