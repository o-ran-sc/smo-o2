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
