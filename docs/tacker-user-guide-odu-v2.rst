.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

ODU Deployment via tacker
=========================

Initial Setup Preparation
-------------------------

1. Setup devstack(Openstack and Kubernetes as VIM) environment

    * https://docs.openstack.org/tacker/latest/install/devstack.html

   .. note::

      In above document Refer “Openstack and Kubernetes as VIM” section to install Kubernetes as VIM.

2. Setup Helm VIM

    * https://docs.openstack.org/tacker/2024.1/user/v2/cnf/deployment_using_helm/index.html#prepare-helm-vim


Preconditions
-------------

1. Create a directory

   .. code:: bash

      $ mkdir ODU_CONTAINER

2. Navigate to the directory

   .. code:: bash

      $ cd ODU_CONTAINER

3. Clone the ODU(high) repository

   .. code:: bash

      $ git clone https://gerrit.o-ran-sc.org/r/o-du/l2

4. Generate Docker Image

   .. code:: bash

      $ cd l2
      $ docker build -f Dockerfile.du -t <my-image>:<tag>
        docker build -f Dockerfile.du -t new-du-container:v1 .

5. Check if the Docker images are created

   .. code:: bash

      $ docker images

6. Upload ODU image to quay.io repository

   .. code:: bash

      $ docker login quay.io
      $ docker <tag> <my-image> quay.io/<myrepository>/<my-image>
      $ docker push quay.io/<myrepository>/<my-image>


   .. note::

      An account is mandatory on quay.io because during ODU pod deployment image is pulled form quay.io repository.

7. Verify Image

   .. code:: bash

      $ docker pull quay.io/<myrepository>/<my-image>


Prepare Helm VNF Package for ODU Deployment using tacker
--------------------------------------------------------

1. Prepare VNF Package.
   The sample VNF Package used in this guide is stored in ``o2/tacker/samples/packages/odu_v2`` directory.

   .. code:: bash

      $ cd /opt/stack
      $ git clone https://gerrit.o-ran-sc.org/r/smo/o2
      $ cd o2/tacker/samples/packages/odu_v2
      $ ls
      Definitions  Files  inst.json  TOSCA-Metadata Scripts

2. Change repository value in Helm chart

   Update the value of “repository” with ODU image name uploaded to quay.io repository in values.yaml file.

   .. code:: bash

      $ cd Files/Kubernetes/test-chart
      $ vi values.yaml

      image:
        repository: <myrepository>/<my-image>  #change repository value here
        # pullPolicy: IfNotPresent
        # Overrides the image tag whose default is the chart appVersion.
      tag: v1

3. Compress VNF Package

   .. code:: bash

      $ cd /opt/stack/o2/tacker/samples/packages/odu_v2/Files/Kubernetes
      $ tar -cvzf test-chart-0.1.0.tgz test-chart/


   Change hash value in TOSCA.meta file using below command-

   .. code:: bash

      $ cd /opt/stack/o2/tacker/samples/packages/odu_v2/
      $ sha256sum Files/kubernetes/test-chart-0.1.0.tgz
      fa05dd35f45adb43ff1c6c77675ac82c477c5a55a3ad14a87a6b542c21cf4f7c

      Name: Files/kubernetes/test-chart-0.1.0.tgz
      Content-Type: test-data
      Algorithm: SHA-256
      Hash: fa05dd35f45adb43ff1c6c77675ac82c477c5a55a3ad14a87a6b542c21cf4f7c  #change hash key

   Compress the VNF Package

   .. code:: bash

      $ zip -r cnf.zip Definitions Files TOSCA-Metadata Scripts


Create and Upload VNF Package
-----------------------------

1. Create VNF Package

   .. code:: bash

      $ openstack vnf package create

        +-------------------+-------------------------------------------------------------------------------------------------+
        | Field             | Value                                                                                           |
        +-------------------+-------------------------------------------------------------------------------------------------+
        | ID                | cb784ab4-2d0f-46f9-a0f1-37ade6661acf                                                            |
        | Links             | {                                                                                               |
        |                   |     "self": {                                                                                   |
        |                   |         "href": "/vnfpkgm/v1/vnf_packages/cb784ab4-2d0f-46f9-a0f1-37ade6661acf"                 |
        |                   |     },                                                                                          |
        |                   |     "packageContent": {                                                                         |
        |                   |         "href": "/vnfpkgm/v1/vnf_packages/cb784ab4-2d0f-46f9-a0f1-37ade6661acf/package_content" |
        |                   |     }                                                                                           |
        |                   | }                                                                                               |
        | Onboarding State  | CREATED                                                                                         |
        | Operational State | DISABLED                                                                                        |
        | Usage State       | NOT_IN_USE                                                                                      |
        | User Defined Data | {}                                                                                              |
        +-------------------+-------------------------------------------------------------------------------------------------+

