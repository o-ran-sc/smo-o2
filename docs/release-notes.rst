.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0


Release-Notes
=============


This document provides the release notes for releases related to SMO O2.

.. contents::
   :depth: 3
   :local:


Version history
---------------

+--------------------+--------------------+--------------------+--------------------+
| **Date**           | **Ver.**           | **Author**         | **Comment**        |
|                    |                    |                    |                    |
+--------------------+--------------------+--------------------+--------------------+
| 2022-07-21         | 1.0                | Toshiaki Takahashi | First Release      |
|                    |                    |                    |                    |
+--------------------+--------------------+--------------------+--------------------+
| 2022-12-23         | 2.0                | Toshiaki Takahashi | G-Release          |
|                    |                    |                    |                    |
+--------------------+--------------------+--------------------+--------------------+
| 2022-06-26         | 3.0                | Toshiaki Takahashi | H-Release          |
|                    |                    |                    |                    |
+--------------------+--------------------+--------------------+--------------------+
| 2023-12-21         | 4.0                | Toshiaki Takahashi | I-Release          |
|                    |                    |                    |                    |
+--------------------+--------------------+--------------------+--------------------+


Summary
-------

<<<<<<< PATCH SET (918268 Update Release note for I-release)
<<<<<<< HEAD   (4857aa Merge "Improve Xtesting for using NFV-TST code directly")
=======
I-release:

The I release was targeted towards implementation of the O2 interface.
Tacker installation method using Helm has been provided
in relation to the NFs LCM total sequence consideration,
a feature for O2 DMS FM/PM has been provided,
and Automated API Conformance testing has been improved.

H-release:

The H release was targeted towards implementation of the O2 interface.
The API version has been upgraded, the testing scope has been expanded, the testing code has been enhanced, and the documentation has been improved.

>>>>>>> CHANGE (223905 Update Release note for I-release)
=======
H-release:

The H release was targeted towards implementation of the O2 interface.
The API version has been upgraded, the testing scope has been expanded, the testing code has been enhanced, and the documentation has been improved.

>>>>>>> BASE      (f368c5 Update Release note for H-release)
G-release:

The G release was targeted towards implementation of the O2 interface for
VM-base VNF and testing with NFV API compliant test code.

F-release(First Release):

The F release was targeted towards implementation of the O2 interface itself
and getting an initial implementation in place.
The OpenStack Tacker is used to implement the O2 interface.


Release Data
------------

+--------------------------------------+------------------------------------------+
| **Project**                          | SMO O2        		                  |
|                                      |                                          |
+--------------------------------------+------------------------------------------+
| **Repo/commit-ID**                   | Repo: smo/o2                             |
+--------------------------------------+------------------------------------------+
| **Release designation**              |                                          |
|                                      |                                          |
+--------------------------------------+------------------------------------------+
| **Release date**                     | 2023-12-21                               |
|                                      |                                          |
+--------------------------------------+------------------------------------------+
| **Purpose of the delivery**          | 	 		     	          |
|                                      |                                          |
+--------------------------------------+------------------------------------------+




Feature Additions
^^^^^^^^^^^^^^^^^

**JIRA BACK-LOG:**

+--------------------------------------+------------------------------------------------+
| **JIRA REFERENCE**                   | **SLOGAN**                                     |
|                                      |                                                |
+--------------------------------------+------------------------------------------------+
<<<<<<< PATCH SET (918268 Update Release note for I-release)
<<<<<<< HEAD   (4857aa Merge "Improve Xtesting for using NFV-TST code directly")
| SMO-77                               | TST010 API Conformance                         |
=======
| SMO-119                              | Automated API Conformance testing              |
>>>>>>> CHANGE (223905 Update Release note for I-release)
=======
| SMO-129                              | Improve API-based documentation                |
>>>>>>> BASE      (f368c5 Update Release note for H-release)
+--------------------------------------+------------------------------------------------+
<<<<<<< PATCH SET (918268 Update Release note for I-release)
<<<<<<< HEAD   (4857aa Merge "Improve Xtesting for using NFV-TST code directly")
| SMO-78                               | Release Artifact                               |
=======
| SMO-137                              | O2dms FM/PM                                    |
>>>>>>> CHANGE (223905 Update Release note for I-release)
=======
| SMO-130                              | Migrate Tacker API version from v1 to v2       |
>>>>>>> BASE      (f368c5 Update Release note for H-release)
+--------------------------------------+------------------------------------------------+
<<<<<<< PATCH SET (918268 Update Release note for I-release)
<<<<<<< HEAD   (4857aa Merge "Improve Xtesting for using NFV-TST code directly")
| SMO-79                               | Alignment with ETSI NFV SOL014 for VM-base VNF |
=======
| SMO-139                              | Update whole sequence flow diagram             |
>>>>>>> CHANGE (223905 Update Release note for I-release)
=======
| SMO-131                              | Improve API Conformance testing with           |
|                                      | NFV-TST010 code                                |
>>>>>>> BASE      (f368c5 Update Release note for H-release)
+--------------------------------------+------------------------------------------------+
+--------------------------------------+------------------------------------------------+
+--------------------------------------+------------------------------------------------+
|                                      |                                                |
+--------------------------------------+------------------------------------------------+

Bug Corrections
^^^^^^^^^^^^^^^

**JIRA TICKETS:**

+--------------------------------------+--------------------------------------+
| **JIRA REFERENCE**                   | **SLOGAN**                           |
|                                      |                                      |
+--------------------------------------+--------------------------------------+
| SMO-132                              | O2 Tacker install script raise       |
|                                      | syntax error                         |
+--------------------------------------+--------------------------------------+
| 	                               |  				      |
|                                      |  				      |
|                                      |                                      |
+--------------------------------------+--------------------------------------+

Deliverables
^^^^^^^^^^^^

Software Deliverables
+++++++++++++++++++++




Documentation Deliverables
++++++++++++++++++++++++++





Known Limitations, Issues and Workarounds
-----------------------------------------

System Limitations
^^^^^^^^^^^^^^^^^^



Known Issues
^^^^^^^^^^^^


**JIRA TICKETS:**

+--------------------------------------+--------------------------------------+
| **JIRA REFERENCE**                   | **SLOGAN**                           |
|                                      |                                      |
+--------------------------------------+--------------------------------------+
| 		                       | 				      |
|                                      | 				      |
|                                      |                                      |
+--------------------------------------+--------------------------------------+
| 	                               |  				      |
|                                      |  				      |
|                                      |                                      |
+--------------------------------------+--------------------------------------+

Workarounds
^^^^^^^^^^^





References
----------




