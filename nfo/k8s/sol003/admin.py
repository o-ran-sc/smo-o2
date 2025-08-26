from django.contrib import admin
from .models import (
    NfDeploymentInstance,
    DeploymentDescriptor,
    VnfLcmOperation,
    KubernetesDeployment,
    KubernetesCluster,
)

# Register your models here.


# NfDeploymentInstance,
@admin.register(NfDeploymentInstance)
class NfDeploymentInstanceAdmin(admin.ModelAdmin):
    list_display = (
        "instance_id",
        "instantiation_state",
        "deployment_namespace",
        "created_at",
        "updated_at",
    )
    list_filter = ("instantiation_state",)
    search_fields = (
        "instance_id",
        "instantiation_state",
    )
    # pass
    # list_display = '__all__'
    # list_filter = '__all__'
    # search_fields = '__all__'


# DeploymentDescriptor,
@admin.register(DeploymentDescriptor)
class DeploymentDescriptorAdmin(admin.ModelAdmin):
    list_display = ("name", "descriptor_id", "profile_type")
    # pass


# KubernetesDeployment
@admin.register(KubernetesDeployment)
class KubernetesDeploymentAdmin(admin.ModelAdmin):
    list_display = (
        "deployment_name",
        "nf_instance",
        "k8s_cluster",
    )
    # pass


# KubernetesCluster
@admin.register(KubernetesCluster)
class KubernetesClusterAdmin(admin.ModelAdmin):
    list_display = (
        "cloud_name",
        "cloud_id",
        "api_endpoint",
        "connection_status",
    )


@admin.register(VnfLcmOperation)
class VnfLcmOperationAdmin(admin.ModelAdmin):
    list_display = (
        "vnf_instance",
        "operation_state",
        "operation_type",
        "error_details",
        "start_time",
    )
