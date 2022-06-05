# Copyright (C) 2022 NEC, Corp.
# All Rights Reserved.
#
#    Licensed under the Apache License, Version 2.0 (the "License"); you may
#    not use this file except in compliance with the License. You may obtain
#    a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#    License for the specific language governing permissions and limitations
#    under the License.

from tacker.tests.compliance.sol005 import cnf_base


class BaseCNFPackageManagementTest(cnf_base.BaseComplSolTest):
    @classmethod
    def setUpClass(cls):
        cls.api = 'VNFPackageManagement-API'

        super(BaseCNFPackageManagementTest, cls).setUpClass()


class IndividualCNFPackageTest(BaseCNFPackageManagementTest):
    @classmethod
    def setUpClass(cls):
        cls.resource = 'IndividualVNFPackage'

        super(IndividualCNFPackageTest, cls).setUpClass()

    @classmethod
    def _disable_and_delete_vnf_packages(cls, vnfpkginfos):
        for vnfpkginfo in vnfpkginfos:
            cls._disable_vnf_package(vnfpkginfo.vnfpkgid)
            cls._delete_vnf_package(vnfpkginfo.vnfpkgid)

    def test_get_individual_cnf_package(self):

        # Pre-conditions: One or more VNF packages are onboarded in the NFVO.
        vnfpkginfos = self._create_and_upload_cnf_packages(['vnflcm1'])
        variables = ['vnfPackageId:' + vnfpkginfos[0].vnfpkgid]

        rc, output = self._run('GET Individual VNF Package', variables)

        # Post-Conditions: none
        self._disable_and_delete_vnf_packages(vnfpkginfos)

        self.assertEqual(0, rc)

