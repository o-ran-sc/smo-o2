import os
import pickle
import sys


class SampleScript(object):

    def __init__(self, req, inst, grant_req, grant, csar_dir):
        self.req = req
        self.inst = inst
        self.grant_req = grant_req
        self.grant = grant
        self.csar_dir = csar_dir

    def instantiate_start(self):
        pass

    def instantiate_end(self):
        pass

    def terminate_start(self):
        pass

    def terminate_end(self):
        pass

    def change_external_connectivity_start(self):
        if os.path.exists('/tmp/change_external_connectivity_start'):
            raise Exception("test change_external_connectivity_start error")
        pass

    def modify_information_start(self):
        if os.path.exists('/tmp/modify_information_start'):
            raise Exception("test modify_information_start error")
        pass


def main():
    script_dict = pickle.load(sys.stdin.buffer)

    operation = script_dict['operation']
    req = script_dict['request']
    inst = script_dict['vnf_instance']
    grant_req = script_dict['grant_request']
    grant = script_dict['grant_response']
    csar_dir = script_dict['tmp_csar_dir']

    script = SampleScript(req, inst, grant_req, grant, csar_dir)
    try:
        getattr(script, operation)()
    except AttributeError:
        raise Exception("{} is not included in the script.".format(operation))


if __name__ == "__main__":
    try:
        main()
        os._exit(0)
    except Exception as ex:
        sys.stderr.write(str(ex))
        sys.stderr.flush()
        os._exit(1)
