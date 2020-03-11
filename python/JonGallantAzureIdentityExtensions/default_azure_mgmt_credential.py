from azure.identity import DefaultAzureCredential
from azure.common.credentials import ServicePrincipalCredentials
import os
import time


class DefaultAzureMgmtCredential(ServicePrincipalCredentials):
    scopes = 'https://management.azure.com/.default'
    accessTokenCache = {}

    def __init__(self, client_id=None, secret=None, credentail=None, **kwargs):
        self.credentail = credentail
        if self.credentail is None:
            self.credentail = DefaultAzureCredential()
        super(DefaultAzureMgmtCredential, self).__init__(client_id, secret, **kwargs)

    def set_token(self):
        if self.accessTokenCache.get(self.resource) and self.accessTokenCache.get(self.resource).get('expiresOn') > int(
                time.time()):
            self.token = self.accessTokenCache.get(self.resource)
            return
        token_tuple = self.credentail.get_token(self.resource + '.default')
        self.token = {'expiresOn': token_tuple.expires_on, 'access_token': token_tuple.token,
                      'resource': self.resource}
        self.accessTokenCache[self.resource] = self.token