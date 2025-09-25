====================================
Subscription installation automation
====================================

This script is used to create, show and delete subscriptions.

Prerequisites
-------------

#. OpenStack Environment

   A running OpenStack Tacker environment.Tacker CLI and OpenStack
   CLI must be installed and configured on the system.

#. VNF instance is created and instantiated

#. Notification server is reachable.

Configuration changes
---------------------

  The script uses a config.env file to define all necessary input
  parameters. User must update this file according to their system
  requirements before executing the script.

Script execution
----------------

#. Make the Script and configuration file Executable

   .. code-block:: console

      $ chmod +x subscription.sh
      $ chmod +x config.env

#. Create Subscription

   To create a new FM subscription:

   .. code-block:: console

      ./subscription.sh create

#. Show Subscription(s)

   1. Show all subscriptions:

      .. code-block:: console

         ./subscription.sh show

   2. Show a specific subscription:

      We can check the subscription ID's using below command

      .. code-block:: console

         openstack vnffm sub list --os-tacker-api-version 2

      .. code-block:: console

         ./subscription.sh show <subscription_id>

#. Delete Subscription(s)

   1. Delete a specific subscription:

      .. code-block:: console

	 ./subscription.sh delete <subscription_id>

      It also deletes the associated JSON file if it exists in
      format <id>_sample_param_file_fm_sub.json.

   2. Delete all subscriptions:

      .. code-block:: console

         ./subscription.sh delete

      It deletes all subscriptions via the Tacker CLI and removes all
      JSON files ending with _sample_param_file_fm_sub.json.
