from rest_framework import viewsets, status, filters
from rest_framework.decorators import action
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend
from django.utils import timezone
from .services.kubernetes_service import KubernetesDeploymentService 
from .models import (
        DeploymentDescriptor,
        NfDeploymentInstance,
        VnfLcmOperation,
        KubernetesCluster,
        KubernetesDeployment,
        )
from .serializers import (
        NfDeploymentInstanceSerializer,
        DeploymentDescriptorSerializer,
        KubernetesClusterSerializer,
        KubernetesDeploymentSerializer,
        DeploymentDescriptorDetailedSerializer,
        NfDeploymentInstanceDetailedSerializer,
        KubernetesDeploymentDetailedSerializer,
        KubernetesClusterCredentialsSerializer,
        )

from .tools.tools import (
        JSONProcess,
        KubernetesResourceMetrics,
        )

import logging
import uuid
import traceback

from datetime import timedelta
logger = logging.getLogger(__name__)

class DeploymentDescriptorViewSet(viewsets.ModelViewSet): 
    """O2dms VNF Instances (Deployment Descriptors)"""
    queryset = DeploymentDescriptor.objects.all()
    serializer_class = DeploymentDescriptorSerializer
    

    def create(self, request):
        """Create NF Deployment Descriptor"""
        data = request.data.copy()

        # User sends only values
        values = data.pop('values', None)
        print(values)
        
        if values and isinstance(values, dict):
            # Expand any dot notation
            values = JSONProcess.expand_dot_notation(self, values)
            data['additional_params'] = {'values': values}

        serializer = self.get_serializer(data=data)
        if serializer.is_valid():
            descriptor = serializer.save()
            return Response(
                DeploymentDescriptorSerializer(descriptor).data,
                status=status.HTTP_201_CREATED
            )
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class NfDeploymentInstanceViewSet(viewsets.ModelViewSet):
    """O2dms NF Deployment Instances"""
    queryset = NfDeploymentInstance.objects.all()
    serializer_class = NfDeploymentInstanceSerializer
    lookup_field = 'instance_id'
    
    """O2dms NF: Instantiate"""
    @action(detail=True, methods=['post'])
    def instantiate(self, request, instance_id=None):
        """Instantiate NF Deployment"""
        instance = self.get_object()
        
        stored_values = {}
        # Get stored values from the descriptor
        if hasattr(instance.descriptor, 'additional_params') and instance.descriptor.additional_params:
            stored_values = instance.descriptor.additional_params.get('values', {})

        print("Stored Value: ",stored_values)
        # Get values from instantiation request

        request_values = request.data.get('values', {})
        request_values = JSONProcess.expand_dot_notation(self, request_values)

        print(request_values)
        
        # Merge them - request values override stored values
        final_values = {**stored_values, **request_values}
        #final_values = request_values
        try:
            instance = NfDeploymentInstance.objects.get(instance_id=instance_id)
        except NfDeploymentInstance.DoesNotExist:
            return Response(
                {'error': 'Deployment instance not found'},
                status=status.HTTP_404_NOT_FOUND
            )
        
        # if instance.instantiation_state != 'NOT_INSTANTIATED' and instance.instantiation_state != 'UPDATING':
        #     return Response(
        #         {'error': f'Instance is in {instance.instantiation_state} state'},
        #         status=status.HTTP_409_CONFLICT
        #     )

        # Create LCM operation
        operation = VnfLcmOperation.objects.create(
            vnf_instance=instance,
            operation_type='INSTANTIATE',
            operation_params=request.data,
            max_retries=request.data.get('max_retries', 0),
            rollback_on_failure=request.data.get('rollback_on_failure', True),
            automatic_rollback=request.data.get('automatic_rollback', True),
            execution_attempts = []
        )
        
        if operation.rollback_on_failure:
            operation.state_snapshot = self._capture_state_snapshot(instance)
            operation.save()

        # Update instance state
        instance.instantiation_state = 'INSTANTIATING'
        instance.smo_callback_url = request.data.get('callback_url', '')
        instance.save()
        
        # Allocate resources from existing infrastructure
        #allocated_machines = self.allocate_machines(instance)

        
        # Deploy based on profile type
        try:
            if instance.descriptor.profile_type == 'kubernetes':
                #result = self.deploy_kubernetes(instance, request.data, final_values)
                result = self._execute_with_retry(
                    operation,
                    self.deploy_kubernetes,
                    instance,
                    request.data,
                    final_values
                )
            elif instance.descriptor.profile_type == 'etsi_nfv':
                result = self.deploy_to_vms(instance, allocated_machines, request.data)
                result = self._execute_with_retry(
                    operation,
                    self.deploy_to_vms,
                    instance,
                    allocated_machines,
                    request.data
                )
            
            print("---->----")
            print(result)
            if result['success']:
                instance.instantiation_state = 'INSTANTIATED'
                instance.deployed_cluster = instance.descriptor.target_cluster
                logger.info("---------------------------------------")
                logger.info(instance.descriptor.target_cluster)
                #instance.allocated_machines.set(allocated_machines)
                instance.save()
                
                operation.operation_state = 'COMPLETED'
                operation.progress_percentage = 100
                operation.end_time = timezone.now()
                operation.save()
                
                return Response({
                    'vnfLcmOpOccId': str(operation.operation_id),
                    'operationState': 'COMPLETED'
                }, status=status.HTTP_202_ACCEPTED)

            else:
                if operation.rollback_on_failure and operation.automatic_rollback:
                    operation.operation_state = 'ROLLING_BACK'
                    operation.save()
                    
                    rollback_success = self._execute_rollback(instance, operation)
                    
                    if rollback_success:
                        operation.operation_state = 'ROLLED_BACK'
                    else:
                        operation.operation_state = 'FAILED'
                else:
                    operation.operation_state = 'FAILED'
                    instance.instantiation_state = 'ERROR'
                    instance.save()

            operation.end_time = timezone.now()
            operation.save()

            raise Exception(result.get('error', 'Deployment failed'))
                
        except Exception as e:
            print("Error")
            instance.instantiation_state = 'ERROR'
            instance.save()
            operation.operation_state = 'FAILED'
            operation.error_details = {'error': str(e)}
            operation.end_time = timezone.now()
            operation.save()

            print(traceback.format_exc())

            #return Response(
            #    {'error': f'Deployment failed: {str(e)}'},
            #    status=status.HTTP_500_INTERNAL_SERVER_ERROR
            #)

            return Response(
                {
                    'error': f'Deployment failed: {str(e)}',
                    'vnfLcmOpOccId': str(operation.operation_id),
                    'operationState': operation.operation_state,
                    'rollbackExecuted': operation.rollback_executed if hasattr(operation, 'rollback_executed') else False
                },
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )
    
    def deploy_to_vms(self, instance, machines, params):
        """Deploy to VMs using  existing infrastructure"""
        try:
            vm_instances = []
            
            for machine in machines:
                vm_instance = {
                    'machine_id': machine.id,
                    'hostname': machine.hostname,
                    'ip': str(machine.ip),
                    'status': 'provisioning'
                }
                vm_instances.append(vm_instance)
                
                machine.status = 'PROVISIONING'
                machine.save()
            
            instance.vm_instances = vm_instances
            instance.save()
            
            # TODO: Implement actual VM provisioning using  existing PXE infrastructure
            
            return {'success': True, 'vm_instances': vm_instances}
            
        except Exception as e:
            return {'success': False, 'error': str(e)}

    def deploy_kubernetes(self, instance, params, values):
        """Deploy to Kubernetes cluster"""
        logger.error("Deploy to Kubernetes")

        try:
            # Get Kubernetes cluster configuration
            logger.error("**********************************************************8")
            logger.error(f"Instance {instance}")
            logger.error(f"Target cluster {instance.descriptor.target_cluster_id}")
            logger.error(f"Type: {type(instance.descriptor.target_cluster)}")
            logger.error(f"Value: {instance.descriptor.target_cluster}")
            logger.error(f"Repr: {repr(instance.descriptor.target_cluster)}")
            logger.error("**********************************************************8")
            try:
                k8s_cluster = KubernetesCluster.objects.get(cloud_id=instance.descriptor.target_cluster_id)
            except KubernetesCluster.DoesNotExist:
                return {'success': False, 'error': 'No Kubernetes configuration found for cluster'}
            
            logger.error("**********************************************************8")
            logger.error("Helm Called...")
            logger.error("**********************************************************8")

            # Create deployment service

            k8s_service = KubernetesDeploymentService(k8s_cluster)
            
            # Test connection
            if not k8s_service.test_connection():
                #return {'success': False, 'error': 'Cannot connect to Kubernetes cluster'}
                return {
                    'success': False, 
                    'error': 'Cannot connect to Kubernetes cluster',
                    'error_type': 'connection_error'
                }

            # Check if user specified a namespace
            user_namespace = params.get('instantiation_params', {}).get('namespace')
            if user_namespace:
                namespace = user_namespace
            else:
                # Generate only if not provided
                namespace = f"nf-{str(instance.instance_id)[:8]}"

            # Prepare deployment details
            deployment_name = f"nf-{instance.name.lower()}"

            if not k8s_service.create_namespace(namespace):
                #return {'success': False, 'error': f'Failed to create namespace {namespace}'}
                return {
                    'success': False,
                    'error': f'Failed to create namespace {namespace}',
                    'error_type': 'namespace_error'
                }
            
            instance.deployment_namespace = namespace
            #instance.helm_release_name = helm_release_name
            instance.save()

            k8s_deployment, created = KubernetesDeployment.objects.update_or_create(
                nf_instance=instance,
                defaults={
                    'k8s_cluster': k8s_cluster,
                    'namespace': namespace,
                    'deployment_name': deployment_name,
                }
            )
            
            # Deploy based on artifact type
            descriptor = instance.descriptor
            if descriptor.artifact_repo_url and descriptor.artifact_name:
                logger.error("Helm Called...")
                # Helm deployment
                helm_release = f"nf-{instance.name.lower()}"
                helm_values = params.get('instantiation_params', {})
                
                result = k8s_service.deploy_helm_chart(
                    namespace=namespace,
                    release_name=helm_release,
                    chart_repo=descriptor.artifact_repo_url,
                    chart_path=descriptor.artifact_name,
                    values=values
                )

                k8s_deployment.helm_release_name = helm_release
                
                if result['success']:
                    #k8s_deployment.helm_values = helm_values
                    k8s_deployment.helm_values =  values
                    k8s_deployment.kubernetes_status = 'RUNNING'
                else:
                    k8s_deployment.kubernetes_status = 'FAILED'
                    k8s_deployment.error_message = result.get('error', 'Helm deployment failed')

                    # k8s_deployment.error_message = result['error']
                    # raise Exception(result.get('error', 'Upgrade failed'))

                    # Classify error type
                    error_type = result.get('error_type', 'unknown')
                    if 'port' in str(result.get('error', '')).lower() and 'allocated' in str(result.get('error', '')).lower():
                        error_type = 'port_conflict'
                    elif 'timeout' in str(result.get('error', '')).lower():
                        error_type = 'timeout'
                    
                    return {
                        'success': False,
                        'error': result.get('error', 'Helm deployment failed'),
                        'error_type': error_type
                    }

                    
            else:
                # Direct manifest deployment
                manifests = params.get('manifests', [])
                if manifests:
                    deployed_manifests = k8s_service.deploy_manifests(namespace, manifests)
                    if deployed_manifests:
                        k8s_deployment.deployed_manifests = deployed_manifests
                        k8s_deployment.kubernetes_status = 'RUNNING'
                    else:
                        k8s_deployment.kubernetes_status = 'FAILED'
                        k8s_deployment.error_message = 'Manifest deployment failed'
                else:
                    # Create default deployment
                    default_manifest = self.generate_default_deployment_manifest(
                        deployment_name, namespace, descriptor
                    )
                    deployed_manifests = k8s_service.deploy_manifests(namespace, [default_manifest])
                    if deployed_manifests:
                        k8s_deployment.deployed_manifests = deployed_manifests
                        k8s_deployment.kubernetes_status = 'RUNNING'
            
            k8s_deployment.save()
            
            # Update NF instance
            instance.deployment_namespace = namespace
            instance.save()

            print("----------->")
            print(result)

            return {
                'success': k8s_deployment.kubernetes_status == 'RUNNING',
                'namespace': namespace,
                'deployment_name': deployment_name,
                'k8s_deployment_id': k8s_deployment.id
            }
            
        except Exception as e:
            #logger.error(f"Kubernetes deployment failed: {str(e)}")
            #return {'success': False, 'error': str(e)}

            logger.error(f"Kubernetes deployment failed: {str(e)}")
            error_type = 'exception'
            if 'connection' in str(e).lower():
                error_type = 'connection_error'
            elif 'permission' in str(e).lower():
                error_type = 'permission_denied'
                
            return {
                'success': False,
                'error': str(e),
                'error_type': error_type
            }


    @action(detail=True, methods=['post'])
    def terminate(self, request, instance_id=None):
        """Terminate NF Deployment"""
        instance = self.get_object()
        
        # Check if already terminating or terminated
        if instance.instantiation_state in ['TERMINATING', 'NOT_INSTANTIATED']:
            return Response(
                {'error': f'Instance is already {instance.instantiation_state}'},
                status=status.HTTP_409_CONFLICT
            )
        
        # Create termination operation
        operation = VnfLcmOperation.objects.create(
            vnf_instance=instance,
            operation_type='TERMINATE',
            operation_params=request.data
        )
        
        # Update state
        instance.instantiation_state = 'TERMINATING'
        instance.save()
        
        try:
            # Perform termination
            graceful = request.data.get('graceful', True)
            force = request.data.get('force', False)
            cleanup_namespace = request.data.get('cleanup_namespace', True)
            
            result = self.terminate_kubernetes(
                instance, 
                graceful=graceful,
                force=force,
                cleanup_namespace=cleanup_namespace
            )

            print(f"RESULT TERMINATION {result}")
            
            if result['success'] or force:
                # Clean up database records
                instance.instantiation_state = 'NOT_INSTANTIATED'
                instance.deployment_namespace = None
                instance.helm_release_name = None
                #instance.allocated_machines.clear()
                instance.save()
                
                # Mark operation complete
                operation.operation_state = 'COMPLETED'
                operation.progress_percentage = 100
                operation.end_time = timezone.now()
                operation.save()
                
                return Response({
                    'vnfLcmOpOccId': str(operation.operation_id),
                    'operationState': 'COMPLETED',
                    'message': 'Termination successful'
                })
            else:
                print(result)
                raise Exception(result.get('error', 'Termination failed'))
                
        except Exception as e:
            instance.instantiation_state = 'ERROR'
            instance.save()
            
            operation.operation_state = 'FAILED'
            operation.error_details = {'error': str(e)}
            operation.end_time = timezone.now()
            operation.save()
            
            return Response(
                {'error': f'Termination failed: {str(e)}'},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )

    def terminate_kubernetes(self, instance, graceful=True, force=False, cleanup_namespace=True):
        """Terminate Kubernetes deployment"""
        try:
            # Get deployment info
            k8s_deployment = getattr(instance, 'k8s_deployment', None)
            if not k8s_deployment:
                return {'success': True, 'message': 'No Kubernetes deployment found'}
            
            k8s_cluster = k8s_deployment.k8s_cluster
            k8s_service = KubernetesDeploymentService(k8s_cluster)
            
            # Test connection
            if not k8s_service.test_connection() and not force:
                return {'success': False, 'error': 'Cannot connect to Kubernetes cluster'}
            
            termination_steps = []

            print("++++++++++++++++++++++++++++++")
            print(k8s_deployment.id)
            print(k8s_deployment.helm_release_name)
            print(instance.deployment_namespace)
            
            # Step 1: Delete Helm release
            if k8s_deployment.helm_release_name:
                helm_result = k8s_service.delete_helm_release(
                    namespace=instance.deployment_namespace,
                    release_name=k8s_deployment.helm_release_name,
                    force=force
                )
                print(helm_result)
                print("++++++++++++++++++++++++++++++")
                termination_steps.append({
                    'step': 'helm_release',
                    'success': helm_result.get('success', False),
                    'message': helm_result.get('message', '')
                })
            
            # Step 2: Clean up any remaining resources
            if graceful and not k8s_deployment.helm_release_name:
                # Manual cleanup for non-Helm deployments
                cleanup_result = k8s_service.cleanup_resources(
                    namespace=instance.deployment_namespace,
                    deployment_name=k8s_deployment.deployment_name
                )
                termination_steps.append({
                    'step': 'resource_cleanup',
                    'success': cleanup_result.get('success', False),
                    'message': cleanup_result.get('message', '')
                })
            
            # Step 3: Delete namespace if requested
            if cleanup_namespace and instance.deployment_namespace:
                ns_result = k8s_service.delete_namespace(
                    namespace=instance.deployment_namespace,
                    force=force
                )
                termination_steps.append({
                    'step': 'namespace_deletion',
                    'success': ns_result.get('success', False),
                    'message': ns_result.get('message', '')
                })
            print("Where the fuck it failed?") 
            # Step 4: Clean up database records
            print(k8s_deployment.nf_instance)
            print(k8s_deployment.kubernetes_status)
            print(k8s_deployment.pk)
            print(k8s_deployment.id)
            k8s_deployment.kubernetes_status = 'TERMINATED'

            # Constraint Failed
            k8s_deployment.save()

            print("Did you pass?") 
            
            # Determine overall success
            all_success = all(step['success'] for step in termination_steps)
            
            return {
                'success': all_success or force,
                'steps': termination_steps,
                'message': 'Termination completed' if all_success else 'Termination completed with errors'
            }
            
        except Exception as e:
            print(traceback.format_exc())
            logger.error(f"Termination failed: {str(e)}")
            if force:
                return {'success': True, 'message': f'Force terminated despite error: {str(e)}'}
            return {'success': False, 'error': str(e)}

    @action(detail=True, methods=['post'])
    def upgrade(self, request, instance_id=None):
        """Upgrade deployment with new values - like helm upgrade"""
        instance = self.get_object()
        
        # Temporary Disable
        # TODO: Should we update broken installation? or the creator should only deploy correct deployment
        # Enabling this will prevent us from upgrading broken helm caused by upgrade sequence
        # if instance.instantiation_state != 'INSTANTIATED':
        #     return Response(
        #         {'error': f'Cannot upgrade instance in {instance.instantiation_state} state'},
        #         status=status.HTTP_409_CONFLICT
        #     )
        
        # Get existing values from descriptor

        stored_values = {}
        if hasattr(instance.descriptor, 'additional_params') and instance.descriptor.additional_params:
            stored_values = instance.descriptor.additional_params.get('values', {})
        
        # Get new values from request
        new_values = request.data.get('values', {})

        if new_values and isinstance(new_values, dict):
            # Expand any dot notation
            new_values = JSONProcess.expand_dot_notation(self, new_values)
            #data['additional_params'] = {'values': newvalues}

        print(new_values)
        print(stored_values)
        print("**************")
        
        # Merge values - new values override stored
        final_values = {**stored_values, **new_values}
        
        # Create LCM operation for upgrade
        operation = VnfLcmOperation.objects.create(
            vnf_instance=instance,
            operation_type='UPDATE',
            operation_params={
                'values': new_values,
                'previous_values': stored_values
            }
        )
        
        # Update instance state
        instance.instantiation_state = 'UPDATING'
        instance.save()
        
        try:
            # Perform the upgrade
            print(final_values)
            print('----------------')
            result = self.upgrade_kubernetes(instance, final_values)

            print(result)
            
            if result['success']:
                # Update stored values if requested
                if request.data.get('persist_values', True):
                    instance.descriptor.additional_params = {'values': final_values}
                    instance.descriptor.save()
                
                instance.instantiation_state = 'INSTANTIATED'
                instance.save()
                
                operation.operation_state = 'COMPLETED'
                operation.progress_percentage = 100
                operation.end_time = timezone.now()
                operation.save()
                
                return Response({
                    'vnfLcmOpOccId': str(operation.operation_id),
                    'operationState': 'COMPLETED',
                    'message': 'Upgrade successful'
                }, status=status.HTTP_202_ACCEPTED)
            else:
                raise Exception(result.get('error', 'Upgrade failed'))
                
        except Exception as e:
            # TODO: Why should we allow the instance change? 
            # What if the error state is caused by upgrade?
            #instance.instantiation_state = 'ERROR'
            #instance.save()
            
            operation.operation_state = 'FAILED'
            operation.error_details = {'error': str(e)}
            operation.end_time = timezone.now()
            operation.save()
            
            return Response(
                {'error': f'Upgrade failed: {str(e)}'},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )

    def upgrade_kubernetes(self, instance, values):
        """Upgrade existing Kubernetes deployment"""
        try:
            # Get K8s cluster and deployment info
            k8s_deployment = instance.k8s_deployment
            k8s_cluster = k8s_deployment.k8s_cluster
            
            # Create deployment service
            k8s_service = KubernetesDeploymentService(k8s_cluster)
            
            # Test connection
            if not k8s_service.test_connection():
                return {'success': False, 'error': 'Cannot connect to Kubernetes cluster'}
            
            # Perform Helm upgrade
            result = k8s_service.upgrade_helm_release(
                namespace=instance.deployment_namespace,
                release_name=k8s_deployment.helm_release_name,
                chart_repo=instance.descriptor.artifact_repo_url,
                chart_path=instance.descriptor.artifact_name,
                values=values
            )
            
            if result.get('success'):
                # Update deployment info
                k8s_deployment.helm_values = values
                k8s_deployment.updated_at = timezone.now()
                k8s_deployment.save()
                
                return {
                    'success': True,
                    'message': 'Upgrade completed successfully'
                }
            else:
                return result
                
        except Exception as e:
            logger.error(f"Kubernetes upgrade failed: {str(e)}")
        return {'success': False, 'error': str(e)}

    """ Delete NF Deployment Instance """
    def destroy(self, request, *args, **kwargs):
        """Delete NF Deployment Instance - database only"""
        instance = self.get_object()
        
        # Refuse if still deployed
        if instance.instantiation_state == 'INSTANTIATED':
            return Response(
                {'error': 'Instance is deployed. Terminate first.'},
                status=status.HTTP_409_CONFLICT
            )
        
        # For ERROR or TERMINATING states, just clean database
        if instance.instantiation_state in ['ERROR', 'TERMINATING']:
            # The Kubernetes resources should already be gone
            # If not, that's a termination problem, not a deletion problem
            logger.warning(f"Deleting instance in {instance.instantiation_state} state")
        
        # Delete database records only
        instance.delete()
        
        return Response(status=status.HTTP_204_NO_CONTENT)

    @action(detail=False, methods=['post'])
    def bulk_delete(self, request):
        """Delete multiple instances"""
        instance_ids = request.data.get('instance_ids', [])
        force = request.data.get('force', False)
        
        if not instance_ids:
            return Response({'error': 'instance_ids required'}, status=400)
        
        results = {
            'deleted': [],
            'failed': [],
            'skipped': []
        }
        
        for instance_id in instance_ids:
            try:
                instance = NfDeploymentInstance.objects.get(instance_id=instance_id)
                
                # Check state
                if instance.instantiation_state in ['INSTANTIATED'] and not force:
                    results['skipped'].append({
                        'instance_id': str(instance_id),
                        'reason': 'Instance is deployed. Use force=true or terminate first.'
                    })
                    continue
                
                # Delete based on force flag
                if force:
                    self.cascade_delete_instance(instance)
                else:
                    instance.delete()
                
                results['deleted'].append(str(instance_id))
                
            except NfDeploymentInstance.DoesNotExist:
                results['failed'].append({
                    'instance_id': str(instance_id),
                    'error': 'Instance not found'
                })
            except Exception as e:
                results['failed'].append({
                    'instance_id': str(instance_id),
                    'error': str(e)
                })
        
        return Response({
            'summary': {
                'requested': len(instance_ids),
                'deleted': len(results['deleted']),
                'failed': len(results['failed']),
                'skipped': len(results['skipped'])
            },
            'results': results
        })

    
    def _execute_with_retry(self, operation, deployment_func, *args, **kwargs):
        """Execute operation with automatic retry on temporary failures"""
        max_retries = operation.max_retries
        retry_count = 0
        
        while retry_count <= max_retries:
            operation.retry_count = retry_count
            attempt = {
                'attempt': retry_count + 1,
                'start_time': timezone.now().isoformat(),
                'status': 'PROCESSING'
            }
            
            try:
                # Execute the deployment function
                result = deployment_func(*args, **kwargs)
                
                if result['success']:
                    attempt['status'] = 'SUCCESS'
                    attempt['end_time'] = timezone.now().isoformat()
                    
                    if not hasattr(operation, 'execution_attempts') or operation.execution_attempts is None:
                        operation.execution_attempts = []
                    operation.execution_attempts.append(attempt)
                    operation.save()
                    return result
                
                # Check if error is retryable
                error_type = result.get('error_type', 'unknown')
                if not self._is_retryable_error(error_type) or retry_count >= max_retries:
                    attempt['status'] = 'FAILED'
                    attempt['error'] = result.get('error')
                    
                    if not hasattr(operation, 'execution_attempts') or operation.execution_attempts is None:
                        operation.execution_attempts = []
                    operation.execution_attempts.append(attempt)
                    operation.error_details = result
                    operation.save()
                    return result
                
                # Calculate backoff
                backoff = self._calculate_backoff(retry_count)
                attempt['status'] = 'FAILED_TEMP'
                attempt['error'] = result.get('error')
                attempt['retry_after'] = backoff
                
                if not hasattr(operation, 'execution_attempts') or operation.execution_attempts is None:
                    operation.execution_attempts = []
                operation.execution_attempts.append(attempt)
                
                # Update operation for retry
                operation.operation_state = 'FAILED_TEMP'
                operation.retry_after = timezone.now() + timedelta(seconds=backoff)
                operation.error_details = result
                operation.save()
                
                # Wait before retry
                logger.info(f"Waiting {backoff}s before retry {retry_count + 1}")
                import time
                time.sleep(backoff)
                
                retry_count += 1
                
            except Exception as e:
                logger.error(f"Operation execution error: {str(e)}")
                attempt['status'] = 'FAILED'
                attempt['error'] = str(e)
                
                if not hasattr(operation, 'execution_attempts') or operation.execution_attempts is None:
                    operation.execution_attempts = []
                operation.execution_attempts.append(attempt)
                operation.error_details = {'error': str(e), 'error_type': 'exception'}
                operation.save()
                
                retry_count += 1
        
        return {'success': False, 'error': 'Max retries exceeded', 'error_type': 'max_retries'}
    
    def _is_retryable_error(self, error_type):
        """Determine if error is retryable per ETSI SOL 003"""
        retryable_errors = [
            'timeout',
            'connection_error',
            'port_conflict',
            'resource_busy',
            'temporary_failure',
            'rate_limit',
            'insufficient_resources'
        ]
        
        non_retryable_errors = [
            'invalid_values',
            'permission_denied',
            'invalid_chart',
            'authentication_failed',
            'quota_exceeded',
            'not_found'
        ]
        
        if error_type in non_retryable_errors:
            return False
        
        return error_type in retryable_errors or error_type == 'unknown'
    
    def _calculate_backoff(self, retry_count):
        """Calculate exponential backoff with jitter"""
        import random
        
        base = 30
        multiplier = 2
        max_backoff = 300
        
        # Exponential backoff with jitter
        backoff = min(base * (multiplier ** retry_count), max_backoff)
        
        # Add jitter (±20%)
        jitter = backoff * 0.2 * (2 * random.random() - 1)
        return int(backoff + jitter)
    
    def _capture_state_snapshot(self, instance):
        """Capture current state for potential rollback"""
        state = {
            'instantiation_state': instance.instantiation_state,
            'deployment_namespace': instance.deployment_namespace,
            'helm_release_name': instance.helm_release_name,
        }
        
        if hasattr(instance, 'k8s_deployment'):
            k8s = instance.k8s_deployment
            state['k8s_deployment'] = {
                'namespace': k8s.namespace,
                'helm_release_name': k8s.helm_release_name,
                'kubernetes_status': k8s.kubernetes_status,
                'helm_values': k8s.helm_values
            }
        
        return state
    
    def _execute_rollback(self, instance, operation):
        """Execute automatic rollback on failure"""
        logger.error(f"Executing automatic rollback for operation {operation.operation_id}")
        
        try:
            if operation.operation_type == 'INSTANTIATE':
                # Rollback instantiation = terminate
                logger.error("Rolling back instantiation by terminating")
                self.terminate_kubernetes(instance, force=True, cleanup_namespace=True)
                
                # Reset instance state
                instance.instantiation_state = 'NOT_INSTANTIATED'
                instance.deployment_namespace = None
                instance.helm_release_name = None
                #instance.allocated_machines.clear()
                instance.save()
                
            elif operation.operation_type == 'UPDATE':
                # Rollback update = restore previous values
                logger.info("Rolling back update by restoring previous values")
                if operation.state_snapshot:
                    previous_values = operation.state_snapshot.get('k8s_deployment', {}).get('helm_values', {})
                    self.upgrade_kubernetes(instance, previous_values)
            
            operation.rollback_executed = True
            operation.save()
            return True
            
        except Exception as e:
            logger.error(f"Rollback failed: {str(e)}")
            if not operation.error_details:
                operation.error_details = {}
            operation.error_details['rollback_error'] = str(e)
            operation.save()
            return False

    def _homing(self, instance, operation):
        try:
            print("Homing called")
            return True
        except Exception as e:
            print("Skunk")
            return False

    
