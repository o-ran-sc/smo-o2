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

from tacker.tests.compliance.sol003 import cnf_base


class BaseCNFLifecycleManagementTest(cnf_base.BaseComplSolTest):
    @classmethod
    def setUpClass(cls):
        cls.api = 'VNFLifecycleManagement-API'

        super(BaseCNFLifecycleManagementTest, cls).setUpClass()

        cls.vnfpkginfos = cls._create_and_upload_cnf_packages(['test_cnf'])

    @classmethod
    def tearDownClass(cls):
        cls._disable_and_delete_vnf_packages(cls.vnfpkginfos)

        super(BaseCNFLifecycleManagementTest, cls).tearDownClass()


class BaseHelmLifecycleManagementTest(cnf_base.BaseComplSolTest):
    @classmethod
    def setUpClass(cls):
        cls.api = 'VNFLifecycleManagement-API'

        super(BaseHelmLifecycleManagementTest, cls).setUpClass()

        cls.vnfpkginfos = cls._create_and_upload_cnf_packages(
            ['test_cnf_helmchart'])

    @classmethod
    def tearDownClass(cls):
        cls._disable_and_delete_vnf_packages(cls.vnfpkginfos)

        super(BaseHelmLifecycleManagementTest, cls).tearDownClass()


class IndividualCnfLcmOperationOccurenceTest(BaseHelmLifecycleManagementTest):
    @classmethod
    def setUpClass(cls):
        cls.resource = 'IndividualVnfLcmOperationOccurence'

        super(IndividualCnfLcmOperationOccurenceTest, cls).setUpClass()

    def test_get_ind_cnf_lcm_op_occs(self):

        # Pre-conditions: none
        res, vnf = self._create_cnf_instance(self.vnfpkginfos[0].vnfdid)

        self._instantiate_cnf_instance(vnf['id'], pkg="helm")

        resbody = self._get_vnf_ind_instance(vnf['id'])
        self.assertEqual(resbody.get('instantiationState'), 'INSTANTIATED')

        lcmid = self._get_lcm_op_occs_id(vnf['id'])

        variables = ['vnfLcmOpOccId:' + lcmid]

        rc, output = self._run('Get Individual VNF LCM OP occurrence',
                               variables=variables)

        # Post-Conditions:
        self._terminate_cnf_instance(vnf['id'])
        self._delete_cnf_instance(vnf['id'])

        self.assertEqual(0, rc)


class KubernetesTest(BaseCNFLifecycleManagementTest):
    @classmethod
    def setUpClass(cls):
        cls.resource = 'IndividualVnfLcmOperationOccurence'

        super(KubernetesTest, cls).setUpClass()

    def test_get_all_pods(self):

        # Pre-conditions: none
        res, vnf = self._create_cnf_instance(self.vnfpkginfos[0].vnfdid)

        self._instantiate_cnf_instance(vnf['id'])
        resbody = self._get_vnf_ind_instance(vnf['id'])
        self.assertEqual(resbody.get('instantiationState'), 'INSTANTIATED')

        pods = self._show_all_pods(self)

        rc, output = self._run('Get All Pods')

        self.assertIsNotNone(pods)

        # Post-Conditions:
        self._terminate_cnf_instance(vnf['id'])
        self._delete_cnf_instance(vnf['id'])

        self.assertEqual(0, rc)

    def test_get_pod(self):

        res, vnf = self._create_cnf_instance(self.vnfpkginfos[0].vnfdid)

        self._instantiate_cnf_instance(vnf['id'])

        resbody = self._get_vnf_ind_instance(vnf['id'])
        self.assertEqual(resbody.get('instantiationState'), 'INSTANTIATED')

        pods = self._show_all_pods(self)
        name = pods.get('items')[0].get('metadata').get('name')
        namespace = pods.get('items')[0].get('metadata').get('namespace')
        specific_pod_data = self._read_spefic_pods(self, namespace, name)

        variables = ['name:' + name, 'namespace:' + namespace]

        rc, output = self._run('Get Specific Pod',
                               variables=variables)

        self.assertIsNotNone(specific_pod_data)
        self.assertEqual(specific_pod_data.get('status').get('phase'),
           "Running")

        # Post-Conditions:
        self._terminate_cnf_instance(vnf['id'])
        self._delete_cnf_instance(vnf['id'])

        self.assertEqual(0, rc)


class KubernetesHelmTest(BaseHelmLifecycleManagementTest):
    @classmethod
    def setUpClass(cls):
        cls.resource = 'IndividualVnfLcmOperationOccurence'

        super(KubernetesHelmTest, cls).setUpClass()

    def test_get_all_pods(self):

        # Pre-conditions: none
        res, vnf = self._create_cnf_instance(self.vnfpkginfos[0].vnfdid)

        self._instantiate_cnf_instance(vnf['id'], pkg="helm")
        resbody = self._get_vnf_ind_instance(vnf['id'])
        self.assertEqual(resbody.get('instantiationState'), 'INSTANTIATED')

        rc, output = self._run('Get All Pods')

        # Post-Conditions:
        self._terminate_cnf_instance(vnf['id'])
        self._delete_cnf_instance(vnf['id'])

        self.assertEqual(0, rc)

    def test_get_pod(self):

        res, vnf = self._create_cnf_instance(self.vnfpkginfos[0].vnfdid)

        self._instantiate_cnf_instance(vnf['id'], pkg="helm")
        resbody = self._get_vnf_ind_instance(vnf['id'])
        self.assertEqual(resbody.get('instantiationState'), 'INSTANTIATED')

        pods = self._show_all_pods(self)
        name = pods.get('items')[0].get('metadata').get('name')
        namespace = pods.get('items')[0].get('metadata').get('namespace')
        specific_pod_data = self._read_spefic_pods(self, namespace, name)

        variables = ['name:' + name, 'namespace:' + namespace]

        rc, output = self._run('Get Specific Pod',
                               variables=variables)

        self.assertIsNotNone(specific_pod_data)
        self.assertEqual(specific_pod_data.get('status').get('phase'),
           "Running")

        # Post-Conditions:
        self._terminate_cnf_instance(vnf['id'])
        self._delete_cnf_instance(vnf['id'])

        self.assertEqual(0, rc)
