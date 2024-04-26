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

"""
URL configuration for nfo_microservice project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/5.0/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
# include necessary libraries
from django.urls import path, include
from .views import home_page

urlpatterns = [
    path('admin/', admin.site.urls),
    path('nfo/api/v1/', home_page),
    #path('apis/', include("apis.urls")),
    #path('nfohelm/api/v1/', include("helmprocessor.urls")),
    #path('nfo/api/v1/', include("helm.urls")),

]
