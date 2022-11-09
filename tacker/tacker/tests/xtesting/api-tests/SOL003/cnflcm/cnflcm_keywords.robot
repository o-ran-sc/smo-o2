*** Settings ***
Resource    environment/configuration.txt
Resource    environment/variables.txt
Resource    environment/scaleVariables.txt
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
Create a new cnfInstance
    Run Process  curl -X POST -H 'Content-Type:${ACCEPT_JSON}' --data ${TOKEN_DATA} -i ${TOKEN_API}  shell=True    alias=result
    ${result}    Get Process Result    result    stdout=true
    ${lines} =  Get Lines Matching Pattern      ${result}     X-Subject-Token*
    ${X-Subject-Token}=   Run   echo ${lines} | cut -d ' ' -f 2
    Log to Console    ${vnfdId}
    Run Process  curl -g -i -X POST ${vnf_instance_api} -H "Accept: ${ACCEPT}" -H "Content-Type: ${ACCEPT_JSON}" -H "X-Auth-Token: ${X-Subject-Token}" -d '{"vnfdId": "${vnfdId}"}'  shell=True    alias=result
    ${result}    Get Process Result    result    stdout=true
    ${line} =   Get Line        ${result}    7
    ${lines1}    Create List     ${line}
    ${x} =      Get From List   ${lines1}    0
    ${json}=    evaluate    json.loads('''${x}''')    json
    ${Instance_ID} =     Get From Dictionary     ${json}    id
    Log to Console    ${Instance_ID}
    Set Global Variable      ${Instance_ID}


POST instantiate individual cnfInstance	
    Log    Trying to Instantiate a vnf Instance
    Run Process  curl -X POST -H 'Content-Type:${ACCEPT_JSON}' --data ${TOKEN_DATA} -i ${TOKEN_API}  shell=True    alias=result
    ${result}    Get Process Result    result    stdout=true
    ${lines} =  Get Lines Matching Pattern      ${result}     X-Subject-Token*
    ${X-Subject-Token}=   Run   echo ${lines} | cut -d ' ' -f 2
    Set Headers  {"Accept":"${ACCEPT}"}  
    Set Headers  {"Content-pe": "${ACCEPT_JSON}"}
    Set Headers    {"${AUTHORIZATION_HEADER}":"${X-Subject-Token}"}
    ${body}=    Get File    api-tests/SOL003/cnflcm/jsons/inst.json
    Post    ${apiRoot}/${apiName}/${apiVersion}/vnf_instances/${Instance_ID}/instantiate    ${body}	
    ${outputResponse}=    Output    response
	Set Global Variable    ${response}    ${outputResponse} 	
    Log to Console    ${outputResponse}
