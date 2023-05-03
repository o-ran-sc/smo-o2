.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0


How to deploy CNF Deployment
============================

This user guide shows how to deploy CNF (Container Network Function) using Helm via Tacker.
See the `ETSI NFV-SOL CNF Deployment by Helm chart` for the original procedure.

.. contents::
   :depth: 3
   :local:

Prerequisites
-------------

VIM
~~~
Register Kubernetes VIM (Virtualized Infrastructure Manager) by following procedure below.

1. Prepare VIM config file, please change the parameters to suit your environment.
   If you get bearer_token via INF O2 service, follow the "Use Kubernetes Control Client through O2 DMS profile" section of `INF O2 Service Userguide`_.

   Sample config file:

   .. code:: bash

      $ cat vim_config_k8s.yaml
      auth_url: "https://192.168.121.170:6443"
      project_name: "default"
      bearer_token: "eyJhbGciOiJSUzI1NiIsImtpZCI6InZQOGs5bjhKdExWVkZiRU5reVZRdEtBS0pnYXJFemROcElKNFpIdEFtMXMifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJhZG1pbi11c2VyLXRva2VuLTk4am1qIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImFkbWluLXVzZXIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiIzZWJkZmEzZi0wMTA1LTRhN2YtODFjZS1kYjg5ODcxYTBiMDYiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6a3ViZS1zeXN0ZW06YWRtaW4tdXNlciJ9.df_mV4RbsRN6oOS2KnkaKid0cJBAvdpautWnK67R0y8PRSm79Vc02NbUCmai0M4QiIF9gKhqtM0OYB5vZYJJng9vkcSNVWFUv6hA4Tvjw8FnEcGWe7TnWE2q-ZywJYiZNHvToRIgP5EH5UuLACEXu8KeeG56LxL3T2qNsUenUYLYaT6EciwKiy5SaEk3H1BB0zvSff0d_6sPvGCtY4xL_Q1pqqdgeL2lwC7tcivwEyRy3rMp1FBRrWOVdyeybww0XiNfEnYOjFyVSI4ED2n2msIdz_2JoHAnphGO7nslpn3MmkO-K1Mnhk7EKiR0kgHboGXwQlcnjW4Cje77PjumPg"
      type: "kubernetes"

2. Register VIM to Tacker by running following command.

   .. code:: bash

      $ source ${devstack_dir}/openrc admin admin
      $ openstack vim register --config-file vim_config_k8s.yaml vim-kubernetes

3. Check the registered VIM status is ``REACHABLE``.

   .. code:: bash

      $ openstack vim list
      +--------------------------------------+----------------+----------------------------------+------------+------------+-----------+
      | ID                                   | Name           | Tenant_id                        | Type       | Is Default | Status    |
      +--------------------------------------+----------------+----------------------------------+------------+------------+-----------+
      | 5a5815fa-bf1d-41f4-a824-3e39bbdcebd0 | vim-kubernetes | a51290751e094e608ad1e5e251b8cd39 | kubernetes | True       | REACHABLE |
      +--------------------------------------+----------------+----------------------------------+------------+------------+-----------+

Helm Environment
~~~~~~~~~~~~~~~~
Create an executable environment for Helm CLI by following procedure below.

1. Install Helm.

   .. code:: bash

      $ HELM_VERSION="3.10.0"  # Change to version that is compatible with your cluster
      $ wget -P /tmp https://get.helm.sh/helm-v$HELM_VERSION-linux-amd64.tar.gz
      $ tar zxf /tmp/helm-v$HELM_VERSION-linux-amd64.tar.gz -C /tmp
      $ sudo mv /tmp/linux-amd64/helm /usr/local/bin/helm

2. Create directory to store Helm chart.

   .. code:: bash

      $ HELM_CHART_DIR="/var/tacker/helm"
      $ sudo mkdir -p $HELM_CHART_DIR

