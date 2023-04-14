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

Credentials Setting
~~~~~~~~~~~~~~~~~~~

To use Tacker CLI, you need to configure the credentials, please change the ``username`` and ``projectname`` to be executed according to your environment.

.. code:: bash

   $ source ${devstack_dir}/openrc admin admin

If you want to access Tacker via API, you need to get auth token by following procedure below.

.. code:: bash

   # Create request body for issuing access token.
   $ vi get_token.json
   $ cat get_token.json
   {
      "auth": {
         "identity": {
            "methods": ["password"],
            "password": {
               "user": {
                  "domain": {
                        "name": "Default"
                  },
                  "name": "admin",
                  "password": "devstack"
               }
            }
         },
         "scope": {
            "project": {
               "domain": {
                  "name":"Default"
               },
               "name":"admin"
            }
         }
      }
   }

   # Issue auth token for accessing via API.
   $ curl -i -X POST -H "Content-Type: application/json" -d @./get_token.json http://192.168.121.170/identity/v3/auth/tokens
   HTTP/1.1 201 CREATED
   Date: Wed, 12 Apr 2023 07:22:44 GMT
   Server: Apache/2.4.41 (Ubuntu)
   Content-Type: application/json
   Content-Length: 7469
   X-Subject-Token: gAAAAABkNlxENR1WGpfgAe8g2Z4z5lCtwCsfUs5GTsg9mvYTMbG7S8HPIZep0vAGUnoPTj0_IYgMP-W1Y0vCDmWFQH7CSq1XWv3qNMd4aFnclk5sHuP1s0JtHSls7IQMM6zbn-FBYUSWTc9d783OSxYKXWqf3qo-CfFjPwrkmNzfkzgtlogkeA4
   Vary: X-Auth-Token
   x-openstack-request-id: req-e94aa763-9578-424b-affb-7ccab80db72c
   Connection: close

   {"token": {"methods": ["password"], "user": {"domain": {"id": "default", "name": "Default"}, "id": "3e2f3db203e347bfa2197f8fdd038f39", "name": "admin", "password_expires_at": null}, "audit_ids": ["1pgGosVvR4azhw29woKvDw"], "expires_at": "2023-04-12T08:22:44.000000Z", "issued_at": "2023-04-12T07:22:44.000000Z", "project": {"domain": {"id": "default", "name": "Default"}, "id": "5af8bd4dd4ed4285ab1d45a95833cc67", "name": "admin"}, "is_domain": false, "roles": [{"id": "a039c220711049e0b77eac89a1504a81", "name": "reader"}, {"id": "57051bcc1fc24eb4875852a8ab32eae7", "name": "member"}, {"id": "029ea703a2534199a412b18cc5bfa31d", "name": "admin"}], "catalog": [{"endpoints": [{"id": "29307c3ec2f94553acbd7682e32602ba", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170:8989/v2", "region": "RegionOne"}, {"id": "45e5c5f2d4ce4841a980e29e6d3713f7", "interface": "internal", "region_id": "RegionOne", "url": "http://192.168.121.170:8989/v2", "region": "RegionOne"}, {"id": "8d79900575e3490cb71ad6fe5ff0697c", "interface": "admin", "region_id": "RegionOne", "url": "http://192.168.121.170:8989/v2", "region": "RegionOne"}], "id": "00c00313624d4c74aeaa55285e2c553d", "type": "workflowv2", "name": "mistral"}, {"endpoints": [{"id": "aafc7809d8a943d39d20490442ed87fa", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/compute/v2/5af8bd4dd4ed4285ab1d45a95833cc67", "region": "RegionOne"}], "id": "131f57b38d7e4874a18446ab50f3f37b", "type": "compute_legacy", "name": "nova_legacy"}, {"endpoints": [{"id": "e1cd2199468a4486a4df2ffe884b9026", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170:12347/v1", "region": "RegionOne"}], "id": "4dc58229363a4e5fa3d863357554678b", "type": "maintenance", "name": "fenix"}, {"endpoints": [], "id": "53b114aa4c2b4cf7b642ef99e767e58c", "type": "kuryr-kubernetes", "name": "kuryr-kubernetes"}, {"endpoints": [{"id": "1156b12e11a04ac2ab4a674976e8bb3e", "interface": "admin", "region_id": "RegionOne", "url": "http://192.168.121.170/metric", "region": "RegionOne"}, {"id": "191a35e87d824e72819c28790d6dac8d", "interface": "internal", "region_id": "RegionOne", "url": "http://192.168.121.170/metric", "region": "RegionOne"}, {"id": "99016e127b7d4f8483636f5531d994c9", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/metric", "region": "RegionOne"}], "id": "5d483e864b484f76a46266dc5640386b", "type": "metric", "name": "gnocchi"}, {"endpoints": [{"id": "d5ce793eee434288901795720538f811", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/volume/v3/5af8bd4dd4ed4285ab1d45a95833cc67", "region": "RegionOne"}], "id": "5e5f3dc6efa545569f67f453a05ac234", "type": "block-storage", "name": "cinder"}, {"endpoints": [{"id": "6ed501fde45047fe9a3684cc791df953", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/load-balancer", "region": "RegionOne"}], "id": "8b06952a46f3448f9e88daccee3212a9", "type": "load-balancer", "name": "octavia"}, {"endpoints": [{"id": "548133af931b4c0ea8d015dbb67d4388", "interface": "internal", "region_id": "RegionOne", "url": "http://192.168.121.170/identity", "region": "RegionOne"}, {"id": "58f0b35802f442f4997318017a37cae9", "interface": "admin", "region_id": "RegionOne", "url": "http://192.168.121.170/identity", "region": "RegionOne"}, {"id": "c062a6ce0ab54ee699b863b38e15c50a", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/identity", "region": "RegionOne"}], "id": "8ee29bc9aa6d4ddda69f7810b0c52ff5", "type": "identity", "name": "keystone"}, {"endpoints": [{"id": "7fdd1dac28874280928e6c9313b4a415", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/heat-api-cfn/v1", "region": "RegionOne"}], "id": "9605cce5cdad422f8934c891ac840fa7", "type": "cloudformation", "name": "heat-cfn"}, {"endpoints": [{"id": "53ed393173944da3bfac9d482907b65e", "interface": "internal", "region_id": "RegionOne", "url": "http://192.168.121.170:9890/", "region": "RegionOne"}, {"id": "d6891cda1327453aa28155fd18e8596e", "interface": "admin", "region_id": "RegionOne", "url": "http://192.168.121.170:9890/", "region": "RegionOne"}, {"id": "fef7c489ad544e708d9c85e4a801e344", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170:9890/", "region": "RegionOne"}], "id": "a2c67888fc7a4f55a4001cd807293daf", "type": "nfv-orchestration", "name": "tacker"}, {"endpoints": [{"id": "413321647af94f2fb948e59c76bc2b87", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/placement", "region": "RegionOne"}], "id": "aa649b2a9f8644a184fd6857400328ab", "type": "placement", "name": "placement"}, {"endpoints": [{"id": "671a41088c4841d18c58db9ac8a97314", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170:9696/networking", "region": "RegionOne"}], "id": "b1abe9867d07457dbc7c84f37906300a", "type": "network", "name": "neutron"}, {"endpoints": [{"id": "ba3d670defb748a1b23a4697a7998fb7", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/volume/v3/5af8bd4dd4ed4285ab1d45a95833cc67", "region": "RegionOne"}], "id": "b481211f6e5742f1913148ab157259ee", "type": "volumev3", "name": "cinderv3"}, {"endpoints": [{"id": "7eca8bcad7df40cda721a960a838f908", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/heat-api/v1/5af8bd4dd4ed4285ab1d45a95833cc67", "region": "RegionOne"}], "id": "c7c437d0564f428db112516273ca2c0b", "type": "orchestration", "name": "heat"}, {"endpoints": [{"id": "2be3a59b29c04cf7a359ec8b973d334a", "interface": "admin", "region_id": "RegionOne", "url": "http://192.168.121.170/key-manager", "region": "RegionOne"}, {"id": "4258ac8e29084b5a82a48e55b2189284", "interface": "internal", "region_id": "RegionOne", "url": "http://192.168.121.170/key-manager", "region": "RegionOne"}, {"id": "d463ed0ea12a4b44974b9239d2c14a49", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/key-manager", "region": "RegionOne"}], "id": "d411db3bd28a44f7b7c0ae53d3f5bb7b", "type": "key-manager", "name": "barbican"}, {"endpoints": [{"id": "05dea080ccc8493b9aa6a22bfe9d7b2b", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/compute/v2.1", "region": "RegionOne"}], "id": "d69f1f3988ee4809a9bb496f4f312bbd", "type": "compute", "name": "nova"}, {"endpoints": [{"id": "86e8d9e1998b4b9caf503dc58fc1297a", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/reservation/v1", "region": "RegionOne"}], "id": "d81b4911762a4c419f3816c36adcdac1", "type": "reservation", "name": "blazar"}, {"endpoints": [{"id": "0b0195a6580d48bf94eed97a35603756", "interface": "admin", "region_id": "RegionOne", "url": "http://192.168.121.170:8042", "region": "RegionOne"}, {"id": "67d676a732bb4c67abcdc5f433e5b3aa", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170:8042", "region": "RegionOne"}, {"id": "f753f73beed8463fa3f594c29911c332", "interface": "internal", "region_id": "RegionOne", "url": "http://192.168.121.170:8042", "region": "RegionOne"}], "id": "ddd00c0c78b448438bb925776fdbb350", "type": "alarming", "name": "aodh"}, {"endpoints": [{"id": "a39c85e31b3446239f958cc96c634216", "interface": "public", "region_id": "RegionOne", "url": "http://192.168.121.170/image", "region": "RegionOne"}], "id": "fe0a0e3590fa4fa69f395bcdc47f1241", "type": "image", "name": "glance"}]}}

   # Set ``X-Subject-Token`` to environment variables as ``TOKEN``.
   $ TOKEN=gAAAAABkNlxENR1WGpfgAe8g2Z4z5lCtwCsfUs5GTsg9mvYTMbG7S8HPIZep0vAGUnoPTj0_IYgMP-W1Y0vCDmWFQH7CSq1XWv3qNMd4aFnclk5sHuP1s0JtHSls7IQMM6zbn-FBYUSWTc9d783OSxYKXWqf3qo-CfFjPwrkmNzfkzgtlogkeA4


VNF Package
~~~~~~~~~~~
Create and upload the VNF Package that you want to deploy by following procedure below.

1. Prepare VNF Package.
   The sample VNF Package used in this guide is stored in ``o2/tacker/samples/packages/vnf_v2`` directory.

   .. code:: bash

      $ git clone https://gerrit.o-ran-sc.org/r/smo/o2
      $ cd o2/tacker/samples/packages/vnf_v2
      $ ls
      BaseHOT  Definitions  input_param.json  Scripts  TOSCA-Metadata

      $ zip sample_vnf_package_v2.zip -r BaseHOT/ Definitions/ Scripts/ TOSCA-Metadata/


   About details to prepare VNF Package, please refer to `Prepare VNF Package`_.

2. Create and Upload VNF Package.

   * via CLI command:

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

   * via API:

     .. code:: bash

        $ TACKER_ENDPOINT=http://192.168.121.170:9890
        $ VNFP_ID=$(curl -s -X POST ${TACKER_ENDPOINT}/vnfpkgm/v1/vnf_packages \
                    -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" -d '{}' | jq -r '.id')

        $ VNFP_CONTENTS=./sample_vnf_package_v2.zip

        $ curl -i -X PUT ${TACKER_ENDPOINT}/vnfpkgm/v1/vnf_packages/$VNFP_ID/package_content \
               -H "Content-type: application/zip" -H "X-Auth-Token:$TOKEN" -H "Accept:application/zip" \
               -F vnf_package_content=@${VNFP_CONTENTS}


Deployment Procedure
--------------------

Create
~~~~~~
Create a VNF Instance by specifying the VNFD ID. The VNFD ID is the value defined in the VNFD file and can be found in the :command:`openstack vnf package show` command.

* via CLI command:

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

* via API:

  .. code:: bash

     $ VNFD_ID=$(curl -s -X GET ${TACKER_ENDPOINT}/vnfpkgm/v1/vnf_packages/$VNFP_ID \
                      -H "X-Auth-Token:$TOKEN" | jq -r '.vnfdId')

     $ VNF_INST_ID=$(curl -sS -X POST ${TACKER_ENDPOINT}/vnflcm/v2/vnf_instances \
                          -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" -H "Version: 2.0.0" \
                          -d '{ "vnfdId": "'$VNFD_ID'"}' | jq -r '.id')

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


* via CLI command:

  .. code:: bash

     $ openstack vnflcm instantiate ae844932-730a-4063-ad1d-7e3f7f9d82d1 input_param.json --os-tacker-api-version 2
     Instantiate request for VNF Instance ae844932-730a-4063-ad1d-7e3f7f9d82d1 has been accepted.

* via API:

  .. code:: bash

     $ curl -i -X POST ${TACKER_ENDPOINT}/vnflcm/v2/vnf_instances/$VNF_INST_ID/instantiate \
            -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" -H "Version: 2.0.0" \
            -d @./input_param.json

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

* via CLI command:

  .. code:: bash

     $ openstack vnflcm terminate fba5bda0-0b52-4d80-bffb-709200baf1e3 --os-tacker-api-version 2
     Terminate request for VNF Instance 'fba5bda0-0b52-4d80-bffb-709200baf1e3' has been accepted.

* via API:

  .. code:: bash

     $ curl -i -X POST ${TACKER_ENDPOINT}/vnflcm/v2/vnf_instances/$VNF_INST_ID/terminate \
            -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" -H "Version: 2.0.0" \
            -d '{"terminationType": "FORCEFUL"}'

.. _ETSI NFV-SOL VNF Deployment: https://docs.openstack.org/tacker/latest/user/etsi_vnf_deployment_as_vm_with_user_data.html
.. _Prepare VNF Package: https://docs.openstack.org/tacker/latest/user/vnf-package.html
