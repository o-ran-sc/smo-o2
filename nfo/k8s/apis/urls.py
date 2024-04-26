# basic URL Configurations
from django.urls import include, path
# import routers
from rest_framework import routers

# import everything from views
from .views import *
#from .views import api_home
from . import views

# define the router
router = routers.DefaultRouter()

# define the router path and viewset to be used
#router.register(r'nfoapi', NFOViewSet)

# specify URL Path for rest_framework
urlpatterns = [
    path('', views.api_home),
	#path('', include(router.urls)),
	path('api-auth/', include('rest_framework.urls'))
]
