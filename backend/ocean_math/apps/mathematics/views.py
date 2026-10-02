from rest_framework.views import APIView
from rest_framework.response import Response
from apps.core.responses import calculation_success_response, calculation_error_response
from .serializers import (
    EvaluateSerializer, DifferentiateSerializer, IntegrateSerializer,
    FactorExpandSerializer, MatrixSerializer
)
from .services import MathematicsService

class EvaluateView(APIView):
    def post(self, request):
        serializer = EvaluateSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid input parameters", details=serializer.errors)

        data = serializer.validated_data
        res = MathematicsService.evaluate(
            expression=data['expression'],
            precision=data.get('precision', 15),
            angle_mode=data.get('angle_mode', 'DEG')
        )
        return calculation_success_response(
            result=res['result'],
            numeric_result=res.get('numeric_result'),
            exact_result=res.get('exact_result'),
            latex=res.get('latex'),
            steps=res.get('steps'),
            method=res.get('method', 'deterministic'),
            precision=data.get('precision', 15)
        )

class DifferentiateView(APIView):
    def post(self, request):
        serializer = DifferentiateSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid differentiation parameters", details=serializer.errors)

        data = serializer.validated_data
        res = MathematicsService.differentiate(
            expression=data['expression'],
            variable=data.get('variable', 'x'),
            order=data.get('order', 1)
        )
        return calculation_success_response(
            result=res['result'],
            exact_result=res.get('exact_result'),
            formula=res.get('formula'),
            latex=res.get('latex'),
            method=res.get('method', 'deterministic')
        )

class IntegrateView(APIView):
    def post(self, request):
        serializer = IntegrateSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid integration parameters", details=serializer.errors)

        data = serializer.validated_data
        res = MathematicsService.integrate_expr(
            expression=data['expression'],
            variable=data.get('variable', 'x'),
            lower_limit=data.get('lower_limit'),
            upper_limit=data.get('upper_limit')
        )
        return calculation_success_response(
            result=res['result'],
            numeric_result=res.get('numeric_result'),
            exact_result=res.get('exact_result'),
            formula=res.get('formula'),
            latex=res.get('latex'),
            method=res.get('method', 'deterministic')
        )

class FactorExpandView(APIView):
    def post(self, request):
        serializer = FactorExpandSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid algebra parameters", details=serializer.errors)

        data = serializer.validated_data
        res = MathematicsService.factor_or_expand(
            expression=data['expression'],
            operation=data.get('operation', 'factor')
        )
        return calculation_success_response(
            result=res['result'],
            exact_result=res.get('exact_result'),
            latex=res.get('latex'),
            steps=res.get('steps'),
            method=res.get('method', 'deterministic')
        )

class MatrixView(APIView):
    def post(self, request):
        serializer = MatrixSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid matrix data", details=serializer.errors)

        data = serializer.validated_data
        res = MathematicsService.matrix_ops(
            matrix_data=data['matrix'],
            operation=data.get('operation', 'det')
        )
        return calculation_success_response(
            result=res['result'],
            numeric_result=res.get('numeric_result'),
            exact_result=res.get('exact_result'),
            latex=res.get('latex')
        )
