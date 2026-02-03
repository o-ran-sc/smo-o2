.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

Use-Cases
=========

.. contents::
   :depth: 3
   :local:

Intelligent auto Homing
-----------------------

The following describes the processing flow of the Intelligent Auto Homing.

.. image:: /_static/intelligent_auto_homing.pu
   :align: center

1: NF instantiate request is generated.
2: Policies are queried from the OSS/BSS.
3-4: NFO checks for the constraints and requirements related to NF.
5-10: [Intelligent homing decision]NFO request homing suggestion from
Non-RT RIC, RIC return with recommended clusterID where the NF can be deployed.
11-14: NFO queries the O-cloud inventory status on the base of which homing
decision in taken by NFO.
15-19: DMS sends NF deployment request to the O-Cloud DMS. Instantiation of CNF/Helm
install is done with health check probes.

Intelligent auto Healing
------------------------

The following describes the processing flow of the Intelligent Auto Healing.

.. image:: /_static/intelligent_auto_healing.pu
   :align: center

1-2: DMS received fault performance data and generate alarmevent. 
3-4: DMS checks for subscriber for the event and send notification in a
subscriber is available.
5-6: DMS detects healing condition for auto healing and then send LCM starting
notification to the NFO.
7-12: DMS confirms healing action and sends grant request to NFO, for intelligent
homing/placement non-RT RIC does analysis based upon operational policies.
13-16: DMS sends LCM operation processing notification to NFO and starts NF
deployment. Once auto healing is complete, LCM operation Completed
notification is sent to NFO.
17: Once the healing is done DMS sends alarm clear notification to the subscriber.
