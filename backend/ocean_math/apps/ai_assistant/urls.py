from django.urls import path
from .views import AiSolveView

urlpatterns = [
    path('solve/', AiSolveView.as_view(), name='ai-solve'),
]
