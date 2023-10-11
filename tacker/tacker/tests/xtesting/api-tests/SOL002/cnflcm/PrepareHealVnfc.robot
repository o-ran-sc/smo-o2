*** Settings ***
Resource    environment/variables.txt
Library    REST    ${VNFM_SCHEMA}://${VNFM_HOST}:${VNFM_PORT}     ssl_verify=false
Library    JSONLibrary

*** Test Cases ***
Get Vnfc Instance Id
    Set Headers    {"Accept":"${ACCEPT}"}
    Set Headers    {"Content-Type": "${CONTENT_TYPE}"}
    Run Keyword If    ${AUTH_USAGE} == 1    Set Headers    {"${AUTHORIZATION_HEADER}":"${AUTHORIZATION_TOKEN}"}
    Get    ${apiRoot}/${apiName}/${apiMajorVersion}/vnf_instances/${vnfInstanceId}
    ${outputResponse}=    Output    response
    ${body} =    Get From Dictionary     ${outputResponse}    body
    ${instantiatedVnfInfo} =    Get From Dictionary    ${body}    instantiatedVnfInfo
    ${vnfcResourceInfo} =    Get From Dictionary    ${instantiatedVnfInfo}    vnfcResourceInfo
    ${dict} =    Get From List    ${vnfcResourceInfo}    0
    ${vnfc_id} =    Get From Dictionary    ${dict}    id
    ${template} =    Get File    jsons/healVnfcRequest.json
    ${json_body} =    Format String    ${template}    vnfcInstanceId=${vnfc_id}
    Run Process    echo ${json_body} > jsons/healVnfRequest.json    shell=yes
