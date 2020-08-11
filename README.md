# Azure Identity Extensions

This repo is a place for us to share ideas and extensions to the Azure Identity libraries.

> **DISCLAIMER**: The code in this repo is not officially supported or intended for production use. The intention of this repo it to unblock customers who would like to use the Azure.Identity capabilities in the Fluent, Resource Management, and Service Bus SDKs before they have been migrated to the new SDK Azure.Core and officially support TokenCredential. We have included minimal tests in this repo, so please take it upon yourself to fully test this code to ensure it works in your environment.

## Languages

We currently have included examples for [.NET](#.NET), [Java](#Java), [JavaScript/TypeScript](#TypeScript), [Golang](#Golang), and [Python](#Python). Please file an issue if you would like examples for other languages as well.

## Usage

The classes contained in this repo are only meant to be a temporary stopgap between now and when the Resource Management, Fluent, and Service Bus SDKs support Azure.Core.  Since those efforts are currently underway, we think it would be best for you to copy the classes in this project to your class instead of releasing them via a package manager.

1. Clone the repo `git clone https://github.com/jongio/azidext`
1. Either reference the project or copy the classes you need into your solution.

## .NET

### AzureIdentityCredentialAdapter.cs

The `AzureIdentityCredentialAdapter` class allows you to use all the goodness of `Azure.Identity.DefaultAzureCredential` in the Azure Management libraries. You can use it in place of `ServiceClientCredential` when calling your Azure Management APIs. The Azure Management libraries will be updated to support Azure Identity and Azure Core in early 2020, so this should just be used a a stopgap between now and then.

```cmd
dotnet add package Microsoft.Azure.Management.ApplicationInsights --version 0.2.0-preview
```

Use `AzureIdentityCredentialAdapter` in place of `ServiceClientCredential`:

```csharp
using Azure.Identity.Extensions;
using Microsoft.Azure.Management.ApplicationInsights.Management;

var appInsightsClient = new ApplicationInsightsManagementClient(new AzureIdentityCredentialAdapter());
```

### AzureIdentityFluentCredentialAdapter.cs

The `AzureIdentityFluentCredentialAdapter` class allows you to use all the goodness of `Azure.Identity.DefaultAzureCredential` in the [Azure Management **Fluent** libraries](https://github.com/Azure/azure-libraries-for-net). You can use it in place of `AzureCredentials` when calling your Azure Management Fluent APIs.

```cmd
dotnet add package Microsoft.Azure.Management.Fluent --version 1.30.0
```

Use `AzureIdentityFluentCredentialAdapter` in place of `AzureCredentials`:

```csharp
using Azure.Identity.Extensions;
using Microsoft.Azure.Management.ResourceManager.Fluent;

var creds = new AzureIdentityFluentCredentialAdapter(tenantId, AzureEnvironment.AzureGlobalCloud);

var resourceGroup = Azure.Authenticate(creds)
                        .WithSubscription(subId)
                        .ResourceGroups
                        .Define(name)
                        .WithRegion(region)
                        .Create();
```

### AzureIdentityServiceBusCredentialAdapter.cs

The `AzureIdentityServiceBusCredentialAdapter` class allows you to use all of the goodness of `DefaultAzureCredential` from [azure-identity](https://mvnrepository.com/artifact/com.azure/azure-identity) with the Service Bus SDKs.  Service Bus will officially be supported by the new SDKs soon, this is a stopgap that enables you to use the same credential flow throughout your application.

```cmd
dotnet add package Microsoft.Azure.ServiceBus --version 4.1.1
```

```csharp
using Azure.Identity.Extensions;
using Microsoft.Azure.ServiceBus;

var client = new TopicClient("sbendpoint", "entitypath", new AzureIdentityServiceBusCredentialAdapter());
```

## Testing .NET

1. Setup test resources with "Test Setup" section below.
2. Open the .Tests project and run dotnet build.

## Java

### AzureIdentityCredentialAdapter.java

The `AzureIdentityCredentialAdapter` class provides a simple bridge to use `DefaultAzureCredential` from `com.azure` namespace in `com.microsoft.azure` SDKs. This is a convenient mechanism to authenticate all fluent Azure Management Resources and a some data plane SDKs that use `ServiceClientCredential` family of credentials.

To use this type, just copy `AzureIdentityCredentialAdapter.java` file located in `java/src/main/java/com/azure/identity/extensions` directory into your application and make necessary package name updates.

After you have created this type, you can reference it in your code as shown below:

```java
Azure azure = Azure.authenticate(new AzureIdentityCredentialAdapter(tenantId)).withDefaultSubscription();
```

Above code will provide an instance of `Azure` fluent type from which you can access all Azure Resource Managers.

### AzureIdentityServiceBusCredential.java

The `AzureIdentityServiceBusCredential` class allows you to use all of the goodness of `DefaultAzureCredential` from [azure-identity](https://mvnrepository.com/artifact/com.azure/azure-identity) with the Service Bus SDKs.  Service Bus will officially be supported by the new SDKs soon, this is a stopgap that enables you to use the same credential flow throughout your application.

To use this type, just copy `AzureIdentityServiceBusCredential.java` file located in `java/src/main/java/com/azure/identity/extensions` directory into your application and make necessary package name updates.

Sample code to create a new topic client:

```java
ClientSettings clientSettings = new ClientSettings(new AzureIdentityServiceBusCredential());
TopicClient topicClient = new TopicClient("servicebus-endpoint", "servicebus-entitypath", clientSettings);
```

#### Testing AzureIdentityCredentialAdapter

This repository has a test class called `AzureIdentityCredentialAdapterTest` that tests creation of a storage account, listing all storage accounts in a resource group to validate successful creation, then deleting the account created earlier in this test and listing again to ensure successful deletion.

To run `AzureIdentityCredentialAdapterTest`, ensure you have `.env` file created and accessible from your classpath. Your `.env` file should have the following properties set:

- AZURE_TENANT_ID
- AZURE_STORAGE_ACCOUNT_NAME
- AZURE_RESOURCE_GROUP

Once you have the `.env` file configured, run the test using JUnit 5 runner.

## TypeScript

### AzureIdentityCredentialAdapter

The `AzureIdentityCredentialAdapter` class provides a simple adapter to use DefaultAzureCredential from [@azure/identity](https://www.npmjs.com/package/@azure/identity) with any SDK
that accepts ServiceClientCredentials from packages like `@azure/arm-*` or `@azure/ms-rest-*`. 

To use this type, just copy `azureIdentityCredentialAdapter.ts`, `package.json`, and `tsconfig.json` file located in `js` directory into your application and install packages in `package.json`.

After you have created this type, you can reference it in your code as shown below:

```TypeScript
# Example for azure-mgmt-resource client
const cred = new AzureIdentityCredentialAdapter();
const client = new ResourceManagementClient(cred, subscriptionId);
```

The above code will instantiate an Azure.Identity compatible TokenCredential object based on DefaultAzureCredential and pass that to the ResourceManagementClient instance.

#### Testing AzureIdentityCredentialAdapter

This repository has a test that creates a resource group in a given subscription.

To run this test, ensure you have `.env` file created and accessible from the root of your repo. Your `.env` file should have the following properties set:

- AZURE_SUBSCRIPTION_ID
- AZURE_TENANT_ID
- AZURE_CLIENT_ID
- AZURE_CLIENT_SECRET

Install the test dependencies using npm under the path of `package.json`.

```
npm i
```
Then install mocha.

```
npm i -g mocha
```
compile ts to js using tsc. 

```
tsc azureIdentityCredentialAdapter.spec.ts --esModuleInterop
```

Once you have the `.env` file configured and js compiled, run the test simply calling `mocha azureIdentityCredentialAdapter.spec.js --timeout 10000`.

## Go

### NewAzureIdentityCredentialAdapter

The `NewAzureIdentityCredentialAdapter` function allows you to use all the goodness of `azidentity` in the Azure Management libraries. You can use it in place of `Authorizer` when calling your Azure Management APIs.

To use this type, just import package github.com/jongio/azidext/go/azidentity and using follow command to get package.

```
go get -u github.com/.............
```

Use `NewAzureIdentityCredentialAdapter` in place of `Authorizer`:

```go
import "github.com/jongio/azidext/go/azidentity"

groupsClient := resources.NewGroupsClient(subscriptionID)
	a, err := azidentity.NewAzureIdentityCredentialAapter()
	if err != nil {		
	}
	groupsClient.Authorizer = a
```

#### Testing NewAzureIdentityCredentialAdapter

This repository has a test that creates a resource group in a given subscription.

To run this test, ensure you have `.env` file created and accessible from the root of your repo. Your `.env` file should have the following properties set:

- AZURE_SUBSCRIPTION_ID
- AZURE_TENANT_ID
- AZURE_CLIENT_ID
- AZURE_CLIENT_SECRET

Once you have the `.env` file configured, run the test simply calling `go test`.

## Python

### AzureIdentityCredentialAdapter

The `AzureIdentityCredentialAdapter` class provides a simple adapter to use any credential from [azure-identity](https://pypi.org/project/azure-identity/) with any SDK
that accepts credentials from `azure.common.credentials` or `msrestazure.azure_active_directory`.

To use this type, just copy the `azure_identity_credential_adapter.py` file located in the `python` directory into your application and make necessary package name updates.

After you have created this type, you can reference it in your code as shown below:

```python
# Example for azure-mgmt-resource client
from azure_identity_credential_adapter import AzureIdentityCredentialAdapter
credentials = AzureIdentityCredentialAdapter()

from azure.mgmt.resource import ResourceManagementClient
client = ResourceManagementClient(credentials, subscription_id)
```

The above code will provide an instance of `ResourceManagementClient` from which you can access ARM resources. You can use any type of client, like `ComputeManagementClient`, etc.

#### Testing AzureIdentityCredentialAdapter

This repository has a test that list the resource groups in a given subscription.

To run this test, ensure you have `.env` file created and accessible from the root of your repo. Your `.env` file should have the following properties set:

- AZURE_SUBSCRIPTION_ID
- AZURE_TENANT_ID
- AZURE_CLIENT_ID
- AZURE_CLIENT_SECRET

General recommendation for Python development is to use a Virtual Environment. For more information, see https://docs.python.org/3/tutorial/venv.html

Install and initialize the virtual environment with the "venv" module on Python 3 (you must install [virtualenv](https://pypi.python.org/pypi/virtualenv) for Python 2.7):

```
python -m venv venv # Might be "python3" or "py -3.6" depending on your Python installation
source venv/bin/activate      # Linux shell (Bash, ZSH, etc.) only
./venv/scripts/activate       # PowerShell only
./venv/scripts/activate.bat   # Windows CMD only
```

Install the test dependencies using pip

```
pip install -r python\dev_requirements.txt
```

Once you have the `.env` file configured and the venv loaded, run the tests simply calling `pytest`


More to come soon.  Please file a GitHub issue with any questions/suggestions.


## Test Setup

1. Create a service principal with `az ad sp create-for-rbac`
2. Rename .env.tmp to .env and update the the following values from the SP

    `AZURE_CLIENT_ID=appId`

    `AZURE_CLIENT_SECRET=password`

    `AZURE_TENANT_ID=tenantId`

3. Run `az account show` to get your subscription id and update the .env file with that.

    `AZURE_SUBSCRIPTION_ID=`

4. Deploy the Service Bus resources with terraform files in iac/terraform

    - Open variables.tf and change the basename value to something unique.
    - Run the following commands:
        - `terraform init`
        - `terraform plan --out tf.plan`
        - `terraform apply tf.plan`

5. Update AZURE_BASE_NAME in .env file to the base name you used for terraform deployment

    - AZURE_BASE_NAME=azidexttest1


6. See each language "Test" section above for instructions on how to run the tests.
