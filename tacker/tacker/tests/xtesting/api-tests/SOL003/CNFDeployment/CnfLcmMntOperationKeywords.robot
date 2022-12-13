*** Settings ***
Resource    environment/configuration.txt
Resource    environment/variables.txt
Library    MockServerLibrary
Library    REST    ${VNFM_SCHEMA}://${VNFM_HOST}:${VNFM_PORT}    ssl_verify=false
Library    OperatingSystem
Library    BuiltIn
Library    JSONLibrary
Library    Collections
Library    JSONSchemaLibrary    schemas/
Library    Process
Library    String

*** Keywords ***
Get All Pods
    Log    Status information for all the pods.
    GET    ${PODS_SCHEMA}://${PODS_HOST}:${PODS_PORT}/api/v1/pods
    ${outputResponse}=    Output    response
    Log    ${outputResponse}
        Set Global Variable    ${response}    ${outputResponse}

Get Specific Pod
    Log    Status information of a specific Pod.
    GET    ${PODS_SCHEMA}://${PODS_HOST}:${PODS_PORT}/api/v1/namespaces/${namespaces}/pods/${name}
    ${outputResponse}=    Output    response
        Set Global Variable    ${response}    ${outputResponse}
