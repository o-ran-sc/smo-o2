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
from rest_framework import viewsets
from helm.models import Application,OAI
from helm.serializers import ApplicationSerializer,OAISerializer
from rest_framework.decorators import action
from rest_framework.response import Response
# Create your views here.
class ApplicationViewSet(viewsets.ModelViewSet):
    queryset= Application.objects.all()
    serializer_class=ApplicationSerializer
    
    #applications/{ApplicationId}/oai
    @action(detail=True,methods=['get'])
    def employees(self,request,pk=None):   
        try:                
            application=Application.objects.get(pk=pk)
            emps=OAI.objects.filter(application=application)
            emps_serializer=OAISerializer(emps,many=True,context={'request':request})
            return Response(emps_serializer.data)
        except Exception as e:
            print(e)
            return Response({
                'message':'application might not exists !! Error'
            })


class OAIViewSet(viewsets.ModelViewSet):
    queryset=OAI.objects.all()
    serializer_class=OAISerializer