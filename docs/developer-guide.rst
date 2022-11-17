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

   1. Check out code from smo/o2 repository into /tmp/ directory in your local machine.

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

   7. Copy the directories under '/tmp/o2/tacker/tacker/tests/xtesting/' to the location under the current directory.

      .. code:: bash

         $ cp -r /tmp/o2/tacker/tacker/tests/xtesting/api-tests/SOL003/CNFDeployment ./api-tests/SOL003
         $ cp -r /tmp/o2/tacker/tacker/tests/xtesting/api-tests/SOL003/cnflcm ./api-tests/SOL003
         $ cp -r /tmp/o2/tacker/tacker/tests/xtesting/api-tests/SOL005/CNFPrecondition ./api-tests/SOL005

   8. Copy 'testcases.yaml' file from '/tmp/o2/tacker/tacker/tests/xtesting/' directory to the location under the current directory.

      .. code:: bash

         $ cp /tmp/o2/tacker/tacker/tests/xtesting/testcases.yaml ./xtesting-py3/lib/python3.8/site-packages/xtesting/ci/

   9. Give permissions to 'api-tests' directory.

      .. code:: bash

         $ sudo chmod -R 775 api-tests

   10. Update 'VNFM_SCHEMA' and 'VNFM_PORT' variables in below files with appropriate Request Type (http or https) and Port.

      .. code:: bash

         $ vi api-tests/SOL003/CNFDeployment/environment/configuration.txt
         $ vi api-tests/SOL003/cnflcm/environment/configuration.txt

      .. note::

         If user is working in other directory than '/opt/stack/', then the path specified in 'suites' parameter in 'testcases.yaml' needs to be updated to the actual one.

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
         $ ./packageTest.sh  ../../SOL003/cnflcm/environment/variables.txt
 
   5. Get 'vimId' and change it in the file 'inst.json' as below.

      .. code:: bash

         $ openstack vim list -c "ID"
 
      E.g: Output of command

         .. code:: bash

            +--------------------------------------+
            | ID                                   |
            +--------------------------------------+
            | 08260b52-c3f6-47a9-bb1f-cec1f0d3956a |
            +--------------------------------------+

      .. code:: bash

         $ vi ~/tacker/tacker/tests/xtesting/api-tests/SOL003/cnflcm/jsons/inst.json

      E.g: Content of file

         .. code:: bash

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
                    "name": "vdu1-externalhelm",
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

* Testing steps

   1. Verify Vnflcm Create and Instantiate.

      .. code:: bash

         $ cd ~/tacker/tacker/tests/xtesting/
         $ . xtesting-py3/bin/activate
         $ sudo xtesting-py3/bin/run_tests -t first

   2. Verify getting all pods and getting specific pod.

      .. code:: bash

         $ cd ~/tacker/tacker/tests/xtesting/
         $ . xtesting-py3/bin/activate
         $ sudo xtesting-py3/bin/run_tests -t second

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
            Output:  /var/lib/xtesting/results/second/output.xml

            2022-12-05 05:10:13,913 - xtesting.core.robotframework - INFO - Results were successfully parsed
            2022-12-05 05:10:13,968 - xtesting.core.robotframework - INFO - Results were successfully generated
            2022-12-05 05:10:13,969 - xtesting.ci.run_tests - INFO - Test result:

            +-------------------+------------------+------------------+----------------+
            |     TEST CASE     |     PROJECT      |     DURATION     |     RESULT     |
            +-------------------+------------------+------------------+----------------+
            |       second      |     xtesting     |      00:01       |      PASS      |
            +-------------------+------------------+------------------+----------------+

   3. For Re-testing, user must delete all the VNF instances and packages created in the above test. An example of steps is below.

      .. code:: bash

         $ grep -nur "vnfInstanceId" ~/tacker/tacker/tests/xtesting/api-tests/SOL003/cnflcm | awk '{print $2}'
         6fc3539c-e602-4afa-8e13-962fb5a7d81f

         $ openstack vnflcm terminate 6fc3539c-e602-4afa-8e13-962fb5a7d81f
         $ openstack vnflcm delete 6fc3539c-e602-4afa-8e13-962fb5a7d81f

         $ grep -nur "{vnfPkgId}" /opt/stack/tacker/tacker/tests/xtesting/api-tests/SOL003/cnflcm | awk '{print $2}'
         718b9054-2a7a-4489-a893-f2b2b1794825

         $ openstack vnf package update --operational-state DISABLED 718b9054-2a7a-4489-a893-f2b2b1794825
         $ openstack vnf package delete 718b9054-2a7a-4489-a893-f2b2b1794825

      .. note::

         In current test, the package name and namespace mentioned in deployment file for "Get Specific Pod" test are "vdu2" and "default".
         If any update in the package with respect to name and namespace, then the name and namespace variables in the file
         '~/tacker/tacker/tests/xtesting/api-tests/SOL003/CNFDeployment/environment/variables.txt' need to be updated accordingly.

.. _ETSI NFV-TST 010: https://www.etsi.org/deliver/etsi_gs/NFV-TST/001_099/010/03.03.01_60/gs_NFV-TST010v030301p.pdf
