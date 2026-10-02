from rest_framework.views import APIView
from apps.core.responses import calculation_success_response, calculation_error_response
from .serializers import AiSolveSerializer
from .services import AiAssistantService

class AiSolveView(APIView):
    def post(self, request):
        serializer = AiSolveSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid AI request parameters", details=serializer.errors)

        res = AiAssistantService.process_query(
            prompt=serializer.validated_data['prompt'],
            context=serializer.validated_data.get('context')
        )
        return calculation_success_response(
            result=res['result'],
            numeric_result=res.get('numeric_result'),
            exact_result=res.get('exact_result'),
            steps=res.get('steps', []),
            method=res.get('method', 'ai_assistant'),
            warnings=res.get('warnings', [])
        )
