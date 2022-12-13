#!/bin/sh

variableFile=$1
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
        echo "Token extract is failed , Please check nfv_user exist or not with admin role"
        exit 1
fi

#curl command for vnf package create
Packageid=$(curl -g -X POST http://localhost:9890/vnfpkgm/v1/vnf_packages -H "Accept: application/json" -H "Content-Type: application/json" -H "X-Auth-Token: $token" -d '{}' | jq -r .id 2>/dev/null)

echo "$Packageid created"
#curl command for vnf package upload
curl -g -i -X PUT http://localhost:9890/vnfpkgm/v1/vnf_packages/$Packageid/package_content \
        -H "Accept: application/zip" -H "Content-Type: application/zip" \
        -H "User-Agent: python-tackerclient" -H "X-Auth-Token: $token" --data-binary "@package_with_helm.zip"

echo "Package is Onboarding..."
sleep 1m

onboardedState=null
onboardedState=$(curl -g -X GET http://localhost:9890/vnfpkgm/v1/vnf_packages/$Packageid -H "Accept: application/json" -H "User-Agent: python-taerclient" -H "X-Auth-Token: $token" | jq -r .onboardingState)
echo "$onboardedState onboardinng state"

if [ "$onboardedState" = "ONBOARDED" ]; then
        echo "$Packageid uploaded successully"
        vnfdid=$(curl -g -X GET http://localhost:9890/vnfpkgm/v1/vnf_packages/$Packageid -H "Accept: application/json" -H "User-Agent: python-taerclient" -H "X-Auth-Token: $token" | jq -r .vnfdId)

        sed -i '/${vnfPkgId}/d' $variableFile
        sed -i '/${vnfdId}/d' $variableFile

        echo "\${vnfPkgId}     $Packageid" >> $variableFile # $variableFile environment file path where package ID will be added
        echo "\${vnfdId}     $vnfdid" >> $variableFile
        exit 0

else
        echo "$Packageid upload Failed, please check tacker logs"
        exit 1
fi
