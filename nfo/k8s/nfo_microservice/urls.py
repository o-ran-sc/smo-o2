"""
URL configuration for nfo_microservice project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/5.0/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
# include necessary libraries
from django.urls import path, include
from .views import home_page
from rest_framework.routers import DefaultRouter

from sol003.views_operation import VnfLcmOperationViewSet
from sol003.views import (
    DeploymentDescriptorViewSet,
    NfDeploymentInstanceViewSet,
    KubernetesClusterViewSet,
    DeploymentDescriptorListViewSet,
    NfDeploymentInstanceListViewSet,
    KubernetesDeploymentListViewSet,
    KubernetesOperation,
    )

router = DefaultRouter()
router.register(r'o2dms/v2/vnf_instances', DeploymentDescriptorViewSet, basename='vnf-instances')
router.register(r'o2dms/v2/deployments', NfDeploymentInstanceViewSet, basename='nf-deployments')
router.register(r'o2dms/v2/vnf_lcm_op_occs', VnfLcmOperationViewSet, basename='vnf-operations')

# IMS
router.register(r'kubernetes-clusters', KubernetesClusterViewSet, basename='k8s-clusters')
router.register(r'descriptors', DeploymentDescriptorListViewSet, basename='descriptors-list')
router.register(r'instances', NfDeploymentInstanceListViewSet, basename='instances-list')
router.register(r'k8s-deployments', KubernetesDeploymentListViewSet, basename='k8s-deployments-list')

router.register(r'kubernetes-operation', KubernetesOperation, basename='k8s-operation')


urlpatterns = [
    path('admin/', admin.site.urls),
    path('', include("helm_processor.urls")),
    path('', include("packagevariant_processor.urls")),
    path('api/', include(router.urls)),

]
