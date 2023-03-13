*** Settings ***
Resource    environment/variables.txt
Library    REST    ${VNFM_SCHEMA}://${VNFM_HOST}:${VNFM_PORT}     ssl_verify=false
Library     OperatingSystem
Library    JSONLibrary
Library    JSONSchemaLibrary    schemas/
Resource    VnfLcmMntOperationKeywords.robot

*** Test Cases ***
POST Heal a vnfInstance
     [Documentation]    Test ID: 7.3.1.8.1
    ...    Test title: POST Heal a vnfInstance
    ...    Test objective: The objective is to test that POST method heal a VNF instance
    ...    Pre-conditions: the VNF instance resource is not in NOT-INSTANTIATED state
    ...    Reference: Clause 5.4.9.3.1 - ETSI GS NFV-SOL 003 [1] v2.8.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none
    POST Heal VNF
    Check HTTP Response Status Code Is    202
    Check HTTP Response Header Contains    Location
    #Check Individual VNF LCM operation occurrence operationState is    STARTING

*** comment ***
POST Heal a vnfInstance Conflict (Not-Instantiated)
     [Documentation]    Test ID: 7.3.1.8.2
    ...    Test title: POST Heal a vnfInstance Conflict (Not-Instantiated)
    ...    Test objective: The objective is to test that the operation cannot be executed currently, due to a conflict with the state of the VNF instance resource.
    ...    Pre-conditions: the VNF instance resource is in NOT-INSTANTIATED state
    ...    Reference: Clause 5.4.9.3.1 - ETSI GS NFV-SOL 003 [1] v2.8.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none
    POST Heal VNF Not Instantiated
    Check HTTP Response Status Code Is    409
    Check HTTP Response Body Json Schema Is    ProblemDetails

POST Heal a vnfInstance Not Found
    [Documentation]    Test ID: 7.3.1.8.3
    ...    Test title: POST Heal a vnfInstance Not Found
    ...    Test objective: The objective is to test that the operation cannot be executed because the VNF instance resource is not found.
    ...    Pre-conditions: the VNF instance resource is not existing
    ...    Reference: Clause 5.4.9.3.1 - ETSI GS NFV-SOL 003 [1] v2.8.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none
    POST Heal VNF Not Existing
    Check HTTP Response Status Code Is    404
    Check HTTP Response Body Json Schema Is    ProblemDetails

GET Heal VNFInstance - Method not implemented
    [Documentation]    Test ID: 7.3.1.8.4
    ...    Test title: GET Heal a vnfInstance - Method not implemented
    ...    Test objective: The objective is to verify that the method is not implemented
    ...    Pre-conditions: none
    ...    Reference: Clause 5.4.9.3.2 - ETSI GS NFV-SOL 003 [1] v2.8.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none
    GET Heal VNF
    Check HTTP Response Status Code Is    405

PUT Heal VNFInstance - Method not implemented
    [Documentation]    Test ID: 7.3.1.8.5
    ...    Test title: PUT Heal a vnfInstance - Method not implemented
    ...    Test objective: The objective is to verify that the method is not implemented
    ...    Pre-conditions: none
    ...    Reference: Clause 5.4.9.3.3 - ETSI GS NFV-SOL 003 [1] v2.8.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none
    PUT Heal VNF
    Check HTTP Response Status Code Is    405

PATCH Heal VNFInstance - Method not implemented
    [Documentation]    Test ID: 7.3.1.8.6
    ...    Test title: PATCH Heal a vnfInstance - Method not implemented
    ...    Test objective: The objective is to verify that the method is not implemented
    ...    Pre-conditions: none
    ...    Reference: Clause 5.4.9.3.4 - ETSI GS NFV-SOL 003 [1] v2.8.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none
    PATCH Heal VNF
    Check HTTP Response Status Code Is    405

DELETE Heal VNFInstance - Method not implemented
    [Documentation]    Test ID: 7.3.1.8.7
    ...    Test title: DELETE Heal a vnfInstance - Method not implemented
    ...    Test objective: The objective is to verify that the method is not implemented
    ...    Pre-conditions: none
    ...    Reference: Clause 5.4.9.3.5 - ETSI GS NFV-SOL 003 [1] v2.8.1
    ...    Config ID: Config_prod_VNFM
    ...    Applicability: none
    ...    Post-Conditions: none
    DELETE Heal VNF
    Check HTTP Response Status Code Is    405
