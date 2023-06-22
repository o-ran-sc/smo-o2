.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0


How to deploy VNF Deployment
============================

This user guide shows how to deploy VNF (Virtualized Network Function) via Tacker.
See the `ETSI NFV-SOL VNF Deployment` for the original procedure.

.. contents::
   :depth: 3
   :local:

Prerequisites
-------------

VIM
~~~
Register OpenStack VIM (Virtualized Infrastructure Manager) by following procedure below.

1. Prepare VIM config file, please change the parameters to suit your environment.

   Sample config file:

   .. code:: bash

      $ cat vim_config.yaml
      auth_url: 'http://192.168.121.170/v3'
      username: 'admin'
      password: 'devstack'
      project_name: 'admin'
      project_domain_name: 'Default'
      user_domain_name: 'Default'
      cert_verify: 'False'

2. Register VIM to Tacker by running following command.

   .. code:: bash

      $ source ${devstack_dir}/openrc admin admin
      $ openstack vim register --config-file vim_config.yaml vim-openstack

3. Check the registered VIM status is ``REACHABLE``.

   .. code:: bash

      $ openstack vim list
      +--------------------------------------+----------------+----------------------------------+------------+------------+-----------+
      | ID                                   | Name           | Tenant_id                        | Type       | Is Default | Status    |
      +--------------------------------------+----------------+----------------------------------+------------+------------+-----------+
      | d8d886e4-fd98-4493-81e2-0e2b9991d629 | vim-openstack  | a51290751e094e608ad1e5e251b8cd39 | openstack  | True       | REACHABLE |
      +--------------------------------------+----------------+----------------------------------+------------+------------+-----------+

VNF Package
~~~~~~~~~~~
Create and upload the VNF Package that you want to deploy by following procedure below.

1. Prepare VNF Package.
   The sample VNF Package used in this guide is stored in ``o2/tacker/samples/packages/vnf`` directory.

   .. code:: bash

      $ git clone https://gerrit.o-ran-sc.org/r/smo/o2
      $ cd o2/tacker/samples/packages/vnf
      $ ls
      BaseHOT  Definitions  Files  input_param.json  TOSCA-Metadata  UserData

      $ wget -P Files/images https://opendev.org/openstack/tacker/raw/branch/master/tacker/tests/etc/samples/etsi/nfv/common/Files/images/cirros-0.5.2-x86_64-disk.img

      $ zip sample_vnf_package.zip -r BaseHOT/ Definitions/ Files/ TOSCA-Metadata/ UserData/


   About details to prepare VNF Package, please refer to `Prepare VNF Package`_.

2. Create and Upload VNF Package.

   .. code:: bash

      $ openstack vnf package create
      +-------------------+-------------------------------------------------------------------------------------------------+
      | Field             | Value                                                                                           |
      +-------------------+-------------------------------------------------------------------------------------------------+
      | ID                | 9f10134f-90ae-4e71-bfdc-de6593552de8                                                            |
      | Links             | {                                                                                               |
      |                   |     "self": {                                                                                   |
      |                   |         "href": "/vnfpkgm/v1/vnf_packages/9f10134f-90ae-4e71-bfdc-de6593552de8"                 |
      |                   |     },                                                                                          |
      |                   |     "packageContent": {                                                                         |
      |                   |         "href": "/vnfpkgm/v1/vnf_packages/9f10134f-90ae-4e71-bfdc-de6593552de8/package_content" |
      |                   |     }                                                                                           |
      |                   | }                                                                                               |
      | Onboarding State  | CREATED                                                                                         |
      | Operational State | DISABLED                                                                                        |
      | Usage State       | NOT_IN_USE                                                                                      |
      | User Defined Data | {}                                                                                              |
      +-------------------+-------------------------------------------------------------------------------------------------+

      $ openstack vnf package upload --path sample_vnf_package.zip 9f10134f-90ae-4e71-bfdc-de6593552de8
      Upload request for VNF package 9f10134f-90ae-4e71-bfdc-de6593552de8 has been accepted.

Deployment Procedure
--------------------

.. note::

   This procedure uses the CLI available by installing python-tackerclient.
   If you want to process with RESTfull API, see the :doc:`api-docs` for more information.

Create
~~~~~~
Create a VNF Instance by specifying the VNFD ID. The VNFD ID is the value defined in the VNFD file and can be found in the :command:`openstack vnf package show` command.

