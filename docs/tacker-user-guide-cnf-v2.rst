.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0


How to deploy CNF Deployment (V2 API)
=====================================

This user guide shows how to deploy CNF (Container Network Function) using Helm via Tacker.
See the `ETSI NFV-SOL CNF Deployment using Helm (v2 VNF LCM API)` for the original procedure.
This procedure can be used after OpenStack Tacker Zed release (version 8.0.0).

.. contents::
   :depth: 3
   :local:

Prerequisites
-------------

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


Deployment Procedure
--------------------

.. note::

   This procedure uses the CLI available by installing python-tackerclient.
   If you want to process with RESTfull API, see the :doc:`api-docs` for more information.

Create
~~~~~~
Create a VNF Instance by specifying the VNFD ID. The VNFD ID is the value defined in the VNFD file and can be found in the :command:`openstack vnf package show` command.

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

   $ openstack vnflcm instantiate 8ed20808-4d28-47c2-a83d-80e35c62d050  input_param.json --os-tacker-api-version 2
   Instantiate request for VNF Instance 8ed20808-4d28-47c2-a83d-80e35c62d050  has been accepted.

You can verify that the deployment was successful in the following ways:

1. Verify that the VNF Instance displayed by the command is as follows:

   * ``Instantiation State`` became ``INSTANTIATED``.
   * Deployed resource information is stored in ``vnfcResourceInfo`` of ``Instantiated Vnf Info``.

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

.. code:: bash

   $ openstack vnflcm terminate 8ed20808-4d28-47c2-a83d-80e35c62d050 --os-tacker-api-version 2
   Terminate request for VNF Instance '8ed20808-4d28-47c2-a83d-80e35c62d050' has been accepted.


.. _ETSI NFV-SOL CNF Deployment using Helm (v2 VNF LCM API): https://docs.openstack.org/tacker/latest/user/etsi_cnf_helm_v2.html
.. _INF O2 Service Userguide: https://docs.o-ran-sc.org/projects/o-ran-sc-pti-o2/en/latest/user-guide.html
.. _Prepare VNF Package: https://docs.openstack.org/tacker/latest/user/etsi_cnf_helm_v2.html#prepare-vnf-package
