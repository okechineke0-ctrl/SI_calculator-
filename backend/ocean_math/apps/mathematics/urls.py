from django.urls import path
from .views import EvaluateView, DifferentiateView, IntegrateView, FactorExpandView, MatrixView

urlpatterns = [
    path('evaluate/', EvaluateView.as_view(), name='math-evaluate'),
    path('differentiate/', DifferentiateView.as_view(), name='math-differentiate'),
    path('integrate/', IntegrateView.as_view(), name='math-integrate'),
    path('algebra/', FactorExpandView.as_view(), name='math-algebra'),
    path('matrix/', MatrixView.as_view(), name='math-matrix'),
]
