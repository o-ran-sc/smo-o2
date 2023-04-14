.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0


How to deploy CNF Deployment
============================

This user guide shows how to deploy CNF (Container Network Function) using Helm via Tacker.
See the `ETSI NFV-SOL CNF Deployment by Helm chart`_ for the original procedure.

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

   * via CLI command:

     .. code:: bash

        $ openstack vim register --config-file vim_config_k8s.yaml vim-kubernetes

   * via API:

     .. code:: bash

        $ TACKER_ENDPOINT=http://192.168.121.170:9890
        $ K8S_ENDPOINT=https://192.168.121.170:6443
        $ K8S_TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6InZQOGs5bjhKdExWVkZiRU5reVZRdEtBS0pnYXJFemROcElKNFpIdEFtMXMifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJhZG1pbi11c2VyLXRva2VuLTk4am1qIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImFkbWluLXVzZXIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiIzZWJkZmEzZi0wMTA1LTRhN2YtODFjZS1kYjg5ODcxYTBiMDYiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6a3ViZS1zeXN0ZW06YWRtaW4tdXNlciJ9.df_mV4RbsRN6oOS2KnkaKid0cJBAvdpautWnK67R0y8PRSm79Vc02NbUCmai0M4QiIF9gKhqtM0OYB5vZYJJng9vkcSNVWFUv6hA4Tvjw8FnEcGWe7TnWE2q-ZywJYiZNHvToRIgP5EH5UuLACEXu8KeeG56LxL3T2qNsUenUYLYaT6EciwKiy5SaEk3H1BB0zvSff0d_6sPvGCtY4xL_Q1pqqdgeL2lwC7tcivwEyRy3rMp1FBRrWOVdyeybww0XiNfEnYOjFyVSI4ED2n2msIdz_2JoHAnphGO7nslpn3MmkO-K1Mnhk7EKiR0kgHboGXwQlcnjW4Cje77PjumPg

        $ curl -g -i -X POST ${TACKER_ENDPOINT}/v1.0/vims \
               -H "Accept: application/json" -H "Content-Type: application/json" -H "X-Auth-Token: $TOKEN" \
               -d '{"vim": {"auth_url": "'${K8S_ENDPOINT}'", "type": "kubernetes", "vim_project": {"name": "default"},
                    "auth_cred": {"bearer_token": "'$K8S_TOKEN'"}, "name": "vim-kubernetes", "is_default": true}}'

3. Check the registered VIM status is ``REACHABLE``.

   * via CLI command:

     .. code:: bash

        $ openstack vim list
        +--------------------------------------+----------------+----------------------------------+------------+------------+-----------+
        | ID                                   | Name           | Tenant_id                        | Type       | Is Default | Status    |
        +--------------------------------------+----------------+----------------------------------+------------+------------+-----------+
        | 5a5815fa-bf1d-41f4-a824-3e39bbdcebd0 | vim-kubernetes | a51290751e094e608ad1e5e251b8cd39 | kubernetes | True       | REACHABLE |
        +--------------------------------------+----------------+----------------------------------+------------+------------+-----------+

   * via API:

     .. code:: bash

        $ curl -g -X GET ${TACKER_ENDPOINT}/v1.0/vims \
                  -H "Accept: application/json" -H "X-Auth-Token: $TOKEN" \
                  | jq -r '.vims[] | .id + ": " + .status'
        5a5815fa-bf1d-41f4-a824-3e39bbdcebd0: REACHABLE

Helm Environment
~~~~~~~~~~~~~~~~
Create an executable environment for Helm CLI by following procedure below.

1. Install Helm.

   .. code:: bash

      $ HELM_VERSION="3.10.3"  # Change to version that is compatible with your cluster
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

   * via CLI command:

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

   * via API:

     .. code:: bash

        $ VNFP_ID=$(curl -s -X POST ${TACKER_ENDPOINT}/vnfpkgm/v1/vnf_packages \
                    -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" -d '{}' | jq -r '.id')

        $ VNFP_CONTENTS=./sample_cnf_package.zip

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

* via API:

  .. code:: bash

     $ VNFD_ID=$(curl -s -X GET ${TACKER_ENDPOINT}/vnfpkgm/v1/vnf_packages/$VNFP_ID \
                      -H "X-Auth-Token:$TOKEN" | jq -r '.vnfdId')

     $ VNF_INST_ID=$(curl -sS -X POST ${TACKER_ENDPOINT}/vnflcm/v1/vnf_instances \
                          -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" \
                          -d '{ "vnfdId": "'$VNFD_ID'"}' | jq -r '.id')

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

* via CLI command:

  .. code:: bash

     $ openstack vnflcm instantiate b0915924-7e04-4c16-b229-d3dfcc366eee input_param.json
     Instantiate request for VNF Instance b0915924-7e04-4c16-b229-d3dfcc366eee has been accepted.

* via API:

  .. code:: bash

     $ curl -i -X POST ${TACKER_ENDPOINT}/vnflcm/v1/vnf_instances/$VNF_INST_ID/instantiate \
            -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" \
            -d @./input_param.json

You can verify that the deployment was successful in the following ways:

1. Verify that the VNF Instance displayed by the command is as follows:

   * ``Instantiation State`` became ``INSTANTIATED``.
   * Deployed resource information is stored in ``vnfcResourceInfo`` of ``Instantiated Vnf Info``.

   * via CLI command:

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

   * via API:

     .. code:: bash

        $ curl -X GET ${TACKER_ENDPOINT}/vnflcm/v1/vnf_instances/$VNF_INST_ID \
               -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" \
               | jq -r '{ instantiationState: .instantiationState,
                          vnfcResourceInfo: .instantiatedVnfInfo.vnfcResourceInfo }'
        {
          "instantiationState": "INSTANTIATED",
          "vnfcResourceInfo": [
            {
              "id": "df202937-2bb8-40a9-8be0-a8aa5e2ec0ae",
              "vduId": "VDU1",
              "computeResource": {
                "vimConnectionId": null,
                "resourceId": "tacker-test-vdu-localhelm-7b5489f949-fzmc5",
                "vimLevelResourceType": "Deployment"
              },
              "storageResourceIds": []
            }
          ]
        }

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

* via CLI command:

  .. code:: bash

     $ openstack vnflcm terminate b0915924-7e04-4c16-b229-d3dfcc366eee
     Terminate request for VNF Instance 'b0915924-7e04-4c16-b229-d3dfcc366eee' has been accepted.

* via API:

  .. code:: bash

     $ curl -i -X POST ${TACKER_ENDPOINT}/vnflcm/v1/vnf_instances/$VNF_INST_ID/terminate \
            -H "Content-type: application/json" -H "X-Auth-Token:$TOKEN" \
            -d '{"terminationType": "FORCEFUL"}'

.. _ETSI NFV-SOL CNF Deployment by Helm chart: https://docs.openstack.org/tacker/latest/user/mgmt_driver_deploy_k8s_and_cnf_with_helm.html#etsi-nfv-sol-cnf-deployment-by-helm-chart
.. _Use Case Guide: https://docs.openstack.org/tacker/latest/user/etsi_use_case_guide.html
.. _API Reference: https://docs.openstack.org/api-ref/nfv-orchestration
.. _INF O2 Service Userguide: https://docs.o-ran-sc.org/projects/o-ran-sc-pti-o2/en/latest/user-guide.html
.. _Prepare VNF Package: https://docs.openstack.org/tacker/latest/user/mgmt_driver_deploy_k8s_and_cnf_with_helm.html#prepare-vnf-package
