#!/bin/bash

# Update values in variables.txt file
instance_id='${vnfInstanceId}    '$1
sed -i "s/$(grep vnfInstanceId api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt)/$instance_id/" api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt

port='${VNFM_PORT}      ''9890'
sed -i "s/$(grep VNFM_PORT api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt)/$port/" api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt

schema='${VNFM_SCHEMA}    ''http'
sed -i "s/$(grep VNFM_SCHEMA api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt)/$schema/" api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt

header='${AUTHORIZATION_HEADER}    ''X-Auth-Token'
sed -i "s/$(grep AUTHORIZATION_HEADER api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt)/$header/" api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt

token='${AUTHORIZATION_TOKEN}     '$2
sed -i "s/$(grep AUTHORIZATION_TOKEN api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt)/$token/" api-tests/SOL003/VNFLifecycleManagement-API/environment/variables.txt

#exit 0

