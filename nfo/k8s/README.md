
Step 1: Installed softwares:
===========================
Python: 3.12 (latest should work)
Pip3
Helm: v3.14.3 (latest should work)
Kubernetes Client: v1.28.2 (latest should work)
Kubernetes Server: v1.28.4 (latest should work)

Step 2: Create virtual environment:
=================================
#pip installed using get-pip.py
pip3 install virtualenv
virtualenv venv_nfo

source   venv_nfo/bin/activate  # On Windows use `env\Scripts\activate`

Step 3: Install Pip packages:
============================
pip3 install requests
pip3 install django
pip3 install djangorestframework
pip3 install pyhelm3
pip3 install kubernetes


python3 manage.py startapp apis //creates new app named apis

Step 4: Change environment configurations in config.ini
======================================================
Change Kubernetes config path
Change Helm executable path

Step 5: Run application:
=======================
python3 manage.py makemigrations  //generates new migration files based on model changes.
python3 manage.py migrate
python3 manage.py runserver

Step 6: Run REST apis:
=====================

GET http://127.0.0.1:8000/nfo/api/v1/
POST http://127.0.0.1:8000/nfo/api/v1/
    {
        "charts": [
            {
                "name": "cert-manager",
                "version": "v1.8.x",
                "repo": "https://charts.jetstack.io"
                
            }
        ]
    }
DELETE http://127.0.0.1:8000/nfo/api/v1/
    {
        "name": "cert-manager",
        "namespace": "default"
    }


#Helm Client Library
#https://github.com/stackhpc/pyhelm3