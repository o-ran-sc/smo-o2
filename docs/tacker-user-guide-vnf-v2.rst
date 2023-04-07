.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0


How to deploy VNF Deployment (V2 API)
=====================================

This user guide shows how to deploy VNF (Virtualized Network Function) via Tacker.
See the `ETSI NFV-SOL VNF Deployment` for the original procedure.

.. contents::
   :depth: 3
   :local:

Prerequisites
-------------

VNF Package
~~~~~~~~~~~
Create and upload the VNF Package that you want to deploy by following procedure below.

1. Prepare VNF Package.
   The sample VNF Package used in this guide is stored in ``o2/tacker/samples/packages/vnf_v2`` directory.

   .. code:: bash

      $ git clone https://gerrit.o-ran-sc.org/r/smo/o2
      $ cd o2/tacker/samples/packages/vnf_v2
      $ ls
      BaseHOT  Definitions  input_param.json  TOSCA-Metadata  UserData

      $ zip sample_vnf_package_v2.zip -r BaseHOT/ Definitions/ TOSCA-Metadata/ UserData/


   About details to prepare VNF Package, please refer to `Prepare VNF Package`_.

2. Create and Upload VNF Package.

   .. code:: bash

      $ openstack vnf package create
      +-------------------+-------------------------------------------------------------------------------------------------+
      | Field             | Value                                                                                           |
      +-------------------+-------------------------------------------------------------------------------------------------+
      | ID                | 18e7b0ec-d006-4b84-8bc5-84f85cfbfff9                                                            |
      | Links             | {                                                                                               |
      |                   |     "self": {                                                                                   |
      |                   |         "href": "/vnfpkgm/v1/vnf_packages/18e7b0ec-d006-4b84-8bc5-84f85cfbfff9"                 |
      |                   |     },                                                                                          |
      |                   |     "packageContent": {                                                                         |
      |                   |         "href": "/vnfpkgm/v1/vnf_packages/18e7b0ec-d006-4b84-8bc5-84f85cfbfff9/package_content" |
      |                   |     }                                                                                           |
      |                   | }                                                                                               |
      | Onboarding State  | CREATED                                                                                         |
      | Operational State | DISABLED                                                                                        |
      | Usage State       | NOT_IN_USE                                                                                      |
      | User Defined Data | {}                                                                                              |
      +-------------------+-------------------------------------------------------------------------------------------------+


      $ openstack vnf package upload --path sample_vnf_package_v2.zip 18e7b0ec-d006-4b84-8bc5-84f85cfbfff9
      Upload request for VNF package 18e7b0ec-d006-4b84-8bc5-84f85cfbfff9 has been accepted.

Deployment Procedure
--------------------

.. note::

   This procedure uses the CLI available by installing python-tackerclient.
   If you want to process with RESTfull API, see the :doc:`api-docs` for more information.

Create
~~~~~~
Create a VNF Instance by specifying the VNFD ID. The VNFD ID is the value defined in the VNFD file and can be found in the :command:`openstack vnf package show` command.

.. code:: bash

   $ openstack vnflcm create e9214953-47d5-45bd-91d1-502accfbe967 --os-tacker-api-version 2
   +-----------------------------+------------------------------------------------------------------------------------------------------------------+
   | Field                       | Value                                                                                                            |
   +-----------------------------+------------------------------------------------------------------------------------------------------------------+
   | ID                          | ae844932-730a-4063-ad1d-7e3f7f9d82d1                                                                             |
   | Instantiation State         | NOT_INSTANTIATED                                                                                                 |
   | Links                       | {                                                                                                                |
   |                             |     "self": {                                                                                                    |
   |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/ae844932-730a-4063-ad1d-7e3f7f9d82d1"             |
   |                             |     },                                                                                                           |
   |                             |     "instantiate": {                                                                                             |
   |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/ae844932-730a-4063-ad1d-7e3f7f9d82d1/instantiate" |
   |                             |     }                                                                                                            |
   |                             | }                                                                                                                |
   | VNF Configurable Properties |                                                                                                                  |
   | VNF Instance Description    |                                                                                                                  |
   | VNF Instance Name           |                                                                                                                  |
   | VNF Product Name            | Sample VNF                                                                                                       |
   | VNF Provider                | Company                                                                                                          |
   | VNF Software Version        | 1.0                                                                                                              |
   | VNFD ID                     | e9214953-47d5-45bd-91d1-502accfbe967                                                                             |
   | VNFD Version                | 1.0                                                                                                              |
   +-----------------------------+------------------------------------------------------------------------------------------------------------------+

