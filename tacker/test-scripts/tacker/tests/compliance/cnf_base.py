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

import json
import os
import time

from oslo_serialization import jsonutils
from oslo_utils import uuidutils
import robot
from urllib.parse import urlparse

from tacker.tests.functional import base
from tacker.tests.functional.sol_kubernetes.vnflcm import base as cnflcmtest

VNFPKG_PATH = '/vnfpkgm/v1/vnf_packages/%s'
VNFINSS_PATH = '/vnflcm/v1/vnf_instances'
VNFINS_PATH = '/vnflcm/v1/vnf_instances/%s'
VNFINS_INST_PATH = '/vnflcm/v1/vnf_instances/%s/instantiate'
VNFINS_TERM_PATH = '/vnflcm/v1/vnf_instances/%s/terminate'
VNFINS_GET_LCM_OP_OCCS_PATH = '/vnflcm/v1/vnf_lcm_op_occs'
VNFINS_GET_IND_LCM_OP_OCCS_PATH = '/vnflcm/v1/vnf_lcm_op_occs/%s'
VNFINS_CREATE_SUBSC_PATH = '/vnflcm/v1/subscriptions'
VNFINS_DEL_SUBSC_PATH = '/vnflcm/v1/subscriptions/%s'
base_get_all_pods_url = 'http://127.0.0.1:8080/api/v1/namespaces/default/pods'
pod_path = 'http://127.0.0.1:8080/api/v1/namespaces/{}/pods/{}'


INSTANTIATION_BODY_CNF = {
    "flavourId": "simple",
    "additionalParams": {
        "lcm-kubernetes-def-files": ["Files/kubernetes/deployment.yaml"]
    },
    "extVirtualLinks": [
        {
            "id": "net0",
            "resourceId": None,
            "extCps": [
                {
                    "cpdId": "CP1",
                    "cpConfig": [
                        {"cpProtocolData": [
                            {"layerProtocol": "IP_OVER_ETHERNET"}]}
                    ],
                }
            ],
        }
    ],
    "vimConnectionInfo": [{"id": None, "vimId": None, "vimType": "kubernetes"}]
}

INSTANTIATION_BODY_CNF_HELM = {
    "flavourId": "helmchart",
    "additionalParams": {
        "namespace": "default",
        "use_helm": "true",
        "using_helm_install_param": [
            {
                "exthelmchart": "false",
                "helmreleasename": "vdu1",
                "helmparameter": [
                    "service.port=8081"
                ],
                "helmchartfile_path": "Files/kubernetes/localhelm-0.1.0.tgz"
            },
            {
                "exthelmchart": "true",
                "helmreleasename": "vdu2",
                "helmrepositoryname": "bitnami",
                "helmchartname": "apache",
                "exthelmrepo_url": "https://charts.bitnami.com/bitnami"
            }
        ],
        "helm_replica_values": {
            "vdu1_aspect": "replicaCount",
            "vdu2_aspect": "replicaCount"
        },
        "vdu_mapping": {
            "VDU1": {
                "kind": "Deployment",
                "name": "vdu1-localhelm",
                "helmreleasename": "vdu1"
            },
            "VDU2": {
                "name": "vdu2-apache",
                "kind": "Deployment",
                "helmreleasename": "vdu2"
            }
        }
    },
    "vimConnectionInfo": [
        {
            "id": None,
            "vimId": None,
            "vimType": "kubernetes"
        }
    ]
}


TERMINATION_BODY = {
    'terminationType': 'GRACEFUL',
    'gracefulTerminationTimeout': 120
}


class VnfPkgInfo:
    def __init__(self, vnfpkgid, vnfdid):
        self._vnfpkgid = vnfpkgid
        self._vnfdid = vnfdid

    @property
    def vnfpkgid(self):
        return self._vnfpkgid

    @property
    def vnfdid(self):
        return self._vnfdid


