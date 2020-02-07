# Azure Identity Extensions

This repo is a place for us to share ideas and extensions to the Azure Identity libraries.

> **DISCLAIMER**: The code in this repo is not officially supported or intended for production use. The intention of this repo it to unblock customers who would like to use the DefaultAzureCredential capabilities in the Fluent, Management, and ServiceBus SDKs before they have been migrated to the new SDK Azure.Core and officially support TokenCredentials. We have included minimal tests in this repo, so please take it upon yourself to fully test this code to ensure it works in your environment.

## Languages

We currently have included examples for [.NET](#.NET) and [Java](#Java).  Please file an issue if you would like examples for other languages as well.

## Usage

The classes contained in this repo are only meant to be a temporary stopgap between now and when the Management, Fluent, and ServiceBus SDKs support Azure.Core.  Since those efforts are currently underway, we think it would be best for you to copy the classes in this project to your class instead of releasing them via a package manager.

1. Clone the repo `git clone https://github.com/jongio/azidext`
1. Either reference the project or copy the classes you need into your solution.

## .NET

### DefaultAzureMgmtCredential.cs

The `DefaultAzureMgmtCredential` class allows you to use all the goodness of `Azure.Identity.DefaultAzureCredential` in the Azure Management libraries. You can use it in place of `ServiceClientCredential` when calling your Azure Management APIs. The Azure Management libraries will be updated to support Azure Identity and Azure Core in early 2020, so this should just be used a a stopgap between now and then.

```cmd
dotnet add package Microsoft.Azure.Management.ApplicationInsights --version 0.2.0-preview
```

Use DefaultAzureMgmtCredential in place of ServiceClientCredential:

```csharp
using JonGallant.Azure.Identity.Extensions;
using Microsoft.Azure.Management.ApplicationInsights.Management;

var appInsightsClient = new ApplicationInsightsManagementClient(new DefaultAzureMgmtCredential());
```

### DefaultAzureFluentCredential.cs

The `DefaultAzureFluentCredential` class allows you to use all the goodness of `Azure.Identity.DefaultAzureCredential` in the [Azure Management **Fluent** libraries](https://github.com/Azure/azure-libraries-for-net). You can use it in place of `AzureCredentials` when calling your Azure Management Fluent APIs.

```cmd
dotnet add package Microsoft.Azure.Management.Fluent --version 1.30.0
```

Use `DefaultAzureFluentCredential` in place of `AzureCredentials`:

```csharp
using JonGallant.Azure.Identity.Extensions;
using Microsoft.Azure.Management.ResourceManager.Fluent;

var creds = new DefaultAzureFluentCredential(tenantId, AzureEnvironment.AzureGlobalCloud);

var resourceGroup = Azure.Authenticate(creds)
                        .WithSubscription(subId)
                        .ResourceGroups
                        .Define(name)
                        .WithRegion(region)
                        .Create();
```

### DefaultAzureServiceBusCredential.cs

The `DefaultAzureServiceBusCredential` class allows you to use all of the goodness of `Azure.Identity.DefaultAzureCredential` with the Service Bus SDKs.  Service Bus will officially be supported by the new SDKs soon, this is a stopgap that enables you to use the same credential flow throughout your application.

```cmd
dotnet add package Microsoft.Azure.ServiceBus --version 4.1.1
```

```csharp
using JonGallant.Azure.Identity.Extensions;
using Microsoft.Azure.ServiceBus;

var client = new TopicClient("sbendpoint", "entitypath", new DefaultAzureServiceBusCredential());
```

## Java

### DefaultAzureServiceBusCredential.java

The `DefaultAzureServiceBusCredential` class allows you to use all of the goodness of `Azure.Identity.DefaultAzureCredential` with the Service Bus SDKs.  Service Bus will officially be supported by the new SDKs soon, this is a stopgap that enables you to use the same credential flow throughout your application.

To use this type, just copy `DefaultAzureServiceBusCredential.java` file located in `java/src/main/java/com/jongallant/azure/identity/extensions` directory into your application and make necessary package name updates.

Sample code to create a new topic client:

```java
ClientSettings clientSettings = new ClientSettings(new DefaultAzureServiceBusCredential());
TopicClient topicClient = new TopicClient("servicebus-endpoint", "servicebus-entitypath", clientSettings);
```

### DefaultAzureCredentialAdapter.java

The `DefaultAzureCredentialAdapter` class provides a simple bridge to use `DefaultAzureCredential` from `com.azure` namespace in `com.microsoft.azure` SDKs. This is a convenient mechanism to authenticate all fluent Azure Management Resources and a some data plane SDKs that use `ServiceClientCredential` family of credentials.

To use this type, just copy `DefaultAzureCredentialAdapter.java` file located in `java/src/main/java/com/jongallant/azure/identity/extensions`directory into your application and make necessary package name updates.

After you have created this type, you can reference it in your code as shown below:

```java
Azure azure = Azure.authenticate(new DefaultAzureCredentialAdapter(tenantId)).withDefaultSubscription();
```

Above code will provide an instance of `Azure` fluent type from which you can access all Azure Resource Managers.

#### Testing DefaultAzureCredentialAdapter

This repository has a test class called `DefaultAzureCredentailAdapterTest` that tests creation of a storage account, listing all storage accounts in a resource group to validate successful creation, then deleting the account created earlier in this test and listing again to ensure successful deletion.

To run `DefaultAzureCredentailAdapterTest`, ensure you have `.env` file created and accessible from your classpath. Your `.env` file should have the following properties set:

- AZURE_TENANT_ID
- AZURE_STORAGE_ACCOUNT_NAME
- AZURE_RESOURCE_GROUP

Once you have the `.env` file configured, run the test using JUnit 5 runner.

More to come soon.  Please file a GitHub issue with any questions/suggestions.
