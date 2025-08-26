import base64
import json
from django.db import models
import uuid
from cryptography.fernet import Fernet
from django.conf import settings


class DeploymentDescriptor(models.Model):
    """NF Deployment Descriptor - existing infrastructure"""

    descriptor_id = models.UUIDField(primary_key=True, default=uuid.uuid4)
    name = models.CharField(max_length=200)
    description = models.TextField(blank=True)

    # Link to existing cluster infrastructure
    # target_cluster = models.ForeignKey('clusters.Cluster', on_delete=models.CASCADE, related_name='deployment_descriptors')

    target_cluster = models.ForeignKey(
        "KubernetesCluster",
        on_delete=models.CASCADE,
        related_name="deployment_descriptors",
        null=True,
    )

    homing = models.BooleanField(default=False)
    # Deployment specification
    profile_type = models.CharField(
        max_length=20,
        choices=[
            ("kubernetes", "Kubernetes Profile"),
            ("etsi_nfv", "ETSI NFV Profile"),
        ],
    )

    artifact_source_type = models.CharField(
        max_length=20,
        choices=[
            ("helm_repo", "Helm Repository"),
            ("github", "GitHub Repository"),
            ("oci", "OCI Registry"),
            ("local", "Local Path"),
        ],
        default="helm_repo",
    )
    artifact_chart_path = models.CharField(max_length=200, blank=True)

    # Artifact information
    artifact_repo_url = models.URLField()
    artifact_name = models.CharField(max_length=200)
    artifact_repo_branch = models.CharField(max_length=200)
    artifact_version = models.CharField(max_length=50, default="latest")
    input_params = models.JSONField(default=dict)
    # Resource requirements (leverages existing Machine model)
    required_cpu_cores = models.IntegerField(default=1)
    required_memory_gb = models.IntegerField(default=1)
    required_storage_gb = models.IntegerField(default=10)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    additional_params = models.JSONField(default=dict, blank=True)


class NfDeploymentInstance(models.Model):
    """NF Deployment Instance"""

    instance_id = models.UUIDField(primary_key=True, default=uuid.uuid4)
    descriptor = models.ForeignKey(
        DeploymentDescriptor, on_delete=models.CASCADE, related_name="instances"
    )
    name = models.CharField(max_length=200)
    # State management
    instantiation_state = models.CharField(
        max_length=20,
        choices=[
            ("NOT_INSTANTIATED", "Not Instantiated"),
            ("INSTANTIATING", "Instantiating"),
            ("INSTANTIATED", "Instantiated"),
            ("TERMINATING", "Terminating"),
            ("TERMINATED", "Terminated"),
            ("ERROR", "Error"),
        ],
        default="NOT_INSTANTIATED",
    )
    # Link to existing infrastructure
    # deployed_cluster = models.ForeignKey('clusters.Cluster', on_delete=models.SET_NULL, null=True, blank=True)
    # allocated_machines = models.ManyToManyField('clusters.Machine', blank=True, related_name='deployments')
    # Deployment details
    deployment_namespace = models.CharField(max_length=100, blank=True, null=True)
    helm_release_name = models.CharField(max_length=100, blank=True, null=True)
    vm_instances = models.JSONField(default=list, blank=True)
    # SMO integration
    smo_callback_url = models.URLField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.name} ({self.instantiation_state})"


class VnfLcmOperation(models.Model):
    """VNF Lifecycle Management Operation tracking"""

    operation_id = models.UUIDField(primary_key=True, default=uuid.uuid4)
    vnf_instance = models.ForeignKey(
        NfDeploymentInstance, on_delete=models.CASCADE, related_name="operations"
    )
    operation_type = models.CharField(
        max_length=20,
        choices=[
            ("INSTANTIATE", "Instantiate"),
            ("TERMINATE", "Terminate"),
            ("HEAL", "Heal"),
            ("SCALE", "Scale"),
        ],
    )
    operation_state = models.CharField(
        max_length=20,
        choices=[
            ("STARTING", "Starting"),
            ("PROCESSING", "Processing"),
            ("COMPLETED", "Completed"),
            ("FAILED_TEMP", "Failed Temporarily"),
            ("FAILED", "Failed"),
            ("ROLLING_BACK", "Rolling Back"),
            ("ROLLED_BACK", "Rolled Back"),
        ],
        default="STARTING",
    )
    operation_params = models.JSONField(default=dict)
    error_details = models.JSONField(default=dict, blank=True)
    progress_percentage = models.IntegerField(default=0)
    start_time = models.DateTimeField(auto_now_add=True)
    end_time = models.DateTimeField(null=True, blank=True)

    retry_count = models.IntegerField(default=0)
    max_retries = models.IntegerField(default=3)
    retry_after = models.DateTimeField(null=True, blank=True)

    # Rollback fields
    rollback_on_failure = models.BooleanField(default=True)
    rollback_executed = models.BooleanField(default=False)
    state_snapshot = models.JSONField(
        default=dict, blank=True
    )  # State before operation

    # Operation execution tracking
    execution_attempts = models.JSONField(default=list)  # Track each attempt
    automatic_rollback = models.BooleanField(default=True)

    # class Meta:
    #     db_table = 'clusters_vnflcmoperation'

    def __str__(self):
        return f"{self.operation_type} - {self.operation_state}"