.. code:: bash

   $ openstack vnflcm create b1bb0ce7-ebca-4fa7-95ed-4840d70a1177
   +-----------------------------+------------------------------------------------------------------------------------------------------------------+
   | Field                       | Value                                                                                                            |
   +-----------------------------+------------------------------------------------------------------------------------------------------------------+
   | ID                          | fba5bda0-0b52-4d80-bffb-709200baf1e3                                                                             |
   | Instantiation State         | NOT_INSTANTIATED                                                                                                 |
   | Links                       | {                                                                                                                |
   |                             |     "self": {                                                                                                    |
   |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/fba5bda0-0b52-4d80-bffb-709200baf1e3"             |
   |                             |     },                                                                                                           |
   |                             |     "instantiate": {                                                                                             |
   |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/fba5bda0-0b52-4d80-bffb-709200baf1e3/instantiate" |
   |                             |     }                                                                                                            |
   |                             | }                                                                                                                |
   | VNF Configurable Properties |                                                                                                                  |
   | VNF Instance Description    | None                                                                                                             |
   | VNF Instance Name           | vnf-fba5bda0-0b52-4d80-bffb-709200baf1e3                                                                         |
   | VNF Product Name            | Sample VNF                                                                                                       |
   | VNF Provider                | Company                                                                                                          |
   | VNF Software Version        | 1.0                                                                                                              |
   | VNFD ID                     | b1bb0ce7-ebca-4fa7-95ed-4840d70a1177                                                                             |
   | VNFD Version                | 1.0                                                                                                              |
   | vnfPkgId                    |                                                                                                                  |
   +-----------------------------+------------------------------------------------------------------------------------------------------------------+

Instantiate
~~~~~~~~~~~
Instantiate a VNF by specifying the ID of the created VNF Instance and a file path of input parameters.

.. note::
  Please change the parameters in ``input_param.json`` to suit your environment.

.. code:: bash

   $ cat input_param.json
   {
     "flavourId": "simple",
     "extVirtualLinks": [
       {
         "id": "test1",
         "resourceId": "0e1cc46a-6808-4738-8b84-9e99a775c9eb",
         "extCps": [
           {
             "cpdId": "CP1",
             "cpConfig": [
               {
                 "cpProtocolData": [
                   {
                     "layerProtocol": "IP_OVER_ETHERNET",
                     "ipOverEthernet": {
                       "ipAddresses": [
                         {
                           "type": "IPV4",
                           "numDynamicAddresses": 1,
                           "subnetId": "309614e6-4aab-4424-977f-fd9c8dfe493e"
                         }
                       ]
                     }
                   }
                 ]
               }
             ]
           }
         ]
       }
     ],
     "vimConnectionInfo": [
       {
         "id": "e24f9796-a8e9-4cb0-85ce-5920dcddafa1",
         "vimId": "d8d886e4-fd98-4493-81e2-0e2b9991d629",
         "vimType": "openstack"
       }
     ],
     "additionalParams": {
       "lcm-operation-user-data": "./UserData/lcm_user_data.py",
       "lcm-operation-user-data-class": "SampleUserData"
     }
   }

   $ openstack vnflcm instantiate b0915924-7e04-4c16-b229-d3dfcc366eee input_param.json
   Instantiate request for VNF Instance b0915924-7e04-4c16-b229-d3dfcc366eee has been accepted.

You can verify that the deployment was successful in the following ways:

