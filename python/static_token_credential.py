# ------------------------------------
# Copyright (c) Microsoft Corporation.
# Licensed under the MIT License.
# ------------------------------------
"""Demonstrates static token credential implementation"""

from typing import TYPE_CHECKING

from azure.core.credentials import AccessToken
from datetime import datetime, timedelta
if TYPE_CHECKING:
    from typing import Any, Union


class StaticTokenCredential(object):

    def __init__(self, access_token):
        # type: (Union[str, AccessToken]) -> None
        if isinstance(access_token, AccessToken):
            self._token = access_token
        else:
            self._token = AccessToken(token=access_token, expires_on=datetime.utcnow() + timedelta(days=1))

    def get_token(self, *scopes, **kwargs):
        # type: (*str, **Any) -> AccessToken
        """get_token is the only method a credential must implement"""

        return self._token