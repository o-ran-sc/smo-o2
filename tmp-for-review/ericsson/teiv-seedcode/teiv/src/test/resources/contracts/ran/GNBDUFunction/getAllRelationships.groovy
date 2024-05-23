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
    Contract.make {
        description "SUCCESS - 200: Get a list of all relationships for GNBDUFunction 1"
        request {
            method GET()
            url "/topology-inventory/v1alpha11/domains/RAN/entity-types/GNBDUFunction/entities/urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio00001%2FGNBDUFunction%3D1/relationships?offset=0&limit=100"
        }
        response {
            status OK()
            headers {
                contentType('application/json')
            }
            body('''{
    "items": [
        {
            "o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU": [
                {
                    "id": "urn:sha512:R05CRFVGdW5jdGlvbjpTdWJOZXR3b3JrPUV1cm9wZSxTdWJOZXR3",
                    "aSide": "urn:3gpp:dn:ManagedElement=1,GNBDUFunction=1",
                    "bSide": "urn:3gpp:dn:ManagedElement=1,GNBDUFunction=1,NRCellDU=1",
                    "decorators": {
                        "location": "Stockholm"
                    },
                    "classifiers": [
                        "Rural"
                    ],
                    "sourceIds": [],
                    "metadata": {
                        "trustLevel": "RELIABLE"
                    }
                }
            ]
        }
    ],
    "self": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities/urn%3A3gpp%3Adn%3A%2FManagedElement%3D1%2FGNBDUFunction%3D1/relationships?offset=0&limit=100"
    },
    "first": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities/urn%3A3gpp%3Adn%3A%2FManagedElement%3D1%2FGNBDUFunction%3D1/relationships?offset=0&limit=100"
    },
    "prev": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities/urn%3A3gpp%3Adn%3A%2FManagedElement%3D1%2FGNBDUFunction%3D1/relationships?offset=0&limit=100"
    },
    "next": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities/urn%3A3gpp%3Adn%3A%2FManagedElement%3D1%2FGNBDUFunction%3D1/relationships?offset=0&limit=100"
    },
    "last": {
        "href": "/domains/RAN/entity-types/GNBDUFunction/entities/urn%3A3gpp%3Adn%3A%2FManagedElement%3D1%2FGNBDUFunction%3D1/relationships?offset=0&limit=100"
    }
}''')
        }
    },
    Contract.make {
        description "BAD_REQUEST - 400: Get a list of all relationships for an unknown objectType"
        request {
            method GET()
            url "/topology-inventory/v1alpha11/domains/RAN/entity-types/5GCell/entities/R05CRFVGdW5jdGlvbg/relationships?offset=1&limit=100"
        }
        response {
            status BAD_REQUEST()
        }
    }
]
