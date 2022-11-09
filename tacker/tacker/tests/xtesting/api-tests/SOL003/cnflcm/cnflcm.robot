*** Settings ***
Resource    environment/configuration.txt
Resource    environment/variables.txt
Library    REST    ${VNFM_SCHEMA}://${VNFM_HOST}:${VNFM_PORT}        ssl_verify=false
Library    JSONLibrary
Library    JSONSchemaLibrary    schemas/
Resource    cnflcm_keywords.robot

*** Test Cases ***
Create and Instantiate a new VNFInstance
    [Documentation]    Test ID: 7.3.1.12.2
    Create a new cnfInstance
    POST instantiate individual cnfInstance
