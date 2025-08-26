from rest_framework import serializers
from .models import (
    NfDeploymentInstance,
    DeploymentDescriptor,
    KubernetesCluster,
    VnfLcmOperation,
    KubernetesDeployment,
)
from django.contrib.auth import get_user_model
from django.utils import timezone


class NfDeploymentInstanceSerializer(serializers.ModelSerializer):
    descriptor_name = serializers.CharField(source="descriptor.name", read_only=True)
    cluster_name = serializers.CharField(source="deployed_cluster.name", read_only=True)

    class Meta:
        model = NfDeploymentInstance
        fields = "__all__"


class DeploymentDescriptorSerializer(serializers.ModelSerializer):
    target_cluster_name = serializers.CharField(
        source="target_cluster.name", read_only=True
    )

    class Meta:
        model = DeploymentDescriptor
        fields = "__all__"


class KubernetesClusterSerializer(serializers.ModelSerializer):
    cluster_name = serializers.CharField(source="cluster.name", read_only=True)

    # Exclude sensitive fields from serialization
    class Meta:
        model = KubernetesCluster
        # fields = ['id', 'cluster_name', 'cluster']
        exclude = [
            "kubeconfig_data",
            "bearer_token",
            "client_cert",
            "client_key",
            "ca_cert",
        ]


class KubernetesClusterCredentialsSerializer(serializers.ModelSerializer):
    """Separate serializer for credential management"""

    kubeconfig_content = serializers.CharField(write_only=True, required=False)
    bearer_token_raw = serializers.CharField(write_only=True, required=False)

    class Meta:
        model = KubernetesCluster
        fields = ["auth_method", "kubeconfig_content", "bearer_token_raw"]

    def update(self, instance, validated_data):
        if "kubeconfig_content" in validated_data:
            instance.set_kubeconfig(validated_data.pop("kubeconfig_content"))
        if "bearer_token_raw" in validated_data:
            instance.set_bearer_token(validated_data.pop("bearer_token_raw"))

        return super().update(instance, validated_data)


class KubernetesDeploymentSerializer(serializers.ModelSerializer):
    nf_instance_name = serializers.CharField(source="nf_instance.name", read_only=True)
    cluster_name = serializers.CharField(
        source="k8s_cluster.cluster.name", read_only=True
    )

    class Meta:
        model = KubernetesDeployment
        fields = "__all__"


class DeploymentDescriptorDetailedSerializer(serializers.ModelSerializer):
    """Detailed serializer for deployment descriptors"""

    target_cluster_name = serializers.CharField(
        source="target_cluster.name", read_only=True
    )
    target_cluster_status = serializers.CharField(
        source="target_cluster.status", read_only=True
    )
    instances_count = serializers.SerializerMethodField()
    active_instances_count = serializers.SerializerMethodField()

    class Meta:
        model = DeploymentDescriptor
        fields = "__all__"

    def get_instances_count(self, obj):
        return obj.instances.count()

    def get_active_instances_count(self, obj):
        return obj.instances.filter(instantiation_state="INSTANTIATED").count()


class NfDeploymentInstanceDetailedSerializer(serializers.ModelSerializer):
    """Detailed serializer for NF deployment instances"""

    descriptor_name = serializers.CharField(source="descriptor.name", read_only=True)
    descriptor_profile = serializers.CharField(
        source="descriptor.profile_type", read_only=True
    )
    cluster_name = serializers.CharField(source="deployed_cluster.name", read_only=True)
    cluster_status = serializers.CharField(
        source="deployed_cluster.status", read_only=True
    )

    operations_count = serializers.SerializerMethodField()
    last_operation = serializers.SerializerMethodField()
    k8s_deployment_info = serializers.SerializerMethodField()

    class Meta:
        model = NfDeploymentInstance
        fields = "__all__"

    def get_operations_count(self, obj):
        return obj.operations.count()

    def get_last_operation(self, obj):
        last_op = obj.operations.order_by("-start_time").first()
        if last_op:
            return {
                "operation_id": str(last_op.operation_id),
                "operation_type": last_op.operation_type,
                "operation_state": last_op.operation_state,
                "start_time": last_op.start_time.isoformat(),
                "progress": last_op.progress_percentage,
            }
        return None

    def get_k8s_deployment_info(self, obj):
        try:
            k8s_deployment = obj.k8s_deployment
            return {
                "namespace": k8s_deployment.namespace,
                "deployment_name": k8s_deployment.deployment_name,
                "helm_release": k8s_deployment.helm_release_name,
                "kubernetes_status": k8s_deployment.kubernetes_status,
                "pods_ready": k8s_deployment.pods_ready,
                "pods_desired": k8s_deployment.pods_desired,
            }
        except:
            return None


class VnfLcmOperationSerializer(serializers.ModelSerializer):
    class Meta:
        model = VnfLcmOperation
        fields = "__all__"


class VnfLcmOperationDetailedSerializer(serializers.ModelSerializer):
    """Detailed serializer for VNF LCM operations"""

    vnf_instance_name = serializers.CharField(
        source="vnf_instance.name", read_only=True
    )
    vnf_instance_state = serializers.CharField(
        source="vnf_instance.instantiation_state", read_only=True
    )
    duration = serializers.SerializerMethodField()

    class Meta:
        model = VnfLcmOperation
        fields = "__all__"

    def get_duration(self, obj):
        if obj.end_time:
            duration = obj.end_time - obj.start_time
            return duration.total_seconds()
        else:
            # Operation still running
            duration = timezone.now() - obj.start_time
            return duration.total_seconds()


class KubernetesDeploymentDetailedSerializer(serializers.ModelSerializer):
    """Detailed serializer for Kubernetes deployments"""

    nf_instance_name = serializers.CharField(source="nf_instance.name", read_only=True)
    cluster_name = serializers.CharField(
        source="k8s_cluster.cluster.name", read_only=True
    )
    cluster_endpoint = serializers.CharField(
        source="k8s_cluster.api_endpoint", read_only=True
    )
    health_status = serializers.SerializerMethodField()

    class Meta:
        model = KubernetesDeployment
        fields = "__all__"

    def get_health_status(self, obj):
        if obj.pods_desired > 0:
            health_percentage = (obj.pods_ready / obj.pods_desired) * 100
            if health_percentage == 100:
                return "HEALTHY"
            elif health_percentage >= 50:
                return "DEGRADED"
            else:
                return "UNHEALTHY"
        return "UNKNOWN"
