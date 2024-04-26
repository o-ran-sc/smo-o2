from django.http import HttpResponse, JsonResponse
import asyncio
from pyhelm3 import Client
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view
import json
from rest_framework import status
from rest_framework.response import Response
from configparser import ConfigParser

@csrf_exempt 
@api_view(['GET', 'POST', 'DELETE'])
def home_page(request):
    books = ['selfhelp','fantacy','novels']
    #return HttpResponse("<h1>This is home page !!</h1>")

    if request.method == "GET":
        result = executeGet()
    if request.method == "DELETE":
        result = "Uninstalled !!"
        executeDelete(request)    
    elif request.method == "POST":
        executPost(request)
        result = "Installed !!"

    return JsonResponse(result, safe=False)

def executeDelete(request):
    print ("Delete request !!")
    data = request.data
    payload = json.loads(json.dumps(data))
    name = payload['name']
    namespace = payload['namespace']
    print ("chart name: "+ name + " chart namespace: "+ namespace )
    return asyncio.run(uninstall_helm(name, namespace))         

async def uninstall_helm(name, namespace):
    client = getHelmClient()

    revision = await client.get_current_revision(name, namespace = namespace)
    await revision.release.uninstall(wait = True)
    #   Or directly by name
    await client.uninstall_release(name, namespace = "default", wait = True)
    return Response("Uninstalled", status=status.HTTP_201_CREATED)

def executeGet():
    print ("Get request !!")
    return asyncio.run(list_releases())
    
async def list_releases():
    client = getHelmClient()

    # List the deployed releases
    releases = await client.list_releases(all = True, all_namespaces = True)
    charts = []
    chart = {}
    for release in releases:
        revision = await release.current_revision()
        print(release.name, release.namespace, revision.revision, str(revision.status))
        chart['name'] = release.name
        chart['revision'] = revision.revision
        chart['namespace'] = release.namespace
        chart['status'] = str(revision.status)
        charts.append(chart)

    result = json.dumps(charts)
    return result
    
def executPost(request):
    print ("POST request !!")
    data = request.data
    payload = json.loads(json.dumps(data))
    charts = payload['charts']
    for chart in charts:
        name = chart['name']
        version = chart['version']
        repo = chart['repo']
        print ("chart name: "+ name + " chart version: "+ version + " chart repo: " + repo)
        asyncio.run(porcessCharts(name, version, repo))    

async def porcessCharts(name, version, repo):
    print ("Post request !!")

    client = getHelmClient()
    
    # Fetch a chart
    chart = await client.get_chart(
        name,
        repo = repo,
        version = version
    )
    print(chart.metadata.name, chart.metadata.version)
    #print(await chart.readme())

    # Install or upgrade a release
    revision = await client.install_or_upgrade_release(
        name,
        chart,
        { "installCRDs": True },
        atomic = True,
        wait = True
    )
    print(
        revision.release.name,
        revision.release.namespace,
        revision.revision,
        str(revision.status)
    )

    content = { revision.release.name, revision.release.namespace, revision.revision, str(revision.status)}
    return Response(content, status=status.HTTP_201_CREATED)

def getHelmClient():

    config = ConfigParser()
    with open("config.ini", "r") as file_object:
        config.read_file(file_object)
        kube_config = config.get("localpath", "kubeconfig_file_path")
        helm_executable = config.get("localpath", "helm_executable_path")
    
    # This will use the Kubernetes configuration from the environment
    client = Client()
    # Specify the kubeconfig file to use
    client = Client(kubeconfig = kube_config)
    # Specify a custom Helm executable (by default, we expect 'helm' to be on the PATH)
    client = Client(executable = helm_executable)

    return client

    

