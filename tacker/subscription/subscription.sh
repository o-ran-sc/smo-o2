#!/bin/bash

# Load config
source ./config.env

OUTPUT_JSON="sample_param_file_fm_sub.json"

#Source Openstack
source $PATH_TO_DEVSTACK/openrc $USERNAME $USER

# -------------------------------
# Function 1: Create Subscription
# -------------------------------
create_subscription() {
    echo " Creating FM Subscription..."

    # Validate required variables
    if [[ -z "$VNF_INSTANCE_ID" || -z "$VNFD_ID" || -z "$SERVER_IP" ]]; then
        echo " Error: VNF_INSTANCE_ID, VNFD_ID, and SERVER_IP must be set in config.env"
        exit 1
    fi

    # Create JSON
    cat <<EOF > "$OUTPUT_JSON"
{
    "filter": {
        "vnfInstanceSubscriptionFilter": {
            "vnfdIds": [
                "$VNFD_ID"
            ],
            "vnfProductsFromProviders": [
                {
                    "vnfProvider": "Company",
                    "vnfProducts": [
                        {
                            "vnfProductName": "Sample VNF",
                            "versions": [
                                {
                                    "vnfSoftwareVersion": 1.0,
                                    "vnfdVersions": [1.0, 2.0]
                                }
                            ]
                        }
                    ]
                }
            ],
            "vnfInstanceIds": [
                "$VNF_INSTANCE_ID"
            ]
        },
        "notificationTypes": [
            "AlarmNotification",
            "AlarmClearedNotification",
            "AlarmListRebuiltNotification"
        ],
        "faultyResourceTypes": [
            "COMPUTE",
            "STORAGE",
            "NETWORK"
        ],
        "perceivedSeverities": [
            "CRITICAL",
            "MAJOR",
            "MINOR",
            "WARNING",
            "INDETERMINATE",
            "CLEARED"
        ],
        "eventTypes": [
            "EQUIPMENT_ALARM",
            "COMMUNICATIONS_ALARM",
            "PROCESSING_ERROR_ALARM",
            "ENVIRONMENTAL_ALARM",
            "QOS_ALARM"
        ],
        "probableCauses": [
            "The server cannot be connected."
        ]
    },
    "callbackUri": "http://$SERVER_IP:$CALLBACK_PORT/$CALLBACK_ENDPOINT",
    "authentication": {
        "authType": [
            "BASIC"
        ],
        "paramsBasic": {
            "userName": "$USERNAME",
            "password": "$PASSWORD"
        }
    }
}
EOF

    echo " JSON file created: $OUTPUT_JSON"

    # Create subscription
    sub_output=$(openstack vnffm sub create "$OUTPUT_JSON" --os-tacker-api-version 2 -f json)
    echo "$sub_output"

    subscriptionId=$(echo "$sub_output" | grep -oP '"ID":\s*"\K[^"]+')
    echo " Subscription created with ID: $subscriptionId"

    if [[ -n "$subscriptionId" ]] ;then
       mv "$OUTPUT_JSON" "${subscriptionId}_${OUTPUT_JSON}"
       exit 1
    fi
}

# ----------------------------
# Function 2: Show Subscription
# ----------------------------
show_subscription() {
    if [[ -n "$1" ]]; then
        echo " Showing subscription ID: $1"
        openstack vnffm sub show "$1" --os-tacker-api-version 2
    else
        echo " Showing FM Subscription..."
        sub_ids=$(openstack vnffm sub list --os-tacker-api-version 2 -f value -c ID)

        if [[ -z "$sub_ids" ]]; then
            echo "  No subscriptions found."
            return
        fi

        for id in $sub_ids; do
            echo -e "\n Subscription ID: $id"
            openstack vnffm sub show "$id" --os-tacker-api-version 2
        done
    fi
}

# ------------------------------
# Function 3: Delete Subscription
# ------------------------------
delete_subscription() {
    if [[ -n "$1" ]]; then
        echo " Deleting subscription ID: $1"
	subscriptionId=$1
        openstack vnffm sub delete "$1" --os-tacker-api-version 2
        [[ -f "${subscriptionId}_${OUTPUT_JSON}" ]] && rm -f "${subscriptionId}_${OUTPUT_JSON}"
        echo " Deleted $1 and removed JSON file."
    else
        echo " No subscription ID provided. Deleting ALL subscriptions..."
        sub_ids=$(openstack vnffm sub list --os-tacker-api-version 2 -f value -c ID)

        if [[ -z "$sub_ids" ]]; then
            echo " No subscriptions found to delete."
            return
        fi

        for id in $sub_ids; do
            echo " Deleting subscription ID: $id"
            openstack vnffm sub delete "$id" --os-tacker-api-version 2
        done

        [[ -n $(ls *"$OUTPUT_JSON" 2>/dev/null) ]] && rm -f *"$OUTPUT_JSON"
        echo " All subscriptions deleted. JSON file removed."
    fi
}

# ------------------------------
# Command Line Dispatcher
# ------------------------------
case "$1" in
    create)
        create_subscription
        ;;
    show)
        show_subscription "$2"
        ;;
    delete)
        delete_subscription "$2"
        ;;
    *)
        echo "Usage: $0 {create|show|delete} [subscription_id]"
        echo
        echo "Examples:"
        echo "  $0 create                    # Create new FM subscription"
        echo "  $0 show                      # Show all subscriptions"
        echo "  $0 show <id>                 # Show specific subscription"
        echo "  $0 delete                    # Delete all subscriptions"
        echo "  $0 delete <id>               # Delete specific subscription"
        ;;
esac
