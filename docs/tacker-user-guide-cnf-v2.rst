.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0


How to deploy CNF Deployment (V2 API)
=====================================

This user guide shows how to deploy CNF (Container Network Function) using Helm via Tacker.
See the `ETSI NFV-SOL CNF Deployment using Helm (v2 VNF LCM API)`_ for the original procedure.
This procedure can be used after OpenStack Tacker Zed release (version 8.0.0).

.. note::

   This document focuses on some operations. See the Tacker `Use Case Guide`_ or `API Reference`_ for other operations.

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

Get VIM Connection Information
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Get VIM connection information by following procedure below.
If you get via INF O2 service, follow the "Use Kubernetes Control Client through O2 DMS profile" section of `INF O2 Service Userguide`_.

1. Get ``bearer_token``:

   You have to confirm Kubernetes Secret name which contains ``bearer_token``.

   .. code:: bash

      $ kubectl get secret
      NAME                  TYPE                                  DATA   AGE
      default-token-cfx5m   kubernetes.io/service-account-token   3      94m

   Then, you can get the bearer token.

   .. code:: bash

      $ TOKEN=$(kubectl get secret default-token-cfx5m -o jsonpath="{.data.token}" | base64 --decode) && echo $TOKEN
      eyJhbGciOiJSUzI1NiIsImtpZCI6IkdWN3VydWFwUW1OYUUxcDc5dlU0V1gxQUZZRmVhTkRuWXJQbElKZmFwaE0ifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImRlZmF1bHQtdG9rZW4tY2Z4NW0iLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZGVmYXVsdCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjVkYzQxODUxLTdkNzYtNGZmYS04MmVmLWEwYjVhODJjMTMzMSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmRlZmF1bHQifQ.JFnE29iPCCOXeVroogNWYMe_r1qBoJNust0paR0yuZ7ctmR5EDeal79-HzFctiIYcoL5fPK0nHc4ZsyAIAyfs5eK-NvBMru3TwY4PduXAZ5U1cu_e1e3SPF31taMwgXXC2NpbtnVocUCC5xJ9V9EXLUV7-AFj14raHvjtnVWFIBkJpTshPbWmbdgMdMMkuAe57OR1kY_KoKlC0fBdHoCRlw-MDwkCN5gTf9eYQstVRmBCtJHDJ638o-2I-wu4bsun7uaZWsA_RCCJrxqdvo9G7EIvoq_LrHhqy7MSA41UhqGCKPwdpl58DoG98PBHNCiyVH47SvFTXyxS6BdYe8ZsQ

2. Get ``ssl_ca_cert``:

   Users can get more information for authenticating to Kubernetes cluster.

   .. code:: bash

      $ kubectl get secrets default-token-cfx5m -o jsonpath="{.data.ca\.crt}" | base64 --decode
      -----BEGIN CERTIFICATE-----
      MIIC/jCCAeagAwIBAgIBADANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDEwprdWJl
      cm5ldGVzMB4XDTIzMDMyMjE0MjQ0NVoXDTMzMDMxOTE0MjQ0NVowFTETMBEGA1UE
      AxMKa3ViZXJuZXRlczCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMRS
      HyraC+APG08cvJqM3NR76PkFnypekpz1XQrtpEk5Jeo11H+ppHIzVBJt6aPrXC0q
      tmK2L6j3MxknSNVxkjWhRwyD24PcyP1b1qXsZK1up0ek6ip0j0YuyUgszSdF204e
      QBp82v1zqAYbuxjy7e6wMv3pDu8yvBkrhqVHvLs6xJ0puUjX7XejrlgnjRwFuc8Q
      X+3VRuHaN9s+OMeiwm4nFDjGwAB7FpA8QPiwCZlA2QD5c6BzdrJA25xlOht6JvBB
      Bk90HS9yHg4kQvnikmudaeohRWv3+xzTK9FjGkFfyV/OBV9F66MsDCE5dGjWySLN
      wwmWlQ7Ad1/6wFfiYCsCAwEAAaNZMFcwDgYDVR0PAQH/BAQDAgKkMA8GA1UdEwEB
      /wQFMAMBAf8wHQYDVR0OBBYEFGwlLS7Sye6uTLz3DYfPUlYKxZj+MBUGA1UdEQQO
      MAyCCmt1YmVybmV0ZXMwDQYJKoZIhvcNAQELBQADggEBAIyCGBpiLsYgTE2WMjYf
      VYjBVZIboDiBfjnAbhn2SdXDjjSGd33gLX/hPOUhE6AkNYeeGD7NcKZfp9Yxg9a5
      0zjEUs19gYjfRx7wP8iSw+Tdml+jibhdvg1oEABfDCy2tZq9R6UwFncRm4dzA3Gc
      t8V94pg6Hd8LgjJBRw9PZ8ui9n5sKtxu1wyXQ1uxyDXoNk8yxnIKnQYadey9LYlp
      FS1jJcArlKfYvXJllNg408I9XJB8gMfJX1gjIBaGOloOCNx69rZ3/LlleKcpTg6V
      6YW/trOaSrZcxIOycBJvemHi8qHxJd1uC9joAT486hT8Bj5jhrFsC3oq3iTgxMNO
      bOc=
      -----END CERTIFICATE-----

