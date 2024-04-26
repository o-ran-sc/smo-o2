from django.contrib import admin
from django.urls import path,include
from helm.views import ApplicationViewSet,OAIViewSet
from rest_framework import routers


router= routers.DefaultRouter()
router.register(r'applications', ApplicationViewSet)
router.register(r'oai', OAIViewSet)

urlpatterns = [    
    path('',include(router.urls))
      
]


#companies/{companyId}/employees