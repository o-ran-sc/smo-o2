.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

Developer-Guide
===============

.. contents::
   :depth: 3
   :local:

.. note:
..   * This section is used to describe what a contributor needs to know in order to work on the componenta

..   * this should be very technical, aimed at people who want to help develop the components

..   * this should be how the component does what it does, not a requirements document of what the component should do

..   * this should contain what language(s) and frameworks are used, with versions

..   * this should contain how to obtain the code, where to look at work items (Jira tickets), how to get started developing

..   * This note must be removed after content has been added.


Testing
-------

API conformance test with ETSI NFV-TST
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
`ETSI NFV-TST 010`_ specifies the method of the conformance test for APIs specified by NFV specifications.
ETSI NFV-TST provides test suites called 'api-tests' based on `ETSI NFV-TST 010`_ specification.
The following steps are the procedure of API conformance test according to the specification by using Xtesting.

* Xtesting environment setup

   1. Check out code from smo-o2 repository into /tmp/ directory in your local machine.

      .. code:: bash

         $ cd /tmp
         $ git clone https://gerrit.o-ran-sc.org/r/smo/o2

   2. Create a virtual environment for xtesting in '~/tacker/tacker/tests/' directory.

      .. code:: bash

         $ cd ~/tacker/tacker/tests
         $ mkdir xtesting
         $ cd xtesting
         $ virtualenv xtesting-py3 -p python3

   3. Activate the virtual environment.

      .. code:: bash

         $ . xtesting-py3/bin/activate

   4. Install Xtesting package.

      .. code:: bash

         $ pip install xtesting

   5. Copy 'requirements.txt' from '/tmp/o2/tacker/tacker/tests/xtesting/' to the current directory and install it.

      .. code:: bash

         $ cp /tmp/o2/tacker/tacker/tests/xtesting/requirements.txt .
         $ pip install -r requirements.txt

   6. Check out 'api-tests' repository in ETSI NFV into the current directory.

      .. code:: bash

         $ git clone https://forge.etsi.org/rep/nfv/api-tests.git

   7. Copy the directories and file under '/tmp/o2/tacker/tacker/tests/xtesting/' to the location under the current directory.

      .. code:: bash

         $ cp -r /tmp/o2/tacker/tacker/tests/xtesting/api-tests/SOL003/CNFDeployment ./api-tests/SOL003
         $ cp -r /tmp/o2/tacker/tacker/tests/xtesting/api-tests/SOL003/cnflcm ./api-tests/SOL003
         $ cp -r /tmp/o2/tacker/tacker/tests/xtesting/api-tests/SOL005/CNFPrecondition ./api-tests/SOL005
         $ mkdir jsons
         $ cp ./api-tests/SOL003/cnflcm/jsons/inst.json ./jsons/instantiateVnfRequest.json

   8. Copy 'testcases.yaml' file from '/tmp/o2/tacker/tacker/tests/xtesting/' directory to the location under the current directory.

      .. code:: bash

         $ cp /tmp/o2/tacker/tacker/tests/xtesting/testcases.yaml ./xtesting-py3/lib/python3.8/site-packages/xtesting/ci/

      .. note::

         If user is working in other directory than '/opt/stack/', then the path specified in 'suites' parameter in 'testcases.yaml' needs to be updated to the actual one.

   9. Give permissions to 'api-tests' directory.

      .. code:: bash

         $ sudo chmod -R 775 api-tests

   10. Update 'VNFM_SCHEMA', 'VNFM_PORT', 'VNFM_HOST', 'PODS_SCHEMA', 'PODS_PORT' and 'PODS_HOST' variables in below files with appropriate Request Type (http or https), Port and Host.

      .. code:: bash

         $ vi api-tests/SOL003/CNFDeployment/environment/variables.txt

   11. Copy necessary files under api-tests directory into the designated location.

      .. code:: bash

         $ cp ./api-tests/SOL003/VNFLifecycleManagement-API/jsons/createVnfRequest.json ./jsons
         $ cp ./api-tests/SOL003/VNFLifecycleManagement-API/jsons/healVnfRequest.json ./jsons
         $ mkdir schemas
         $ cp ./api-tests/SOL003/VNFLifecycleManagement-API/schemas/vnfInstance.schema.json ./schemas