3. Get Kubernetes server url:

   By default Kubernetes API server listens on \https://127.0.0.1:6443 and \https://{HOST_IP}:6443.
   Users can get this information through kubectl cluster-info command and try to access API server with the bearer token described in the previous step.

   .. code:: bash

      $ kubectl cluster-info
      Kubernetes control plane is running at https://192.168.121.170:6443

      To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.

Helm Environment
~~~~~~~~~~~~~~~~
Create an executable environment for Helm CLI by following procedure below.
Helm CLI must be executable in an environment running Tacker for V2 API.

.. code:: bash

   $ HELM_VERSION="3.10.3"  # Change to version that is compatible with your cluster
   $ wget -P /tmp https://get.helm.sh/helm-v$HELM_VERSION-linux-amd64.tar.gz
   $ tar zxf /tmp/helm-v$HELM_VERSION-linux-amd64.tar.gz -C /tmp
   $ sudo mv /tmp/linux-amd64/helm /usr/local/bin/helm

VNF Package
~~~~~~~~~~~
Create and upload the VNF Package that you want to deploy by following procedure below.

1. Prepare VNF Package.
   The sample VNF Package used in this guide is stored in ``o2/tacker/samples/packages/cnf_v2`` directory.

   .. code:: bash

      $ git clone https://gerrit.o-ran-sc.org/r/smo/o2
      $ cd o2/tacker/samples/packages/cnf_v2
      $ ls
      Definitions  Files  input_param.json  TOSCA-Metadata

      $ zip sample_cnf_package_v2.zip -r Definitions/ Files/ TOSCA-Metadata/

   About details to prepare VNF Package, please refer to `Prepare VNF Package`_.

