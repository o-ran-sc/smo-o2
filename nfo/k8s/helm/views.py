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