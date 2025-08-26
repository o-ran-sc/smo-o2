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

logger = logging.getLogger(__name__)
import traceback

class KubernetesDeploymentService:
    """Service for real Kubernetes deployments"""
    
    def __init__(self, k8s_cluster):
        self.k8s_cluster = k8s_cluster
        self.k8s_client = None
        self._setup_client()

    def delete_helm_release(self, namespace, release_name, force=False):
        """Delete Helm release"""
        print("Going to delete Noww...")
        k8s_config = self.k8s_cluster.get_kubernetes_config()
        try:
            env = os.environ.copy()
            temp_kubeconfig = self._get_helm_env(k8s_config)
            env['KUBECONFIG'] = temp_kubeconfig
            
            # Check if release exists
            list_cmd = ['helm', 'list', '-n', namespace, '-o', 'json']
            print(list_cmd)
            list_result = subprocess.run(list_cmd, capture_output=True, text=True, env=env)
            
            releases = json.loads(list_result.stdout) if list_result.stdout else []
            release_exists = any(r['name'] == release_name for r in releases)
            
            if not release_exists:
                return {'success': True, 'message': 'Release not found (already deleted)'}
            
            print("Going to delete Noww...")
            # Delete the release
            delete_cmd = ['helm', 'uninstall', release_name, '--namespace', namespace]
            if not force:
                delete_cmd.append('--wait')
            
            print(f"Deleting Helm release: {release_name}", flush=True)
            result = subprocess.run(delete_cmd, capture_output=True, text=True, env=env)
            print(result)
            
            if result.returncode == 0:
                return {'success': True, 'message': f'Helm release {release_name} deleted'}
            else:
                print("Termination failed")
                print(result.stderr)
                return {'success': False, 'error': result.stderr}
                
        except Exception as e:
            print(traceback.format_exc())
            return {'success': False, 'error': str(e)}

        finally:
            os.unlink(temp_kubeconfig)
    
    def cleanup_resources(self, namespace, deployment_name):
        """Clean up Kubernetes resources manually"""
        try:
            from kubernetes import client
            
            apps_v1 = client.AppsV1Api(self.k8s_client)
            core_v1 = client.CoreV1Api(self.k8s_client)
            
            cleaned = []
            
            # Delete deployment
            try:
                apps_v1.delete_namespaced_deployment(
                    name=deployment_name,
                    namespace=namespace
                )
                cleaned.append('deployment')
            except:
                pass
            
            # Delete services
            try:
                services = core_v1.list_namespaced_service(namespace=namespace)
                for service in services.items:
                    if deployment_name in service.metadata.name:
                        core_v1.delete_namespaced_service(
                            name=service.metadata.name,
                            namespace=namespace
                        )
                        cleaned.append(f'service/{service.metadata.name}')
            except:
                pass
            
            # Delete configmaps
            try:
                configmaps = core_v1.list_namespaced_config_map(namespace=namespace)
                for cm in configmaps.items:
                    if deployment_name in cm.metadata.name:
                        core_v1.delete_namespaced_config_map(
                            name=cm.metadata.name,
                            namespace=namespace
                        )
                        cleaned.append(f'configmap/{cm.metadata.name}')
            except:
                pass
            
            return {
                'success': True,
                'message': f'Cleaned up resources: {", ".join(cleaned)}'
            }
            
        except Exception as e:
            return {'success': False, 'error': str(e)}
    
    def delete_namespace(self, namespace, force=False):
        """Delete namespace and all resources within"""
        try:
            from kubernetes import client
            
            core_v1 = client.CoreV1Api(self.k8s_client)
            
            # Check if namespace exists
            try:
                core_v1.read_namespace(name=namespace)
            except client.exceptions.ApiException as e:
                if e.status == 404:
                    return {'success': True, 'message': 'Namespace already deleted'}
                raise
            
            # Delete namespace
            if force:
                # Force delete by removing finalizers
                body = client.V1Namespace(
                    metadata=client.V1ObjectMeta(
                        name=namespace,
                        finalizers=[]
                    )
                )
                core_v1.patch_namespace(name=namespace, body=body)
            
            # Delete the namespace
            core_v1.delete_namespace(name=namespace)
            
            print(f"Deleted namespace: {namespace}", flush=True)
            return {'success': True, 'message': f'Namespace {namespace} deleted'}
            
        except Exception as e:
            return {'success': False, 'error': str(e)}


    def _get_helm_env(self, k8s_config):
        with tempfile.NamedTemporaryFile(mode='w', suffix='.yaml', delete=False) as f:
            kubeconfig_content = base64.b64decode(k8s_config['kubeconfig']).decode()
            f.write(kubeconfig_content)
            temp_kubeconfig = f.name

        return temp_kubeconfig

    def upgrade_helm_release(self, namespace, release_name, chart_repo, chart_path, values=None):
        """Upgrade existing Helm release - like helm upgrade"""
        """Deploy Helm chart from any source - repository or GitHub"""
        k8s_config = self.k8s_cluster.get_kubernetes_config()

        
        try:
            # Check if release exists
            env = os.environ.copy()
            temp_kubeconfig = self._get_helm_env(k8s_config)
            env['KUBECONFIG'] = temp_kubeconfig

            check_cmd = ['helm', 'list', '-n', namespace, '-o', 'json']
            check_result = subprocess.run(check_cmd, capture_output=True, text=True, env=env)

            print(check_result)
            
            if check_result.returncode != 0:
                return {'success': False, 'error': 'Failed to check release status'}
            
            releases = json.loads(check_result.stdout) if check_result.stdout else []
            release_exists = any(r['name'] == release_name for r in releases)
            
            if not release_exists:
                return {'success': False, 'error': f'Release {release_name} not found'}
            
            # Set KUBECONFIG environment variable for Helm
            if 'github.com' in chart_repo:
                return self._upgrade_from_git(namespace, release_name, chart_repo, chart_path, values, env)
            else:
                return self._upgrade_from_helm_repo(namespace, release_name, chart_repo, chart_path, values, env)
                
        finally:
            # Clean up
            os.unlink(temp_kubeconfig)

        # except Exception as e:
        #     logger.error(f"Helm upgrade failed: {str(e)}")
        #     return {'success': False, 'error': str(e)}

    def _upgrade_from_git(self, namespace, release_name, repo_url, chart_path, values=None, env=None):
        """Upgrade from Git repository"""
        import tempfile
        import shutil
        
        work_dir = None
        temp_files = []
        logger.info("Upgrade called") 
        
        try:
            # Clone repository (same as deploy)
            work_dir = tempfile.mkdtemp()
            clone_cmd = ['git', 'clone', '--depth', '1', repo_url, work_dir]
            
            if subprocess.run(clone_cmd, capture_output=True).returncode != 0:
                return {'success': False, 'error': 'Failed to clone repository'}
            
            # Build dependencies
            chart_location = os.path.join(work_dir, chart_path)
            subprocess.run(['helm', 'dependency', 'build', chart_location], capture_output=True)

            print(values)
            
            # Prepare values
            values_files = self._prepare_helm_values(work_dir, chart_path, values)

            logger.info("Upgrade called") 
            print(values_files)

            # Build upgrade command
            helm_cmd = [
                'helm', 'upgrade', release_name, chart_location,
                '--namespace', namespace,
                '--wait',
                '--timeout', '10m',
                '--force'
            ]
            
            # Add values files
            for vf in values_files:
                helm_cmd.extend(['--values', vf])
                if vf.startswith('/tmp'):
                    temp_files.append(vf)
            
            # Execute upgrade
            result = subprocess.run(helm_cmd, capture_output=True, text=True, env=env)
            logger.error(helm_cmd)
            
            if result.returncode == 0:
                print(f"Upgraded {release_name}")
                return {'success': True, 'message': 'Upgrade successful'}
            else:
                # Build upgrade command
                helm_cmd = [
                    'helm', 'rollback', release_name, 
                    '--namespace', namespace,
                    '--wait',
                    '--timeout', '10m',
                    '--force'
                ]
                rollback_attempt = subprocess.run(helm_cmd, capture_output=True, text=True, env=env)
                print(rollback_attempt.stderr)
                return {'success': False, 'error': result.stderr}
                
        finally:
            if work_dir:
                shutil.rmtree(work_dir, ignore_errors=True)
            for tf in temp_files:
                if os.path.exists(tf):
                    os.unlink(tf) 
    
    def _setup_client(self):
        """Setup Kubernetes client"""
        try:
            k8s_config = self.k8s_cluster.get_kubernetes_config()
            
            if 'kubeconfig' in k8s_config:

                kubeconfig_content = base64.b64decode(k8s_config['kubeconfig']).decode()

                # Use kubeconfig file
                with tempfile.NamedTemporaryFile(mode='w', suffix='.config', delete=False) as f:
                    f.write(kubeconfig_content)
                    kubeconfig_path = f.name
                    logger.error(kubeconfig_path)
                
                config.load_kube_config(config_file=kubeconfig_path)
                configuration = client.Configuration()
                config.load_kube_config(config_file=kubeconfig_path, 
                                  client_configuration=configuration)
                #config.load_kube_config(config_file='/home/infidel/Sync/NTUST/BMW-Lab/K8S-CICD-cluster/CD/kubeconfigs/smo-nnag.config')
                os.unlink(kubeconfig_path)  # Clean up temp file
                self.k8s_client = client.ApiClient(configuration=configuration)
            else:
                # Use direct configuration
                configuration = client.Configuration()
                configuration.host = k8s_config['host']
                configuration.verify_ssl = k8s_config.get('verify_ssl', True)
                
                if 'api_key' in k8s_config:
                    configuration.api_key = k8s_config['api_key']
                
                client.Configuration.set_default(configuration)
            
            logger.error(self.k8s_client)
            logger.info(f"Kubernetes client setup successful for {self.k8s_cluster.cloud_id}")
            
        except Exception as e:
            logger.exception(e)
            logger.error(f"Failed to setup Kubernetes client: {str(e)}")
            raise
    
    def test_connection(self):
        """Test Kubernetes cluster connection"""
        try:
            v1 = client.CoreV1Api(self.k8s_client)
            ns = v1.list_namespace()
            
            # Update cluster status
            self.k8s_cluster.connection_status = 'CONNECTED'
            self.k8s_cluster.last_health_check = timezone.now()
            self.k8s_cluster.save()
            
            return True

        except Exception as e:
            logger.error(f"Kubernetes connection test failed: {str(e)}")
            self.k8s_cluster.connection_status = 'ERROR'
            self.k8s_cluster.save()
            return False
    def create_namespace(self, namespace):
        """Create namespace if it doesn't exist"""
        try:
            v1 = client.CoreV1Api(self.k8s_client)
            
            # Check if namespace exists
            try:
                v1.read_namespace(name=namespace)
                logger.info(f"Namespace {namespace} already exists")
                return True
            except ApiException as e:
                if e.status != 404:
                    raise
            
            # Create namespace
            namespace_manifest = client.V1Namespace(
                metadata=client.V1ObjectMeta(name=namespace)
            )
            v1.create_namespace(body=namespace_manifest)
            logger.info(f"Created namespace: {namespace}")
            return True
            
        except Exception as e:
            logger.error(f"Failed to create namespace {namespace}: {str(e)}")
            return False


    def deploy_helm_chart(self, namespace, release_name, chart_repo, chart_path, values=None):
        """Deploy Helm chart from any source - repository or GitHub"""
        k8s_config = self.k8s_cluster.get_kubernetes_config()


        # Create temporary kubeconfig file for Helm
        with tempfile.NamedTemporaryFile(mode='w', suffix='.yaml', delete=False) as f:
            kubeconfig_content = base64.b64decode(k8s_config['kubeconfig']).decode()
            f.write(kubeconfig_content)
            temp_kubeconfig = f.name
        
        try:
            # Set KUBECONFIG environment variable for Helm
            env = os.environ.copy()
            env['KUBECONFIG'] = temp_kubeconfig
            
            # Now Helm will use the correct cluster
            print(f"Chart Repo {chart_repo}")
            if 'git' in chart_repo:
                return self._deploy_from_git(namespace, release_name, chart_repo, 
                                           chart_path, values, env)
            else:
                return self._deploy_from_helm_repo(namespace, release_name, chart_repo, 
                                                 chart_path, values, env)
        finally:
            # Clean up
            os.unlink(temp_kubeconfig)

        #------------------------------------
        # print(values)
        # try:
        #     if not self.create_namespace(namespace):
        #         return False
        #     
        #     # For GitHub repos, chart_path is the path to the chart
        #     # For Helm repos, chart_path is just the chart name
        #     if 'github.com' in chart_repo or chart_repo.startswith('git@'):
        #         return self._deploy_from_git(namespace, release_name, chart_repo, chart_path, values)
        #     else:
        #         # Traditional Helm repository
        #         return self._deploy_from_helm_repo(namespace, release_name, chart_repo, chart_path, values)
        # 
        # except Exception as e:
        #     logger.error(f"Deployment failed: {str(e)}")
        #     return False

    def _deploy_from_git(self, namespace, release_name, repo_url, chart_path, values=None, env=None):
        """Deploy chart from Git repository - the ArgoCD way"""
        import tempfile
        import shutil
        
        work_dir = None
        values_file = None
        temp_files = []

        logger.error("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        logger.error("Deploy from GIT")
        logger.error("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        
        try:
            # Create temporary directory
            work_dir = tempfile.mkdtemp()
            
            # Clone only what we need - single branch, shallow
            print(f"Fetching chart from: {repo_url}")
            clone_cmd = [
                'git', 'clone', 
                '--depth', '1',
                '--single-branch',
                repo_url, 
                work_dir
            ]
            
            result = subprocess.run(clone_cmd, capture_output=True, text=True)
            if result.returncode != 0:
                logger.error(f"Git clone failed: {result.stderr}")
                return False

            logger.error("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
            logger.error(result)
            logger.error("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
            
            # The chart is at repo_root/chart_path
            chart_location = os.path.join(work_dir, chart_path)
            
            if not os.path.exists(chart_location):
                logger.error(f"Chart not found at path: {chart_path}")
                return False

            logger.info(f"Building chart dependencies...")
            dep_result = subprocess.run(
                ['helm', 'dependency', 'build', chart_location],
                capture_output=True, 
                text=True
            )
            
            if dep_result.returncode != 0:
                # Log but don't fail - some charts have no dependencies
                print(f"Dependency build note: {dep_result.stderr}")
            else:
                print("Dependencies built")

            values_files = self._prepare_helm_values(work_dir, chart_path, values)

            # Dry RUN
            helm_cmd = [
                # Build Helm command
                'helm', 'install', release_name, chart_location,
                '--dry-run',
                '--namespace', namespace,
                '--wait',
                '--create-namespace',
                '--timeout', '10m'
            ]
            

            for vf in values_files:
                helm_cmd.extend(['--values', vf])
                if vf.startswith('/tmp'):
                    temp_files.append(vf)
            
            # Deploy
            result = subprocess.run(helm_cmd, capture_output=True, text=True, env=env)
            
            # print("|DRY RUN|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
            # print(result)
            # print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")

            if result.returncode != 0:
                print(f"Dry RUN Failed {release_name}")
                #return True
                return {
                    'success': False,
                    'error': result.stderr,
                }
            
            # Normal Uninstall
            helm_cmd = [
                # Build Helm command 'helm', 'install', release_name, chart_location,
                '--namespace', namespace,
                '--wait',
                '--create-namespace',
                '--timeout', '10m'
            ]
            

            for vf in values_files:
                helm_cmd.extend(['--values', vf])
                if vf.startswith('/tmp'):
                    temp_files.append(vf)
            
            # Deploy
            result = subprocess.run(helm_cmd, capture_output=True, text=True, env=env)
            
            print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
            print(result)
            print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
            # return result.returncode == 0

            if result.returncode == 0:
                print(f"Deployed {release_name}")
                #return True
                return {'success': True, 'message': f'Deployed {release_name}'}
            else:

                # Helm Uninstall
                # helm_cmd = [
                #     'helm', 'uninstall', release_name, 
                #     '--namespace', namespace,
                #     '--wait',
                #     '--timeout', '10m'
                # ]

                # Helm Delete
                # helm_cmd = [
                #     # Build Helm command
                #     'helm', 'delete', release_name, 
                #     '--namespace', namespace,
                #     '--wait',
                #     '--timeout', '10m'
                #]

                #helm delete my-release --purge
                #uninstall = subprocess.run(helm_cmd, capture_output=True, text=True, env=env)

                if "provided port" in result.stderr:
                    print("Port Error Condition")
                    return {'success': True, 'message': f'Deployed {release_name} with Port Error'}

                print("Attempt uninstall")
                #print(uninstall)
                #logger.error(f"Helm install failed: {result.stderr}")
                #logger.error(f"Uninstall: {uninstall.stderr}")

                return {
                    'success': False,
                    'error': result.stderr,
                }
                
        finally:
            # Clean up
            if work_dir and os.path.exists(work_dir):
                shutil.rmtree(work_dir)
            if values_file and os.path.exists(values_file.name):
                os.unlink(values_file.name)
    
    def _deploy_from_helm_repo(self, namespace, release_name, repo_url, chart_name, values=None, env=None):
        """Deploy from standard Helm repository"""
        repo_name = 'temp-repo'
        
        # Add repository
        subprocess.run(['helm', 'repo', 'add', repo_name, repo_url], capture_output=True)
        subprocess.run(['helm', 'repo', 'update'], capture_output=True)
        
        # Deploy
        helm_cmd = [
            'helm', 'install', release_name, f"{repo_name}/{chart_name}",
            '--namespace', namespace,
            '--wait',
            '--timeout', '10m'
        ]
        
        # Handle values as before...
        
        result = subprocess.run(helm_cmd, capture_output=True, text=True, env=env)
        return result.returncode == 0


    def _prepare_helm_values(self, work_dir, chart_path, user_values):
        print(user_values)
        """Find and merge values intelligently"""
        final_values = {}
        values_files = []
        
        # Common values file patterns - check them silently
        common_files = [
            'values.yaml',
            'values-prod.yaml',
            'values-production.yaml',
            'prod-values.yaml',
            'production.yaml'
        ]
        
        # Look for values files in the chart
        chart_dir = os.path.join(work_dir, chart_path)
        for filename in common_files:
            filepath = os.path.join(chart_dir, filename)
            if os.path.exists(filepath):
                values_files.append(filepath)
        
        # Also check parent directory
        parent_dir = os.path.dirname(chart_dir)
        for filename in ['values.yaml', 'global-values.yaml']:
            filepath = os.path.join(parent_dir, filename)
            if os.path.exists(filepath):
                values_files.append(filepath)
        
        # User values always win
        if user_values:
            temp_file = tempfile.NamedTemporaryFile(mode='w', suffix='.yaml', delete=False)
            if isinstance(user_values, str):
                temp_file.write(user_values)
            else:
                yaml.dump(user_values, temp_file)
            temp_file.close()
            values_files.append(temp_file.name)
        
        return values_files

    def deploy_manifests(self, namespace, manifests):
        """Deploy raw Kubernetes manifests"""
        try:
            # Ensure namespace exists
            if not self.create_namespace(namespace):
                return False
            
            deployed_resources = []
            
            for manifest_content in manifests:
                # Apply manifest using kubectl
                with tempfile.NamedTemporaryFile(mode='w', suffix='.yaml', delete=False) as f:
                    f.write(manifest_content)
                    manifest_file = f.name
                
                kubectl_cmd = ['kubectl', 'apply', '-f', manifest_file, '-n', namespace]
                result = subprocess.run(kubectl_cmd, capture_output=True, text=True)
                
                os.unlink(manifest_file)  # Clean up
                
                if result.returncode == 0:
                    deployed_resources.append(manifest_content)
                    logger.info(f"Applied manifest successfully")
                else:
                    logger.error(f"Failed to apply manifest: {result.stderr}")
                    return False
            
            return deployed_resources
            
        except Exception as e:
            logger.error(f"Manifest deployment error: {str(e)}")
            return False
    
    def get_deployment_status(self, namespace, deployment_name):
        """Get deployment status"""
        try:
            apps_v1 = client.AppsV1Api(self.k8s_client)
            deployment = apps_v1.read_namespaced_deployment(
                name=deployment_name, 
                namespace=namespace
            )
            
            status = {
                'ready_replicas': deployment.status.ready_replicas or 0,
                'desired_replicas': deployment.spec.replicas or 0,
                'available_replicas': deployment.status.available_replicas or 0,
                'conditions': []
            }
            
            if deployment.status.conditions:
                for condition in deployment.status.conditions:
                    status['conditions'].append({
                        'type': condition.type,
                        'status': condition.status,
                        'reason': condition.reason,
                        'message': condition.message
                    })
            
            return status
            
        except Exception as e:
            logger.error(f"Failed to get deployment status: {str(e)}")
            return None
    
    def delete_deployment(self, namespace, deployment_name=None, helm_release=None):
        """Delete deployment (Helm release or K8s resources)"""
        try:
            if helm_release:
                # Delete Helm release
                helm_cmd = ['helm', 'uninstall', helm_release, '--namespace', namespace]
                result = subprocess.run(helm_cmd, capture_output=True, text=True)
                
                if result.returncode == 0:
                    logger.info(f"Helm release {helm_release} deleted successfully")
                    return True
                else:
                    logger.error(f"Failed to delete Helm release: {result.stderr}")
                    return False
            else:
                # Delete specific deployment
                apps_v1 = client.AppsV1Api(self.k8s_client)
                apps_v1.delete_namespaced_deployment(
                    name=deployment_name,
                    namespace=namespace
                )
                logger.info(f"Deployment {deployment_name} deleted successfully")
                return True
                
        except Exception as e:
            logger.error(f"Failed to delete deployment: {str(e)}")
            return False
