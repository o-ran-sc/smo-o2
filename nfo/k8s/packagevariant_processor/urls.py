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
from .views import du_processor, cucp_processor, cuup_processor

urlpatterns = [
    path('admin/', admin.site.urls),
    path('nfo/api/v1/operator/du/', du_processor),
    path('nfo/api/v1/operator/cucp/', cucp_processor),
    path('nfo/api/v1/operator/cuup/', cuup_processor), 
]
