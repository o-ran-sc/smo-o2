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

The following steps are the procedure of API conformance test according to ETSI NFV-TST by using Xtesting.

* Xtesting environment setup

1. Check out code from smo/o2 repository into your local machine.

2. Create a virtual environment for xtesting in 'tacker/tacker/tests/xtesting/' directory.

  .. code:: bash

        $ virtualenv xtesting-py3 -p python3

3. Activate the virtual environment.

  .. code:: bash

        $ . xtesting-py3/bin/activate

4. Install Xtesting package.

  .. code:: bash

        $ pip install xtesting

5. Install 'requirements.txt' in 'tacker/tacker/tests/xtesting/' directory.

  .. code:: bash

        $ pip install -r requirement.txt

6. Check out 'api-tests' repository in ETSI NFV into 'tacker/tacker/tests/xtesting/' directory and give permissions.

  .. code:: bash

        $ git clone https://forge.etsi.org/rep/nfv/api-tests.git
        $ sudo chmod -R 775 api-tests

7. Copy 'testcases.yaml' file from 'tacker/tacker/tests/xtesting' directory to 'tacker/tacker/tests/xtesting/xtesting-py3/lib/python3.8/site-packages/xtesting/ci/' directory.

  .. code:: bash

        $ cp tacker/tacker/tests/xtesting/testcases.yaml tacker/tacker/tests/xtesting/xtesting-py3/lib/python3.8/site-packages/xtesting/ci/

* Preconditioning for test execution

1. Install Kubernetes VIM from steps mentioned in below link.

  * https://docs.openstack.org/tacker/latest/install/kubernetes_vim_installation.html

2. Execute script 'packageTest.sh' for package creation and uploading.

  .. code:: bash

        $ cd tacker/tacker/tests/xtesting/api-tests/SOL005/CNFPrecondition
        $ ./packageTest.sh  ../../SOL003/cnflcm/environment/variables.txt

3. Update vimId variable value in 'tacker/tacker/tests/xtesting/api-tests/SOL003/cnflcm/jsons/inst.json'. To get vimId, execute below command.

  .. code:: bash

        $ openstack vim list -c "ID"

E.g: Output of command

  .. code:: bash

        +--------------------------------------+
        | ID                                   |
        +--------------------------------------+
        | 08260b52-c3f6-47a9-bb1f-cec1f0d3956a |
        +--------------------------------------+

* Testing steps

1. Verify Vnflcm Create and Instantiate.

  .. code:: bash

        $ cd tacker/tacker/tests/xtesting/
        $ . xtesting-py3/bin/activate
        $ sudo xtesting-py3/bin/run_tests -t first

2: Verify getting all pods and getting specific pod.

  .. code:: bash

        $ cd tacker/tacker/tests/xtesting/
        $ . xtesting-py3/bin/activate
        $ sudo xtesting-py3/bin/run_tests -t second

*Note:*
In current test, the package name and namespace mentioned in deployment file for "Get Specific Pod" test are "vdu2" and "default".
If any update in the package with respect to name and namespace, then the name and namespace variables in the file
'tacker/tacker/tests/xtesting/api-tests/SOL003/CNFDeployment/environment/variables.txt' need to be updated accordingly.
