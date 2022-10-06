.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0



Installation Guide
==================

.. contents::
   :depth: 3
   :local:

Abstract
--------

This document describes how to install the software for SMO O2, it's dependencies and required system resources.


Version history

+--------------------+--------------------+--------------------+--------------------+
| **Date**           | **Ver.**           | **Author**         | **Comment**        |
|                    |                    |                    |                    |
+--------------------+--------------------+--------------------+--------------------+
| 20XX-XX-XX         | 0.1.0              | 		       | First draft        |
|                    |                    |                    |                    |
+--------------------+--------------------+--------------------+--------------------+
|                    | 0.1.1              |                    |                    |
|                    |                    |                    |                    |
+--------------------+--------------------+--------------------+--------------------+
|                    | 1.0                |                    |                    |
|                    |                    |                    |                    |
|                    |                    |                    |                    |
+--------------------+--------------------+--------------------+--------------------+

Introduction
------------

.. <INTRODUCTION TO THE SCOPE AND INTENTION OF THIS DOCUMENT AS WELL AS TO THE SYSTEM TO BE INSTALLED>

This document describes the supported software and hardware configurations for the reference component as well as providing guidelines on how to install and configure such reference system.

The audience of this document is assumed to have good knowledge in RAN network nd Linux system.


Preface
-------
.. <DESCRIBE NEEDED PREREQUISITES, PLANNING, ETC.>

Tacker Preface
~~~~~~~~~~~~~~
Before starting the installation of Tacker, make sure git is installed on the system.

Hardware Requirements
---------------------
.. <PROVIDE A LIST OF MINIMUM HARDWARE REQUIREMENTS NEEDED FOR THE INSTALL>

Tacker Hardware Requirements
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Following minimum hardware requirements must be met for installation of Tacker:

+--------------------+----------------------------------------------------+
| **HW Aspect**      | **Requirement**                                    |
|                    |                                                    |
+--------------------+----------------------------------------------------+
| **# of servers**   |  1                                                 |
+--------------------+----------------------------------------------------+
| **CPU**            |  4                                                 |
|                    |                                                    |
+--------------------+----------------------------------------------------+
| **RAM**            |  16G                                               |
|                    |                                                    |
+--------------------+----------------------------------------------------+
| **Disk**           |  80G                                               |
|                    |                                                    |
+--------------------+----------------------------------------------------+
| **NICs**           |  1                                                 |
|                    |                                                    |
+--------------------+----------------------------------------------------+


Software Installation and Deployment
------------------------------------
.. <DESCRIBE THE FULL PROCEDURES FOR THE INSTALLATION OF THE O-RAN COMPONENT INSTALLATION AND DEPLOYMENT>

Tacker Installation
~~~~~~~~~~~~~~~~~~~
This section describes the installation of the Tacker installation on the reference hardware.

* Installation Using Install Script

  The install script can be retrieved from smo-o2 repository::

      $ git clone https://gerrit.o-ran-sc.org/r/smo/o2

  Usage::

      $ cd o2/tacker/scripts
      $ ./install.sh <HOST_IP>

      <HOST_IP>: You should set host IP for API endpoint of Tacker.

  This script installs the Tacker via Devstack (Standalone mode).
  It uses the latest repositories (Devstack and Tacker).

  If you want to use other version, you have to change the URL.

  e.g.::

      git clone https://opendev.org/openstack-dev/devstack -b stable/yoga
      wget https://opendev.org/openstack/tacker/raw/branch/stable/yoga/devstack/local.conf.standalone

* Manual Installation

  See the following documents.

  .. toctree::
     :maxdepth: 1

     https://docs.openstack.org/tacker/latest/install/manual_installation.html


References
----------
.. <PROVIDE NEEDED/USEFUL REFERENCES>