2. Upload VNF Package

   .. code:: bash

       $ openstack vnf package upload --path cnf.zip cb784ab4-2d0f-46f9-a0f1-37ade6661acf
         Upload request for VNF package 9c9e71b2-2710-43f2-913c-3c53f056fad1  has been accepted.

3. Get VNFD ID

   .. code:: bash

      $ openstack vnf package show cb784ab4-2d0f-46f9-a0f1-37ade6661acf -c "VNFD ID"

         +--------------------------------------+
         | ID                                   |
         +--------------------------------------+
         | 6fd8696a-2c3a-48e9-8f59-3cbb250844c3 |
         +--------------------------------------+


Create and Instantiate VNF
--------------------------

1. Create VNF

   .. code:: bash

      $ openstack vnflcm create 6fd8696a-2c3a-48e9-8f59-3cbb250844c3 --os-tacker-api 2

     +-----------------------------+------------------------------------------------------------------------------------------------------------------+
     | Field                       | Value                                                                                                            |
     +-----------------------------+------------------------------------------------------------------------------------------------------------------+
     | ID                          | f770aa83-1a9c-4c8e-9bce-fc9d1e652c25                                                                             |
     | Instantiation State         | NOT_INSTANTIATED                                                                                                 |
     | Links                       | {                                                                                                                |
     |                             |     "self": {                                                                                                    |
     |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/f770aa83-1a9c-4c8e-9bce-fc9d1e652c25"             |
     |                             |     },                                                                                                           |
     |                             |     "instantiate": {                                                                                             |
     |                             |         "href": "http://127.0.0.1:9890/vnflcm/v2/vnf_instances/f770aa83-1a9c-4c8e-9bce-fc9d1e652c25/instantiate" |
     |                             |     }                                                                                                            |
     |                             | }                                                                                                                |
     | VNF Configurable Properties |                                                                                                                  |
     | VNF Instance Description    |                                                                                                                  |
     | VNF Instance Name           |                                                                                                                  |
     | VNF Product Name            | Sample CNF                                                                                                       |
     | VNF Provider                | Company                                                                                                          |
     | VNF Software Version        | 1.0                                                                                                              |
     | VNFD ID                     | 6fd8696a-2c3a-48e9-8f59-3cbb250844c3                                                                             |
     | VNFD Version                | 1.0                                                                                                              |
     +-----------------------------+------------------------------------------------------------------------------------------------------------------+

2. Instantiate VNF

   A json file which includes Helm VIM information and additionalParams should be provided for instantiating a containerized VNF.

   .. code:: bash

      $ cat inst.json
      {
        "flavourId": "simple",
        "vimConnectionInfo": {
          "vim1": {
            "vimId": "897af4d6-9340-4f81-87ca-2bb6b13ca4f7",
            "vimType": "kubernetes"
           }
        },
        "additionalParams": {
          "helm_chart_path": "Files/kubernetes/du.tgz",
          "helm_parameters": {
            "service.port": 8081,
            "service.type": "NodePort"
          },
          "helm_value_names": {
            "odu1": {
              "replica": "replicaCount"
             }
          },
          "namespace": "default"
        }
      }

   Instantiate VNF created in first step using the json file mentioned above.

   .. code:: bash

      $ openstack vnflcm instantiate f770aa83-1a9c-4c8e-9bce-fc9d1e652c25  inst.json --os-tacker-api-version 2
      Instantiate request for VNF Instance f770aa83-1a9c-4c8e-9bce-fc9d1e652c25  has been accepted.


Check ODU status
----------------

1. Helm status

   .. code:: bash

      $ helm list

      NAME                                    NAMESPACE       REVISION        UPDATED                                 STATUS          CHART           APP VERSION
      vnff770aa831a9c4c8e9bcefc9d1e652c25     default         1               2025-06-02 05:51:18.013317536 +0000 UTC deployed        du-0.1.0        1.16.0

2. Pod status

   .. code:: bash

      $ kubectl get pods

      NAME                                                       READY   STATUS    RESTARTS   AGE
      odu1-vnff770aa831a9c4c8e9bcefc9d1e652c25-d5887d5dc-gh6xg   1/1     Running   0          7d4h

3. VNF Status

   .. code:: bash

      $ openstack vnflcm list --os-tacker-api 2 | grep  f770aa83-1a9c-4c8e-9bce-fc9d1e652c25

      +--------------------------------------+-------------------+---------------------+--------------+----------------------+------------------+--------------------------------------+
      | ID                                   | VNF Instance Name | Instantiation State | VNF Provider | VNF Software Version | VNF Product Name | VNFD ID                              |
      +--------------------------------------+-------------------+---------------------+--------------+----------------------+------------------+--------------------------------------+
      | f770aa83-1a9c-4c8e-9bce-fc9d1e652c25 |                   | INSTANTIATED        | Company      | 1.0                  | Sample VNF       | 6fd8696a-2c3a-48e9-8f59-3cbb250844c3 |
      +--------------------------------------+-------------------+---------------------+--------------+----------------------+------------------+--------------------------------------+
