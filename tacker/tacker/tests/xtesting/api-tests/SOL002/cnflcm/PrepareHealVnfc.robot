*** Settings ***
Resource    ../VNFLifecycleManagement-API/environment/variables.txt
Library    REST    ${VNFM_SCHEMA}://${VNFM_HOST}:${VNFM_PORT}     ssl_verify=false
Library    JSONLibrary
Library    OperatingSystem
Library    Process
Library    Collections
Library    String

*** Test Cases ***
GET VNFC Instance Id
    [Documentation]    Test ID: 6.3.4.9.3
    ...    Test title: GET VNFC Instance Id
    ...    Test objective: The objective is to Get the VNF Instance Id
    ...    Pre-conditions: none
    ...    Applicability: none
    ...    Post-Conditions: none
    Get VNFC Instance Id

*** Keywords ***
Get VNFC Instance Id
    log    Trying to perform a GET. This keyword is defined for fetching the Vnfc Instance ID.
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
    ${json_body} =    Format String    ${template}    healingCause=${healingCause}    vnfcInstanceId=${vnfc_id}
    Run Process    echo ${json_body} > jsons/healVnfcRequest.json    shell=yes
