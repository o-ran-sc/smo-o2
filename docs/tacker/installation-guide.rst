.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0



Tacker Installation Guide
=========================

.. contents::
   :depth: 3
   :local:

Abstract
--------

This document describes how to install OpenStack Tacker.


Introduction
------------

.. <INTRODUCTION TO THE SCOPE AND INTENTION OF THIS DOCUMENT AS WELL AS TO THE SYSTEM TO BE INSTALLED>

Tacker:

This document describes the supported software and hardware configurations for the reference component as well as providing guidelines on how to install and configure such reference system.

The audience of this document is assumed to have good knowledge in RAN network nd Linux system.


Preface
-------
.. <DESCRIBE NEEDED PREREQUISITES, PLANNING, ETC.>

Tacker:

(T.B.D.)

.. note:any preperation you need before setting up sotfware and hardware


Hardware Requirements
---------------------
.. <PROVIDE A LIST OF MINIMUM HARDWARE REQUIREMENTS NEEDED FOR THE INSTALL>

Tacker:

(T.B.D.)
Following minimum hardware requirements must be met for installation of Tacker:

+--------------------+----------------------------------------------------+
| **HW Aspect**      | **Requirement**                                    |
|                    |                                                    |
+--------------------+----------------------------------------------------+
| **# of servers**   | 		                                          |
+--------------------+----------------------------------------------------+
| **CPU**            | 						          |
|                    |                                                    |
+--------------------+----------------------------------------------------+
| **RAM**            | 							  |
|                    |                                                    |
+--------------------+----------------------------------------------------+
| **Disk**           | 					                  |
|                    |                                                    |
+--------------------+----------------------------------------------------+
| **NICs**           | 							  |
|                    |                                                    |
|                    | 							  |
|                    |                                                    |
|                    |  					 	  |
|                    |                                                    |
+--------------------+----------------------------------------------------+



Software Installation and Deployment
------------------------------------
.. <DESCRIBE THE FULL PROCEDURES FOR THE INSTALLATION OF THE O-RAN COMPONENT INSTALLATION AND DEPLOYMENT>

Tacker:

This section describes the installation of the Tacker installation on the reference hardware.


Installation Using Install Script
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Usage: ./install.sh [HOST_IP]

This script installs the Tacker via Devstack(Standalone mode).
It uses the latest repositories(Devstack and Tacker).
If you want to use other version, you have to change the URL.
e.g. 
git clone https://opendev.org/openstack-dev/devstack -b stable/yoga
wget https://opendev.org/openstack/tacker/raw/branch/master/devstack/local.conf.standalone

Manual Installation
~~~~~~~~~~~~~~~~~~~
See the following documents.

.. toctree::
   :maxdepth: 1

   https://docs.openstack.org/tacker/latest/install/manual_installation.html


References
----------
.. <PROVIDE NEEDED/USEFUL REFERENCES>




