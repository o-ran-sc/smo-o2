from django.shortcuts import render
import pyhelm
import asyncio
from kubernetes import client
from pyhelm3 import Client


# Create your views here.
# import viewsets
from rest_framework import viewsets
import json
 
# import local data
#from .serializers import NFOSerializer
#from .models import NFOModel

from django.http import JsonResponse


async def api_home(request, *args, **kwargs):
    body = request.body
    data = {}
    try:
        data = json.loads(body)
    except:
        pass
    #return JsonResponse({"message: Hi there"}, safe=False)
    await asyncio.gather(process_helm_charts())
    #process_helm_charts()
    return JsonResponse(data)

async def process_helm_charts():
    print("Processing Helm charts")
    
    # This will use the Kubernetes configuration from the environment
    client = Client()
    # Specify the kubeconfig file to use
    client = Client(kubeconfig = "/home/fnclab/.kube/config")
    # Specify a custom Helm executable (by default, we expect 'helm' to be on the PATH)
    client = Client(executable = "/usr/local/bin/helm")

    # List the deployed releases
    releases = await client.list_releases(all = True, all_namespaces = True)
    for release in releases:
        revision = await release.current_revision()
        print(release.name, release.namespace, revision.revision, str(revision.status))



# create a viewset  
#class NFOViewSet(viewsets.ModelViewSet):
    # define queryset
    #queryset = NFOModel.objects.all()
 
    # specify serializer to be used
    #serializer_class = NFOSerializer