<<<<<<< HEAD   (4857aa Merge "Improve Xtesting for using NFV-TST code directly")
   12. Modify robot files under api-tests directory as below.

      .. code:: bash

         $ vi api-tests/SOL003/VNFLifecycleManagement-API/VnfLcmMntOperationKeywords.robot

      E.g: Part of file content

         .. code:: bash

            (Omitted)
           
            POST Create a new vnfInstance
                Log    Create VNF instance by POST to /vnf_instances
                Set Headers  {"Accept":"${ACCEPT}"}
                Set Headers  {"Content-Type": "${CONTENT_TYPE}"}
                Run Keyword If    ${AUTH_USAGE} == 1    Set Headers    {"${AUTHORIZATION_HEADER}":"${AUTHORIZATION_TOKEN}"}
                Run Keyword If    ${check_descriptors} == 1    PARSE the Descriptor File
                ${template}=    Get File    jsons/createVnfRequest.json
                ${body}=        Format String   ${template}     vnfdId=${vnfdId}    vnfProvider=${Provider}    vnfProductName=${Product_Name}    vnfSoftwareVersion=${Software_Version}    vnfdVersion= ${Descriptor_Version}
                Post    ${apiRoot}/${apiName}/${apiMajorVersion}/vnf_instances    ${body}
                ${outputResponse}=    Output    response
                    Set Global Variable    ${response}    ${outputResponse}
                ${res_body}=    Get From Dictionary     ${outputResponse}    body                       # Add this line
                ${vnfInstanceId}=    Get From Dictionary     ${res_body}    id                          # Add this line
                Set Global Variable    ${vnfInstanceId}                                                 # Add this line
                Run Process    api-tests/SOL003/cnflcm/update_variable.sh  ${vnfInstanceId}  shell=yes  # Add this line
           
            GET multiple vnfInstances
                Log    Query VNF The GET method queries information about multiple VNF instances.

            (Omitted)

      .. note::

         This change is for holding variable between test cases.

      .. code:: bash

         $ vi api-tests/SOL003/VNFLifecycleManagement-API/VNFInstances.robot

      E.g: Part of file content

         .. code:: bash

            (Omitted)

            POST Create a new vnfInstance
                [Documentation]    Test ID: 7.3.1.1.1
                ...    Test title: POST Create a new vnfInstance
                ...    Test objective: The objective is to create a new VNF instance resource
                ...    Pre-conditions: none
                ...    Reference: Clause 5.4.2.3.1 - ETSI GS NFV-SOL 003 [1] v2.8.1
                ...    Config ID: Config_prod_VNFM
                ...    Applicability: none
                ...    Post-Conditions: VNF instance created
                POST Create a new vnfInstance
                Check HTTP Response Status Code Is    201
                Check HTTP Response Body Json Schema Is    vnfInstance
           
            *** comment ***                                           # Add this line
            GET information about multiple VNF instances
                [Documentation]    Test ID: 7.3.1.1.2
           
            (Omitted)

      .. note::

         This change is for avoiding running unnecessary test cases.

