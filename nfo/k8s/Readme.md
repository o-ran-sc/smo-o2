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

    > *) `kubeconfig_content` is a base64 format of kubeconfig file

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

2. Retry, TBD
3. Rollback, TBD
4. Fail, TBD
5. Cancel, TBD
