from django.urls import path
from .views import UnitConvertView

urlpatterns = [
    path('convert/', UnitConvertView.as_view(), name='units-convert'),
]