=======
>>>>>>> CHANGE (56673b Improve Xtesting code for interconnecting test cases)
* Preconditioning for test execution

   1. If there is no 'nfv_user' and 'nfv' project, create them in your local environment.

      .. code:: bash

         $ source ${devstack_dir}/openrc admin admin
         $ openstack project create --domain default nfv
         $ openstack user create --domain default --project nfv --password devstack nfv_user
         $ openstack role add --project nfv --user nfv_user admin

   2. Register Kubernetes VIM and create an executable environment for Helm CLI from steps mentioned in below link.

      * https://docs.o-ran-sc.org/projects/o-ran-sc-smo-o2/en/latest/tacker-user-guide-cnf.html

   3. Install 'dos2unix' and 'jq'.

      .. code:: bash

         $ sudo apt-get install dos2unix
         $ sudo apt install jq

   4. Execute script 'packageTest.sh' for package creation and uploading.

      .. code:: bash

         $ cd ~/tacker/tacker/tests/xtesting/api-tests/SOL005/CNFPrecondition
         $ ./packageTest.sh ../../SOL003/VNFLifecycleManagement-API/environment/variables.txt

<<<<<<< HEAD   (4857aa Merge "Improve Xtesting for using NFV-TST code directly")
   5. Get 'vimId' and change it in the file 'instantiateVnfRequest.json' as below.
=======
   5. Get 'vnfdId' and change it in the file 'createVnfRequest.json' as below.

     .. code:: bash

         $ openstack vnf package list -c "Id"

           E.g: Output of command

           +--------------------------------------+
           | ID                                   |
           +--------------------------------------+
           | 0ca03e2e-1c51-4696-9baa-36f974185825 |
           +--------------------------------------+

         $ openstack vnf package show 0ca03e2e-1c51-4696-9baa-36f974185825 -c "VNFD ID"

           E.g: Output of command

           +---------+--------------------------------------+
           | Field   | Value                                |
           +---------+--------------------------------------+
           | VNFD ID | 4688aff3-b456-4b07-bca6-089db8aec8b0 |
           +---------+--------------------------------------+

         $ vi ~/tacker/tacker/tests/xtesting/jsons/createVnfRequest.json

           E.g: Content of file

           {
             "vnfdId": "4688aff3-b456-4b07-bca6-089db8aec8b0", # Update value here
             "vnfInstanceName": "",
             "vnfInstanceDescription": "",
             "vnfProvider":"Company",
             "vnfProductName":"Sample CNF",
             "vnfSoftwareVersion":"1.0",
             "vnfdVersion":"1.0",
             "metadata":{}
           }

   6. Get 'vimId' and change it in the file 'instantiateVnfRequest.json' as below.
>>>>>>> CHANGE (56673b Improve Xtesting code for interconnecting test cases)

      .. code:: bash

         $ openstack vim list -c "ID"

           E.g: Output of command

           +--------------------------------------+
           | ID                                   |
           +--------------------------------------+
           | 08260b52-c3f6-47a9-bb1f-cec1f0d3956a |
           +--------------------------------------+

         $ vi ~/tacker/tacker/tests/xtesting/jsons/instantiateVnfRequest.json

           E.g: Content of file

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
               },
               "vdu_mapping": {
                 "VDU1": {
                   "kind": "Deployment",
                   "name": "tacker-test-vdu-localhelm",
                   "helmreleasename": "tacker-test-vdu"
                 }
               }
             },
             "vimConnectionInfo": [
               {
                 "id": "742f1fc7-7f00-417d-85a6-d4e788353181",
                 "vimId": "d7a811a3-e3fb-41a1-a4e2-4dce2209bcfe",  # Update value here
                 "vimType": "kubernetes"
               }
             ]
           }

   6. Start kubectl proxy.

      .. code:: bash

         $ kubectl proxy --port=8080 &

