.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0



How to use Fault Management
===========================

This user guide shows how to create subscription on DMS-ETSI for FM and how
DMS-ETSI set alert rules in monitoring tool (prometheus).

.. contents::
   :depth: 3
   :local:


Prerequisites
-------------

#. To create PaaS environment for FM that can be integrated with INF O2 service,
   the following software need to be installed.

   * OpenStack
   * Prometheus
   * Alert Manager
   * Node Exporter
   * Kube-state-metrics
   * Notification server

#. In PaaS environment containerized VNF should be installed, deployed and
   instantiated according to the steps below.

   https://docs.openstack.org/tacker/latest/user/v2/cnf/deployment_using_helm/index.html


Abbreviations
-------------

* FM           : Fault Management
* DMS-ETSI     : Deployment Management Services of ETSI like tacker
* NF FM        : NF FM may be human in this version or a component part of SMO


Procedure
---------

#. Create FM subscription

   NF FM sends a request to DMS-ETSI to create FM subscription. In this FM
   subscription, multiple filter conditions can be set for fault like compute,
   storage, network etc mentioned in create subscription sample file
   sample_param_file.json.

   Follow the below steps to create a subscription for fault:

   * Confirm "ID" of the instantiated VNF by executing below command.

      .. code-block:: console

          $ openstack vnflcm list --os-tacker-api-version 2
          +--------------------------------------+-------------------+---------------------+--------------+----------------------+------------------+--------------------------------------+
          | ID                                   | VNF Instance Name | Instantiation State | VNF Provider | VNF Software Version | VNF Product Name | VNFD ID                              |
          +--------------------------------------+-------------------+---------------------+--------------+----------------------+------------------+--------------------------------------+
          | d2e61392-14dc-4b23-8d33-a19456de65c4 |                   | INSTANTIATED        | Company      | 1.0                  | Sample VNF       | b1bb0ce7-ebca-4fa7-95ed-4840d70a1177 |
          +--------------------------------------+-------------------+---------------------+--------------+----------------------+------------------+--------------------------------------+

   * Change the following values in subscription sample file
     sample_param_file.json to the actual values confirmed
     from above and save the file.

      * "vnfdIds"              : Set the value of "VNFD ID"
      * "vnfProvider"          : Set the string of "VNF Provider"
      * "vnfProductName"       : Set the string of "VNF Product Name"
      * "vnfSoftwareVersion"   : Set the value of "VNF Software Version"
      * "vnfInstanceIds"       : Set the value of "ID"

     The content of the subscription sample sample_param_file.json is as follows:

     .. code-block:: console

         {
             "filter": {
                 "vnfInstanceSubscriptionFilter": {
                     "vnfdIds": [
                         "b1bb0ce7-ebca-4fa7-95ed-4840d70a1177"
                     ],
                     "vnfProductsFromProviders": [
                         {
                             "vnfProvider": "Company",
                             "vnfProducts": [
                                 {
                                     "vnfProductName": "Sample VNF",
                                     "versions": [
                                         {
                                             "vnfSoftwareVersion": 1.0,
                                             "vnfdVersions": [1.0, 2.0]
                                         }
                                     ]
                                 }
                             ]
                         }
                     ],
                     "vnfInstanceIds": [
                         "d2e61392-14dc-4b23-8d33-a19456de65c4"
                     ]
                 },
                 "notificationTypes": [
                     "AlarmNotification",
                     "AlarmClearedNotification",
                     "AlarmListRebuiltNotification"
                 ],
                 "faultyResourceTypes": [
                     "COMPUTE",
                     "STORAGE",
                    "NETWORK"
                 ],
                 "perceivedSeverities": [
                     "CRITICAL",
                     "MAJOR",
                     "MINOR",
                     "WARNING",
                     "INDETERMINATE",
                     "CLEARED"
                 ],
                 "eventTypes": [
                     "EQUIPMENT_ALARM",
                     "COMMUNICATIONS_ALARM",
                     "PROCESSING_ERROR_ALARM",
                     "ENVIRONMENTAL_ALARM",
                     "QOS_ALARM"
                 ],
                 "probableCauses": [
                     "The server cannot be connected."
                 ]
             },
             "callbackUri": "http://10.0.0.194:5000/your-callback-endpoint",
             "authentication": {
                 "authType": [
                     "BASIC"
                 ],
                 "paramsBasic": {
                     "userName": "nfv_user",
                     "password": "devstack"
                 }
             }
         }

   * Execute below command to create FM subscription.

     .. code-block:: console

         $ openstack vnffm sub create sample_param_file.json --os-tacker-api-version 2

   * Verify FM subscription by executing following command.

     .. code-block:: console

         $ openstack vnffm sub list --os-tacker-api-version 2
         +--------------------------------------+-----------------------------------------------+
         | ID                                   | Callback Uri                                  |
         +--------------------------------------+-----------------------------------------------+
         | 724b6752-b782-48e8-a8bb-a20a0fdb8d9f | http://10.0.0.194:5000/your-callback-endpoint |
         +--------------------------------------+-----------------------------------------------+


