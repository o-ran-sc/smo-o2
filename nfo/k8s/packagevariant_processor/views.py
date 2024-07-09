from django.shortcuts import render
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view
from django.http import HttpResponse, JsonResponse
from kubernetes import client, config, utils
import requests
import yaml
from kubernetes.dynamic import DynamicClient
import json
from rest_framework.renderers import JSONRenderer
from configparser import ConfigParser

# Create your views here.

@csrf_exempt 
@api_view(['GET', 'POST', 'DELETE'])
def du_processor(request):
    if request.method == "GET":
        result = executeGet(request)
    if request.method == "DELETE":
        executeDelete(request)  
        result = "Deleted !!"  
    elif request.method == "POST":
        result = executePost(request)

    return JsonResponse(result, safe=False)

@csrf_exempt 
@api_view(['GET', 'POST', 'DELETE'])
def cucp_processor(request):
    if request.method == "GET":
        result = executeGet(request)
    if request.method == "DELETE":
        executeDelete(request)  
        result = "Deleted !!"  
    elif request.method == "POST":
        result = executePost(request)

    return JsonResponse(result, safe=False)

@csrf_exempt 
@api_view(['GET', 'POST', 'DELETE'])
def cuup_processor(request):
    if request.method == "GET":
        result = executeGet(request)
    if request.method == "DELETE":
        executeDelete(request) 
        result = "Deleted !!"   
    elif request.method == "POST":
        result = executePost(request)

    return JsonResponse(result, safe=False)

def executePost(request):
    data = request.data
    payload = json.loads(json.dumps(data))
    pv = payload['pv']
    pv_location = payload['pv-location']

    get_remote_file(pv, pv_location)
    # Load kube config
    #config.load_kube_config()
    config.load_kube_config(config_file=fetchKubeLocalPath())

    # Create a dynamic client
    dyn_client = DynamicClient(client.ApiClient())

    with open("examples/oai/"+ pv +".yaml", "r") as stream:
        custom_resource = yaml.safe_load(stream)
        print (custom_resource)

    # Create the custom resource using the dynamic client
    my_resource = dyn_client.resources.get(api_version='config.porch.kpt.dev/v1alpha1', kind='PackageVariant')
    created_resource = my_resource.create(body=custom_resource, namespace='default')
    print(f"Created resource mycrd") 
    cuup_res = json.dumps(created_resource.to_dict(), indent=2)
    print (cuup_res)   
    return cuup_res

def executeDelete(request):
    data = request.data
    payload = json.loads(json.dumps(data))
    group = payload['group']
    version = payload['version']
    namespace = payload['namespace']
    plural = payload['plural']
    name = payload['name']

    # Load kube config
    #config.load_kube_config()
    config.load_kube_config(config_file=fetchKubeLocalPath())

    # Create a CustomObjectsAPI instance
    custom_api = client.CustomObjectsApi()

    # Delete the custom resource
    custom_api.delete_namespaced_custom_object(
        group = group,
        version = version,
        namespace = namespace,
        plural = plural,
        name = name
    )

def executeGet(request):
    data = request.data
    payload = json.loads(json.dumps(data))
    group = payload['group']
    version = payload['version']
    plural = payload['plural']
    name = payload['name']

    print ("--- Retriving Kubernetes custom resource ---")
    config.load_kube_config(config_file=fetchKubeLocalPath())

    dyn_client = DynamicClient(client.ApiClient())

    # Specify custom resource details
    group = group
    version = version
    plural = plural

    # Retrieve custom resources
    custom_resources = dyn_client.resources.get(api_version=f"{group}/{version}", kind=plural)
    print(custom_resources)   
    #resource_details = custom_resources.get(name="oai-cuup")
    #print(resource_details)
     
    resource_list = custom_resources.get()
    for resource in resource_list.items:
        if resource.metadata.name == name:
            cucp_res = json.dumps(resource.to_dict(), indent=2)
            print (cucp_res) 
            return  cucp_res  

def get_remote_file(pv, pv_location):
    # Send a HTTP request to the URL of the file
    response = requests.get(pv_location)

    # Check if the request was successful
    if response.status_code == 200:
        # Open the file in write mode
        with open('examples/oai/'+ pv +'.yaml', 'wb') as file:
            # Write the contents of the response to the file
            file.write(response.content)
    else:
        print('Failed to download the file.')


def fetchKubeLocalPath():
    config = ConfigParser()
    with open("config.ini", "r") as file_object:
        config.read_file(file_object)
        kube_config_path = config.get("localpath", "kubeconfig_file_path")
        
    return kube_config_path