class KubernetesClusterViewSet(viewsets.ModelViewSet):
    """Kubernetes cluster configuration management"""
    queryset = KubernetesCluster.objects.all()
    serializer_class = KubernetesClusterSerializer
    
    @action(detail=True, methods=['post'])
    def set_credentials(self, request, pk=None):
        """Set Kubernetes credentials"""
        k8s_cluster = self.get_object()
        serializer = KubernetesClusterCredentialsSerializer(k8s_cluster, data=request.data)

        # Return Cluster ID 
        if serializer.is_valid():
            serializer.save()
            target_id = self.serializer_class(k8s_cluster).data
            #return Response(target_id['id'])
            return Response({'k8s_id': target_id['cloud_id'], 'status': 'Credentials updated successfully'})

        return Response(serializer.errors, status=400)
    
    @action(detail=True, methods=['post'])
    def test_connection(self, request, pk=None):
        """Test Kubernetes cluster connection"""
        k8s_cluster = self.get_object()
        
        try:
            k8s_service = KubernetesDeploymentService(k8s_cluster)
            success = k8s_service.test_connection()
            
            return Response({
                'connected': success,
                'status': k8s_cluster.connection_status,
                'last_check': k8s_cluster.last_health_check
            })
        except Exception as e:
            return Response({
                'connected': False,
                'error': str(e),
                'skunk': 'skunk',
            }, status=500)
    
    @action(detail=True, methods=['get'])
    def get_namespaces(self, request, pk=None):
        """Get available namespaces"""
        k8s_cluster = self.get_object()
        
        try:
            k8s_service = KubernetesDeploymentService(k8s_cluster)
            v1 = client.CoreV1Api(k8s_service.k8s_client)
            namespaces = v1.list_namespace()
            
            namespace_list = [ns.metadata.name for ns in namespaces.items]
            return Response({'namespaces': namespace_list})
        except Exception as e:
            return Response({'error': str(e)}, status=500)