class BaseComplTest(base.BaseTackerTest):
    @classmethod
    def setUpClass(cls):
        super(BaseComplTest, cls).setUpClass()

        for vim_list in cls.client.list_vims().values():
            for vim in vim_list:
                if vim['name'] == 'vim-kubernetes':
                    cls.vimid = vim['id']

        for net_list in cls.neutronclient().list_networks().values():
            for net in net_list:
                if net['name'] == 'net0':
                    cls.net0_id = net['id']

        cls.base_dir = os.getcwd()
        cls.test_root_dir = os.path.join(cls.base_dir, 'api-tests')
        cls.sol_dir = os.path.join(cls.test_root_dir, cls.sol)
        cls.api_dir = os.path.join(cls.sol_dir, cls.api)
        cls.test_file = cls.resource + '.robot'
        os.chdir(cls.api_dir)

        parts = urlparse(cls.http_client.get_endpoint())

        cls.common_variables = []
        cls.common_variables.append('VNFM_SCHEMA:%s' % parts.scheme)
        cls.common_variables.append('NFVO_SCHEMA:%s' % parts.scheme)
        cls.common_variables.append('VNFM_HOST:%s' % parts.hostname)
        cls.common_variables.append('NFVO_HOST:%s' % parts.hostname)
        cls.common_variables.append('VNFM_PORT:%s' % parts.port)
        cls.common_variables.append('NFVO_PORT:%s' % parts.port)
        cls.common_variables.append('AUTH_USAGE:1')
        cls.common_variables.append('AUTHORIZATION_HEADER:X-Auth-Token')
        cls.common_variables.append('AUTHORIZATION_TOKEN:%s' %
                                    cls.http_client.get_token())

    @classmethod
    def tearDownClass(cls):
        os.chdir(cls.base_dir)

        super(BaseComplTest, cls).tearDownClass()

    @classmethod
    def _create_and_upload_cnf_packages(cls, pkgnames):
        vnfpkginfos = []
        for pkgname in pkgnames:
            vnfpkgid, vnfdid = \
                cnflcmtest.BaseVnfLcmKubernetesTest.\
                _create_and_upload_cnf_package(
                    cls, cls.http_client, pkgname, {})
            vnfpkginfos.append(VnfPkgInfo(vnfpkgid, vnfdid))

        return vnfpkginfos

    @classmethod
    def _delete_vnf_package(cls, vnfpkgid):
        cls.http_client.do_request(VNFPKG_PATH % vnfpkgid, 'DELETE')

    @classmethod
    def _disable_vnf_package(cls, vnfpkgid):
        cls.http_client.do_request(VNFPKG_PATH % vnfpkgid,
            'PATCH', content_type='application/json',
            body=jsonutils.dumps({"operationalState": "DISABLED"}))

    @classmethod
    def _disable_and_delete_vnf_packages(cls, vnfpkginfos):
        for vnfpkginfo in vnfpkginfos:
            cls._disable_vnf_package(vnfpkginfo.vnfpkgid)
            cls._delete_vnf_package(vnfpkginfo.vnfpkgid)

    @classmethod
    def _get_lcm_op_occs_id(cls, vnfid, lcm='INSTANTIATE'):
        res, resbody = cls.http_client.do_request(
            VNFINS_GET_LCM_OP_OCCS_PATH, 'GET')

        lcmid = None
        for entry in resbody:
            lcm_dict = entry
            if ((lcm_dict['vnfInstanceId'] == vnfid) and
                    (lcm_dict['operation'] == lcm)):
                lcmid = lcm_dict['id']
                break

        return lcmid

    @classmethod
    def _create_cnf_instance(cls, vnfdid, name=None, description=None):
        body = {'vnfdId': vnfdid}
        if name:
            body['vnfInstanceName'] = name
        if description:
            body['vnfInstanceDescription'] = description

        res, resbody = cls.http_client.do_request(VNFINSS_PATH, 'POST',
                body=jsonutils.dumps(body))

        return res, resbody

    @classmethod
    def _instantiate_cnf_instance(cls, vnfid, pkg=None):
        if pkg == "helm":
            body = INSTANTIATION_BODY_CNF_HELM
            body['vimConnectionInfo'][0]['id'] = uuidutils.generate_uuid()
            body['vimConnectionInfo'][0]['vimId'] = cls.vimid

            cls.http_client.do_request(VNFINS_INST_PATH % vnfid,
                'POST', body=jsonutils.dumps(body))

            cls._wait_vnf_status(vnfid, 'instantiationState', 'INSTANTIATED')
        else:
            body = INSTANTIATION_BODY_CNF
            body['extVirtualLinks'][0]['resourceId'] = cls.net0_id
            body['vimConnectionInfo'][0]['id'] = uuidutils.generate_uuid()
            body['vimConnectionInfo'][0]['vimId'] = cls.vimid

            cls.http_client.do_request(VNFINS_INST_PATH % vnfid,
                'POST', body=jsonutils.dumps(body))

            cls._wait_vnf_status(vnfid, 'instantiationState', 'INSTANTIATED')

    def _get_vnf_ind_instance(cls, vnfid):
        res, resbody = cls.http_client.do_request(VNFINS_PATH % vnfid, 'GET')

        return resbody

    @classmethod
    def _wait_vnf_status(cls, vnfid, attr, value, expire=600):
        start_time = int(time.time())
        while True:
            resp, body = cls.http_client.do_request(VNFINS_PATH % vnfid, 'GET')
            if body[attr] == value:
                break

            if ((int(time.time()) - start_time) > expire):
                break

            time.sleep(5)
        time.sleep(30)

    @classmethod
    def _show_all_pods(cls, self, id=None):
        _, pods = cls.http_client.do_request(base_get_all_pods_url, 'GET')
        json_string = json.dumps(pods, indent=4,
                         skipkeys=True)
        dict_string = json.loads(json_string)

        return dict_string

    @classmethod
    def _read_spefic_pods(cls, self, namespace, pod_name):
        _, pod = cls.http_client.do_request(pod_path.format(namespace,
                                            pod_name), 'GET')

        return pod

    @classmethod
    def _delete_cnf_instance(cls, vnfid):
        resp, body = cls.http_client.do_request(VNFINS_PATH % vnfid, 'DELETE')

    @classmethod
    def _terminate_cnf_instance(cls, vnfid):
        cls.http_client.do_request(VNFINS_TERM_PATH % vnfid,
            'POST', body=jsonutils.dumps(TERMINATION_BODY))

        cls._wait_vnf_status(vnfid, 'instantiationState', 'NOT_INSTANTIATED')

    def _run(self, test_case, variables=[], body=None, filename=None):
        if (body is not None and filename is not None):
            with open(os.path.join('jsons', filename), 'w') as f:
                f.write(body)
        all_vars = []
        all_vars.extend(variables)
        all_vars.extend(self.common_variables)

        odir = os.path.join(self.base_dir, 'log',
                            self.sol, self.api, self.resource,
                            test_case.replace(' ', '_').replace('"', ''))

        if not os.path.exists(odir):
            os.makedirs(odir)

        with open(os.path.join(odir, 'stdout.txt'), 'w') as stdout:
            rc = robot.run(self.test_file, variable=all_vars, test=test_case,
                           outputdir=odir, stdout=stdout)

        with open(os.path.join(odir, 'output.xml'), 'r') as ofile:
            outputxml = ofile.read()

        return rc, outputxml
