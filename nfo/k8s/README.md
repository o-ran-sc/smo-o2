# Summary
The O-RAN Software Community (OSC) and Open Air Interface (OAI) and Nephio open source communities are working together to provide a reference implementation of the Open RAN (O-RAN) Alliance’s cloud-centric specifications using the Nephio enablers and capabilities in order to deploy and manage O-RAN NFs and xApps. The focus of the O-RAN Integration within Nephio focuses on the Federated O-Cloud Orchestration and Management (FOCOM), Network Function Orchestration (NFO), Infrastructure Management (IMS) and Deployment Management (DMS) O-RAN services.

# NFO installation steps

# Build the service

    NFO services needs to be installed on any VM
    
    For testing, you can run the service locally :

    `make build`
    
# Build an image through docker
    
    docker build -t image-name(nfo-service) .
    
    deploy using docker
    
    docker run -d -p 0000:0000 image-name(nfo-service)

#### prerequisites: python 3.12,helm v3.8.0

## Run helm_processor REST APIs

    BASE_URL : http://127.0.0.1:8080(any*)

##### DU:

    GET: BASE_URL/nfo/api/v1/helm/du/ 
    POST: BASE_URL/nfo/api/v1/helm/du/  
    DELETE: BASE_URL/nfo/api/v1/helm/du/ 

##### CUCP:

    GET: BASE_URL/nfo/api/v1/helm/cucp/
    POST: BASE_URL/nfo/api/v1/helm/cucp/
    DELETE: BASE_URL/nfo/api/v1/helm/cucp/

##### CUUP:

    GET: BASE_URL/nfo/api/v1/helm/cuup/
    POST: BASE_URL/nfo/api/v1/helm/cuup/
    DELETE: BASE_URL/nfo/api/v1/helm/cuup/


## Run packagevariant_processor REST APIs

##### DU:

    POST: BASE_URL/nfo/api/v1/operator/du/
    GET: BASE_URL/nfo/api/v1/operator/du/
    DELETE: BASE_URL/nfo/api/v1/operator/du/

##### CU_CP:

    POST: BASE_URL/nfo/api/v1/operator/cucp/
    GET: BASE_URL/nfo/api/v1/operator/cucp/
    DELETE: BASE_URL/nfo/api/v1/operator/cucp/

##### CU_UP:

    POST: BASE_URL/nfo/api/v1/operator/cuup/
    GET: BASE_URL/nfo/api/v1/operator/cuup/
    DELETE: BASE_URL/nfo/api/v1/operator/cuup/