#. Create alert rules on Monitoring tool

   * Prometheus configuration has two files.

      #. deployment.yaml which contains all the configurations to discover pods
         and services running in the Kubernetes cluster dynamically. No need to
         change in deployment.yaml

      #. configmap.yaml which contains all the alert rules for sending alerts
         to the Alert manager.

         The content of the sample configmap.yaml is as follow:

         .. code-block:: console

             apiVersion: v1
             kind: ConfigMap
             metadata:
               name: prometheus-config
               namespace: monitoring
             data:
               prometheus.rules: |-
                 groups:
                 - name: example
                   rules:
                   - alert: KubePodCrashLooping
                     annotations:
                       probable_cause: The server cannot be connected.
                       fault_type: Server Down
                       fault_details: fault details
                     expr: |
                       increase(kube_pod_container_status_restarts_total[10m]) > 0
                     for: 1m
                     labels:
                       receiver_type: tacker
                       function_type: vnffm
                       vnf_instance_id: 8c93a232-92fb-461a-a5b4-60efa2dd5f81
                       pod: vdu2-798d577c96-6t42j
                       perceived_severity: CRITICAL
                       event_type: EQUIPMENT_ALARM

   * After add/delete/modify alert rule in sample configmap.yaml, perform
     following steps to make it effective.

      #. Delete old Prometheus ConfigMap

         .. code-block:: console

             $ kubectl delete -f configmap.yaml

      #. Delete old Prometheus Deployment File

         .. code-block:: console

             $ kubectl delete -f deployment.yaml

      #. Delete Prometheus Service

         .. code-block:: console

             $ kubectl delete -f service.yaml

      #. Create Prometheus ConfigMap with updated ConfigMap

         .. code-block:: console

             $ kubectl apply -f configmap.yaml

      #. Create Prometheus Deployment File

         .. code-block:: console

             $ kubectl apply -f deployment.yaml

      #. Create Prometheus Service

         .. code-block:: console

             $ kubectl apply -f service.yaml


Requirements
------------

#. Receiving Notification

   * The NF FM sends a create subscription request to the DMS-ETSI.

   * After sending the create subscription request, DMS-ETSI will send a
     GET request to the callback_uri in NF FM to verify its correctness.
     NF FM should receive this request and then return HTTP 204 to DMS-ETSI.

#. Sending Heal Request

   * When a fault occurs in a CNF and matches subscribed alarm condition,
     DMS-ETSI will send an Alarm Notification to NF FM.

   * NF FM should receive the notification, get VNF/VNFC information
     (vnfInstanceId, vnfcInstanceId) from it, and then send Heal CNF
     request to NF-LCM, which further sends heal request to DMS-ETSI.


References
----------

.. [1]  ETSI NFV-SOL CNF Auto Healing with Prometheus via FM Interfaces [1]_.
        https://docs.openstack.org/tacker/zed/user/etsi_cnf_auto_healing_fm.html

.. [2]  Tacker API Document for Receiving Notification [3]_.
        https://docs.openstack.org/api-ref/nfv-orchestration/v2/vnflcm.html#create-a-new-subscription-v2

.. [3]  Tacker API Document for Heal Request [4]_.
        https://docs.openstack.org/api-ref/nfv-orchestration/v2/vnflcm.html#heal-a-vnf-instance-v2