1. Verify that the VNF Instance displayed by :command:`openstack vnflcm show` command is as follows:

   * ``Instantiation State`` became ``INSTANTIATED``.
   * Deployed resource information is stored in ``vnfcResourceInfo`` of ``Instantiated Vnf Info``.

   .. code:: bash

      $ openstack vnflcm show fba5bda0-0b52-4d80-bffb-709200baf1e3
      +-----------------------------+----------------------------------------------------------------------------------------------------------------------+
      | Field                       | Value                                                                                                                |
      +-----------------------------+----------------------------------------------------------------------------------------------------------------------+
      | ID                          | fba5bda0-0b52-4d80-bffb-709200baf1e3                                                                                 |
      | Instantiated Vnf Info       | {                                                                                                                    |
      |                             |     "flavourId": "simple",                                                                                           |
      |                             |     "vnfState": "STARTED",                                                                                           |
      |                             |     "extCpInfo": [                                                                                                   |
      |                             |         {                                                                                                            |
      |                             |             "id": "4e9cda91-f625-4790-8efb-273b3fbd03a1",                                                            |
      |                             |             "cpdId": "CP1",                                                                                          |
      |                             |             "extLinkPortId": null,                                                                                   |
      |                             |             "associatedVnfcCpId": "65676b39-1e80-435f-997d-217963d25298",                                            |
      |                             |             "cpProtocolInfo": [                                                                                      |
      |                             |                 {                                                                                                    |
      |                             |                     "layerProtocol": "IP_OVER_ETHERNET",                                                             |
      |                             |                     "ipOverEthernet": {                                                                              |
      |                             |                         "macAddress": null,                                                                          |
      |                             |                         "ipAddresses": [                                                                             |
      |                             |                             {                                                                                        |
      |                             |                                 "type": "IPV4",                                                                      |
      |                             |                                 "subnetId": "309614e6-4aab-4424-977f-fd9c8dfe493e",                                  |
      |                             |                                 "isDynamic": true,                                                                   |
      |                             |                                 "addresses": []                                                                      |
      |                             |                             }                                                                                        |
      |                             |                         ]                                                                                            |
      |                             |                     }                                                                                                |
      |                             |                 }                                                                                                    |
      |                             |             ]                                                                                                        |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "extVirtualLinkInfo": [                                                                                          |
      |                             |         {                                                                                                            |
      |                             |             "id": "test1",                                                                                           |
      |                             |             "resourceHandle": {                                                                                      |
      |                             |                 "vimConnectionId": null,                                                                             |
      |                             |                 "resourceId": "0e1cc46a-6808-4738-8b84-9e99a775c9eb",                                                |
      |                             |                 "vimLevelResourceType": null                                                                         |
      |                             |             }                                                                                                        |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "vnfcResourceInfo": [                                                                                            |
      |                             |         {                                                                                                            |
      |                             |             "id": "65676b39-1e80-435f-997d-217963d25298",                                                            |
      |                             |             "vduId": "VDU1",                                                                                         |
      |                             |             "computeResource": {                                                                                     |
      |                             |                 "vimConnectionId": "d8d886e4-fd98-4493-81e2-0e2b9991d629",                                           |
      |                             |                 "resourceId": "0f0ee6b9-cf6c-41c7-a36c-78d41fcba99c",                                                |
      |                             |                 "vimLevelResourceType": "OS::Nova::Server"                                                           |
      |                             |             },                                                                                                       |
      |                             |             "storageResourceIds": [],                                                                                |
      |                             |             "vnfcCpInfo": [                                                                                          |
      |                             |                 {                                                                                                    |
      |                             |                     "id": "a3da58ce-039d-42ee-9569-a0e2a9adf9bb",                                                    |
      |                             |                     "cpdId": "CP1",                                                                                  |
      |                             |                     "vnfExtCpId": null,                                                                              |
      |                             |                     "vnfLinkPortId": "d61dcdf1-d5c6-4add-8b0f-0095b00908cd",                                         |
      |                             |                     "cpProtocolInfo": [                                                                              |
      |                             |                         {                                                                                            |
      |                             |                             "layerProtocol": "IP_OVER_ETHERNET",                                                     |
      |                             |                             "ipOverEthernet": {                                                                      |
      |                             |                                 "macAddress": null,                                                                  |
      |                             |                                 "ipAddresses": [                                                                     |
      |                             |                                     {                                                                                |
      |                             |                                         "type": "IPV4",                                                              |
      |                             |                                         "subnetId": "309614e6-4aab-4424-977f-fd9c8dfe493e",                          |
      |                             |                                         "isDynamic": true,                                                           |
      |                             |                                         "addresses": []                                                              |
      |                             |                                     }                                                                                |
      |                             |                                 ]                                                                                    |
      |                             |                             }                                                                                        |
      |                             |                         }                                                                                            |
      |                             |                     ]                                                                                                |
      |                             |                 }                                                                                                    |
      |                             |             ]                                                                                                        |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "vnfVirtualLinkResourceInfo": [                                                                                  |
      |                             |         {                                                                                                            |
      |                             |             "id": "006531d5-7bb9-472d-9243-7dd415ca9839",                                                            |
      |                             |             "vnfVirtualLinkDescId": "internalVL1",                                                                   |
      |                             |             "networkResource": {                                                                                     |
      |                             |                 "vimConnectionId": null,                                                                             |
      |                             |                 "resourceId": "",                                                                                    |
      |                             |                 "vimLevelResourceType": null                                                                         |
      |                             |             },                                                                                                       |
      |                             |             "vnfLinkPorts": [                                                                                        |
      |                             |                 {                                                                                                    |
      |                             |                     "id": "11d2a97d-884d-4943-9539-bc6c8e4e9e2b",                                                    |
      |                             |                     "resourceHandle": {                                                                              |
      |                             |                         "vimConnectionId": "d8d886e4-fd98-4493-81e2-0e2b9991d629",                                   |
      |                             |                         "resourceId": "601c9ce5-91df-4636-9fc3-0ae979781d9a",                                        |
      |                             |                         "vimLevelResourceType": "OS::Neutron::Port"                                                  |
      |                             |                     },                                                                                               |
      |                             |                     "cpInstanceId": "a3da58ce-039d-42ee-9569-a0e2a9adf9bb"                                           |
      |                             |                 }                                                                                                    |
      |                             |             ]                                                                                                        |
      |                             |         },                                                                                                           |
      |                             |         {                                                                                                            |
      |                             |             "id": "3a9607a4-0d5a-42da-aca3-2c471544ee86",                                                            |
      |                             |             "vnfVirtualLinkDescId": "test1",                                                                         |
      |                             |             "networkResource": {                                                                                     |
      |                             |                 "vimConnectionId": null,                                                                             |
      |                             |                 "resourceId": "0e1cc46a-6808-4738-8b84-9e99a775c9eb",                                                |
      |                             |                 "vimLevelResourceType": "OS::Neutron::Net"                                                           |
      |                             |             },                                                                                                       |
      |                             |             "vnfLinkPorts": [                                                                                        |
      |                             |                 {                                                                                                    |
      |                             |                     "id": "d61dcdf1-d5c6-4add-8b0f-0095b00908cd",                                                    |
      |                             |                     "resourceHandle": {                                                                              |
      |                             |                         "vimConnectionId": null,                                                                     |
      |                             |                         "resourceId": "",                                                                            |
      |                             |                         "vimLevelResourceType": null                                                                 |
      |                             |                     },                                                                                               |
      |                             |                     "cpInstanceId": "a3da58ce-039d-42ee-9569-a0e2a9adf9bb"                                           |
      |                             |                 }                                                                                                    |
      |                             |             ]                                                                                                        |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "vnfcInfo": [                                                                                                    |
      |                             |         {                                                                                                            |
      |                             |             "id": "341f2d8c-b53f-4d4b-b7f2-2f1726355803",                                                            |
      |                             |             "vduId": "VDU1",                                                                                         |
      |                             |             "vnfcState": "STARTED"                                                                                   |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "additionalParams": {                                                                                            |
      |                             |         "lcm-operation-user-data": "./UserData/lcm_user_data.py",                                                    |
      |                             |         "lcm-operation-user-data-class": "SampleUserData"                                                            |
      |                             |     }                                                                                                                |
      |                             | }                                                                                                                    |
      | Instantiation State         | INSTANTIATED                                                                                                         |
      | Links                       | {                                                                                                                    |
      |                             |     "self": {                                                                                                        |
      |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/fba5bda0-0b52-4d80-bffb-709200baf1e3"                 |
      |                             |     },                                                                                                               |
      |                             |     "terminate": {                                                                                                   |
      |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/fba5bda0-0b52-4d80-bffb-709200baf1e3/terminate"       |
      |                             |     },                                                                                                               |
      |                             |     "heal": {                                                                                                        |
      |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/fba5bda0-0b52-4d80-bffb-709200baf1e3/heal"            |
      |                             |     },                                                                                                               |
      |                             |     "changeExtConn": {                                                                                               |
      |                             |         "href": "http://localhost:9890/vnflcm/v1/vnf_instances/fba5bda0-0b52-4d80-bffb-709200baf1e3/change_ext_conn" |
      |                             |     }                                                                                                                |
      |                             | }                                                                                                                    |
      | VIM Connection Info         | [                                                                                                                    |
      |                             |     {                                                                                                                |
      |                             |         "id": "e24f9796-a8e9-4cb0-85ce-5920dcddafa1",                                                                |
      |                             |         "vimId": "d8d886e4-fd98-4493-81e2-0e2b9991d629",                                                             |
      |                             |         "vimType": "openstack",                                                                                      |
      |                             |         "interfaceInfo": {},                                                                                         |
      |                             |         "accessInfo": {},                                                                                            |
      |                             |         "extra": {}                                                                                                  |
      |                             |     }                                                                                                                |
      |                             | ]                                                                                                                    |
      | VNF Configurable Properties |                                                                                                                      |
      | VNF Instance Description    | None                                                                                                                 |
      | VNF Instance Name           | vnf-fba5bda0-0b52-4d80-bffb-709200baf1e3                                                                             |
      | VNF Product Name            | Sample VNF                                                                                                           |
      | VNF Provider                | Company                                                                                                              |
      | VNF Software Version        | 1.0                                                                                                                  |
      | VNFD ID                     | b1bb0ce7-ebca-4fa7-95ed-4840d70a1177                                                                                 |
      | VNFD Version                | 1.0                                                                                                                  |
      | vnfPkgId                    |                                                                                                                      |
      +-----------------------------+----------------------------------------------------------------------------------------------------------------------+

2. Verify the VM created successfully by :command:`openstack stack list/show` command or OpenStack Dashboard.

Terminate
~~~~~~~~~
Terminate a VNF by specifying the VNF Instance ID.

.. code:: bash

   $ openstack vnflcm terminate fba5bda0-0b52-4d80-bffb-709200baf1e3
   Terminate request for VNF Instance 'fba5bda0-0b52-4d80-bffb-709200baf1e3' has been accepted.


.. _ETSI NFV-SOL VNF Deployment: https://docs.openstack.org/tacker/latest/user/etsi_vnf_deployment_as_vm_with_user_data.html
.. _Prepare VNF Package: https://docs.openstack.org/tacker/latest/user/vnf-package.html