class DeploymentDescriptorListViewSet(viewsets.ModelViewSet):
    """
    Comprehensive list API for Deployment Descriptors (VNF Instances)
    """
    queryset = DeploymentDescriptor.objects.all()
    serializer_class = DeploymentDescriptorDetailedSerializer
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['profile_type', 'target_cluster', 'target_cluster__status']
    search_fields = ['name', 'description', 'artifact_name']
    ordering_fields = ['name', 'created_at', 'updated_at']
    ordering = ['-created_at']
    
    @action(detail=False, methods=['get'])
    def summary(self, request):
        """Get summary statistics for deployment descriptors"""
        total = self.queryset.count()
        by_profile = self.queryset.values('profile_type').annotate(count=Count('profile_type'))
        
        # Recently created (last 7 days)
        week_ago = timezone.now() - timedelta(days=7)
        recent = self.queryset.filter(created_at__gte=week_ago).count()
        
        return Response({
            'total_descriptors': total,
            'by_profile_type': list(by_profile),
            'recent_descriptors': recent
        })
    

class NfDeploymentInstanceListViewSet(viewsets.ModelViewSet):
    """
    Comprehensive list API for NF Deployment Instances
    """
    queryset = NfDeploymentInstance.objects.select_related(
        'descriptor', 'deployed_cluster'
    ).prefetch_related('operations')
    serializer_class = NfDeploymentInstanceDetailedSerializer
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = [
        'instantiation_state', 'descriptor__profile_type', 
        'deployed_cluster', 'descriptor__target_cluster'
    ]
    search_fields = ['name', 'descriptor__name', 'deployment_namespace']
    ordering_fields = ['name', 'created_at', 'updated_at', 'instantiation_state']
    ordering = ['-created_at']
    
    @action(detail=False, methods=['get'])
    def summary(self, request):
        """Get summary statistics for deployments"""
        total = self.queryset.count()
        by_state = self.queryset.values('instantiation_state').annotate(count=Count('instantiation_state'))
        by_profile = self.queryset.values(
            'descriptor__profile_type'
        ).annotate(count=Count('descriptor__profile_type'))
        
        # Active deployments
        active = self.queryset.filter(instantiation_state='INSTANTIATED').count()
        
        # Recent deployments (last 24 hours)
        day_ago = timezone.now() - timedelta(hours=24)
        recent = self.queryset.filter(created_at__gte=day_ago).count()
        
        # Average deployment time for completed operations
        avg_deployment_time = VnfLcmOperation.objects.filter(
            operation_type='INSTANTIATE',
            operation_state='COMPLETED',
            end_time__isnull=False
        ).aggregate(
            avg_time=Avg('end_time') - Avg('start_time')
        )
        
        return Response({
            'total_instances': total,
            'active_instances': active,
            'by_state': list(by_state),
            'by_profile_type': list(by_profile),
            'recent_instances': recent,
            'average_deployment_time': avg_deployment_time.get('avg_time')
        })
    
    @action(detail=False, methods=['get'])
    def active(self, request):
        """List only active (instantiated) deployments"""
        active_instances = self.queryset.filter(instantiation_state='INSTANTIATED')
        serializer = self.get_serializer(active_instances, many=True)
        return Response(serializer.data)
    
    @action(detail=False, methods=['get'])
    def failed(self, request):
        """List failed deployments with error details"""
        failed_instances = self.queryset.filter(instantiation_state='ERROR')
        
        failed_data = []
        for instance in failed_instances:
            last_operation = instance.operations.filter(
                operation_state='FAILED'
            ).order_by('-start_time').first()
            
            failed_data.append({
                'instance_id': str(instance.instance_id),
                'name': instance.name,
                'descriptor': instance.descriptor.name,
                'created_at': instance.created_at.isoformat(),
                'error_details': last_operation.error_details if last_operation else {},
                'last_operation': {
                    'operation_id': str(last_operation.operation_id),
                    'operation_type': last_operation.operation_type,
                    'start_time': last_operation.start_time.isoformat(),
                    'error_details': last_operation.error_details
                } if last_operation else None
            })
        
        return Response({
            'failed_count': len(failed_data),
            'failed_instances': failed_data
        })
    
    
    @action(detail=True, methods=['get'])
    def detailed_status(self, request, instance_id=None):
        """Get detailed status of a specific deployment"""
        try:
            instance = self.get_object()
            
            # Get all operations
            operations = VnfLcmOperationDetailedSerializer(
                instance.operations.order_by('-start_time'), many=True
            ).data
            
            # Get K8s deployment info if exists
            k8s_info = None
            try:
                k8s_deployment = instance.k8s_deployment
                k8s_info = KubernetesDeploymentDetailedSerializer(k8s_deployment).data
            except:
                pass
            
            # Get allocated resources
            machines = [
                {
                    'id': m.id,
                    'hostname': m.hostname,
                    'ip': str(m.ip),
                    'status': m.status,
                    'cpu_cores': m.cpu_cores if hasattr(m, 'cpu_cores') else None
                }
                for m in instance.allocated_machines.all()
            ]
            
            return Response({
                'instance': NfDeploymentInstanceDetailedSerializer(instance).data,
                'operations': operations,
                'kubernetes_deployment': k8s_info,
                'allocated_machines': machines,
                'deployment_timeline': self._get_deployment_timeline(instance)
            })
            
        except Exception as e:
            return Response({'error': str(e)}, status=500)
    
    def _get_deployment_timeline(self, instance):
        """Get deployment timeline"""
        timeline = []
        
        timeline.append({
            'event': 'Instance Created',
            'timestamp': instance.created_at.isoformat(),
            'status': 'completed'
        })
        
        for operation in instance.operations.order_by('start_time'):
            timeline.append({
                'event': f'{operation.operation_type} Started',
                'timestamp': operation.start_time.isoformat(),
                'status': 'in_progress' if not operation.end_time else 'completed'
            })
            
            if operation.end_time:
                timeline.append({
                    'event': f'{operation.operation_type} {operation.operation_state}',
                    'timestamp': operation.end_time.isoformat(),
                    'status': 'completed' if operation.operation_state == 'COMPLETED' else 'failed'
                })
        
        return timeline


