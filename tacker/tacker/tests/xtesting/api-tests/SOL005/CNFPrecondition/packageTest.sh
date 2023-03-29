#!/bin/sh

variableFile=$1    # file path of variables.txt in which environmental variables should be updated
if [ ! $1 ]; then
        echo "Input parameter is missing"
        exit 1
fi

#curl command for token generation
curl -X POST -H 'Content-Type:application/json' --data '{"auth": {"scope":
        {"project": {"domain": {"id": "default"}, "name": "nfv"}}, "identity":
        {"password": {"user": {"domain": {"id": "default"}, "password":
        "devstack", "name": "nfv_user"}}, "methods": ["password"]}}}' \
        -i http://localhost/identity/v3/auth/tokens > outtoken 2>&1

dos2unix outtoken 2>/dev/null
token=$(cat ./outtoken | grep "X-Subject-Token" | cut -d ' ' -f 2)
rm -rf outtoken
if [ ! $token ]; then
        echo "Token extract is failed, please check nfv_user exist or not with admin role"
        exit 1
fi

#curl command for vnf package create
Packageid=$(curl -g -X POST http://localhost:9890/vnfpkgm/v1/vnf_packages \
        -H "Accept: application/json" -H "Content-Type: application/json" \
        -H "X-Auth-Token: $token" -d '{}' | jq -r .id 2>/dev/null)

echo "$Packageid created"
#curl command for vnf package upload
curl -g -i -X PUT http://localhost:9890/vnfpkgm/v1/vnf_packages/$Packageid/package_content \
        -H "Accept: application/zip" -H "Content-Type: application/zip" \
        -H "User-Agent: python-tackerclient" -H "X-Auth-Token: $token" --data-binary "@package_with_helm.zip"

echo "Please wait for 1 minute. Package is onboarding..."
sleep 1m

onboardedState=null
onboardedState=$(curl -g -X GET http://localhost:9890/vnfpkgm/v1/vnf_packages/$Packageid \
        -H "Accept: application/json" -H "User-Agent: python-tackerclient" \
        -H "X-Auth-Token: $token" | jq -r .onboardingState)
echo "Onboarding state is $onboardedState"

if [ "$onboardedState" = "ONBOARDED" ]; then
        echo "$Packageid uploaded successfully"
else
        echo "$Packageid upload Failed, please check tacker logs"
        exit 1
fi

#update environmental variables in variables.txt
vnfdid=$(curl -g -X GET http://localhost:9890/vnfpkgm/v1/vnf_packages/$Packageid \
        -H "Accept: application/json" -H "User-Agent: python-tackerclient" \
        -H "X-Auth-Token: $token" | jq -r .vnfdId)

sed -i '/${vnfPkgId}/d' $variableFile
sed -i '/${vnfdId}/d' $variableFile
sed -i '/${AUTHORIZATION_HEADER}/d' $variableFile
sed -i '/${AUTHORIZATION_TOKEN}/d' $variableFile
sed -i '/${VNFM_PORT}/d' $variableFile
sed -i '/${VNFM_SCHEMA}/d' $variableFile

echo "\${vnfPkgId}     $Packageid" >> $variableFile
echo "\${vnfdId}     $vnfdid" >> $variableFile
echo "\${AUTHORIZATION_HEADER}    X-Auth-Token" >> $variableFile
echo "\${AUTHORIZATION_TOKEN}     $token" >> $variableFile
echo "\${VNFM_PORT}      9890" >> $variableFile
echo "\${VNFM_SCHEMA}    http" >> $variableFile

#change variable names and values to adapt our test
sed -i 's/vnfdId=${Descriptor_ID}/vnfdId=${vnfdId}/g' ../../SOL003/VNFLifecycleManagement-API/VnfLcmMntOperationKeywords.robot
sed -i 's/  "vnfInstanceName": "string",/  "vnfInstanceName": "",/g' ../../../jsons/createVnfRequest.json
sed -i 's/  "vnfInstanceDescription": "string",/  "vnfInstanceDescription": "",/g' ../../../jsons/createVnfRequest.json

exit 0
