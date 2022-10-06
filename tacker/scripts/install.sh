#!/usr/bin/env bash

if [[ `whoami` != "stack" ]]
then
    echo "You are not stack user."
    echo https://docs.openstack.org/devstack/latest/#add-stack-user-optional
    exit 1
fi

if [ $# != 1 ]; then
    echo Parameter error: usage: ./install.sh <HOST_IP>
    exit 1
fi

git clone https://git.openstack.org/openstack-dev/devstack
cd devstack

wget https://opendev.org/openstack/tacker/raw/branch/master/devstack/local.conf.standalone
sed -e "s/127.0.0.1/$1/g" local.conf.standalone > local.conf

./stack.sh