3. Update Helm Connection Information to VIM DB.

   .. code:: bash

      $ mysql
      mysql> use tacker;
      mysql> update vims set extra=json_object(
               'helm_info', '{"masternode_ip": ["127.0.0.1"], "masternode_username": "stack", "masternode_password": "******"}')
               where id="5a5815fa-bf1d-41f4-a824-3e39bbdcebd0";
      mysql> exit

   .. note::

      The specified user must meet the following criteria:
      * User can run Helm CLI commands via SSH.
      * User can access to masternode_ip via ssh with password.
      * User can execute sudo mkdir/chown/rm command without password.

VNF Package
~~~~~~~~~~~
Create and upload the VNF Package that you want to deploy by following procedure below.

1. Prepare VNF Package.
   The sample VNF Package used in this guide is stored in ``o2/tacker/samples/packages/cnf`` directory.

   .. code:: bash

      $ git clone https://gerrit.o-ran-sc.org/r/smo/o2
      $ cd o2/tacker/samples/packages/cnf
      $ ls
      Definitions  Files  input_param.json  TOSCA-Metadata

      $ zip sample_cnf_package.zip -r Definitions/ Files/ TOSCA-Metadata/

   About details to prepare VNF Package, please refer to `Prepare VNF Package`_.

2. Create and Upload VNF Package.

   .. code:: bash

      $ openstack vnf package create
      +-------------------+-------------------------------------------------------------------------------------------------+
      | Field             | Value                                                                                           |
      +-------------------+-------------------------------------------------------------------------------------------------+
      | ID                | 1efcf585-3fea-4813-88dd-bbc93692b51a                                                            |
      | Links             | {                                                                                               |
      |                   |     "self": {                                                                                   |
      |                   |         "href": "/vnfpkgm/v1/vnf_packages/1efcf585-3fea-4813-88dd-bbc93692b51a"                 |
      |                   |     },                                                                                          |
      |                   |     "packageContent": {                                                                         |
      |                   |         "href": "/vnfpkgm/v1/vnf_packages/1efcf585-3fea-4813-88dd-bbc93692b51a/package_content" |
      |                   |     }                                                                                           |
      |                   | }                                                                                               |
      | Onboarding State  | CREATED                                                                                         |
      | Operational State | DISABLED                                                                                        |
      | Usage State       | NOT_IN_USE                                                                                      |
      | User Defined Data | {}                                                                                              |
      +-------------------+-------------------------------------------------------------------------------------------------+

      $ openstack vnf package upload --path sample_cnf_package.zip 1efcf585-3fea-4813-88dd-bbc93692b51a
      Upload request for VNF package 1efcf585-3fea-4813-88dd-bbc93692b51a has been accepted.


Deployment Procedure
--------------------

.. note::

   This procedure uses the CLI available by installing python-tackerclient.
   If you want to process with RESTfull API, see the :doc:`api-docs` for more information.

Create
~~~~~~
Create a VNF Instance by specifying the VNFD ID. The VNFD ID is the value defined in the VNFD file and can be found in the :command:`openstack vnf package show` command.

.. code:: bash

   $ openstack vnflcm create 37391b92-a1d9-44e5-855a-83644cdc3265
   +-----------------------------+------------------------------------------------------------------------------------------------------------------+
   | Field                       | Value                                                                                                            |
   +-----------------------------+------------------------------------------------------------------------------------------------------------------+
   | ID                          | b0915924-7e04-4c16-b229-d3dfcc366eee                                                                             |
   | Instantiation State         | NOT_INSTANTIATED                                                                                                 |
   | Links                       | {                                                                                                                |
   |                             |     "self": {                                                                                                    |
   |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/b0915924-7e04-4c16-b229-d3dfcc366eee"             |
   |                             |     },                                                                                                           |
   |                             |     "instantiate": {                                                                                             |
   |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/b0915924-7e04-4c16-b229-d3dfcc366eee/instantiate" |
   |                             |     }                                                                                                            |
   |                             | }                                                                                                                |
   | VNF Configurable Properties |                                                                                                                  |
   | VNF Instance Description    | None                                                                                                             |
   | VNF Instance Name           | vnf-b0915924-7e04-4c16-b229-d3dfcc366eee                                                                         |
   | VNF Product Name            | Sample CNF                                                                                                       |
   | VNF Provider                | Company                                                                                                          |
   | VNF Software Version        | 1.0                                                                                                              |
   | VNFD ID                     | 37391b92-a1d9-44e5-855a-83644cdc3265                                                                             |
   | VNFD Version                | 1.0                                                                                                              |
   | vnfPkgId                    |                                                                                                                  |
   +-----------------------------+------------------------------------------------------------------------------------------------------------------+

