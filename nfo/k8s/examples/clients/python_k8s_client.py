import kubernetes
from kubernetes import client, config, utils
import requests
import yaml
from kubernetes.dynamic import DynamicClient
import json
import yaml

def main():   
    #get_remote_file()

    #config.load_kube_config()
    #k8s_client = client.ApiClient()
    #aml_file = 'nginx.yaml'
    #yaml_file = 'nginx-deployment.yaml'
    #print (yaml_file)

    #create_native_resource(yaml_file)
    #delete_native_resource(yaml_file)
    #get_native_resource(yaml_file)

    create_custom_resource()
    #get_custom_resource()
    #delete_custom_resource("config.porch.kpt.dev", "v1alpha1", "default", "packagevariants", "edge-oai-cuup-test")

    #utils.delete_from_yaml(k8s_client, yaml_file)

def create_native_resource(yaml_file): 
    print ("--- Creating Kubernetes native resource ---")
    config.load_kube_config()
    k8s_client = client.ApiClient()
    utils.create_from_yaml(k8s_client, yaml_file)     

def create_custom_resource(): 
    # Load kube config
    #config.load_kube_config()
    config.load_kube_config(config_file='/home/ubuntu/.kube/config')

    # Create a dynamic client
    dyn_client = DynamicClient(client.ApiClient())

    with open("oai/oai-cuup.yaml", "r") as stream:
        custom_resource = yaml.safe_load(stream)
        print (custom_resource)

    # Create the custom resource using the dynamic client
    my_resource = dyn_client.resources.get(api_version='config.porch.kpt.dev/v1alpha1', kind='PackageVariant')
    created_resource = my_resource.create(body=custom_resource, namespace='default')
    print("created_resource before yaml.safe_load():", created_resource)

    yaml_data = created_resource.to_yaml()
    # Parse the YAML content into a Python dictionary
    data = yaml.safe_load(yaml_data)

    # Convert the Python dictionary to a JSON string
    json_str = json.dumps(data, indent=2)
    print(json_str)

    print(f"Created resource mycrd")    

    ''' # Create a CustomObjectsAPI instance
    custom_api = client.CustomObjectsApi()   
    custom_api.create_cluster_custom_object(group=group, version=version, plural=plural, body=custom_resource) '''

def delete_custom_resource(group, version, namespace, plural, name):
    # Load kube config
    #config.load_kube_config()
    config.load_kube_config(config_file='/home/ubuntu/.kube/config')

    # Create a CustomObjectsAPI instance
    custom_api = client.CustomObjectsApi()

    # Delete the custom resource
    custom_api.delete_namespaced_custom_object(
        group=group,
        version=version,
        namespace=namespace,
        plural=plural,
        name=name
    )

    print("********** Delted *********")  

def delete_native_resource(yaml_file):
    # Load kube config
    config.load_kube_config()

    # Create API client
    api_client = client.ApiClient()

    # Create a Kubernetes object from the YAML file
    #k8s_obj = utils.create_from_yaml(api_client, yaml_file)

    with open(yaml_file, "r") as f:
        k8s_obj = yaml.load(f, Loader=yaml.FullLoader)
        print(k8s_obj)

    # Get the kind of the object (e.g., Pod, Service, etc.)
    kind = k8s_obj["kind"]

    # Get the metadata of the object
    metadata = k8s_obj["metadata"]

    # Create a delete options object
    delete_options = client.V1DeleteOptions()

    if kind == "Pod":
        v1 = client.CoreV1Api(api_client)
        v1.delete_namespaced_pod(metadata["name"], metadata["namespace"], body=delete_options)

    #elif kind == "Deployment":
        #v1 = client.CoreV1Api(api_client)
        #v1.delete_namespaced_deployment(metadata["name"], metadata["namespace"], body=delete_options)
    
    elif kind == "Service":
        v1 = client.CoreV1Api(api_client)
        v1.delete_namespaced_service(metadata["name"], metadata["namespace"], body=delete_options)
        # Add more elif statements for other kinds of resources 

def get_custom_resource():
    print ("--- Retriving Kubernetes custom resource ---")
    # Initialize Kubernetes configuration
    #config.load_kube_config()
    config.load_kube_config(config_file='/home/ubuntu/.kube/config')
    #config.load_kube_config(config_file='/home/ubuntu/.kube/edge-kubeconfig')
    #config.load_kube_config(config_file='/home/ubuntu/.kube/edge01-kubeconfig')
    #config.load_kube_config(config_file='/home/ubuntu/.kube/edge02-kubeconfig')
    #config.load_kube_config(config_file='/home/ubuntu/.kube/regional-kubeconfig')
    #config.load_kube_config(config_file='/home/ubuntu/.kube/core-kubeconfig')
    
    #print(config.load_kube_config())
    #kubernetes.config.load_config()

    # Create a dynamic client
    #dyn_client = client.DynamicClient(client.ApiClient())
    dyn_client = DynamicClient(client.ApiClient())

    # Specify custom resource details
    group = "config.porch.kpt.dev"
    version = "v1alpha1"
    plural = "PackageVariant"

    # Retrieve custom resources
    custom_resources = dyn_client.resources.get(api_version=f"{group}/{version}", kind=plural)
    print(custom_resources)   
    #resource_details = custom_resources.get(name="oai-cuup")
    #print(resource_details)
     
    resource_list = custom_resources.get()
    for resource in resource_list.items:
        if resource.metadata.name == "oai-cuup":
            print(json.dumps(resource.to_dict(), indent=2))


def get_remote_file():
    # URL of the file to be downloaded
    url = 'https://gist.githubusercontent.com/sdenel/1bd2c8b5975393ababbcff9b57784e82/raw/f1b885349ba17cb2a81ca3899acc86c6ad150eb1/nginx-hello-world-deployment.yaml'
    #url = 'https://raw.githubusercontent.com/nephio-project/catalog/main/workloads/oai/package-variants/oai-du.yaml'
    #url = 'https://raw.githubusercontent.com/nephio-project/catalog/main/workloads/oai/package-variants/oai-cucp.yaml'
    #url = 'https://raw.githubusercontent.com/nephio-project/catalog/main/workloads/oai/package-variants/oai-cuup.yaml'
    # Send a HTTP request to the URL of the file
    response = requests.get(url)

    # Check if the request was successful
    if response.status_code == 200:
        # Open the file in write mode
        with open('nginx-deployment.yaml', 'wb') as file:
            # Write the contents of the response to the file
            file.write(response.content)
    else:
        print('Failed to download the file.') 

if __name__ == "__main__":
    main()







