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

* Installation using openstack-helm

  Below is the method for installing INF o-cloud and Tacker using openstack-helm.
  Please refer to Step 3, 4, and 5 for Tacker installation.

  .. toctree::
     :maxdepth: 1

     https://wiki.o-ran-sc.org/display/IN/ETSI-DMS+on+INF+O-Cloud%2C+Tacker+installation+guide

  Reference: About openstack-helm

  .. toctree::
     :maxdepth: 1

     https://wiki.openstack.org/wiki/Openstack-helm

* Manual Installation

  See the following documents.

  .. toctree::
     :maxdepth: 1

     https://docs.openstack.org/tacker/latest/install/manual_installation.html


Software Configuration
----------------------

* Enabling Fault Management

  This section describes how to enable Fault Management in Tacker.

  #. Fault management is disabled by default in Tacker. To enable it, update
     the fault_management configuration parameters in the tacker.conf file,
     setting this value to “true”.

     .. code-block:: console

         $ vi /etc/tacker/tacker.conf
         ...
         [prometheus_plugin]
         # Enable prometheus plugin fault management (boolean value)
         fault_management = true
         ...

  #. After modifying the configuration file, restart the Tacker services for
     the changes to take effect.

     .. code-block:: console

         $ sudo systemctl restart devstack@tacker-conductor.service
         $ sudo systemctl restart devstack@tacker.service

  #. Confirm that the Tacker service is running properly.

     .. code-block:: console

         $ sudo systemctl status devstack@tacker-conductor.service
         ● devstack@tacker-conductor.service - OpenStack tacker conductor service
              Loaded: loaded (/etc/systemd/system/devstack@tacker-conductor.service; enabled; vendor preset: enabled)
              Active: active (running) since Fri 2024-06-14 06:56:50 UTC; 16s ago
            Main PID: 1447858 (tacker-conducto)
               Tasks: 1 (limit: 77041)
              Memory: 153.7M
                 CPU: 2.222s
              CGroup: /system.slice/system-devstack.slice/devstack@tacker-conductor.service
                      └─1447858 /opt/stack/data/venv/bin/python3.10 /opt/stack/data/venv/bin/tacker-conductor --config-file /etc/tacker/tacker.co>

            Jun 14 06:56:50 instance-vnfm-ubuntu22-5th-20231207 systemd[1]: Started OpenStack tacker conductor service.
            Jun 14 06:56:51 instance-vnfm-ubuntu22-5th-20231207 tacker-conductor[1447858]: /opt/stack/data/venv/lib/python3.10/site-packages/oslo_db>
            Jun 14 06:56:51 instance-vnfm-ubuntu22-5th-20231207 tacker-conductor[1447858]:   warnings.warn(

         $ sudo systemctl status devstack@tacker.service
         ● devstack@tacker.service - OpenStack tacker service
              Loaded: loaded (/etc/systemd/system/devstack@tacker.service; enabled; vendor preset: enabled)
              Active: active (running) since Fri 2024-06-14 06:58:36 UTC; 4s ago
            Main PID: 1448235 (tacker-server)
               Tasks: 1 (limit: 77041)
              Memory: 144.5M
                 CPU: 2.090s
              CGroup: /system.slice/system-devstack.slice/devstack@tacker.service
                      └─1448235 /opt/stack/data/venv/bin/python3.10 /opt/stack/data/venv/bin/tacker-server --config-file /etc/tacker/tacker.conf


References
----------
.. <PROVIDE NEEDED/USEFUL REFERENCES>