class KubernetesDeploymentListViewSet(viewsets.ReadOnlyModelViewSet):
    """
    List API for Kubernetes deployments
    """
    queryset = KubernetesDeployment.objects.select_related(
        'nf_instance', 'k8s_cluster'
    ).order_by('-created_at')
    serializer_class = KubernetesDeploymentDetailedSerializer
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['kubernetes_status', 'k8s_cluster', 'namespace']
    search_fields = ['deployment_name', 'namespace', 'helm_release_name']
    ordering_fields = ['created_at', 'updated_at', 'kubernetes_status']
    ordering = ['-created_at']
    
    @action(detail=False, methods=['get'])
    def summary(self, request):
        """Get summary of Kubernetes deployments"""
        total = self.queryset.count()
        by_status = self.queryset.values('kubernetes_status').annotate(count=Count('kubernetes_status'))
        
        # Health status
        healthy = self.queryset.filter(
            kubernetes_status='RUNNING',
            pods_ready__gte=models.F('pods_desired')
        ).count()
        
        return Response({
            'total_k8s_deployments': total,
            'healthy_deployments': healthy,
            'by_status': list(by_status),
        })


class KubernetesOperation(viewsets.ModelViewSet):
    queryset = KubernetesCluster.objects.all()
    serializer_class = KubernetesClusterSerializer
    lookup_field = 'cloud_id'

    @action(detail=True, methods=['get'])
    def query_resource(self, request, cloud_id=None):
        print("Calling Query Resource") 

        instance = self.get_object()

        print(instance.prometheus_endpoint)
        
        metrics = KubernetesResourceMetrics(instance.prometheus_endpoint)
        summary = metrics.get_cluster_summary()

        return Response(summary['cluster_totals'])

    @action(detail=False, methods=['post'])
    def homing(self, request):
        """Find best cluster for deployment with specific requirements."""

        #skunk = request.data.copy()
        #print(skunk)

        # Get requirements from request (optional)
        cpu_needed = request.data.get('cpu_cores', 1)  # Default 1 core
        memory_needed = request.data.get('memory_gi', 2)  # Default 2Gi
        
        best_cluster = None
        best_score = -1
        all_clusters = []
        
        for cluster in self.get_queryset():
            try:
                metrics = KubernetesResourceMetrics(cluster.prometheus_endpoint)
                summary = metrics.get_cluster_summary()
                
                cpu_available = summary['cluster_totals']['cpu']['available_millicores'] / 1000
                memory_available = summary['cluster_totals']['memory']['available_gi']
                nic_available = summary['nic_features']['dpdk_ready_nodes']
                
                # Check if cluster can handle the deployment
                can_deploy = (cpu_available >= cpu_needed and memory_available >= memory_needed)
                
                # Score based on available headroom after deployment
                score = (cpu_available - cpu_needed) + (memory_available - memory_needed)
                
                cluster_data = {
                    'cloud_name': cluster.cloud_name,
                    'cloud_id': cluster.cloud_id,
                    'cpu_available': cpu_available,
                    'memory_available_gi': memory_available,
                    'can_deploy': can_deploy,
                    'nic_available': nic_available,
                    'score': score if can_deploy else 0
                }
                
                all_clusters.append(cluster_data)
                
                if can_deploy and score > best_score:
                    best_score = score
                    best_cluster = cluster_data
                    
            except Exception as e:
                print(f"Skip cluster {cluster.cloud_id}: {e}")
        
        return Response({
            'best_cluster': best_cluster,
            'requirements': {
                'cpu_cores': cpu_needed,
                'memory_gi': memory_needed
            },
            'all_clusters': all_clusters
        })
    
