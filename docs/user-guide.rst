.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. (c) <optionally add copywriters name>


User Guide
==========

This is the user guide of OSC SMO O2.

.. contents::
   :depth: 3
   :local:

..  a user guide should be how to use the component or system; it should not be a requirements document
..  delete this content after editing it


Description
-----------
.. Describe the target users of the project, for example, modeler/data scientist, ORAN-OSC platform admin, marketplace user, design studio end user, etc
.. Describe how the target users can get use of a O-RAN SC component.
.. If the guide contains sections on third-party tools, is it clearly stated why the O-RAN-OSC platform is using those tools? Are there instructions on how to install and configure each tool/toolset?

Feature Introduction
--------------------
.. Provide enough information that a user will be able to operate the feature on a deployed scenario. content can be added from administration, management, using, Troubleshooting sections perspectives.

Tacker
~~~~~~
An example of the procedure how to deploy VNF or CNF by using Tacker is shown on the following pages.
If you want to see the other procedure, please refer to `Tacker User Guide`_

V1 API
""""""

.. toctree::
   :maxdepth: 1

   tacker-user-guide-vnf.rst
   tacker-user-guide-cnf.rst

V2 API
""""""

.. toctree::
   :maxdepth: 1

   tacker-user-guide-vnf-v2.rst
   tacker-user-guide-cnf-v2.rst
   how-to-use-fm-guide.rst

O2dms FM/PM
"""""""""""
Please refer to the following document for Monitor fault of VNF(App)
using O2dms NFV Profile/SOL002

.. toctree::
   :maxdepth: 1

   https://docs.openstack.org/tacker/latest/user/etsi_cnf_auto_healing_fm.html

.. _Tacker User Guide: https://docs.openstack.org/tacker/latest/user/index.html
