from django.urls import path
from .views import MolarMassView, BalanceView

urlpatterns = [
    path('molar-mass/', MolarMassView.as_view(), name='chem-molar-mass'),
    path('balance/', BalanceView.as_view(), name='chem-balance'),
]
