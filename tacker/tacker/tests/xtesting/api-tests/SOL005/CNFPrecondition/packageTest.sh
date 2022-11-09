#!/bin/sh

variableFile=$1
#curl command for token generation
curl -X POST -H 'Content-Type:application/json' --data '{"auth": {"scope":
	{"project": {"domain": {"id": "default"}, "name": "nfv"}}, "identity":
	{"password": {"user": {"domain": {"id": "default"}, "password":
	"devstack", "name": "nfv_user"}}, "methods": ["password"]}}}' \
	-i http://localhost/identity/v3/auth/tokens > outtoken 2>&1

dos2unix outtoken 2>/dev/null
token=$(cat ./outtoken | grep "X-Subject-Token" | cut -d ' ' -f 2)

#curl command for vnf package create
curl -g -i -X POST http://localhost:9890/vnfpkgm/v1/vnf_packages \
	-H "Accept: application/json" -H "Content-Type: application/json" \
	-H "X-Auth-Token: $token" -d '{}' > outtoken 2>&1
dos2unix outtoken 2>/dev/null

Packageid=$(cat ./outtoken | grep id | cut -d ' ' -f 2 |sed 's/.$//'|tail -c +2 | head -c -2)

echo "$Packageid created"
#curl command for vnf package upload
curl -g -i -X PUT http://localhost:9890/vnfpkgm/v1/vnf_packages/$Packageid/package_content \
	-H "Accept: application/zip" -H "Content-Type: application/zip" \
	-H "User-Agent: python-tackerclient" -H "X-Auth-Token: $token" --data-binary "@vnfpackage.zip"

echo "$Packageid uploaded"

sleep 1
curl -g -i -X GET http://localhost:9890/vnfpkgm/v1/vnf_packages/$Packageid \
	-H "Accept: application/json" -H "User-Agent: python-tackerclient" \
	-H "X-Auth-Token: $token" > outtoken 2>&1
dos2unix outtoken 2>/dev/null

vnfdid=$(cat ./outtoken | grep vnfdId | cut -d ' ' -f 15 |sed 's/.$//'|tail -c +2 | head -c -2)

rm -rf outtoken
sed -i '/${vnfPkgId}/d' $variableFile
sed -i '/${vnfdId}/d' $variableFile

echo "\${vnfPkgId}     $Packageid" >> $variableFile # $variableFile environment file path where package ID will be added
echo "\${vnfdId}     $vnfdid" >> $variableFile
