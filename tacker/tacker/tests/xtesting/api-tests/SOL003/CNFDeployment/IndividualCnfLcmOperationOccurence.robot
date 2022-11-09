*** Settings ***
Resource    environment/configuration.txt
Resource    environment/variables.txt 
Library    REST    ${VNFM_SCHEMA}://${VNFM_HOST}:${VNFM_PORT}        ssl_verify=false
Library    JSONLibrary
Library    JSONSchemaLibrary    schemas/
Resource    CnfLcmMntOperationKeywords.robot

*** Test Cases ***
Get All Pods
    [Documentation]    Test ID: 7.3.1.12.7
    ...    Test title: Get All Pods
    ...    Test objective: The objective is to get the data of All Pods
    ...    Pre-conditions: none
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none
    Get All Pods

Get Specific Pod
    [Documentation]    Test ID: 7.3.1.12.8
    ...    Test title: Get Specific Pod
    ...    Test objective: The objective is to get the data of a specific Pod
    ...    Pre-conditions: none
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none
    Get Specific Pod 
