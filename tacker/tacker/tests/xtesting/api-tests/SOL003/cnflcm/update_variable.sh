#!/bin/bash

# Update VNF instance ID in variables.txt file
instance_id='${vnfInstanceId}      '$1
sed -i "s/$(grep vnfInstanceId api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt)/$instance_id/" api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt

#exit 0
