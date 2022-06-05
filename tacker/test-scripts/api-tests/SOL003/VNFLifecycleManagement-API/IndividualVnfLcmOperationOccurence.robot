*** Settings ***
Resource    environment/configuration.txt
Resource    environment/variables.txt 
Library    REST    ${VNFM_SCHEMA}://${VNFM_HOST}:${VNFM_PORT}        ssl_verify=false
Library    JSONLibrary
Library    JSONSchemaLibrary    schemas/
Resource    VnfLcmMntOperationKeywords.robot

*** Test Cases ***
Post Individual VNF LCM OP occurrence - Method not implemented
    [Documentation]    Test ID: 7.3.1.12.1
    ...    Test title: Post Individual VNF LCM OP occurrence - Method not implemented
    ...    Test objective: The objective is to test that POST method is not implemented
    ...    Pre-conditions: none
    ...    Reference: Clause 5.4.13.3.1 - ETSI GS NFV-SOL 003 [2] v2.6.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none 
    Post Individual VNF LCM OP occurrence
    Check HTTP Response Status Code Is    405
    
Get Individual VNF LCM OP occurrence
    [Documentation]    Test ID: 7.3.1.12.2
    ...    Test title: Get Individual VNF LCM OP occurrence
    ...    Test objective: The objective is to test that this method retrieve a VNF lifecycle management operation occurrence and perform a JSON schema validation of the returned data structure
    ...    Pre-conditions: none
    ...    Reference: Clause 5.4.13.3.2 - ETSI GS NFV-SOL 003 [2] v2.6.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none 
    Get Individual VNF LCM OP occurrence
    Check HTTP Response Status Code Is    200
    Check HTTP Response Body Json Schema Is    VnfLcmOpOcc 

PUT Individual VNF LCM OP occurrence - Method not implemented
    [Documentation]    Test ID: 7.3.1.12.3
    ...    Test title: PUT Individual VNF LCM OP occurrence - Method not implemented
    ...    Test objective: The objective is to test that PUT method is not implemented
    ...    Pre-conditions: none
    ...    Reference: Clause 5.4.13.3.3 - ETSI GS NFV-SOL 003 [2] v2.6.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none  
    Put Individual VNF LCM OP occurrence
    Check HTTP Response Status Code Is    405

PATCH Individual VNF LCM OP occurrence - Method not implemented
    [Documentation]    Test ID: 7.3.1.12.4
    ...    Test title: PATCH Individual VNF LCM OP occurrence - Method not implemented
    ...    Test objective: The objective is to test that PATCH method is not implemented
    ...    Pre-conditions: none
    ...    Reference: Clause 5.4.13.3.4 - ETSI GS NFV-SOL 003 [2] v2.6.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none      
    Patch Individual VNF LCM OP occurrence
    Check HTTP Response Status Code Is    405
    
DELETE Individual VNF LCM OP occurrence - Method not implemented
    [Documentation]    Test ID: 7.3.1.12.5
    ...    Test title: DELETE Individual VNF LCM OP occurrence - Method not implemented
    ...    Test objective: The objective is to test that DELETE method is not implemented
    ...    Pre-conditions: none
    ...    Reference: Clause 5.4.13.3.5 - ETSI GS NFV-SOL 003 [2] v2.6.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none  
    Delete Individual VNF LCM OP occurrence
    Check HTTP Response Status Code Is    405

Get All Pods
    [Documentation]    Test ID: 7.3.1.12.7
    ...    Test title: Get All Pods
    ...    Test objective: The objective is to get the data of All Pods
    ...    Pre-conditions: none
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none
    Get All Pods
    Check HTTP Response Status Code Is    200

Get Specific Pod
    [Documentation]    Test ID: 7.3.1.12.8
    ...    Test title: Get Specific Pod
    ...    Test objective: The objective is to get the data of a specific Pod
    ...    Pre-conditions: none
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none
    Get Specific Pod
 
