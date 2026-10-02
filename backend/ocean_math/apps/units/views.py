from rest_framework.views import APIView
from apps.core.responses import calculation_success_response, calculation_error_response
from .serializers import UnitConvertSerializer
from .services import UnitsService

class UnitConvertView(APIView):
    def post(self, request):
        serializer = UnitConvertSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid conversion parameters", details=serializer.errors)

        d = serializer.validated_data
        res = UnitsService.convert(value=d['value'], from_unit=d['from_unit'], to_unit=d['to_unit'])
        return calculation_success_response(
            result=res['result'],
            numeric_result=res.get('numeric_result'),
            exact_result=res.get('exact_result'),
            unit=res.get('unit'),
            formula=res.get('formula'),
            method=res.get('method', 'deterministic')
        )
