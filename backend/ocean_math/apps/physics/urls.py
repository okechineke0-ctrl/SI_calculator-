from django.urls import path
from .views import KinematicsView, DynamicsView, EnergyView

urlpatterns = [
    path('kinematics/', KinematicsView.as_view(), name='physics-kinematics'),
    path('dynamics/', DynamicsView.as_view(), name='physics-dynamics'),
    path('energy/', EnergyView.as_view(), name='physics-energy'),
]