Instantiate
~~~~~~~~~~~
Instantiate a VNF by specifying the ID of the created VNF Instance and a file path of input parameters.

.. note::
  Please change the parameters in ``input_param.json`` to suite your environment.

.. code:: bash

   $ cat input_param.json
   {
     "flavourId": "simple",
     "vimConnectionInfo": {
       "vim1": {
         "accessInfo": {
           "username": "admin",
           "password": "devstack",
           "project": "admin",
           "projectDomain": "Default",
           "region": "RegionOne",
           "userDomain": "Default"
         },
         "interfaceInfo": {
           "endpoint": "http://192.168.121.170/identity"
         },
         "vimId": "defb2f96-5670-4bef-8036-27bf61267fc1",
         "vimType": "ETSINFV.OPENSTACK_KEYSTONE.V_3"
       }
     }
   }

   $ openstack vnflcm instantiate ae844932-730a-4063-ad1d-7e3f7f9d82d1 input_param.json --os-tacker-api-version 2
   Instantiate request for VNF Instance ae844932-730a-4063-ad1d-7e3f7f9d82d1 has been accepted.

You can verify that the deployment was successful in the following ways:

1. Verify that the VNF Instance displayed by :command:`openstack vnflcm show` command is as follows:

   * ``Instantiation State`` became ``INSTANTIATED``.
   * Deployed resource information is stored in ``vnfcResourceInfo`` of ``Instantiated Vnf Info``.

   .. code:: bash

      $ openstack vnflcm show ae844932-730a-4063-ad1d-7e3f7f9d82d1 --os-tacker-api-version 2
      +-----------------------------+----------------------------------------------------------------------------------------------------------------------+
      | Field                       | Value                                                                                                                |
      +-----------------------------+----------------------------------------------------------------------------------------------------------------------+
      | ID                          | ae844932-730a-4063-ad1d-7e3f7f9d82d1                                                                                 |
      | Instantiated Vnf Info       | {                                                                                                                    |
      |                             |     "flavourId": "simple",                                                                                           |
      |                             |     "vnfState": "STARTED",                                                                                           |
      |                             |     "scaleStatus": [                                                                                                 |
      |                             |         {                                                                                                            |
      |                             |             "aspectId": "VDU1_scale",                                                                                |
      |                             |             "scaleLevel": 0                                                                                          |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "maxScaleLevels": [                                                                                              |
      |                             |         {                                                                                                            |
      |                             |             "aspectId": "VDU1_scale",                                                                                |
      |                             |             "scaleLevel": 2                                                                                          |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "vnfcResourceInfo": [                                                                                            |
      |                             |         {                                                                                                            |
      |                             |             "id": "5f201bdf-671b-4ba8-9c19-35eb9717ea9d",                                                            |
      |                             |             "vduId": "VDU1",                                                                                         |
      |                             |             "computeResource": {                                                                                     |
      |                             |                 "vimConnectionId": "defb2f96-5670-4bef-8036-27bf61267fc1",                                           |
      |                             |                 "resourceId": "5f201bdf-671b-4ba8-9c19-35eb9717ea9d",                                                |
      |                             |                 "vimLevelResourceType": "OS::Nova::Server"                                                           |
      |                             |             },                                                                                                       |
      |                             |             "vnfcCpInfo": [                                                                                          |
      |                             |                 {                                                                                                    |
      |                             |                     "id": "VDU1_CP1-5f201bdf-671b-4ba8-9c19-35eb9717ea9d",                                           |
      |                             |                     "cpdId": "VDU1_CP1",                                                                             |
      |                             |                     "vnfLinkPortId": "09a8a6ab-9a43-4d3a-9cf8-92b18dd74d17"                                          |
      |                             |                 }                                                                                                    |
      |                             |             ],                                                                                                       |
      |                             |             "metadata": {                                                                                            |
      |                             |                 "creation_time": "2023-04-07T09:49:22Z",                                                             |
      |                             |                 "stack_id": "vnf-ae844932-730a-4063-ad1d-7e3f7f9d82d1-VDU1_scale_group-4qhp7z3cangj-mc4dbvvk73vc-    |
      |                             | edjhilnlkdww/de2707cf-f222-4c97-9c29-33404a50df94",                                                                  |
      |                             |                 "parent_stack_id": "vnf-                                                                             |
      |                             | ae844932-730a-4063-ad1d-7e3f7f9d82d1-VDU1_scale_group-4qhp7z3cangj/3eecd59f-5476-47f5-8135-62debac7499b",            |
      |                             |                 "parent_resource_name": "mc4dbvvk73vc",                                                              |
      |                             |                 "flavor": "m1.tiny",                                                                                 |
      |                             |                 "image-VDU1": "cirros-0.5.2-x86_64-disk"                                                             |
      |                             |             }                                                                                                        |
      |                             |         },                                                                                                           |
      |                             |         {                                                                                                            |
      |                             |             "id": "9fcd21b5-301b-44e2-bb25-6bbffee99c26",                                                            |
      |                             |             "vduId": "VDU2",                                                                                         |
      |                             |             "computeResource": {                                                                                     |
      |                             |                 "vimConnectionId": "defb2f96-5670-4bef-8036-27bf61267fc1",                                           |
      |                             |                 "resourceId": "9fcd21b5-301b-44e2-bb25-6bbffee99c26",                                                |
      |                             |                 "vimLevelResourceType": "OS::Nova::Server"                                                           |
      |                             |             },                                                                                                       |
      |                             |             "vnfcCpInfo": [                                                                                          |
      |                             |                 {                                                                                                    |
      |                             |                     "id": "VDU2_CP1-9fcd21b5-301b-44e2-bb25-6bbffee99c26",                                           |
      |                             |                     "cpdId": "VDU2_CP1",                                                                             |
      |                             |                     "vnfLinkPortId": "b0e4a59e-5831-4deb-aaba-fd4d0d02248b"                                          |
      |                             |                 }                                                                                                    |
      |                             |             ],                                                                                                       |
      |                             |             "metadata": {                                                                                            |
      |                             |                 "creation_time": "2023-04-07T09:49:14Z",                                                             |
      |                             |                 "stack_id": "vnf-ae844932-730a-4063-ad1d-7e3f7f9d82d1/87e00a91-17d7-496e-b30e-a99af1a6726e",         |
      |                             |                 "flavor": "m1.tiny",                                                                                 |
      |                             |                 "image-VDU2": "cirros-0.5.2-x86_64-disk"                                                             |
      |                             |             }                                                                                                        |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "vnfVirtualLinkResourceInfo": [                                                                                  |
      |                             |         {                                                                                                            |
      |                             |             "id": "c2688b4d-f444-4a9c-b5d1-e25766082a14",                                                            |
      |                             |             "vnfVirtualLinkDescId": "internalVL3",                                                                   |
      |                             |             "networkResource": {                                                                                     |
      |                             |                 "vimConnectionId": "defb2f96-5670-4bef-8036-27bf61267fc1",                                           |
      |                             |                 "resourceId": "c2688b4d-f444-4a9c-b5d1-e25766082a14",                                                |
      |                             |                 "vimLevelResourceType": "OS::Neutron::Net"                                                           |
      |                             |             },                                                                                                       |
      |                             |             "vnfLinkPorts": [                                                                                        |
      |                             |                 {                                                                                                    |
      |                             |                     "id": "b0e4a59e-5831-4deb-aaba-fd4d0d02248b",                                                    |
      |                             |                     "resourceHandle": {                                                                              |
      |                             |                         "vimConnectionId": "defb2f96-5670-4bef-8036-27bf61267fc1",                                   |
      |                             |                         "resourceId": "b0e4a59e-5831-4deb-aaba-fd4d0d02248b",                                        |
      |                             |                         "vimLevelResourceType": "OS::Neutron::Port"                                                  |
      |                             |                     },                                                                                               |
      |                             |                     "cpInstanceId": "VDU2_CP1-9fcd21b5-301b-44e2-bb25-6bbffee99c26",                                 |
      |                             |                     "cpInstanceType": "VNFC_CP"                                                                      |
      |                             |                 },                                                                                                   |
      |                             |                 {                                                                                                    |
      |                             |                     "id": "09a8a6ab-9a43-4d3a-9cf8-92b18dd74d17",                                                    |
      |                             |                     "resourceHandle": {                                                                              |
      |                             |                         "vimConnectionId": "defb2f96-5670-4bef-8036-27bf61267fc1",                                   |
      |                             |                         "resourceId": "09a8a6ab-9a43-4d3a-9cf8-92b18dd74d17",                                        |
      |                             |                         "vimLevelResourceType": "OS::Neutron::Port"                                                  |
      |                             |                     },                                                                                               |
      |                             |                     "cpInstanceId": "VDU1_CP1-5f201bdf-671b-4ba8-9c19-35eb9717ea9d",                                 |
      |                             |                     "cpInstanceType": "VNFC_CP"                                                                      |
      |                             |                 }                                                                                                    |
      |                             |             ]                                                                                                        |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "vnfcInfo": [                                                                                                    |
      |                             |         {                                                                                                            |
      |                             |             "id": "VDU1-5f201bdf-671b-4ba8-9c19-35eb9717ea9d",                                                       |
      |                             |             "vduId": "VDU1",                                                                                         |
      |                             |             "vnfcResourceInfoId": "5f201bdf-671b-4ba8-9c19-35eb9717ea9d",                                            |
      |                             |             "vnfcState": "STARTED"                                                                                   |
      |                             |         },                                                                                                           |
      |                             |         {                                                                                                            |
      |                             |             "id": "VDU2-9fcd21b5-301b-44e2-bb25-6bbffee99c26",                                                       |
      |                             |             "vduId": "VDU2",                                                                                         |
      |                             |             "vnfcResourceInfoId": "9fcd21b5-301b-44e2-bb25-6bbffee99c26",                                            |
      |                             |             "vnfcState": "STARTED"                                                                                   |
      |                             |         }                                                                                                            |
      |                             |     ],                                                                                                               |
      |                             |     "metadata": {                                                                                                    |
      |                             |         "stack_id": "87e00a91-17d7-496e-b30e-a99af1a6726e"                                                           |
      |                             |     }                                                                                                                |
      |                             | }                                                                                                                    |
      | Instantiation State         | INSTANTIATED                                                                                                         |
      | Links                       | {                                                                                                                    |
      |                             |     "self": {                                                                                                        |
      |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/ae844932-730a-4063-ad1d-7e3f7f9d82d1"                 |
      |                             |     },                                                                                                               |
      |                             |     "terminate": {                                                                                                   |
      |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/ae844932-730a-4063-ad1d-7e3f7f9d82d1/terminate"       |
      |                             |     },                                                                                                               |
      |                             |     "scale": {                                                                                                       |
      |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/ae844932-730a-4063-ad1d-7e3f7f9d82d1/scale"           |
      |                             |     },                                                                                                               |
      |                             |     "heal": {                                                                                                        |
      |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/ae844932-730a-4063-ad1d-7e3f7f9d82d1/heal"            |
      |                             |     },                                                                                                               |
      |                             |     "changeExtConn": {                                                                                               |
      |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/ae844932-730a-4063-ad1d-7e3f7f9d82d1/change_ext_conn" |
      |                             |     }                                                                                                                |
      |                             | }                                                                                                                    |
      | VIM Connection Info         | {                                                                                                                    |
      |                             |     "vim1": {                                                                                                        |
      |                             |         "vimId": "defb2f96-5670-4bef-8036-27bf61267fc1",                                                             |
      |                             |         "vimType": "ETSINFV.OPENSTACK_KEYSTONE.V_3",                                                                 |
      |                             |         "interfaceInfo": {                                                                                           |
      |                             |             "endpoint": "http://192.168.121.170/identity"                                                            |
      |                             |         },                                                                                                           |
      |                             |         "accessInfo": {                                                                                              |
      |                             |             "region": "RegionOne",                                                                                   |
      |                             |             "project": "admin",                                                                                      |
      |                             |             "username": "admin",                                                                                     |
      |                             |             "userDomain": "Default",                                                                                 |
      |                             |             "projectDomain": "Default"                                                                               |
      |                             |         }                                                                                                            |
      |                             |     }                                                                                                                |
      |                             | }                                                                                                                    |
      | VNF Configurable Properties |                                                                                                                      |
      | VNF Instance Description    |                                                                                                                      |
      | VNF Instance Name           |                                                                                                                      |
      | VNF Product Name            | Sample VNF                                                                                                           |
      | VNF Provider                | Company                                                                                                              |
      | VNF Software Version        | 1.0                                                                                                                  |
      | VNFD ID                     | e9214953-47d5-45bd-91d1-502accfbe967                                                                                 |
      | VNFD Version                | 1.0                                                                                                                  |
      +-----------------------------+----------------------------------------------------------------------------------------------------------------------+

2. Verify the VM created successfully by :command:`openstack stack list/show` command or OpenStack Dashboard.

Terminate
~~~~~~~~~
Terminate a VNF by specifying the VNF Instance ID.

.. code:: bash

   $ openstack vnflcm terminate fba5bda0-0b52-4d80-bffb-709200baf1e3 --os-tacker-api-version 2
   Terminate request for VNF Instance 'fba5bda0-0b52-4d80-bffb-709200baf1e3' has been accepted.


.. _ETSI NFV-SOL VNF Deployment: https://docs.openstack.org/tacker/latest/user/etsi_vnf_deployment_as_vm_with_user_data.html
.. _Prepare VNF Package: https://docs.openstack.org/tacker/latest/user/vnf-package.html
