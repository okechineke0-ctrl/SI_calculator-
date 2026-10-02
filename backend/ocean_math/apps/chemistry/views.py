from rest_framework.views import APIView
from apps.core.responses import calculation_success_response, calculation_error_response
from .serializers import MolarMassSerializer, BalanceSerializer
from .services import ChemistryService

class MolarMassView(APIView):
    def post(self, request):
        serializer = MolarMassSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid chemical formula", details=serializer.errors)

        res = ChemistryService.calculate_molar_mass(serializer.validated_data['formula'])
        return calculation_success_response(
            result=res['result'],
            numeric_result=res.get('numeric_result'),
            exact_result=res.get('exact_result'),
            unit=res.get('unit'),
            formula=serializer.validated_data['formula'],
            steps=res.get('steps'),
            method=res.get('method', 'deterministic')
        )

class BalanceView(APIView):
    def post(self, request):
        serializer = BalanceSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid chemical equation", details=serializer.errors)

        res = ChemistryService.balance_equation(serializer.validated_data['equation'])
        return calculation_success_response(
            result=res['result'],
            formula=res.get('formula'),
            method=res.get('method', 'deterministic')
        )