* Testing steps

   1. Verify Vnflcm Create, Instantiate and Heal.

      .. code:: bash

         $ cd ~/tacker/tacker/tests/xtesting/
         $ . xtesting-py3/bin/activate
         $ sudo xtesting-py3/bin/run_tests -t cnf-lcm-validation

   2. Verify getting all pods and getting specific pod.

      .. code:: bash

         $ cd ~/tacker/tacker/tests/xtesting/
         $ . xtesting-py3/bin/activate
         $ sudo xtesting-py3/bin/run_tests -t cnf-deployments-validation

      E.g: Output of command

         .. code:: bash

            2022-12-05 05:10:13,908 - xtesting.core.robotframework - INFO -
            ==============================================================================
            IndividualCnfLcmOperationOccurrence
            ==============================================================================
            Get All Pods :: Test ID: 7.3.1.12.7 Test title: Get All Pods Test ... | PASS |
            ------------------------------------------------------------------------------
            Get Specific Pod :: Test ID: 7.3.1.12.8 Test title: Get Specific P... | PASS |
            ------------------------------------------------------------------------------
            IndividualCnfLcmOperationOccurrence                                   | PASS |
            2 tests, 2 passed, 0 failed
            ==============================================================================
            Output:  /var/lib/xtesting/results/cnf-deployments-validation/output.xml

            2022-12-05 05:10:13,913 - xtesting.core.robotframework - INFO - Results were successfully parsed
            2022-12-05 05:10:13,968 - xtesting.core.robotframework - INFO - Results were successfully generated
            2022-12-05 05:10:13,969 - xtesting.ci.run_tests - INFO - Test result:

            +-------------------------------+-----------------+------------------+----------------+
            |           TEST CASE           |     PROJECT     |     DURATION     |     RESULT     |
            +-------------------------------+-----------------+------------------+----------------+
            |   cnf-deployments-validation  |       smo       |      00:01       |      PASS      |
            +-------------------------------+-----------------+------------------+----------------+

   3. For Re-testing, user must delete all the VNF instances and packages created in the above test. An example of steps is below.

      .. code:: bash

<<<<<<< HEAD   (4857aa Merge "Improve Xtesting for using NFV-TST code directly")
         $ grep -nu "vnfInstanceId" ~/tacker/tacker/tests/xtesting/api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt | awk '{print $2}'
         6fc3539c-e602-4afa-8e13-962fb5a7d81f
=======
         $ openstack vnflcm list  -c "ID"

           E.g: Output of command

           +--------------------------------------+
           | ID                                   |
           +--------------------------------------+
           | 6fc3539c-e602-4afa-8e13-962fb5a7d81f |
           +--------------------------------------+
>>>>>>> CHANGE (56673b Improve Xtesting code for interconnecting test cases)

         $ openstack vnflcm terminate 6fc3539c-e602-4afa-8e13-962fb5a7d81f
         $ openstack vnflcm delete 6fc3539c-e602-4afa-8e13-962fb5a7d81f

<<<<<<< HEAD   (4857aa Merge "Improve Xtesting for using NFV-TST code directly")
         $ grep -nu "{vnfPkgId}" ~/tacker/tacker/tests/xtesting/api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt | awk '{print $2}'
         718b9054-2a7a-4489-a893-f2b2b1794825
=======
         $ openstack vnf package list -c "Id"

           E.g: Output of command

           +--------------------------------------+
           | ID                                   |
           +--------------------------------------+
           | 718b9054-2a7a-4489-a893-f2b2b1794825 |
           +--------------------------------------+
>>>>>>> CHANGE (56673b Improve Xtesting code for interconnecting test cases)

         $ openstack vnf package update --operational-state DISABLED 718b9054-2a7a-4489-a893-f2b2b1794825
         $ openstack vnf package delete 718b9054-2a7a-4489-a893-f2b2b1794825

      .. note::

         In current test, the package name and namespace mentioned in deployment file for "Get Specific Pod" test are "vdu2" and "default".
         If any update in the package with respect to name and namespace, then the name and namespace variables in the file
         '~/tacker/tacker/tests/xtesting/api-tests/SOL003/CNFDeployment/environment/variables.txt' need to be updated accordingly.

.. _ETSI NFV-TST 010: https://www.etsi.org/deliver/etsi_gs/NFV-TST/001_099/010/02.08.01_60/gs_NFV-TST010v020801p.pdf
