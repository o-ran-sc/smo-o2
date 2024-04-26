"""
========================LICENSE_START=================================
O-RAN-SC
%%
Copyright (C) 2024 Capgemini
%%
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
========================LICENSE_END===================================
"""

from django.db import models

# Create your models here.

#Creating Application Model

class Application(models.Model):
    app_id=models.AutoField(primary_key=True)
    app_name= models.CharField(max_length=50)
    location=models.CharField(max_length=50)
    about=models.TextField()
    type=models.CharField(max_length=100,choices=
                          (('RAN','RAN'),
                           ('CORE','CORE'),
                           ("IT",'IT')
                           ))
    added_date=models.DateTimeField(auto_now=True)
    active=models.BooleanField(default=True)
    
    def __str__(self):
        return self.name +'--'+ self.location
    
    
    
#Employee Model
class OAI(models.Model):
    oai_helm_chart_name=models.CharField(max_length=100)
    repo=models.CharField(max_length=50)
    version=models.TextField()
    description=models.TextField()
    
    
    company=models.ForeignKey(Application, on_delete=models.CASCADE)
    
    