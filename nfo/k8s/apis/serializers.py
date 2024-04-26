# import serializer from rest_framework
from rest_framework import serializers

# import model from models.py
from .models import NFOModel

# Create a model serializer
class NFOSerializer(serializers.HyperlinkedModelSerializer):
	# specify model and fields
	class Meta:
		model = NFOModel
		fields = ('title', 'description')
