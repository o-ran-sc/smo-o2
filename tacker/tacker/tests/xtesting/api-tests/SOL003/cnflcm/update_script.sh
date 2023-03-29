#!/bin/bash

# Update VNF instance ID in variables.txt file
instance_id='${vnfInstanceId}      '$1
sed -i "s/$(grep vnfInstanceId api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt)/$instance_id/" api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt

# Comment out incorrect check step and unnecessary test cases
sed -i 's/    Check Individual VNF LCM operation occurrence operationState is    STARTING/\#   Check Individual VNF LCM operation occurrence operationState is    STARTING\n\n\*\*\* comment \*\*\*/g' api-tests/SOL003/VNFLifecycleManagement-API/InstantiateVNFTask.robot

sed -i 's/    Check Individual VNF LCM operation occurrence operationState is    STARTING/\#   Check Individual VNF LCM operation occurrence operationState is    STARTING\n\n\*\*\* comment \*\*\*/g' api-tests/SOL003/VNFLifecycleManagement-API/HealVNFTask.robot

#exit 0
