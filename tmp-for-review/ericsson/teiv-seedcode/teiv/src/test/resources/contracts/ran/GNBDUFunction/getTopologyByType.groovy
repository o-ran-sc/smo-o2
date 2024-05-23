/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Ericsson
 *  Modifications Copyright (C) 2024 OpenInfra Foundation Europe
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package contracts.ran.GNBDUFunction

import org.springframework.cloud.contract.spec.Contract

[
    /**************************
     * 'targetFilter' query parameter
     ***************************/
    Contract.make {
        description "SUCCESS - 200: Get topology for GNBDUFunction using 'targetFilter' query parameter"
        request {
            method GET()
            url("/topology-inventory/v1alpha11/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&offset=0&limit=50")
        }
        response {
            status OK()
            headers {
                contentType('application/json')
            }
            body('''{
    "items": [
        {
            "o-ran-smo-teiv-ran:GNBDUFunction": [
                {
                    "id": "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1",
                    "attributes": {
                        "gNBDUId": 1
                    },
                    "metadata": {
                        "trustLevel": "RELIABLE"
                    }
                }
            ]
        }
    ],
    "self": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&offset=0&limit=50"
    },
    "first": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&offset=0&limit=50"
    },
    "prev": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&offset=0&limit=50"
    },
    "next": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&offset=0&limit=50"
    },
    "last": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&offset=0&limit=50"
    }
}''')
        }
    },
    Contract.make {
        description "NOT_FOUND - 404: Get topology for GNBDUFunction using unknown 'targetFilter' query parameter"
        request {
            method GET()
            url "/ties/v1alpha11/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2F%2FGNBDUFunction&offset=1&limit=100"
        }
        response {
            status NOT_FOUND()
        }
    },
    Contract.make {
        description "BAD_REQUEST - 400: The provided request is not valid"
        request {
            method GET()
            url "/topology-inventory/v1alpha11/domains/RAN/entity-types/GNBBUFunction/entities?targetFilter=%2F%2FGNBBUFunction&offset=1&limit=100"
        }
        response {
            status BAD_REQUEST()
        }
    },
    /***************************************
     * 'targetFilter' and 'scopeFilter' query parameter
     ****************************************/
    Contract.make {
        description "SUCCESS - 200: Get topology for GNBDUFunction using 'targetFilter' and 'scopeFilter' query parameter"
        request {
            method GET()
            url("/topology-inventory/v1alpha11/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&scopeFilter=%2Fattributes[@gNBDUId=1]&offset=0&limit=100")
        }
        response {
            status OK()
            headers {
                contentType('application/json')
            }
            body('''{
    "items": [
        {
            "o-ran-smo-teiv-ran:GNBDUFunction": [
                {
                    "id": "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1",
                    "attributes": {
                        "gNBDUId": 1
                    },
                    "metadata": {
                        "trustLevel": "RELIABLE"
                    }
                }
            ]
        }
    ],
    "self": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&scopeFilter=%2Fattributes[@gNBDUId=1]&offset=0&limit=100"
    },
    "first": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&scopeFilter=%2Fattributes[@gNBDUId=1]&offset=0&limit=100"
    },
    "prev": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&scopeFilter=%2Fattributes[@gNBDUId=1]&offset=0&limit=100"
    },
    "next": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&scopeFilter=%2Fattributes[@gNBDUId=1]&offset=0&limit=100"
    },
    "last": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&scopeFilter=%2Fattributes[@gNBDUId=1]&offset=0&limit=100"
    }
}''')
        }
    },
    Contract.make {
        description "NOT_FOUND - 404: Get unknown for GNBCUCPFunction using 'targetFilter' and 'scopeFilter' query parameter"
        request {
            method GET()
            url "/ties/v1alpha11/domains/RAN/entity-type/GNBDUFunction/entities?targetFilter=%2F%2FGNBDUFunction&scopeFilter=%2F%2FNRCellDU%2FnRTAC%3D50&offset=1&limit=100"
        }
        response {
            status NOT_FOUND()
        }
    },
    Contract.make {
        description "BAD_REQUEST - 400: The provided request is not valid"
        request {
            method GET()
            url "/topology-inventory/v1alpha11/domains/RAN/entity-types/GNBBUFunction/entities?targetFilter=%2F%2FGNBDUFunction&scopeFilter=%2F%2FNRCellDU%2FnRTAC%3D50&offset=1&limit=100"
        }
        response {
            status BAD_REQUEST()
        }
    }
]
