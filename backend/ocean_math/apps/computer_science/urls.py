from django.urls import path
from .views import BaseConvertView, BitwiseView, AsciiView, TruthTableView

urlpatterns = [
    path('base-convert/', BaseConvertView.as_view(), name='cs-base-convert'),
    path('bitwise/', BitwiseView.as_view(), name='cs-bitwise'),
    path('ascii/', AsciiView.as_view(), name='cs-ascii'),
    path('truth-table/', TruthTableView.as_view(), name='cs-truth-table'),
]
