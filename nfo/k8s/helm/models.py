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
    
    