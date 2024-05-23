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
package contracts.ran.NRCellDU

import org.springframework.cloud.contract.spec.Contract

[
    Contract.make {
        description "SUCCESS - 200: Get topology for object type by NRCellDU 1"
        request {
            method GET()
            url("/topology-inventory/v1alpha11/domains/RAN/entity-types/NRCellDU/entities/urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio%2FGNBDUFunction%3D1%2FNRCellDU%3D1")
        }
        response {
            status OK()
            headers {
                contentType('application/yang.data+json')
            }
            body('''{
    "o-ran-smo-teiv-ran:NRCellDU": [
        {
            "id": "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1,NRCellDU=1",
            "attributes": {
                "cellLocalId": 91,
                "nCI": 1,
                "nRPCI": 35,
                "nRTAC": 50
            },
            "decorators": {
                "location": "Stockholm"
            },
            "classifiers": [
                "Rural"
            ],
            "sourceIds": [
                "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1,NRCellDU=1"
            ],
            "metadata": {
                "trustLevel": "RELIABLE"
            }
        }
    ]
}''')
        }
    },
    Contract.make {
        description "SUCCESS - 200: Get topology for object type by NRCellDU 2"
        request {
            method GET()
            url("/topology-inventory/v1alpha11/domains/RAN/entity-types/NRCellDU/entities/urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio%2FGNBDUFunction%3D1%2FNRCellDU%3D2")
        }
        response {
            status OK()
            headers {
                contentType('application/yang.data+json')
            }
            body('''{
    "o-ran-smo-teiv-ran:NRCellDU": [
        {
            "id": "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1,NRCellDU=2",
            "attributes": {
                "cellLocalId": 95,
                "nCI": 5,
                "nRPCI": 35,
                "nRTAC": 50
            },
            "decorators": {
                "location": "Stockholm"
            },
            "classifiers": [
                "Rural"
            ],
            "sourceIds": [
                "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1,NRCellDU=2"
            ],
            "metadata": {
                "trustLevel": "RELIABLE"
            }
        }
    ]
}''')
        }
    },
    Contract.make {
        description "NOT_FOUND - 404: Get unknown for object type by NRCellDU 1"
        request {
            method GET()
            url "/ties/v1alpha11/domains/RAN/entity-types/NRCellDU/entities/urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio%2FGNBDUFunction%3D1%2FNRCellDU%3D1"
        }
        response {
            status NOT_FOUND()
        }
    },
    Contract.make {
        description "BAD_REQUEST - 400: Get topology for unknown object type by NRCellDU 1"
        request {
            method GET()
            url "/topology-inventory/v1alpha11/domains/RAN/entity-types/NRDU/entities/urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio%2FGNBDUFunction%3D1%2FNRCellDU%3D1"
        }
        response {
            status BAD_REQUEST()
        }
    }
]