Instantiate
~~~~~~~~~~~
Instantiate a VNF by specifying the ID of the created VNF Instance and a file path of input parameters.

.. code:: bash

   $ cat input_param.json
   {
     "flavourId": "helmchart",
     "additionalParams": {
       "namespace": "default",
       "use_helm": "true",
       "using_helm_install_param": [
         {
           "exthelmchart": "false",
           "helmchartfile_path": "Files/kubernetes/localhelm-0.1.0.tgz",
           "helmreleasename": "tacker-test-vdu"
         }
       ],
       "helm_replica_values": {
         "vdu1_aspect": "replicaCount"
       }
     },
     "vimConnectionInfo": [
       {
         "id": "742f1fc7-7f00-417d-85a6-d4e788353181",
         "vimId": "5a5815fa-bf1d-41f4-a824-3e39bbdcebd0",
         "vimType": "kubernetes"
       }
     ]
   }

   $ openstack vnflcm instantiate b0915924-7e04-4c16-b229-d3dfcc366eee input_param.json
   Instantiate request for VNF Instance b0915924-7e04-4c16-b229-d3dfcc366eee has been accepted.

You can verify that the deployment was successful in the following ways:

1. Verify that the VNF Instance displayed by the command is as follows:

   * ``Instantiation State`` became ``INSTANTIATED``.
   * Deployed resource information is stored in ``vnfcResourceInfo`` of ``Instantiated Vnf Info``.

   .. code:: bash

      $ openstack vnflcm show b0915924-7e04-4c16-b229-d3dfcc366eee
      +-----------------------------+----------------------------------------------------------------------------------------------------------------------+
      | Field                       | Value                                                                                                                |
      +-----------------------------+----------------------------------------------------------------------------------------------------------------------+
      | ID                          | b0915924-7e04-4c16-b229-d3dfcc366eee                                                                                 |
      | Instantiated Vnf Info       | {                                                                                                                    |
      |                             |     "flavourId": "helmchart",                                                                                        |
      |                             |     "vnfState": "STARTED",                                                                                           |
      |                             |     "scaleStatus": [                                                                                                 |
      |                             |         {                                                                                                            |
      |                             |             "aspectId": "vdu1_aspect",                                                                               |
      |                             |             "scaleLevel": 0                                                                                          |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "extCpInfo": [],                                                                                                 |
      |                             |     "vnfcResourceInfo": [                                                                                            |
      |                             |         {                                                                                                            |
      |                             |             "id": "df202937-2bb8-40a9-8be0-a8aa5e2ec0ae",                                                            |
      |                             |             "vduId": "VDU1",                                                                                         |
      |                             |             "computeResource": {                                                                                     |
      |                             |                 "vimConnectionId": null,                                                                             |
      |                             |                 "resourceId": "tacker-test-vdu-localhelm-7b5489f949-fzmc5",                                          |
      |                             |                 "vimLevelResourceType": "Deployment"                                                                 |
      |                             |             },                                                                                                       |
      |                             |             "storageResourceIds": []                                                                                 |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "additionalParams": {                                                                                            |
      |                             |         "useHelm": "true",                                                                                           |
      |                             |         "namespace": "default",                                                                                      |
      |                             |         "helmReplicaValues": {                                                                                       |
      |                             |             "vdu1Aspect": "replicaCount"                                                                             |
      |                             |         },                                                                                                           |
      |                             |         "usingHelmInstallParam": [                                                                                   |
      |                             |             {                                                                                                        |
      |                             |                 "exthelmchart": "false",                                                                             |
      |                             |                 "helmreleasename": "tacker-test-vdu",                                                                |
      |                             |                 "helmchartfilePath": "Files/kubernetes/localhelm-0.1.0.tgz"                                          |
      |                             |             }                                                                                                        |
      |                             |         ]                                                                                                            |
      |                             |     }                                                                                                                |
      |                             | }                                                                                                                    |
      | Instantiation State         | INSTANTIATED                                                                                                         |
      | Links                       | {                                                                                                                    |
      |                             |     "self": {                                                                                                        |
      |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/b0915924-7e04-4c16-b229-d3dfcc366eee"                 |
      |                             |     },                                                                                                               |
      |                             |     "terminate": {                                                                                                   |
      |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/b0915924-7e04-4c16-b229-d3dfcc366eee/terminate"       |
      |                             |     },                                                                                                               |
      |                             |     "heal": {                                                                                                        |
      |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/b0915924-7e04-4c16-b229-d3dfcc366eee/heal"            |
      |                             |     },                                                                                                               |
      |                             |     "changeExtConn": {                                                                                               |
      |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/b0915924-7e04-4c16-b229-d3dfcc366eee/change_ext_conn" |
      |                             |     }                                                                                                                |
      |                             | }                                                                                                                    |
      | VIM Connection Info         | [                                                                                                                    |
      |                             |     {                                                                                                                |
      |                             |         "id": "742f1fc7-7f00-417d-85a6-d4e788353181",                                                                |
      |                             |         "vimId": "5a5815fa-bf1d-41f4-a824-3e39bbdcebd0",                                                             |
      |                             |         "vimType": "kubernetes",                                                                                     |
      |                             |         "interfaceInfo": {},                                                                                         |
      |                             |         "accessInfo": {},                                                                                            |
      |                             |         "extra": {}                                                                                                  |
      |                             |     }                                                                                                                |
      |                             | ]                                                                                                                    |
      | VNF Configurable Properties |                                                                                                                      |
      | VNF Instance Description    | None                                                                                                                 |
      | VNF Instance Name           | vnf-b0915924-7e04-4c16-b229-d3dfcc366eee                                                                             |
      | VNF Product Name            | Sample CNF                                                                                                           |
      | VNF Provider                | Company                                                                                                              |
      | VNF Software Version        | 1.0                                                                                                                  |
      | VNFD ID                     | 37391b92-a1d9-44e5-855a-83644cdc3265                                                                                 |
      | VNFD Version                | 1.0                                                                                                                  |
      | metadata                    | namespace=default                                                                                                    |
      | vnfPkgId                    |                                                                                                                      |
      +-----------------------------+----------------------------------------------------------------------------------------------------------------------+

2. Verify the CNF resources that were actually created as follows:

   .. code:: bash

      $ kubectl get deployment
      NAME                        READY   UP-TO-DATE   AVAILABLE   AGE
      tacker-test-vdu-localhelm   1/1     1            1           20s

      $ kubectl get pod
      NAME                                         READY   STATUS    RESTARTS   AGE
      tacker-test-vdu-localhelm-7b5489f949-fzmc5   1/1     Running   0          24s

      $ helm list
      NAME            NAMESPACE REVISION UPDATED                                 STATUS   CHART           APP VERSION
      tacker-test-vdu default   1        2022-06-29 14:54:32.20990033 +0000 UTC  deployed localhelm-0.1.0 1.16.0

Terminate
~~~~~~~~~
Terminate a VNF by specifying the VNF Instance ID.

.. code:: bash

   $ openstack vnflcm terminate b0915924-7e04-4c16-b229-d3dfcc366eee
   Terminate request for VNF Instance 'b0915924-7e04-4c16-b229-d3dfcc366eee' has been accepted.


.. _ETSI NFV-SOL CNF Deployment by Helm chart: https://docs.openstack.org/tacker/latest/user/mgmt_driver_deploy_k8s_and_cnf_with_helm.html#etsi-nfv-sol-cnf-deployment-by-helm-chart
.. _INF O2 Service Userguide: https://docs.o-ran-sc.org/projects/o-ran-sc-pti-o2/en/latest/user-guide.html
.. _Prepare VNF Package: https://docs.openstack.org/tacker/latest/user/mgmt_driver_deploy_k8s_and_cnf_with_helm.html#prepare-vnf-package