class KubernetesCluster(models.Model):
    """Extended Kubernetes cluster information with credentials"""

    # cluster = models.OneToOneField('clusters.Cluster', on_delete=models.CASCADE, related_name='k8s_config')

    # Kubernetes API connection details
    cloud_name = models.CharField(max_length=50, blank=False, unique=True)
    cloud_id = models.UUIDField(primary_key=True, default=uuid.uuid4)
    api_endpoint = models.URLField(
        help_text="Kubernetes API server endpoint", unique=True
    )
    api_version = models.CharField(max_length=20, default="v1.29")

    # Authentication method
    AUTH_METHODS = [
        ("token", "Bearer Token"),
        ("cert", "Client Certificate"),
        ("kubeconfig", "Kubeconfig File"),
        ("service_account", "Service Account"),
    ]
    auth_method = models.CharField(
        max_length=20, choices=AUTH_METHODS, default="kubeconfig"
    )

    # Encrypted credentials storage
    kubeconfig_data = models.TextField(
        blank=True, help_text="Base64 encoded kubeconfig file"
    )
    bearer_token = models.TextField(blank=True, help_text="Encrypted bearer token")
    client_cert = models.TextField(blank=True, help_text="Encrypted client certificate")
    client_key = models.TextField(blank=True, help_text="Encrypted client key")
    ca_cert = models.TextField(blank=True, help_text="Encrypted CA certificate")

    # Cluster capabilities
    cluster_version = models.CharField(max_length=50, blank=True)
    node_count = models.IntegerField(default=0)
    supports_helm = models.BooleanField(default=True)
    supports_operators = models.BooleanField(default=True)
    default_namespace = models.CharField(max_length=63, default="default")

    # Resource limits
    max_pods_per_node = models.IntegerField(default=110)
    max_namespaces = models.IntegerField(default=100)
    storage_classes = models.JSONField(default=list)  # Available storage classes

    # Homing Related
    prometheus_endpoint = models.CharField(max_length=300, blank=True)

    # Status
    connection_status = models.CharField(
        max_length=20,
        choices=[
            ("CONNECTED", "Connected"),
            ("DISCONNECTED", "Disconnected"),
            ("ERROR", "Error"),
            ("UNKNOWN", "Unknown"),
        ],
        default="UNKNOWN",
    )
    last_health_check = models.DateTimeField(null=True, blank=True)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    # def __str__(self):
    #     return f"K8s Config for {self.cluster.name}"

    @property
    def encryption_key(self):
        """Get or create encryption key for this cluster"""
        key = getattr(settings, "CLUSTER_ENCRYPTION_KEY", None)
        if not key:
            # Generate a key - in production, this should be managed externally
            key = Fernet.generate_key()
        return key

    def encrypt_data(self, data):
        """Encrypt sensitive data"""
        if not data:
            return ""
        fernet = Fernet(self.encryption_key)
        return fernet.encrypt(data.encode()).decode()

    def decrypt_data(self, encrypted_data):
        """Decrypt sensitive data"""
        if not encrypted_data:
            return ""
        fernet = Fernet(self.encryption_key)
        return fernet.decrypt(encrypted_data.encode()).decode()

    def set_bearer_token(self, token):
        """Set encrypted bearer token"""
        self.bearer_token = self.encrypt_data(token)

    def get_bearer_token(self):
        """Get decrypted bearer token"""
        return self.decrypt_data(self.bearer_token)

    def set_kubeconfig(self, kubeconfig_content):
        """Set base64 encoded kubeconfig"""
        if isinstance(kubeconfig_content, str):
            self.kubeconfig_data = base64.b64encode(
                kubeconfig_content.encode()
            ).decode()
        else:
            self.kubeconfig_data = base64.b64encode(kubeconfig_content).decode()

    def get_kubeconfig(self):
        """Get decoded kubeconfig content"""
        if self.kubeconfig_data:
            return base64.b64decode(self.kubeconfig_data).decode()
        return ""

    def get_kubernetes_config(self):
        """Get kubernetes configuration for API client"""
        config = {"host": self.api_endpoint, "verify_ssl": True}

        if self.auth_method == "token" and self.bearer_token:
            config["api_key"] = {"authorization": f"Bearer {self.get_bearer_token()}"}
        elif self.auth_method == "cert":
            config["cert_file"] = self.decrypt_data(self.client_cert)
            config["key_file"] = self.decrypt_data(self.client_key)
        elif self.auth_method == "kubeconfig":
            # Return kubeconfig for kubectl usage
            return {"kubeconfig": self.get_kubeconfig()}

        if self.ca_cert:
            config["ssl_ca_cert"] = self.decrypt_data(self.ca_cert)

        return config


