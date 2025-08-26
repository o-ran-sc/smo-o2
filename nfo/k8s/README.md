# Summary
The O-RAN Software Community (OSC) and Open Air Interface (OAI) and Nephio open source communities are working together to provide a reference implementation of the Open RAN (O-RAN) Alliance’s cloud-centric specifications using the Nephio enablers and capabilities in order to deploy and manage O-RAN NFs and xApps. The focus of the O-RAN Integration within Nephio focuses on the Federated O-Cloud Orchestration and Management (FOCOM), Network Function Orchestration (NFO), Infrastructure Management (IMS) and Deployment Management (DMS) O-RAN services.

# NFO installation steps

# Build the service

    NFO services needs to be installed on any VM

    For testing, you can run the service locally :

    `make build`

# Build an image through docker

    docker build -t image-name(nfo-service) .

    deploy using docker

    docker run -d -p 0000:0000 image-name(nfo-service)

#### prerequisites: python 3.12,helm v3.8.0

## Run helm_processor REST APIs

    BASE_URL : http://127.0.0.1:8080(any*)

##### DU:

    GET: BASE_URL/nfo/api/v1/helm/du/
    POST: BASE_URL/nfo/api/v1/helm/du/
    DELETE: BASE_URL/nfo/api/v1/helm/du/

##### CUCP:

    GET: BASE_URL/nfo/api/v1/helm/cucp/
    POST: BASE_URL/nfo/api/v1/helm/cucp/
    DELETE: BASE_URL/nfo/api/v1/helm/cucp/

##### CUUP:

    GET: BASE_URL/nfo/api/v1/helm/cuup/
    POST: BASE_URL/nfo/api/v1/helm/cuup/
    DELETE: BASE_URL/nfo/api/v1/helm/cuup/


## Run packagevariant_processor REST APIs

##### DU:

    POST: BASE_URL/nfo/api/v1/operator/du/
    GET: BASE_URL/nfo/api/v1/operator/du/
    DELETE: BASE_URL/nfo/api/v1/operator/du/

##### CU_CP:

    POST: BASE_URL/nfo/api/v1/operator/cucp/
    GET: BASE_URL/nfo/api/v1/operator/cucp/
    DELETE: BASE_URL/nfo/api/v1/operator/cucp/

##### CU_UP:

    POST: BASE_URL/nfo/api/v1/operator/cuup/
    GET: BASE_URL/nfo/api/v1/operator/cuup/
    DELETE: BASE_URL/nfo/api/v1/operator/cuup/

# ETSI SOL003 Implementation

## Cluster MGMT

1. Create Cluster Definition

    POST: `{{ _.HOST }}/api/o2dms/v2/deployments/`

    ```json
    {
        "api_endpoint": "https://192.168.8.44:6443",
        "auth_method": "kubeconfig",
        "cluster_version": "v1.29.0",
        "node_count": 1,
        "supports_helm": true
    }
    ```

2. Provide Credentials for Cluster

    POST: `{{ _.HOST }}/api/kubernetes-clusters/{{ _.CURRENT_CLUSTER }}/set_credentials/`

    ```json
    {
     "auth_method": "kubeconfig",
     "kubeconfig_content": "{{ _.KUBECONFIG_CICD }}"
    }

    ```

    > *) `kubeconfig_content` is a base64 format of kubeconfig file \
    > Will be taken from O-Cloud IMS in the future

## VNF LCM

1. Create Deployment Descriptor

    POST: `{{ _.HOST }}/api/o2dms/v2/vnf_instances/`

    ```json
    {
        "name": "oai-gnb-test",
        "description": "Another VNF Descriptor",
        "profile_type": "kubernetes",
        "artifact_repo_url": "https://gitlab.eurecom.fr/oai/cn5g/oai-cn5g-fed.git",
        "artifact_name": "charts/oai-5g-ran/oai-gnb",
        "target_cluster": "{{ _.CURRENT_CLUSTER }}"
    }
    ```

2. Create Deployment Instance

    POST: `{{ _.HOST }}/api/o2dms/v2/deployments/`

    ```json
    {
        "descriptor": "{{ _.CURRENT_DESCRIPTOR }}",
        "name": "post-test-abcd"
    }
    ```

3. Instantiate Deployment Instance

    POST: `{{ _.HOST }}/api/o2dms/v2/deployments/{{ _.CURRENT_DEPLOYMENT }}/instantiate/`

    ```json
    {
        "instantiation_params": {
            "replicas": 2
        }
    }

    ```

4. Terminate Deployment Instance

    POST: `{{ _.HOST }}/api/o2dms/v2/deployments/{{ _.CURRENT_DEPLOYMENT }}/terminate/`

    ```json
    {
        "graceful": true,
        "cleanup_namespace": true
    }
    ```

## VNF LCM Operation

1. List Operation

    GET: `{{ _.HOST }}/api/o2dms/v2/vnf_lcm_op_occs/`

    ```json
    {}
    ```
