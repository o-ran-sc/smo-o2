.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0



How to use Performance Management
=================================

This user guide shows how to check CPU utilization and threshold for
Performance Management.

.. contents::
   :depth: 3
   :local:


Prerequisites
-------------

#. Environment Preparation

   To create PaaS environment for PM that can be integrated with INF O2 service,
   the following software need to be installed.

   * OpenStack
   * Prometheus
   * Alert Manager
   * Node Exporter
   * Kube-state-metrics
   * Notification server
   * Tacker

#. VNF Deployment

   Tacker use VNF package to deploy a set of kubernetes resources such as pods or
   deployment and Tacker will manage such resources as a VNF instance. In PaaS
   environment containerized VNF should be installed, deployed and instantiated
   according to the steps below.

   https://docs.openstack.org/tacker/latest/user/v2/cnf/deployment_using_helm/index.html


Procedure
---------

By integrating tacker with prometheus, fault management and performance management
are performed. Kubernetes manages such pods or deployment resources and prometheus
monitors such resources. Tacker maps the kubernetes resource information to the VNF
instance information, so tacker can enable for fault management and the performance
management for the VNF instance.

There are two types of Performance Management functions-
* PM Job
* PM Threshold

#. PM Job

   #. Check VNF Status

      .. code-block:: console

          $ openstack vnflcm show --os-tacker-api-version 2 ec096028-e5ba-44e7-a912-a2214d567e7a -c 'Instantiation State'

          +---------------------+------------------+
          | Field                       |  Value   |
          +---------------------+------------------+
          | Instantiation State | INSTANTIATED     |
          +---------------------+------------------+

   #. Create PM job

      Using below command, PM job will be created-

      .. code-block:: console

          $ openstack --os-tacker-api-version 2 vnfpm job create pmjob_cpu_report.json

            +--------------------------+--------------------------------------------------------------------------------------------------------+
            |  Field                   |  Value                                                                                                 |
            +--------------------------+--------------------------------------------------------------------------------------------------------+
            |  Callback Uri            |  http://128.224.232.182:9998/notification/callbackuri/ca1f1cb8-8436-41d5-b584-986c49763442             |
            |  Criteria                |  {                                                                                                     |
            |	                       |    "performanceMetric": [                                                                              |
            |	                       |        "VCpuUsageMeanVnf.calf1cb0-8436-41d5-b584-986c49763442"                                         |
            |	                       |        "collectionPeriod": 30,                                                                         |
            |                          |        "reportingPeriod": 60                                                                           |
            |                          |  }                                                                                                     |
            |  ID                      |  84196619-23ea-4dcd-bfb6-af0c48f0b213                                                                  |
            |  Links                   |  {                                                                                                     |
            |                          |     "self": {                                                                                          |
            |                          |         "href": "http://127.0.0.1:9890/vnfpm/v2/pm_jobs/84196619-23ea-4dcd-bfb6-af0c48f0b213"          |
            |                          |      },                                                                                                |
            |                          |      "objects":[                                                                                       |
            |                          |          {                                                                                             |
            |                          |            "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/calf1cb0-8436-41d5-b584-906c49763442"|
            |                          |          }                                                                                             |
            |                          |       ]                                                                                                |
            |                          |    }                                                                                                   |
            |                          |                                                                                                        |
            |  Object Instance Ids     |  [                                                                                                     |
            |                          |     "calf1cb0-8436-41d5-b584-986c49763442"                                                             |
            |                          |  ]                                                                                                     |
            |  Object Type             |  Vnf                                                                                                   |
            |  Reports                 |  []                                                                                                    |
            |  Sub Object Instance Ids |                                                                                                        |
            +--------------------------+--------------------------------------------------------------------------------------------------------+


   #. Check CPU utilization value

      Using Job show command below we get the  PM report id.

      .. code-block:: console

          $ openstack –os-tacker-api-version 2 vnfpm job show 84196619-23ea-4dcd-bfb6-af0c48f0b213

      Then to get the PM Report data we use below command which uses PM job ID and
      PM report ID respectively.

      .. code-block:: console

          $ openstack –os-tacker-api-version 2 report show 84196619-23ea-4dcd-bfb6-af0c48f0b213 cdec8edd-82bb-426d-98fb-9df1be1725f6

            +--------------------------+--------------------------------------------------------------------------------------------------------+
            |  Field                   |  Value                                                                                                 |
            +--------------------------+--------------------------------------------------------------------------------------------------------+
            |  Entries                 |  [                                                                                                     |
            |                          |      {                                                                                                 |
            |                          |        "objectType": "Vnf",                                                                            |
            |                          |          "objectInstanceId": "calflcb0-8436-41d5-b584-906c49763442",                                   |
            |                          |          "performanceMetric": "VCpuUsageMeanVnf.ca1f1cb8-8436-41d5-b584-986c49763442",                 |
            |                          |          "performanceValues": [                                                                        |
            |                          |              {                                                                                         |
            |                          |                 "timestamp": "2024-09-24T14:22:272",                                                   |
            |                          |                 "value": "2.261168096206560-05"                                                        |
            |                          |              }                                                                                         |
            |                          |           ]                                                                                            |
            |                          |       }                                                                                                |
            |                          |     ]                                                                                                  |
            +--------------------------+--------------------------------------------------------------------------------------------------------+

      The “value” here represents the actual value of CPU utilization.

#. PM Threshold

   #. Create Threshold

      .. code-block:: console

          $ openstack vnfpm threshold create sample_param_file.json --os-tacker-api-version 2
            +-------------------------+------------------------------------------------------------------------------------------------------+
            | Field                   | Value                                                                                                |
            +-------------------------+------------------------------------------------------------------------------------------------------+
            | Callback Uri            | http://127.0.0.1:9990/notification/callbackuri/c21fd71b-2866-45f6-89d0-70c458a5c32e                  |
            | Criteria                | {                                                                                                    |
            |                         |     "performanceMetric": "VCpuUsageMeanVnf.c21fd71b-2866-45f6-89d0-70c458a5c32e",                    |
            |                         |     "thresholdType": "SIMPLE",                                                                       |
            |                         |     "simpleThresholdDetails": {                                                                      |
            |                         |         "thresholdValue": 1.0,                                                                       |
            |                         |         "hysteresis": 0.5                                                                            |
            |                         |     }                                                                                                |
            |                         | }                                                                                                    |
            | ID                      | 135db472-4f7b-4d55-abaf-27a3ab4d7ba1                                                                 |
            | Links                   | {                                                                                                    |
            |                         |     "self": {                                                                                        |
            |                         |         "href": "http://127.0.0.1:9890/vnfpm/v2/thresholds/135db472-4f7b-4d55-abaf-27a3ab4d7ba1"     |
            |                         |     },                                                                                               |
            |                         |     "object": {                                                                                      |
            |                         |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/c21fd71b-2866-45f6-89d0-70c458a5c32e" |
            |                         |     }                                                                                                |
            |                         | }                                                                                                    |
            | Object Instance Id      | c21fd71b-2866-45f6-89d0-70c458a5c32e                                                                 |
            | Object Type             | Vnf                                                                                                  |
            | Sub Object Instance Ids |                                                                                                      |
            +-------------------------+------------------------------------------------------------------------------------------------------+

      When creating a PM threshold, Tacker will modify the configuration file
      on the specified Prometheus based on metadata. Then Prometheus will monitor
      the specified resource and send the monitored information to Tacker.

   #. Check CPU utilization

      To check CPU usage, we need to perform some operations.
      Eg. Connect to pod and do some operations on it.

      Then we can check the usage value using below command-

      .. code-block:: console

          $ openstack vnfpm threshold show 135db472-4f7b-4d55-abaf-27a3ab4d7ba1 --os-tacker-api-version 2
            +-------------------------+------------------------------------------------------------------------------------------------------+
            | Field                   | Value                                                                                                |
            +-------------------------+------------------------------------------------------------------------------------------------------+
            | Callback Uri            | http://127.0.0.1:9990/notification/callbackuri/c21fd71b-2866-45f6-89d0-70c458a5c32e                  |
            | Criteria                | {                                                                                                    |
            |                         |     "performanceMetric": "VCpuUsageMeanVnf.c21fd71b-2866-45f6-89d0-70c458a5c32e",                    |
            |                         |     "thresholdType": "SIMPLE",                                                                       |
            |                         |     "simpleThresholdDetails": {                                                                      |
            |                         |         "thresholdValue": 1.0,                                                                       |
            |                         |         "hysteresis": 0.5                                                                            |
            |                         |     }                                                                                                |
            |                         | }                                                                                                    |
            | ID                      | 135db472-4f7b-4d55-abaf-27a3ab4d7ba1                                                                 |
            | Links                   | {                                                                                                    |
            |                         |     "self": {                                                                                        |
            |                         |         "href": "http://127.0.0.1:9890/vnfpm/v2/thresholds/135db472-4f7b-4d55-abaf-27a3ab4d7ba1"     |
            |                         |     },                                                                                               |
            |                         |     "object": {                                                                                      |
            |                         |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/c21fd71b-2866-45f6-89d0-70c458a5c32e" |
            |                         |     }                                                                                                |
            |                         | }                                                                                                    |
            | Object Instance Id      | c21fd71b-2866-45f6-89d0-70c458a5c32e                                                                 |
            | Object Type             | Vnf                                                                                                  |
            | Sub Object Instance Ids |                                                                                                      |
            +-------------------------+------------------------------------------------------------------------------------------------------+


References
----------

.. [1]  VNF Performance Management [1]_.
        https://docs.openstack.org/tacker/zed/cli/cli-etsi-vnfpm.html

.. [2]  Support AutoHeal and AutoScale with External Monitoring Tools via FM/PM
        Interfaces [2]_.
        https://specs.openstack.org/openstack/tacker-specs/specs/zed/prometheus-plugin-autoheal-and-autoscale.html

.. [3]  ETSI NFV-SOL CNF Auto Scaling With Prometheus via PM Threshold Interfaces [3]_.
        https://docs.openstack.org/tacker/latest/user/v2/cnf/auto_scale_pm_th/index.html#how-to-create-a-pm-threshold
