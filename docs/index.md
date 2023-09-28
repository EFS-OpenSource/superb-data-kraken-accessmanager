# AccessManager Service

The AccessManager is a service of the Superb Data Kraken that can create Shared-Access-Signature-Tokens (SAS) for accessing Azure Storage.

### Installing

Execute the following steps to set up your local environment for development and testing:

- Clone the repository
- [Connect to Azure Artifacts-Feed](https://dev.azure.com/EFSCIS/EFS-SDK/_packaging?_a=connect&feed=sdk-snapshots) -> Select Maven and edit your ```settings.xml``` according to the instructions
- copy-paste application-local-template.yml into application-local.yml and check for necessary adjustments to your environment.
  In particular, please replace the eventhub connection string.
- set "Active profiles" to "local" within your IDE
- Run "AccessManagerApplication" (preferably through your IDE)
- access swagger under http://localhost:8090/accessmanager/swagger-ui/index.html

## Usage

Commands that are required in order to use the service.

- <code>mvn package</code> to build the service
- <code>mvn test</code> to execute the tests
- <code>mvn spring-boot:run</code> to run a spring service
- <code>docker build</code> for building the docker container

## Deployment

For deployment push the service to Azure DevOps, the pipeline will start automatically for development (deploys to development environment) and master (deploys to production environment) branches. For feature branches, please start it manually. The deployment manifest is [azure-pipeline-prod.yml](azure-pipeline-prod.yml) for production environment (prod cluster) and [azure-pipeline-dev.yml](azure-pipeline-dev.yml) for development environment (dev cluster).

### Build/Deployment steps:
- Build and Push: an image is built using the [Dockerfile](Dockerfile) and pushed to the corresponding ACR (SDK or AICloud).
- Deployment: kubernetes manifests are deployed to the corresponding AKS (SDK or AICloud):
    - [config-map.yml](kubernetes/config-map.yml) writes the spring boot configuration application.yml as a config map
    - [rbac.yml](kubernetes/rbac.yml) gives permission for backend namespace
    - [deployment.yml](kubernetes/deployment.yml)  yields the k8 deployment "accessmanager", i.e. describes the desired state for Pods and ReplicaSets
    - [service.yml](kubernetes/service.yml) yields the corresponding k8 service "accessmanager", i.e. an abstract way to expose an application running on a set of Pods as a network service.
    - [ingress.yml](kubernetes/ingress.yml) yields the ingress "accessmanager" to the service, i.e. manages external http access to the service in the cluster via the public IP https://efs-aicloud.westeurope.cloudapp.azure.com/sdk-frontend/
- When deploying to the development environment (dev cluster) the kubernetes manifests that are applied are set up for dev using extra variables (i.e. `postfix`) or files (i.e. `ingress-dev.yml`)

### Service connections
For setting up the pipelines, the following service connections are needed in Azure Devops -> Project Settings:

#### Docker Registry service connection
- for SDK tenant: sc-efs-sdk-acrsdk (type: Azure Container Registry).
    - NOTE: dev environment uses the same registry from SDK tenant

- for AICloud tenant: sc-efs-sdk-acraicloud (type: others)
    - docker registry: https://acraicloud.azurecr.io/
    - docker id: acraicloud
    - docker password: obtained from portal -> ACR -> access keys -> enable admin user -> copy password

#### Kubernetes service connection
- for SDK tenant:
    - sc-efs-sdk-aks-sdk_devops (prod environment)
    - sc-efs-sdk-aks-sdk-dev_devops (dev environment)
- for AICloud tenant:
    - sc-efs-sdk-aks-aicloud_devops

Both are of type Service Account and have the following parameters
- server url: obtained (as described in Azure DevOps) from
  ```bash
  kubectl config view --minify -o jsonpath={.clusters[0].cluster.server}
  ```
- secret: obtained from
    ```bash
  kubectl get serviceAccounts <service-account-name> -n <namespace> -o=jsonpath={.secrets[*].name}
  ```
  where namespace is default and the service account is e.g. appreg-aicloud-aks-main.
---
### Pipeline Variables
the following pipeline variables are required:

| name                            | example                                        |
|---------------------------------|------------------------------------------------| 
| dockerRegistryServiceConnection | sc-efs-sdk-acraicloud                          |
| kubernetesServiceConnection     | sc-efs-sdk-aks-aicloud_devops                  |
| environment                     | aicloud                                        |
| issuers                         | https://aicloud.efs.ai/auth/realms/efs-aicloud |
| main-domain                     | aicloud.efs.ai                                 |

The container registry service connection is established during pipeline creation.

## Built With

- Maven v3.6.3 (see this [Link](https://maven.apache.org/))

## Contributing

See the [Contribution Guide](CONTRIBUTING.md).

## Changelog

See the [Changelog](CHANGELOG.md).

## Documentation

### Storage-Organization

Storage is organized in organizations and spaces, where a space represents a use-case and an organization packages use-cases. Technically, an organization is a Storage Account, whereas a space is a Container. Each organization has a dedicated space called 'loadingzone', which serves as container for incoming data. After processing, this data will be moved to the main-storage (target-space) - however this is out of accessmanager's scope.

![Storage-Organization](docs/images/storage-organization.png)

#### Deprecated endpoints for managing organizations and spaces

The functionality for managing organizations and spaces was moved to the organizationmanager backend service. Endpoints were kept in accessmanager for backwards compatibility but are deprecated. They recall the corresponding endpoints of the organizationmanager (see there to get more detailed information to the endpoints).

- *deprecated* ```POST /api/v1.0/organization``` Create a new organization plus space 'loadingzone'
- *deprecated* ```GET /api/v1.0/organization?permissions={permissions}``` Get all organizations (optionally filtered by given permissions)
- *deprecated* ```PUT /api/v1.0/organization/{id}``` Update the given organization
- *deprecated* ```GET /api/v1.0/organization/{id}``` Get all information to given organization
- *deprecated* ```GET /api/v1.0/organization/name/{name}``` Get all information to given organization
- *deprecated* ```POST /api/v1.0/space/{orgaId}``` Create a new space within the given organization
- *deprecated* ```GET /api/v1.0/space/{orgaId}?permissions={permissions}``` Get all spaces within the given organization (optionally filtered by given permissions)
- *deprecated* ```PUT /api/v1.0/space/{orgaId}/{spaceId}``` Update the given space within the given organization
- *deprecated* ```GET /api/v1.0/space/{orgaId}/{spaceId}``` Get all information to given space within the given organization
- *deprecated* ```GET /api/v1.0/space/{orgaId}/name/{spaceName}``` Get all information to given space within the given organization

#### Security

In order to access storage-instances (be it organization or space) in a read-only-manner, one must have the role ```org_<organization>_access```, in case of public organizations ```org_all_public``` should be enough.

To edit a storage-instance one must have ```org_<organization>_admin``` (independent of the confidentiality).

### Stored Access Signatures (Azure)

This service provides Endpoints for creating Shared Access Signatures.

- ```POST /api/v2.0/accessmanager/read?organization={orgaName}&space={spaceName}``` Generating Shared Access Signature-Token for read-actions, which consists of list and read permissions.
- ```POST /api/v2.0/accessmanager/upload?organization={orgaName}&space={spaceName}``` Generating Shared Access Signature-Token for upload-actions, which expands download-actions by create and add permissions.
- ```POST /api/v2.0/accessmanager/upload/main?organization={orgaName}&space={spaceName}``` Generating Shared Access Signature-Token for upload-actions to main-storage, which expands download-actions by create and add permissions. Only viable with DELETE-Permission(!).
- ```POST /api/v2.0/accessmanager/delete?organization={orgaName}&space={spaceName}``` Generating Shared Access Signature-Token for delete-actions, which expands upload-actions by delete permissions.
- ```POST /api/v2.0/accessmanager/commit?organization={orgaName}&space={spaceName}&rootDir={rootDir}``` Signalizes, that all file-transaction is complete and notifies further processing. Return files within the given Storage-Container within the root directory.

#### Security

The rights for each endpoint consider the SDK-roles/rights concept, where the following roles are defined:

| Role     | Description                                               |
|----------|-----------------------------------------------------------|
| user     | can list and read/download data                           |
| supplier | user-rights, plus upload data and delete from loadingzone |
| trustee  | supplier-rights, plus delete data                         |

The logic for checking the user rights is handled by the organizationmanager. Its endpoints to list organizations and spaces by requested permission (READ, WRITE, DELETE) are used here to check if users are allowed to create read/upload/delete SAS tokens. Here's an overview of how the rights are being checked by organizationmanager when listing spaces by permission:

![SAS-Rights](docs/images/sas-right-validation.png)

Additionally, to the rights mentioned in the concept there is the role 'CreateContainer': users with this role are allowed to create new Storage-Containers. This behaviour depends on a certain [configuration](#create-container).

However all right-checks (save for CreateContainer) will be bypassed, if the user has right ```SDK_ADMIN```. This marks a superuser-role - users with that role will have access to everything.

##### Security-Examples

In case a user wants to store data into a PRIVATE/INTERNAL ```space1``` in ```orga1```, the following role-combinations are valid:

* ```org_orga1_access``` plus ```spc_space1_supplier```
* ```org_orga1_access``` plus ```spc_space1_trustee```

In case a user wants to read data from a PUBLIC ```space1``` in ```orga1```, the following role-combinations are valid:

* ```org_orga1_access``` plus ```spc_space1_user```
* ```org_orga1_access``` plus ```spc_space1_supplier```
* ```org_orga1_access``` plus ```spc_space1_trustee```
* ```org_all_public``` plus ```spc_space1_user```
* ```org_all_public``` plus ```spc_space1_supplier```
* ```org_all_public``` plus ```spc_space1_trustee```
* ```org_orga1_access``` plus ```spc_all_public```
* ```org_orga1_access``` plus ```spc_all_public```
* ```org_orga1_access``` plus ```spc_all_public```

However, if a user wants to upload or delete from PUBLIC ```space1``` in ```orga1```, ```<XX>_all_public``` will not be enough:
* ```org_orga1_access``` plus ```spc_space1_supplier``` (in case of upload)
* ```org_orga1_access``` plus ```spc_space1_trustee``` (in case of upload or delete)

A special case is the loading zone as a space, where deletion is even possible for "suppliers", i.e., with read-access only. Being a "trustee", i.e., having explicit deletion rights, is not required.

#### Deprecated functionality

Version 1.0 of this service provided the following endpoints:

- ```POST /api/v1.0/accessmanager/read?connectionId={connectionId}&containerName={containerName}``` Generating Shared Access Signature-Token for read-actions, which consists of list and read permissions.
- ```POST /api/v1.0/accessmanager/upload?connectionId={connectionId}&containerName={containerName}``` Generating Shared Access Signature-Token for upload-actions, which expands download-actions by create and add permissions.
- ```POST /api/v1.0/accessmanager/delete?connectionId={connectionId}&containerName={containerName}``` Generating Shared Access Signature-Token for delete-actions, which expands upload-actions by delete permissions.
- ```POST /api/v1.0/accessmanager/commit?connectionId={connectionId}&containerName={containerName}&&filePattern={filePattern}``` Signalizes, that all file-transaction is complete and notifies further processing. Return files within the given Storage-Container satisfying the file-pattern.

However, it did not make use of a loadingzone-functionality.

### S3

In case of s3-storage none of the SAS-endpoints makes any sense, so only the commit-endpoint is available:

- ```POST /api/v2.0/accessmanager/commit?organization={orgaName}&space={spaceName}&rootDir={rootDir}``` Signalizes, that all file-transaction is complete and notifies further processing. Return files within the given Storage-Container within the root directory.


### Upload

As mentioned before, the upload to SDK does not work directly, but via a 'loadingzone'. This allows us to take advantage of the following aspects:

- only processed data in main-storage
- validation and security-checks in dedicated storage
- supplier can fix errors without additional rights
- in case of dispensation, system can prevent overriding

![Upload-Process](docs/images/upload.png)

### Configuration

#### Azure

In case of azure-usage, the token-expiration can be configured via the following parameters:

- for read-access:
  ```sas.token.expiration.read```
- for write-access:
  ```sas.token.expiration.write```
- for delete-access
  ```sas.token.expiration.delete```

Each parameter is defined as minutes from creation-time.

To avoid errors on the part of the cloud provider, the sas tokens are cached. The transaction-duration is taken into account via a timeout buffer (definition in minutes):
```sas.token.cache.buffer```

### Swagger

The API is documented using [Swagger](https://swagger.io/) (OpenAPI Specification). Developers may use [Swagger UI](https://swagger.io/tools/swagger-ui/) to visualize and interact with the API's resources at `http(s)://(host.domain:port)/accessmanager/swagger-ui/index.html`.

## TODO

Currently, the documentation is located in usual files like `README.md`, `CHANGELOG.md`, `CONTRIBUTING.md` and `LICENSE.md` inside the root folder of the repository. That folder is not processed by MkDocs. To build the technical documentation for MkDocs we could follow these steps:

- Move the documentation to Markdown files inside the `docs` folder.
- Build a proper folder/file structure in `docs` and update the navigation in `mkdocs.yaml`.
- Keep the usual files like `README.md`, `CHANGELOG.md`, `CONTRIBUTING.md` and `LICENSE.md` inside the root folder of the repository (developers expect them to be there, especially in open source projects), but keep them short/generic and just refer to the documentation in the `docs` folder.