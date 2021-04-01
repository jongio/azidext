# ------------------------------------
# Copyright (c) Microsoft Corporation.
# Licensed under the MIT License.
# ------------------------------------
import pytest
from static_token_credential import StaticTokenCredential
from datetime import datetime, timedelta
from azure.core.credentials import AccessToken

class TestStaticTokenCredential(object):

    def test_valid_static_token_string(self):       
        expected_token="token1"
        credential = StaticTokenCredential(access_token=expected_token)
        actual_token = credential.get_token()
        assert expected_token == actual_token.token

    def test_valid_static_access_token(self):       
        expected_token=AccessToken(token="token1",expires_on=datetime.utcnow() + timedelta(days=1))
        credential = StaticTokenCredential(access_token=expected_token)
        actual_token = credential.get_token()
        assert expected_token.token == actual_token.token
        assert expected_token.expires_on == actual_token.expires_on