# ------------------------------------
# Copyright (c) Microsoft Corporation.
# Licensed under the MIT License.
# ------------------------------------
"""Demonstrates OnBehalfOf credentiall implementation"""

import time
from typing import TYPE_CHECKING

from azure.core.credentials import AccessToken
from azure.core.exceptions import ClientAuthenticationError
from azure.identity import AzureAuthorityHosts
import msal

if TYPE_CHECKING:
    from typing import Any, Union

class OnBehalfOfCredential(object):

    def __init__(self, tenant_id, client_id, client_secret, user_access_token):
        # type: (str, str, str, str) -> None
        self._confidential_client = msal.ConfidentialClientApplication(
            client_id=client_id,
            client_credential=client_secret,
            authority="https://{}/{}".format(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD, tenant_id)
        )
        self._user_token = user_access_token

    def get_token(self, *scopes, **kwargs):
        # type: (*str, **Any) -> AccessToken

        now = int(time.time())
        result = self._confidential_client.acquire_token_on_behalf_of(
            user_assertion=self._user_token, scopes=list(scopes)
        )

        if result and "access_token" in result and "expires_in" in result:
            return AccessToken(result["access_token"], now + int(result["expires_in"]))

        raise ClientAuthenticationError(
            message="Authentication failed: {}".format(result.get("error_description") or result.get("error"))
        )