class KubernetesDeployment(models.Model):
    """Track Kubernetes deployments for NF instances"""

    nf_instance = models.OneToOneField(
        "NfDeploymentInstance", on_delete=models.CASCADE, related_name="k8s_deployment"
    )
    k8s_cluster = models.ForeignKey(
        KubernetesCluster, on_delete=models.CASCADE, related_name="deployments"
    )

    # Kubernetes resource details
    namespace = models.CharField(max_length=63)
    deployment_name = models.CharField(max_length=63)
    helm_release_name = models.CharField(max_length=63, blank=True)

    # Deployed resources
    deployed_manifests = models.JSONField(default=list)  # List of applied K8s manifests
    helm_values = models.JSONField(default=dict)  # Helm values used

    # Resource status
    pods_ready = models.IntegerField(default=0)
    pods_desired = models.IntegerField(default=1)
    services_created = models.JSONField(default=list)
    ingress_urls = models.JSONField(default=list)

    # Deployment status
    kubernetes_status = models.CharField(
        max_length=20,
        choices=[
            ("DEPLOYING", "Deploying"),
            ("RUNNING", "Running"),
            ("FAILED", "Failed"),
            ("SCALING", "Scaling"),
            ("UPDATING", "Updating"),
            ("TERMINATING", "Terminating"),
        ],
        default="DEPLOYING",
    )

    error_message = models.TextField(blank=True)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"K8s Deployment: {self.deployment_name} in {self.namespace}"


# class Machine(models.Model):
#     cluster = models.ForeignKey(Cluster, related_name='machines', on_delete=models.CASCADE)
#     hostname = models.CharField(max_length=100)
#     ip = models.GenericIPAddressField()
#     mac = models.CharField(max_length=17)  # Format: XX:XX:XX:XX:XX:XX
#     cpu_cores = models.IntegerField()
#     role = models.CharField(max_length=20, choices=[
#         ('MASTER', 'Master'),
#         ('WORKER', 'Worker'),
#         ('STORAGE', 'Storage'),
#         ('GATEWAY', 'Gateway'),
#     ])
#     os_type = models.CharField(max_length=20, choices=[
#         ('LINUX', 'Linux'),
#         ('WINDOWS', 'Windows'),
#         ('MACOS', 'MacOS'),
#     ])
#     status = models.CharField(max_length=20, choices=[
#         ('ACTIVE', 'Active'),
#         ('INACTIVE', 'Inactive'),
#         ('MAINTENANCE', 'Maintenance'),
#         ('ERROR', 'Error'),
#         ('DEPLOYING', 'Deploying'),
#         ('UPGRADING', 'Upgrading'),
#         ('UNREACHABLE', 'Unreachable'),
#     ])
#     health = models.CharField(max_length=20, choices=[
#         ('HEALTHY', 'Healthy'),
#         ('WARNING', 'Warning'),
#         ('CRITICAL', 'Critical'),
#         ('UNKNOWN', 'Unknown'),
#     ])
#     created_at = models.DateTimeField(auto_now_add=True)
#     updated_at = models.DateTimeField(auto_now=True)
#
#     def __str__(self):
#         return f"{self.hostname} ({self.ip})"
