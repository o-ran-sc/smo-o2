from rest_framework import serializers
from helm.models import Application,OAI


#create serializers here
class ApplicationSerializer(serializers.HyperlinkedModelSerializer):
    appl_id=serializers.ReadOnlyField()
    class Meta:
        model=Application
        fields="__all__"
        
        
        
class OAISerializer(serializers.HyperlinkedModelSerializer):
    id=serializers.ReadOnlyField()    
    class Meta:
        model=OAI
        fields="__all__"