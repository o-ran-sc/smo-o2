from django.contrib import admin
from helm.models import Application,OAI
# Register your models here..

class ApplicationAdmin(admin.ModelAdmin):
    list_display=('app_name','about')
    search_fields=('app_name',)   
    
class OAIAdmin(admin.ModelAdmin):
    list_display=('oai_helm_chart_name','repo','version')
    list_filter=('oai_helm_chart_name',)

admin.site.register(Application,ApplicationAdmin)
admin.site.register(OAI,OAIAdmin)
