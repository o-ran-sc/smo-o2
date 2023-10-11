#!/bin/sh

sol002VariableFile=../../SOL002/VNFLifecycleManagement-API/environment/variables.txt
sol003VariableFile=../../SOL003/VNFLifecycleManagement-API/environment/variables.txt

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

sed -i '/${AUTHORIZATION_HEADER}/d' $sol002VariableFile
sed -i '/${AUTHORIZATION_TOKEN}/d' $sol002VariableFile
sed -i '/${VNFM_PORT}/d' $sol002VariableFile
sed -i '/${VNFM_SCHEMA}/d' $sol002VariableFile

echo "" >> $sol002VariableFile
echo "\${AUTHORIZATION_HEADER}    X-Auth-Token" >> $sol002VariableFile
echo "\${AUTHORIZATION_TOKEN}     $token" >> $sol002VariableFile
echo "\${VNFM_PORT}      9890" >> $sol002VariableFile
echo "\${VNFM_SCHEMA}    http" >> $sol002VariableFile

sed -i '/${vnfPkgId}/d' $sol003VariableFile
sed -i '/${vnfdId}/d' $sol003VariableFile
sed -i '/${AUTHORIZATION_HEADER}/d' $sol003VariableFile
sed -i '/${AUTHORIZATION_TOKEN}/d' $sol003VariableFile
sed -i '/${VNFM_PORT}/d' $sol003VariableFile
sed -i '/${VNFM_SCHEMA}/d' $sol003VariableFile

echo "" >> $sol003VariableFile
echo "\${vnfPkgId}     $Packageid" >> $sol003VariableFile
echo "\${vnfdId}     $vnfdid" >> $sol003VariableFile
echo "\${AUTHORIZATION_HEADER}    X-Auth-Token" >> $sol003VariableFile
echo "\${AUTHORIZATION_TOKEN}     $token" >> $sol003VariableFile
echo "\${VNFM_PORT}      9890" >> $sol003VariableFile
echo "\${VNFM_SCHEMA}    http" >> $sol003VariableFile

#comment out test cases in api-tests
# TODO: After the bug in the test case "Check Individual VNF LCM operation occurrence operationState is"
#       is fixed by ESTI NFV TST, we need to remove the step for commenting it out below.
sed -i 's/    Check Individual VNF LCM operation occurrence operationState is    STARTING/\#   Check Individual VNF LCM operation occurrence operationState is    STARTING\n\n\*\*\* comment \*\*\*/g' ../../SOL003/VNFLifecycleManagement-API/InstantiateVNFTask.robot
sed -i 's/    Check Individual VNF LCM operation occurrence operationState is    STARTING/\#   Check Individual VNF LCM operation occurrence operationState is    STARTING\n\n\*\*\* comment \*\*\*/g' ../../SOL003/VNFLifecycleManagement-API/HealVNFTask.robot

#change variable names and values to adapt our test
sed -i 's/vnfdId=${Descriptor_ID}/vnfdId=${vnfdId}/g' ../../SOL003/VNFLifecycleManagement-API/VnfLcmMntOperationKeywords.robot

#comment out test cases in api-tests which are unnecessary for conformance test
robotFile=../../SOL003/VNFLifecycleManagement-API/VNFInstances.robot
lineNo=`cat -n $robotFile | sed -n '/POST Create a new vnfInstance/,$p' | grep -E '^([0-9]|[[:space:]])+$' | head -1`
insertSteps="*** comment ***"
Command="sed -i '$((lineNo))a $insertSteps' $robotFile"
eval "$Command"

robotFile=../../SOL002/VNFLifecycleManagement-API/HealVNFTask.robot
lineNo=`cat -n $robotFile | sed -n '/POST Heal a vnfInstance/,$p' | grep -E '^([0-9]|[[:space:]])+$' | head -1`
insertSteps="*** comment ***"
Command="sed -i '$((lineNo))a $insertSteps' $robotFile"
eval "$Command"

#modify api-tests code so that vnfInstanceId is treated as global variable
# TODO: After the modification is officially done in api-tests by ETSI NFV TST, we need to remove below step.
robotFile=../../SOL003/VNFLifecycleManagement-API/VnfLcmMntOperationKeywords.robot
lineNo=`cat -n $robotFile | sed -n '/POST Create a new vnfInstance/,$p' | grep -E '^([0-9]|[[:space:]])+$' | head -1`
insertSteps="\    \${res_body}=    Get From Dictionary     \${outputResponse}    body\n    \${res_id}=    Get From Dictionary     \${res_body}    id\n    Set Global Variable    \${vnfInstanceId}     \${res_id}"
Command="sed -i '$((lineNo))i $insertSteps' $robotFile"
eval "$Command"

exit 0