2. Create and Upload VNF Package.

   * via CLI command:

     .. code:: bash

        $ openstack vnf package create
        +-------------------+-------------------------------------------------------------------------------------------------+
        | Field             | Value                                                                                           |
        +-------------------+-------------------------------------------------------------------------------------------------+
        | ID                | 9c9e71b2-2710-43f2-913c-3c53f056fad1                                                            |
        | Links             | {                                                                                               |
        |                   |     "self": {                                                                                   |
        |                   |         "href": "/vnfpkgm/v1/vnf_packages/9c9e71b2-2710-43f2-913c-3c53f056fad1"                 |
        |                   |     },                                                                                          |
        |                   |     "packageContent": {                                                                         |
        |                   |         "href": "/vnfpkgm/v1/vnf_packages/9c9e71b2-2710-43f2-913c-3c53f056fad1/package_content" |
        |                   |     }                                                                                           |
        |                   | }                                                                                               |
        | Onboarding State  | CREATED                                                                                         |
        | Operational State | DISABLED                                                                                        |
        | Usage State       | NOT_IN_USE                                                                                      |
        | User Defined Data | {}                                                                                              |
        +-------------------+-------------------------------------------------------------------------------------------------+

        $ openstack vnf package upload --path sample_cnf_package_v2.zip 9c9e71b2-2710-43f2-913c-3c53f056fad1
        Upload request for VNF package 9c9e71b2-2710-43f2-913c-3c53f056fad1  has been accepted.

   * via API:

     .. code:: bash

        $ TACKER_ENDPOINT=http://192.168.121.170:9890
        $ VNFP_ID=$(curl -s -X POST ${TACKER_ENDPOINT}/vnfpkgm/v1/vnf_packages \
                    -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" -d '{}' | jq -r '.id')

        $ VNFP_CONTENTS=./sample_cnf_package_v2.zip

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

     $ openstack vnflcm create 37391b92-a1d9-44e5-855a-83644cdc3265 --os-tacker-api-version 2
     +-----------------------------+------------------------------------------------------------------------------------------------------------------+
     | Field                       | Value                                                                                                            |
     +-----------------------------+------------------------------------------------------------------------------------------------------------------+
     | ID                          | 8ed20808-4d28-47c2-a83d-80e35c62d050                                                                             |
     | Instantiation State         | NOT_INSTANTIATED                                                                                                 |
     | Links                       | {                                                                                                                |
     |                             |     "self": {                                                                                                    |
     |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/8ed20808-4d28-47c2-a83d-80e35c62d050"             |
     |                             |     },                                                                                                           |
     |                             |     "instantiate": {                                                                                             |
     |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/8ed20808-4d28-47c2-a83d-80e35c62d050/instantiate" |
     |                             |     }                                                                                                            |
     |                             | }                                                                                                                |
     | VNF Configurable Properties |                                                                                                                  |
     | VNF Instance Description    |                                                                                                                  |
     | VNF Instance Name           |                                                                                                                  |
     | VNF Product Name            | Sample CNF                                                                                                       |
     | VNF Provider                | Company                                                                                                          |
     | VNF Software Version        | 1.0                                                                                                              |
     | VNFD ID                     | 37391b92-a1d9-44e5-855a-83644cdc3265                                                                             |
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
V2 API allows you to insert VIM connection information directly into the instantiate input parameters.

.. code:: bash

   $ cat input_param.json
   {
      "flavourId": "helmchart",
      "vimConnectionInfo": {
         "vim1": {
               "vimType": "ETSINFV.HELM.V_3",
               "interfaceInfo": {
                  "endpoint": "https://192.168.121.170:6443",
                  "ssl_ca_cert": "-----BEGIN CERTIFICATE-----\nMIIC/jCCAeagAwIBAgIBADANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDEwprdWJl\ncm5ldGVzMB4XDTIzMDMyMjE0MjQ0NVoXDTMzMDMxOTE0MjQ0NVowFTETMBEGA1UE\nAxMKa3ViZXJuZXRlczCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMRS\nHyraC+APG08cvJqM3NR76PkFnypekpz1XQrtpEk5Jeo11H+ppHIzVBJt6aPrXC0q\ntmK2L6j3MxknSNVxkjWhRwyD24PcyP1b1qXsZK1up0ek6ip0j0YuyUgszSdF204e\nQBp82v1zqAYbuxjy7e6wMv3pDu8yvBkrhqVHvLs6xJ0puUjX7XejrlgnjRwFuc8Q\nX+3VRuHaN9s+OMeiwm4nFDjGwAB7FpA8QPiwCZlA2QD5c6BzdrJA25xlOht6JvBB\nBk90HS9yHg4kQvnikmudaeohRWv3+xzTK9FjGkFfyV/OBV9F66MsDCE5dGjWySLN\nwwmWlQ7Ad1/6wFfiYCsCAwEAAaNZMFcwDgYDVR0PAQH/BAQDAgKkMA8GA1UdEwEB\n/wQFMAMBAf8wHQYDVR0OBBYEFGwlLS7Sye6uTLz3DYfPUlYKxZj+MBUGA1UdEQQO\nMAyCCmt1YmVybmV0ZXMwDQYJKoZIhvcNAQELBQADggEBAIyCGBpiLsYgTE2WMjYf\nVYjBVZIboDiBfjnAbhn2SdXDjjSGd33gLX/hPOUhE6AkNYeeGD7NcKZfp9Yxg9a5\n0zjEUs19gYjfRx7wP8iSw+Tdml+jibhdvg1oEABfDCy2tZq9R6UwFncRm4dzA3Gc\nt8V94pg6Hd8LgjJBRw9PZ8ui9n5sKtxu1wyXQ1uxyDXoNk8yxnIKnQYadey9LYlp\nFS1jJcArlKfYvXJllNg408I9XJB8gMfJX1gjIBaGOloOCNx69rZ3/LlleKcpTg6V\n6YW/trOaSrZcxIOycBJvemHi8qHxJd1uC9joAT486hT8Bj5jhrFsC3oq3iTgxMNO\nbOc=\n-----END CERTIFICATE-----"
               },
               "accessInfo": {
                  "bearer_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IkdWN3VydWFwUW1OYUUxcDc5dlU0V1gxQUZZRmVhTkRuWXJQbElKZmFwaE0ifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImRlZmF1bHQtdG9rZW4tY2Z4NW0iLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZGVmYXVsdCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjVkYzQxODUxLTdkNzYtNGZmYS04MmVmLWEwYjVhODJjMTMzMSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmRlZmF1bHQifQ.JFnE29iPCCOXeVroogNWYMe_r1qBoJNust0paR0yuZ7ctmR5EDeal79-HzFctiIYcoL5fPK0nHc4ZsyAIAyfs5eK-NvBMru3TwY4PduXAZ5U1cu_e1e3SPF31taMwgXXC2NpbtnVocUCC5xJ9V9EXLUV7-AFj14raHvjtnVWFIBkJpTshPbWmbdgMdMMkuAe57OR1kY_KoKlC0fBdHoCRlw-MDwkCN5gTf9eYQstVRmBCtJHDJ638o-2I-wu4bsun7uaZWsA_RCCJrxqdvo9G7EIvoq_LrHhqy7MSA41UhqGCKPwdpl58DoG98PBHNCiyVH47SvFTXyxS6BdYe8ZsQ"
               }
         }
      },
      "additionalParams": {
         "helm_chart_path": "Files/kubernetes/test-chart-0.1.0.tgz",
         "helm_value_names": {
               "VDU1": {
                  "replica": "replicaCount"
               }
         },
         "namespace": "default"
      }
   }

* via CLI command:

  .. code:: bash

     $ openstack vnflcm instantiate 8ed20808-4d28-47c2-a83d-80e35c62d050  input_param.json --os-tacker-api-version 2
     Instantiate request for VNF Instance 8ed20808-4d28-47c2-a83d-80e35c62d050  has been accepted.

* via API:

  .. code:: bash

     $ curl -i -X POST ${TACKER_ENDPOINT}/vnflcm/v2/vnf_instances/$VNF_INST_ID/instantiate \
            -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" -H "Version: 2.0.0" \
            -d @./input_param.json

You can verify that the deployment was successful in the following ways:

1. Verify that the VNF Instance displayed by the command is as follows:

   * ``Instantiation State`` became ``INSTANTIATED``.
   * Deployed resource information is stored in ``vnfcResourceInfo`` of ``Instantiated Vnf Info``.

   * via CLI command:

     .. code:: bash

        $ openstack vnflcm show 8ed20808-4d28-47c2-a83d-80e35c62d050 --os-tacker-api-version 2
        +-----------------------------+----------------------------------------------------------------------------------------------------------------------+
        | Field                       | Value                                                                                                                |
        +-----------------------------+----------------------------------------------------------------------------------------------------------------------+
        | ID                          | 8ed20808-4d28-47c2-a83d-80e35c62d050                                                                                 |
        | Instantiated Vnf Info       | {                                                                                                                    |
        |                             |     "flavourId": "helmchart",                                                                                        |
        |                             |     "vnfState": "STARTED",                                                                                           |
        |                             |     "scaleStatus": [                                                                                                 |
        |                             |         {                                                                                                            |
        |                             |             "aspectId": "vdu1_aspect",                                                                               |
        |                             |             "scaleLevel": 0                                                                                          |
        |                             |         }                                                                                                            |
        |                             |     ],                                                                                                               |
        |                             |     "maxScaleLevels": [                                                                                              |
        |                             |         {                                                                                                            |
        |                             |             "aspectId": "vdu1_aspect",                                                                               |
        |                             |             "scaleLevel": 2                                                                                          |
        |                             |         }                                                                                                            |
        |                             |     ],                                                                                                               |
        |                             |     "vnfcResourceInfo": [                                                                                            |
        |                             |         {                                                                                                            |
        |                             |             "id": "vdu1-vnf8ed208084d2847c2a83d80e35c62d050-5b6b57ddbc-lznmz",                                       |
        |                             |             "vduId": "VDU1",                                                                                         |
        |                             |             "computeResource": {                                                                                     |
        |                             |                 "resourceId": "vdu1-vnf8ed208084d2847c2a83d80e35c62d050-5b6b57ddbc-lznmz",                           |
        |                             |                 "vimLevelResourceType": "Deployment"                                                                 |
        |                             |             },                                                                                                       |
        |                             |             "metadata": {}                                                                                           |
        |                             |         }                                                                                                            |
        |                             |     ],                                                                                                               |
        |                             |     "vnfcInfo": [                                                                                                    |
        |                             |         {                                                                                                            |
        |                             |             "id": "VDU1-vdu1-vnf8ed208084d2847c2a83d80e35c62d050-5b6b57ddbc-lznmz",                                  |
        |                             |             "vduId": "VDU1",                                                                                         |
        |                             |             "vnfcResourceInfoId": "vdu1-vnf8ed208084d2847c2a83d80e35c62d050-5b6b57ddbc-lznmz",                       |
        |                             |             "vnfcState": "STARTED"                                                                                   |
        |                             |         }                                                                                                            |
        |                             |     ],                                                                                                               |
        |                             |     "metadata": {                                                                                                    |
        |                             |         "namespace": "default",                                                                                      |
        |                             |         "vdu_reses": {                                                                                               |
        |                             |             "VDU1": {                                                                                                |
        |                             |                 "apiVersion": "apps/v1",                                                                             |
        |                             |                 "kind": "Deployment",                                                                                |
        |                             |                 "metadata": {                                                                                        |
        |                             |                     "name": "vdu1-vnf8ed208084d2847c2a83d80e35c62d050",                                              |
        |                             |                     "labels": {                                                                                      |
        |                             |                         "helm.sh/chart": "test-chart-0.1.0",                                                         |
        |                             |                         "app.kubernetes.io/name": "test-chart",                                                      |
        |                             |                         "app.kubernetes.io/instance": "vnf8ed208084d2847c2a83d80e35c62d050",                         |
        |                             |                         "app.kubernetes.io/version": "1.16.0",                                                       |
        |                             |                         "app.kubernetes.io/managed-by": "Helm"                                                       |
        |                             |                     },                                                                                               |
        |                             |                     "namespace": "default"                                                                           |
        |                             |                 },                                                                                                   |
        |                             |                 "spec": {                                                                                            |
        |                             |                     "replicas": 1,                                                                                   |
        |                             |                     "selector": {                                                                                    |
        |                             |                         "matchLabels": {                                                                             |
        |                             |                             "app.kubernetes.io/name": "test-chart",                                                  |
        |                             |                             "app.kubernetes.io/instance": "vnf8ed208084d2847c2a83d80e35c62d050"                      |
        |                             |                         }                                                                                            |
        |                             |                     },                                                                                               |
        |                             |                     "template": {                                                                                    |
        |                             |                         "metadata": {                                                                                |
        |                             |                             "labels": {                                                                              |
        |                             |                                 "app.kubernetes.io/name": "test-chart",                                              |
        |                             |                                 "app.kubernetes.io/instance": "vnf8ed208084d2847c2a83d80e35c62d050"                  |
        |                             |                             }                                                                                        |
        |                             |                         },                                                                                           |
        |                             |                         "spec": {                                                                                    |
        |                             |                             "serviceAccountName": "vnf8ed208084d2847c2a83d80e35c62d050-test-chart",                  |
        |                             |                             "securityContext": {},                                                                   |
        |                             |                             "containers": [                                                                          |
        |                             |                                 {                                                                                    |
        |                             |                                     "name": "test-chart",                                                            |
        |                             |                                     "securityContext": {},                                                           |
        |                             |                                     "image": "nginx:1.16.0",                                                         |
        |                             |                                     "imagePullPolicy": "IfNotPresent",                                               |
        |                             |                                     "ports": [                                                                       |
        |                             |                                         {                                                                            |
        |                             |                                             "name": "http",                                                          |
        |                             |                                             "containerPort": 80,                                                     |
        |                             |                                             "protocol": "TCP"                                                        |
        |                             |                                         }                                                                            |
        |                             |                                     ],                                                                               |
        |                             |                                     "resources": {}                                                                  |
        |                             |                                 }                                                                                    |
        |                             |                             ]                                                                                        |
        |                             |                         }                                                                                            |
        |                             |                     }                                                                                                |
        |                             |                 }                                                                                                    |
        |                             |             }                                                                                                        |
        |                             |         },                                                                                                           |
        |                             |         "helm_chart_path": "Files/kubernetes/test-chart-0.1.0.tgz",                                                  |
        |                             |         "helm_value_names": {                                                                                        |
        |                             |             "VDU1": {                                                                                                |
        |                             |                 "replica": "replicaCount"                                                                            |
        |                             |             }                                                                                                        |
        |                             |         },                                                                                                           |
        |                             |         "release_name": "vnf8ed208084d2847c2a83d80e35c62d050",                                                       |
        |                             |         "revision": "1"                                                                                              |
        |                             |     }                                                                                                                |
        |                             | }                                                                                                                    |
        | Instantiation State         | INSTANTIATED                                                                                                         |
        | Links                       | {                                                                                                                    |
        |                             |     "self": {                                                                                                        |
        |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/8ed20808-4d28-47c2-a83d-80e35c62d050"                 |
        |                             |     },                                                                                                               |
        |                             |     "terminate": {                                                                                                   |
        |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/8ed20808-4d28-47c2-a83d-80e35c62d050/terminate"       |
        |                             |     },                                                                                                               |
        |                             |     "scale": {                                                                                                       |
        |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/8ed20808-4d28-47c2-a83d-80e35c62d050/scale"           |
        |                             |     },                                                                                                               |
        |                             |     "heal": {                                                                                                        |
        |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/8ed20808-4d28-47c2-a83d-80e35c62d050/heal"            |
        |                             |     },                                                                                                               |
        |                             |     "changeExtConn": {                                                                                               |
        |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/8ed20808-4d28-47c2-a83d-80e35c62d050/change_ext_conn" |
        |                             |     }                                                                                                                |
        |                             | }                                                                                                                    |
        | VIM Connection Info         | {                                                                                                                    |
        |                             |     "vim1": {                                                                                                        |
        |                             |         "vimType": "ETSINFV.HELM.V_3",                                                                               |
        |                             |         "interfaceInfo": {                                                                                           |
        |                             |             "endpoint": "https://192.168.121.170:6443",                                                              |
        |                             |             "ssl_ca_cert": "-----BEGIN CERTIFICATE-----\nMIIC/jCCAeagAwIBAgIBADANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDEwp |
        |                             | rdWJl\ncm5ldGVzMB4XDTIzMDMyMjE0MjQ0NVoXDTMzMDMxOTE0MjQ0NVowFTETMBEGA1UE\nAxMKa3ViZXJuZXRlczCCASIwDQYJKoZIhvcNAQEBBQA |
        |                             | DggEPADCCAQoCggEBAMRS\nHyraC+APG08cvJqM3NR76PkFnypekpz1XQrtpEk5Jeo11H+ppHIzVBJt6aPrXC0q\ntmK2L6j3MxknSNVxkjWhRwyD24P |
        |                             | cyP1b1qXsZK1up0ek6ip0j0YuyUgszSdF204e\nQBp82v1zqAYbuxjy7e6wMv3pDu8yvBkrhqVHvLs6xJ0puUjX7XejrlgnjRwFuc8Q\nX+3VRuHaN9s |
        |                             | +OMeiwm4nFDjGwAB7FpA8QPiwCZlA2QD5c6BzdrJA25xlOht6JvBB\nBk90HS9yHg4kQvnikmudaeohRWv3+xzTK9FjGkFfyV/OBV9F66MsDCE5dGjWy |
        |                             | SLN\nwwmWlQ7Ad1/6wFfiYCsCAwEAAaNZMFcwDgYDVR0PAQH/BAQDAgKkMA8GA1UdEwEB\n/wQFMAMBAf8wHQYDVR0OBBYEFGwlLS7Sye6uTLz3DYfPU |
        |                             | lYKxZj+MBUGA1UdEQQO\nMAyCCmt1YmVybmV0ZXMwDQYJKoZIhvcNAQELBQADggEBAIyCGBpiLsYgTE2WMjYf\nVYjBVZIboDiBfjnAbhn2SdXDjjSGd |
        |                             | 33gLX/hPOUhE6AkNYeeGD7NcKZfp9Yxg9a5\n0zjEUs19gYjfRx7wP8iSw+Tdml+jibhdvg1oEABfDCy2tZq9R6UwFncRm4dzA3Gc\nt8V94pg6Hd8Lg |
        |                             | jJBRw9PZ8ui9n5sKtxu1wyXQ1uxyDXoNk8yxnIKnQYadey9LYlp\nFS1jJcArlKfYvXJllNg408I9XJB8gMfJX1gjIBaGOloOCNx69rZ3/LlleKcpTg6 |
        |                             | V\n6YW/trOaSrZcxIOycBJvemHi8qHxJd1uC9joAT486hT8Bj5jhrFsC3oq3iTgxMNO\nbOc=\n-----END CERTIFICATE-----"                |
        |                             |         },                                                                                                           |
        |                             |         "accessInfo": {                                                                                              |
        |                             |             "bearer_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IkdWN3VydWFwUW1OYUUxcDc5dlU0V1gxQUZZRmVhTkRuWXJQbElKZmFwaE0 |
        |                             | ifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0 |
        |                             | Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImRlZmF1bHQtdG9rZW4tY2Z4NW0iLCJrdWJlcm5ldGVzLmlvL3NlcnZp |
        |                             | Y2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZGVmYXVsdCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50 |
        |                             | LnVpZCI6IjVkYzQxODUxLTdkNzYtNGZmYS04MmVmLWEwYjVhODJjMTMzMSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmRlZmF1 |
        |                             | bHQifQ.JFnE29iPCCOXeVroogNWYMe_r1qBoJNust0paR0yuZ7ctmR5EDeal79-HzFctiIYcoL5fPK0nHc4ZsyAIAyfs5eK-NvBMru3TwY4PduXAZ5U1 |
        |                             | cu_e1e3SPF31taMwgXXC2NpbtnVocUCC5xJ9V9EXLUV7-AFj14raHvjtnVWFIBkJpTshPbWmbdgMdMMkuAe57OR1kY_KoKlC0fBdHoCRlw-MDwkCN5gT |
        |                             | f9eYQstVRmBCtJHDJ638o-2I-wu4bsun7uaZWsA_RCCJrxqdvo9G7EIvoq_LrHhqy7MSA41UhqGCKPwdpl58DoG98PBHNCiyVH47SvFTXyxS6BdYe8Zs |
        |                             | Q"                                                                                                                   |
        |                             |         }                                                                                                            |
        |                             |     }                                                                                                                |
        |                             | }                                                                                                                    |
        | VNF Configurable Properties |                                                                                                                      |
        | VNF Instance Description    |                                                                                                                      |
        | VNF Instance Name           |                                                                                                                      |
        | VNF Product Name            | Sample CNF                                                                                                           |
        | VNF Provider                | Company                                                                                                              |
        | VNF Software Version        | 1.0                                                                                                                  |
        | VNFD ID                     | 37391b92-a1d9-44e5-855a-83644cdc3265                                                                                 |
        | VNFD Version                | 1.0                                                                                                                  |
        +-----------------------------+----------------------------------------------------------------------------------------------------------------------+

   * via API:

     .. code:: bash

        $ curl -X GET ${TACKER_ENDPOINT}/vnflcm/v2/vnf_instances/$VNF_INST_ID \
               -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN"  -H "Version: 2.0.0" \
               | jq -r '{ instantiationState: .instantiationState,
                          vnfcResourceInfo: .instantiatedVnfInfo.vnfcResourceInfo }'
        {
          "instantiationState": "INSTANTIATED",
          "vnfcResourceInfo": [
            {
              "id": "vdu1-vnf8ed208084d2847c2a83d80e35c62d050-5b6b57ddbc-lznmz",
              "vduId": "VDU1",
              "computeResource": {
                "resourceId": "vdu1-vnf8ed208084d2847c2a83d80e35c62d050-5b6b57ddbc-lznmz",
                "vimLevelResourceType": "Deployment"
              },
              "metadata": {}
            }
          ]
        }

