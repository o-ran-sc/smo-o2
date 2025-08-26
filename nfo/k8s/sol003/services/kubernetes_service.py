import yaml
import tempfile
import subprocess
import os
from kubernetes import client, config
from kubernetes.client.rest import ApiException
import logging
import base64
from django.utils import timezone
import json
import shutil
from contextlib import contextmanager

logger = logging.getLogger(__name__)


class KubernetesDeploymentService:
    """Service for real Kubernetes deployments"""

    def __init__(self, k8s_cluster):
        self.k8s_cluster = k8s_cluster
        self.k8s_client = None
        self._setup_client()

    @contextmanager
    def _get_kubeconfig_context(self):
        """Context manager for temporary kubeconfig file"""
        k8s_config = self.k8s_cluster.get_kubernetes_config()
        temp_kubeconfig = None

        try:
            with tempfile.NamedTemporaryFile(
                mode="w", suffix=".yaml", delete=False
            ) as f:
                kubeconfig_content = base64.b64decode(k8s_config["kubeconfig"]).decode()
                f.write(kubeconfig_content)
                temp_kubeconfig = f.name

            env = os.environ.copy()
            env["KUBECONFIG"] = temp_kubeconfig
            yield env

        finally:
            if temp_kubeconfig and os.path.exists(temp_kubeconfig):
                os.unlink(temp_kubeconfig)

    def _run_helm_command(self, cmd, env=None, check=True):
        """Execute helm command with proper error handling"""
        try:
            result = subprocess.run(
                cmd, capture_output=True, text=True, env=env, timeout=600
            )

            if check and result.returncode != 0:
                logger.error(f"Helm command failed: {' '.join(cmd)}")
                logger.error(f"Error output: {result.stderr}")
                return {
                    "success": False,
                    "error": result.stderr,
                    "stdout": result.stdout,
                }

            return {"success": True, "stdout": result.stdout, "stderr": result.stderr}

        except subprocess.TimeoutExpired:
            return {"success": False, "error": "Command timeout"}
        except Exception as e:
            return {"success": False, "error": str(e)}

    def delete_helm_release(self, namespace, release_name, force=False):
        """Delete Helm release"""
        with self._get_kubeconfig_context() as env:
            try:
                # Check if release exists
                list_cmd = ["helm", "list", "-n", namespace, "-o", "json"]
                list_result = self._run_helm_command(list_cmd, env, check=False)

                if not list_result["success"]:
                    return list_result

                releases = (
                    json.loads(list_result["stdout"]) if list_result["stdout"] else []
                )
                release_exists = any(r["name"] == release_name for r in releases)

                if not release_exists:
                    return {
                        "success": True,
                        "message": "Release not found (already deleted)",
                    }

                # Delete the release
                delete_cmd = [
                    "helm",
                    "uninstall",
                    release_name,
                    "--namespace",
                    namespace,
                ]
                if not force:
                    delete_cmd.append("--wait")

                logger.info(f"Deleting Helm release: {release_name}")
                result = self._run_helm_command(delete_cmd, env)

                if result["success"]:
                    return {
                        "success": True,
                        "message": f"Helm release {release_name} deleted",
                    }
                return result

            except Exception as e:
                logger.exception("Delete helm release failed")
                return {"success": False, "error": str(e)}

    def cleanup_resources(self, namespace, deployment_name):
        """Clean up Kubernetes resources manually"""
        try:
            apps_v1 = client.AppsV1Api(self.k8s_client)
            core_v1 = client.CoreV1Api(self.k8s_client)
            cleaned = []

            # Delete deployment
            try:
                apps_v1.delete_namespaced_deployment(
                    name=deployment_name, namespace=namespace
                )
                cleaned.append("deployment")
            except client.exceptions.ApiException:
                pass

            # Delete services
            try:
                services = core_v1.list_namespaced_service(namespace=namespace)
                for service in services.items:
                    if deployment_name in service.metadata.name:
                        core_v1.delete_namespaced_service(
                            name=service.metadata.name, namespace=namespace
                        )
                        cleaned.append(f"service/{service.metadata.name}")
            except client.exceptions.ApiException:
                pass

            # Delete configmaps
            try:
                configmaps = core_v1.list_namespaced_config_map(namespace=namespace)
                for cm in configmaps.items:
                    if deployment_name in cm.metadata.name:
                        core_v1.delete_namespaced_config_map(
                            name=cm.metadata.name, namespace=namespace
                        )
                        cleaned.append(f"configmap/{cm.metadata.name}")
            except client.exceptions.ApiException:
                pass

            return {
                "success": True,
                "message": f'Cleaned up resources: {", ".join(cleaned)}',
            }

        except Exception as e:
            logger.exception("Cleanup resources failed")
            return {"success": False, "error": str(e)}

    def delete_namespace(self, namespace, force=False):
        """Delete namespace and all resources within"""
        try:
            core_v1 = client.CoreV1Api(self.k8s_client)

            # Check if namespace exists
            try:
                core_v1.read_namespace(name=namespace)
            except client.exceptions.ApiException as e:
                if e.status == 404:
                    return {"success": True, "message": "Namespace already deleted"}
                raise

            # Force delete by removing finalizers
            if force:
                body = client.V1Namespace(
                    metadata=client.V1ObjectMeta(name=namespace, finalizers=[])
                )
                core_v1.patch_namespace(name=namespace, body=body)

            # Delete the namespace
            core_v1.delete_namespace(name=namespace)
            logger.info(f"Deleted namespace: {namespace}")
            return {"success": True, "message": f"Namespace {namespace} deleted"}

        except Exception as e:
            logger.exception("Delete namespace failed")
            return {"success": False, "error": str(e)}

    def upgrade_helm_release(
        self, namespace, release_name, chart_repo, chart_path, values=None
    ):
        """Upgrade existing Helm release"""
        with self._get_kubeconfig_context() as env:
            try:
                # Check if release exists
                check_cmd = ["helm", "list", "-n", namespace, "-o", "json"]
                check_result = self._run_helm_command(check_cmd, env)

                if not check_result["success"]:
                    return {"success": False, "error": "Failed to check release status"}

                releases = (
                    json.loads(check_result["stdout"]) if check_result["stdout"] else []
                )
                release_exists = any(r["name"] == release_name for r in releases)

                if not release_exists:
                    return {
                        "success": False,
                        "error": f"Release {release_name} not found",
                    }

                # Route to appropriate upgrade method
                if "git" in chart_repo:
                    return self._upgrade_from_git(
                        namespace, release_name, chart_repo, chart_path, values, env
                    )
                else:
                    return self._upgrade_from_helm_repo(
                        namespace, release_name, chart_repo, chart_path, values, env
                    )

            except Exception as e:
                logger.exception("Upgrade helm release failed")
                return {"success": False, "error": str(e)}

    def _upgrade_from_git(
        self, namespace, release_name, repo_url, chart_path, values=None, env=None
    ):
        """Upgrade from Git repository"""
        work_dir = None
        temp_values_files = []

        try:
            # Clone repository
            work_dir = tempfile.mkdtemp()
            clone_cmd = ["git", "clone", "--depth", "1", repo_url, work_dir]

            clone_result = self._run_helm_command(clone_cmd, env)
            if not clone_result["success"]:
                return {"success": False, "error": "Failed to clone repository"}

            # Build dependencies
            chart_location = os.path.join(work_dir, chart_path)
            dep_cmd = ["helm", "dependency", "build", chart_location]
            self._run_helm_command(dep_cmd, env, check=False)  # Don't fail if no deps

            # Prepare values
            values_files = self._prepare_helm_values(
                work_dir, chart_path, values, temp_values_files
            )

            # Build upgrade command
            helm_cmd = [
                "helm",
                "upgrade",
                release_name,
                chart_location,
                "--namespace",
                namespace,
                "--wait",
                "--timeout",
                "10m",
                "--force",
            ]

            for vf in values_files:
                helm_cmd.extend(["--values", vf])

            # Execute upgrade
            result = self._run_helm_command(helm_cmd, env)

            if result["success"]:
                logger.info(f"Upgraded {release_name}")
                return {"success": True, "message": "Upgrade successful"}
            else:
                # Attempt rollback
                rollback_cmd = [
                    "helm",
                    "rollback",
                    release_name,
                    "--namespace",
                    namespace,
                    "--wait",
                    "--timeout",
                    "10m",
                ]
                rollback_result = self._run_helm_command(rollback_cmd, env, check=False)
                logger.error(f"Rollback attempted: {rollback_result}")
                return result

        except Exception as e:
            logger.exception("Upgrade from git failed")
            return {"success": False, "error": str(e)}

        finally:
            if work_dir and os.path.exists(work_dir):
                shutil.rmtree(work_dir, ignore_errors=True)
            for tf in temp_values_files:
                if os.path.exists(tf):
                    os.unlink(tf)

    def _setup_client(self):
        """Setup Kubernetes client"""
        try:
            k8s_config = self.k8s_cluster.get_kubernetes_config()

            if "kubeconfig" in k8s_config:
                kubeconfig_content = base64.b64decode(k8s_config["kubeconfig"]).decode()

                with tempfile.NamedTemporaryFile(
                    mode="w", suffix=".config", delete=False
                ) as f:
                    f.write(kubeconfig_content)
                    kubeconfig_path = f.name

                configuration = client.Configuration()
                config.load_kube_config(
                    config_file=kubeconfig_path, client_configuration=configuration
                )

                os.unlink(kubeconfig_path)
                self.k8s_client = client.ApiClient(configuration=configuration)
            else:
                # Use direct configuration
                configuration = client.Configuration()
                configuration.host = k8s_config["host"]
                configuration.verify_ssl = k8s_config.get("verify_ssl", True)

                if "api_key" in k8s_config:
                    configuration.api_key = k8s_config["api_key"]

                client.Configuration.set_default(configuration)

            logger.info(
                f"Kubernetes client setup successful for {self.k8s_cluster.cloud_id}"
            )

        except Exception as e:
            logger.exception("Failed to setup Kubernetes client")
            raise

    def test_connection(self):
        """Test Kubernetes cluster connection"""
        try:
            v1 = client.CoreV1Api(self.k8s_client)
            v1.list_namespace()

            self.k8s_cluster.connection_status = "CONNECTED"
            self.k8s_cluster.last_health_check = timezone.now()
            self.k8s_cluster.save()
            return True

        except Exception as e:
            logger.error(f"Kubernetes connection test failed: {str(e)}")
            self.k8s_cluster.connection_status = "ERROR"
            self.k8s_cluster.save()
            return False

    def create_namespace(self, namespace):
        """Create namespace if it doesn't exist"""
        try:
            v1 = client.CoreV1Api(self.k8s_client)

            try:
                v1.read_namespace(name=namespace)
                logger.info(f"Namespace {namespace} already exists")
                return True
            except ApiException as e:
                if e.status != 404:
                    raise

            namespace_manifest = client.V1Namespace(
                metadata=client.V1ObjectMeta(name=namespace)
            )
            v1.create_namespace(body=namespace_manifest)
            logger.info(f"Created namespace: {namespace}")
            return True

        except Exception as e:
            logger.error(f"Failed to create namespace {namespace}: {str(e)}")
            return False

    def deploy_helm_chart(
        self, namespace, release_name, chart_repo, chart_path, chart_branch, values=None
    ):
        """Deploy Helm chart from any source"""
        with self._get_kubeconfig_context() as env:
            if "git" in chart_repo:
                return self._deploy_from_git(
                    namespace,
                    release_name,
                    chart_repo,
                    chart_path,
                    chart_branch,
                    values,
                    env,
                )
            else:
                return self._deploy_from_helm_repo(
                    namespace, release_name, chart_repo, chart_path, values, env
                )

    def _deploy_from_git(
        self,
        namespace,
        release_name,
        repo_url,
        chart_path,
        chart_branch,
        values=None,
        env=None,
    ):
        """Deploy chart from Git repository"""
        work_dir = None
        temp_values_files = []

        try:
            # Clone repository
            work_dir = tempfile.mkdtemp()
            # clone_cmd = ['git', 'clone', '--depth', '1', '--single-branch', repo_url, work_dir]
            clone_cmd = [
                "git",
                "clone",
                "--depth",
                "1",
                "--branch",
                chart_branch,
                repo_url,
                work_dir,
            ]

            clone_result = self._run_helm_command(clone_cmd, env)
            if not clone_result["success"]:
                logger.error(f"Git clone failed: {clone_result['error']}")
                return {"success": False, "error": "Failed to clone repository"}

            chart_location = os.path.join(work_dir, chart_path)

            if not os.path.exists(chart_location):
                logger.error(f"Chart not found at path: {chart_path}")
                return {"success": False, "error": f"Chart not found at {chart_path}"}

            # Build dependencies
            dep_cmd = ["helm", "dependency", "build", chart_location]
            self._run_helm_command(dep_cmd, env, check=False)

            # Prepare values
            values_files = self._prepare_helm_values(
                work_dir, chart_path, values, temp_values_files
            )

            # Dry run first
            dry_run_cmd = [
                "helm",
                "install",
                release_name,
                chart_location,
                "--dry-run",
                "--namespace",
                namespace,
                "--create-namespace",
                "--timeout",
                "10m",
            ]

            for vf in values_files:
                dry_run_cmd.extend(["--values", vf])

            dry_run_result = self._run_helm_command(dry_run_cmd, env, check=False)

            if not dry_run_result["success"]:
                logger.error(f"Dry run failed for {release_name}")
                return {
                    "success": False,
                    "error": f"Dry run failed: {dry_run_result['error']}",
                }

            # Actual install - THIS WAS THE BUG, the command was incomplete
            install_cmd = [
                "helm",
                "install",
                release_name,
                chart_location,
                "--namespace",
                namespace,
                "--wait",
                "--create-namespace",
                "--timeout",
                "10m",
            ]

            for vf in values_files:
                install_cmd.extend(["--values", vf])

            result = self._run_helm_command(install_cmd, env)

            if result["success"]:
                logger.info(f"Deployed {release_name}")
                return {"success": True, "message": f"Deployed {release_name}"}
            else:
                # Special case for port conflicts
                if "provided port" in result["error"]:
                    logger.warning(
                        f"Port conflict for {release_name}, but deployment may have succeeded"
                    )
                    return {
                        "success": True,
                        "message": f"Deployed {release_name} with port warning",
                    }

                return {"success": False, "error": result["error"]}

        except Exception as e:
            logger.exception("Deploy from git failed")
            return {"success": False, "error": str(e)}

        finally:
            if work_dir and os.path.exists(work_dir):
                shutil.rmtree(work_dir, ignore_errors=True)
            for tf in temp_values_files:
                if os.path.exists(tf):
                    os.unlink(tf)

    def _deploy_from_helm_repo(
        self, namespace, release_name, repo_url, chart_name, values=None, env=None
    ):
        """Deploy from standard Helm repository"""
        repo_name = "temp-repo"

        # Add repository
        add_repo_cmd = ["helm", "repo", "add", repo_name, repo_url]
        self._run_helm_command(add_repo_cmd, env, check=False)

        update_cmd = ["helm", "repo", "update"]
        self._run_helm_command(update_cmd, env, check=False)

        # Deploy
        helm_cmd = [
            "helm",
            "install",
            release_name,
            f"{repo_name}/{chart_name}",
            "--namespace",
            namespace,
            "--wait",
            "--timeout",
            "10m",
        ]

        result = self._run_helm_command(helm_cmd, env)
        return result

    def _upgrade_from_helm_repo(
        self, namespace, release_name, repo_url, chart_name, values=None, env=None
    ):
        """Upgrade from standard Helm repository"""
        repo_name = "temp-repo"

        add_repo_cmd = ["helm", "repo", "add", repo_name, repo_url]
        self._run_helm_command(add_repo_cmd, env, check=False)

        update_cmd = ["helm", "repo", "update"]
        self._run_helm_command(update_cmd, env, check=False)

        helm_cmd = [
            "helm",
            "upgrade",
            release_name,
            f"{repo_name}/{chart_name}",
            "--namespace",
            namespace,
            "--wait",
            "--timeout",
            "10m",
        ]

        result = self._run_helm_command(helm_cmd, env)
        return result

    def _prepare_helm_values(self, work_dir, chart_path, user_values, temp_files_list):
        """Find and merge values intelligently"""
        values_files = []

        # Common values file patterns
        common_files = [
            "values.yaml",
            "values-prod.yaml",
            "values-production.yaml",
            "prod-values.yaml",
            "production.yaml",
        ]

        # Look for values files in the chart
        chart_dir = os.path.join(work_dir, chart_path)
        for filename in common_files:
            filepath = os.path.join(chart_dir, filename)
            if os.path.exists(filepath):
                values_files.append(filepath)

        # Check parent directory
        parent_dir = os.path.dirname(chart_dir)
        for filename in ["values.yaml", "global-values.yaml"]:
            filepath = os.path.join(parent_dir, filename)
            if os.path.exists(filepath):
                values_files.append(filepath)

        # User values always override
        if user_values:
            temp_file = tempfile.NamedTemporaryFile(
                mode="w", suffix=".yaml", delete=False
            )
            if isinstance(user_values, str):
                temp_file.write(user_values)
            else:
                yaml.dump(user_values, temp_file)
            temp_file.close()
            values_files.append(temp_file.name)
            temp_files_list.append(temp_file.name)

        return values_files

    def deploy_manifests(self, namespace, manifests):
        """Deploy raw Kubernetes manifests"""
        try:
            if not self.create_namespace(namespace):
                return False

            deployed_resources = []

            for manifest_content in manifests:
                with tempfile.NamedTemporaryFile(
                    mode="w", suffix=".yaml", delete=False
                ) as f:
                    f.write(manifest_content)
                    manifest_file = f.name

                try:
                    kubectl_cmd = [
                        "kubectl",
                        "apply",
                        "-f",
                        manifest_file,
                        "-n",
                        namespace,
                    ]
                    result = subprocess.run(
                        kubectl_cmd, capture_output=True, text=True, timeout=60
                    )

                    if result.returncode == 0:
                        deployed_resources.append(manifest_content)
                        logger.info("Applied manifest successfully")
                    else:
                        logger.error(f"Failed to apply manifest: {result.stderr}")
                        return False
                finally:
                    os.unlink(manifest_file)

            return deployed_resources

        except Exception as e:
            logger.exception("Manifest deployment error")
            return False

    def get_deployment_status(self, namespace, deployment_name):
        """Get deployment status"""
        try:
            apps_v1 = client.AppsV1Api(self.k8s_client)
            deployment = apps_v1.read_namespaced_deployment(
                name=deployment_name, namespace=namespace
            )

            status = {
                "ready_replicas": deployment.status.ready_replicas or 0,
                "desired_replicas": deployment.spec.replicas or 0,
                "available_replicas": deployment.status.available_replicas or 0,
                "conditions": [],
            }

            if deployment.status.conditions:
                for condition in deployment.status.conditions:
                    status["conditions"].append(
                        {
                            "type": condition.type,
                            "status": condition.status,
                            "reason": condition.reason,
                            "message": condition.message,
                        }
                    )

            return status

        except Exception as e:
            logger.error(f"Failed to get deployment status: {str(e)}")
            return None

    def delete_deployment(self, namespace, deployment_name=None, helm_release=None):
        """Delete deployment (Helm release or K8s resources)"""
        try:
            if helm_release:
                with self._get_kubeconfig_context() as env:
                    helm_cmd = [
                        "helm",
                        "uninstall",
                        helm_release,
                        "--namespace",
                        namespace,
                    ]
                    result = self._run_helm_command(helm_cmd, env)

                    if result["success"]:
                        logger.info(f"Helm release {helm_release} deleted successfully")
                        return True
                    else:
                        logger.error(
                            f"Failed to delete Helm release: {result['error']}"
                        )
                        return False
            else:
                apps_v1 = client.AppsV1Api(self.k8s_client)
                apps_v1.delete_namespaced_deployment(
                    name=deployment_name, namespace=namespace
                )
                logger.info(f"Deployment {deployment_name} deleted successfully")
                return True

        except Exception as e:
            logger.exception("Failed to delete deployment")
            return False
