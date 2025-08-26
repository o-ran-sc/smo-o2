from rest_framework import viewsets, status, filters
from rest_framework.decorators import action
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend
from django.utils import timezone
from .services.kubernetes_service import KubernetesDeploymentService
from .models import (
    VnfLcmOperation,
)
from .serializers import (
    VnfLcmOperationSerializer,
    VnfLcmOperationDetailedSerializer,
)

import logging

logger = logging.getLogger(__name__)
from datetime import timedelta


class VnfLcmOperationViewSet(viewsets.ReadOnlyModelViewSet):
    """VNF LCM Operation Occurrences"""

    # queryset = VnfLcmOperation.objects.all()
    # serializer_class = VnfLcmOperationSerializer
    # lookup_field = 'operation_id'

    queryset = VnfLcmOperation.objects.select_related("vnf_instance").order_by(
        "-start_time"
    )
    serializer_class = VnfLcmOperationDetailedSerializer
    filter_backends = [
        DjangoFilterBackend,
        filters.SearchFilter,
        filters.OrderingFilter,
    ]
    filterset_fields = ["operation_type", "operation_state", "vnf_instance"]
    search_fields = ["vnf_instance__name", "operation_type"]
    ordering_fields = ["start_time", "end_time", "operation_type", "operation_state"]
    ordering = ["-start_time"]

    # Retry Existing operation
    @action(
        detail=False,
        methods=["post"],
        url_path="operations/(?P<operation_id>[^/.]+)/retry",
    )
    def retry_operation(self, request, operation_id=None):
        """Manually retry a failed operation per ETSI SOL 003"""
        try:
            operation = VnfLcmOperation.objects.get(operation_id=operation_id)
        except VnfLcmOperation.DoesNotExist:
            return Response({"error": "Operation not found"}, status=404)

        # Check if operation can be retried
        if operation.operation_state not in ["FAILED", "FAILED_TEMP"]:
            return Response(
                {
                    "error": f"Cannot retry operation in {operation.operation_state} state"
                },
                status=status.HTTP_409_CONFLICT,
            )

        # Check retry limit
        if operation.retry_count >= operation.max_retries:
            return Response(
                {"error": f"Maximum retries ({operation.max_retries}) already reached"},
                status=status.HTTP_409_CONFLICT,
            )

        # Get the instance
        instance = operation.vnf_instance

        # Reset operation state for retry
        operation.operation_state = "PROCESSING"
        operation.retry_count += 1
        operation.save()

        # Execute retry based on operation type
        try:
            if operation.operation_type == "INSTANTIATE":
                result = self._retry_instantiate_operation(instance, operation)
            elif operation.operation_type == "UPDATE":
                result = self._retry_update_operation(instance, operation)
            elif operation.operation_type == "TERMINATE":
                result = self._retry_terminate_operation(instance, operation)
            else:
                return Response(
                    {"error": f"Retry not supported for {operation.operation_type}"},
                    status=status.HTTP_400_BAD_REQUEST,
                )

            return Response(
                {
                    "vnfLcmOpOccId": str(operation.operation_id),
                    "operationState": operation.operation_state,
                    "retryCount": operation.retry_count,
                    "message": result.get("message", "Retry initiated"),
                },
                status=status.HTTP_202_ACCEPTED,
            )

        except Exception as e:
            operation.operation_state = "FAILED"
            operation.error_details = {"error": str(e)}
            operation.save()

            return Response(
                {"error": f"Retry failed: {str(e)}"},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

    def _retry_instantiate_operation(self, instance, operation):
        """Retry instantiation operation"""
        # Reset instance state
        instance.instantiation_state = "INSTANTIATING"
        instance.save()

        # Get stored values
        stored_values = {}
        if (
            hasattr(instance.descriptor, "additional_params")
            and instance.descriptor.additional_params
        ):
            stored_values = instance.descriptor.additional_params.get("values", {})

        # Get values from original operation
        request_values = operation.operation_params.get("values", {})
        final_values = {**stored_values, **request_values}

        # Execute with single retry attempt
        result = self.deploy_kubernetes(
            instance, operation.operation_params, final_values
        )

        if result["success"]:
            instance.instantiation_state = "INSTANTIATED"
            instance.deployed_cluster = instance.descriptor.target_cluster
            instance.save()

            operation.operation_state = "COMPLETED"
            operation.progress_percentage = 100
            operation.end_time = timezone.now()
            operation.save()

            return {"success": True, "message": "Retry successful"}
        else:
            # Check if it's still a temporary failure
            error_type = result.get("error_type", "unknown")
            if (
                self._is_retryable_error(error_type)
                and operation.retry_count < operation.max_retries
            ):
                operation.operation_state = "FAILED_TEMP"
                backoff = self._calculate_backoff(operation.retry_count)
                operation.retry_after = timezone.now() + timedelta(seconds=backoff)
            else:
                operation.operation_state = "FAILED"
                instance.instantiation_state = "ERROR"
                instance.save()

            operation.error_details = result
            operation.end_time = timezone.now()
            operation.save()

            return {"success": False, "message": f'Retry failed: {result.get("error")}'}

    def _retry_update_operation(self, instance, operation):
        """Retry update/upgrade operation"""
        # Reset instance state
        instance.instantiation_state = "UPDATING"
        instance.save()

        # Get values from operation params
        previous_values = operation.operation_params.get("previous_values", {})
        new_values = operation.operation_params.get("values", {})
        final_values = {**previous_values, **new_values}

        # Execute upgrade
        result = self.upgrade_kubernetes(instance, final_values)

        if result["success"]:
            instance.instantiation_state = "INSTANTIATED"
            instance.save()

            operation.operation_state = "COMPLETED"
            operation.end_time = timezone.now()
            operation.save()

            return {"success": True, "message": "Update retry successful"}
        else:
            operation.operation_state = "FAILED"
            operation.error_details = result
            operation.end_time = timezone.now()
            operation.save()

            return {
                "success": False,
                "message": f'Update retry failed: {result.get("error")}',
            }

    @action(
        detail=False, methods=["get"], url_path="operations/(?P<operation_id>[^/.]+)"
    )
    def get_operation(self, request, operation_id=None):
        """Get operation status per ETSI SOL 003"""
        try:
            operation = VnfLcmOperation.objects.get(operation_id=operation_id)

            response_data = {
                "id": str(operation.operation_id),
                "operationState": operation.operation_state,
                "vnfInstanceId": str(operation.vnf_instance.instance_id),
                "operation": operation.operation_type,
                "startTime": operation.start_time.isoformat(),
                "operationParams": operation.operation_params,
            }

            if operation.end_time:
                response_data["endTime"] = operation.end_time.isoformat()

            # Add retry information
            if operation.retry_count > 0:
                response_data["retryCount"] = operation.retry_count
                response_data["executionAttempts"] = getattr(
                    operation, "execution_attempts", []
                )

            # Add error details
            if operation.operation_state in ["FAILED", "FAILED_TEMP", "ROLLED_BACK"]:
                response_data["error"] = operation.error_details

            # Add rollback info
            if hasattr(operation, "rollback_executed") and operation.rollback_executed:
                response_data["rollbackExecuted"] = True

            return Response(response_data)

        except VnfLcmOperation.DoesNotExist:
            return Response({"error": "Operation not found"}, status=404)

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
                return {
                    "success": False,
                    "error": "Cannot connect to Kubernetes cluster",
                }

            # Perform Helm upgrade
            result = k8s_service.upgrade_helm_release(
                namespace=instance.deployment_namespace,
                release_name=k8s_deployment.helm_release_name,
                chart_repo=instance.descriptor.artifact_repo_url,
                chart_path=instance.descriptor.artifact_name,
                values=values,
            )

            if result.get("success"):
                # Update deployment info
                k8s_deployment.helm_values = values
                k8s_deployment.updated_at = timezone.now()
                k8s_deployment.save()

                return {"success": True, "message": "Upgrade completed successfully"}
            else:
                return result

        except Exception as e:
            logger.error(f"Kubernetes upgrade failed: {str(e)}")
        return {"success": False, "error": str(e)}

    def _retry_terminate_operation(self, instance, operation):
        """Retry termination operation"""
        # Reset instance state
        instance.instantiation_state = "TERMINATING"
        instance.save()

        # Get termination parameters from original operation
        graceful = operation.operation_params.get("graceful", True)
        force = operation.operation_params.get("force", False)
        cleanup_namespace = operation.operation_params.get("cleanup_namespace", True)

        # Add attempt tracking
        attempt = {
            "attempt": operation.retry_count,
            "start_time": timezone.now().isoformat(),
            "status": "PROCESSING",
        }

        try:
            # Execute termination
            result = self.terminate_kubernetes(
                instance,
                graceful=graceful,
                force=force,
                cleanup_namespace=cleanup_namespace,
            )

            if result["success"] or force:
                # Clean up database records
                instance.instantiation_state = "NOT_INSTANTIATED"
                instance.deployment_namespace = None
                instance.helm_release_name = None
                instance.allocated_machines.clear()
                instance.save()

                # Mark operation complete
                operation.operation_state = "COMPLETED"
                operation.progress_percentage = 100
                operation.end_time = timezone.now()
                operation.save()

                attempt["status"] = "SUCCESS"
                attempt["end_time"] = timezone.now().isoformat()
                attempt["message"] = result.get("message", "Termination successful")

                if (
                    not hasattr(operation, "execution_attempts")
                    or operation.execution_attempts is None
                ):
                    operation.execution_attempts = []
                operation.execution_attempts.append(attempt)
                operation.save()

                return {
                    "success": True,
                    "message": "Termination retry successful",
                    "steps": result.get("steps", []),
                }
            else:
                # Check if error is still retryable
                error_msg = str(result.get("error", "")).lower()
                error_type = "unknown"

                # Classify termination-specific errors
                if "connection" in error_msg or "connect" in error_msg:
                    error_type = "connection_error"
                elif "timeout" in error_msg:
                    error_type = "timeout"
                elif "not found" in error_msg:
                    # Resources might already be deleted
                    error_type = "not_found"
                elif "permission" in error_msg or "forbidden" in error_msg:
                    error_type = "permission_denied"

                attempt["status"] = "FAILED"
                attempt["error"] = result.get("error")
                attempt["error_type"] = error_type

                if (
                    not hasattr(operation, "execution_attempts")
                    or operation.execution_attempts is None
                ):
                    operation.execution_attempts = []
                operation.execution_attempts.append(attempt)

                # Determine if we should retry again
                if (
                    self._is_retryable_error(error_type)
                    and operation.retry_count < operation.max_retries
                ):
                    operation.operation_state = "FAILED_TEMP"
                    backoff = self._calculate_backoff(operation.retry_count)
                    operation.retry_after = timezone.now() + timedelta(seconds=backoff)

                    # For termination, we might want to be more aggressive
                    if error_type == "not_found":
                        # Resources already gone, consider it success
                        logger.warning(
                            "Resources not found during termination retry - considering as success"
                        )
                        instance.instantiation_state = "NOT_INSTANTIATED"
                        instance.deployment_namespace = None
                        instance.helm_release_name = None
                        instance.allocated_machines.clear()
                        instance.save()

                        operation.operation_state = "COMPLETED"
                        operation.end_time = timezone.now()
                        operation.save()

                        return {
                            "success": True,
                            "message": "Termination completed (resources already removed)",
                        }
                else:
                    operation.operation_state = "FAILED"
                    instance.instantiation_state = "ERROR"
                    instance.save()

                operation.error_details = {
                    "error": result.get("error"),
                    "error_type": error_type,
                    "termination_steps": result.get("steps", []),
                }
                operation.end_time = timezone.now()
                operation.save()

                return {
                    "success": False,
                    "message": f'Termination retry failed: {result.get("error")}',
                    "error_type": error_type,
                }

        except Exception as e:
            logger.error(f"Termination retry exception: {str(e)}")

            attempt["status"] = "FAILED"
            attempt["error"] = str(e)
            attempt["error_type"] = "exception"

            if (
                not hasattr(operation, "execution_attempts")
                or operation.execution_attempts is None
            ):
                operation.execution_attempts = []
            operation.execution_attempts.append(attempt)

            # For termination, if force is enabled, we might still want to clean up
            if force:
                logger.warning(
                    f"Force termination: cleaning up despite error: {str(e)}"
                )
                instance.instantiation_state = "NOT_INSTANTIATED"
                instance.deployment_namespace = None
                instance.helm_release_name = None
                instance.allocated_machines.clear()
                instance.save()

                operation.operation_state = "COMPLETED"
                operation.progress_percentage = 100
                operation.end_time = timezone.now()
                operation.error_details = {
                    "warning": "Force terminated with errors",
                    "error": str(e),
                }
                operation.save()

                return {
                    "success": True,
                    "message": "Force termination completed despite errors",
                    "warning": str(e),
                }
            else:
                operation.operation_state = "FAILED"
                operation.error_details = {"error": str(e), "error_type": "exception"}
                operation.end_time = timezone.now()
                operation.save()

                instance.instantiation_state = "ERROR"
                instance.save()

                return {
                    "success": False,
                    "message": f"Termination retry failed with exception: {str(e)}",
                }
