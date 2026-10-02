from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/v1/calculator/', include('apps.mathematics.urls')),
    path('api/v1/math/', include('apps.mathematics.urls')),
    path('api/v1/physics/', include('apps.physics.urls')),
    path('api/v1/chemistry/', include('apps.chemistry.urls')),
    path('api/v1/computer-science/', include('apps.computer_science.urls')),
    path('api/v1/units/', include('apps.units.urls')),
    path('api/v1/ai/', include('apps.ai_assistant.urls')),
]