2. Verify the CNF resources that were actually created as follows:

   .. code:: bash

      $ kubectl get deployment
      NAME                                       READY   UP-TO-DATE   AVAILABLE   AGE
      vdu1-vnf8ed208084d2847c2a83d80e35c62d050   1/1     1            1           3m15s

      $ kubectl get pod
      NAME                                                        READY   STATUS    RESTARTS   AGE
      vdu1-vnf8ed208084d2847c2a83d80e35c62d050-5b6b57ddbc-lznmz   1/1     Running   0          3m35s

      $ helm list
      NAME                                 NAMESPACE  REVISION  UPDATED                                 STATUS    CHART             APP VERSION
      vnf8ed208084d2847c2a83d80e35c62d050  default    1         2023-04-06 07:15:50.502657283 +0000 UTC deployed  test-chart-0.1.0  1.16.0

Terminate
~~~~~~~~~
Terminate a VNF by specifying the VNF Instance ID.

* via CLI command:

  .. code:: bash

     $ openstack vnflcm terminate 8ed20808-4d28-47c2-a83d-80e35c62d050 --os-tacker-api-version 2
     Terminate request for VNF Instance '8ed20808-4d28-47c2-a83d-80e35c62d050' has been accepted.

* via API:

  .. code:: bash

     $ curl -i -X POST ${TACKER_ENDPOINT}/vnflcm/v2/vnf_instances/$VNF_INST_ID/terminate \
            -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" -H "Version: 2.0.0" \
            -d '{"terminationType": "FORCEFUL"}'

.. _ETSI NFV-SOL CNF Deployment using Helm (v2 VNF LCM API): https://docs.openstack.org/tacker/latest/user/v2/cnf/deployment_using_helm/index.html
.. _Use Case Guide: https://docs.openstack.org/tacker/latest/user/etsi_use_case_guide.html
.. _API Reference: https://docs.openstack.org/api-ref/nfv-orchestration
.. _INF O2 Service Userguide: https://docs.o-ran-sc.org/projects/o-ran-sc-pti-o2/en/latest/user-guide.html
.. _Prepare VNF Package: https://docs.openstack.org/tacker/latest/user/v2/cnf/deployment_using_helm/index.html#prepare-vnf-package
