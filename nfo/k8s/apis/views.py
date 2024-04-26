"""
========================LICENSE_START=================================
O-RAN-SC
%%
Copyright (C) 2024 Capgemini
%%
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
========================LICENSE_END===================================
"""

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