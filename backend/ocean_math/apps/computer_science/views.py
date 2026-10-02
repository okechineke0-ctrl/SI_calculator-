from rest_framework.views import APIView
from apps.core.responses import calculation_success_response, calculation_error_response
from .serializers import BaseConvertSerializer, BitwiseSerializer, AsciiSerializer, TruthTableSerializer
from .services import ComputerScienceService

class BaseConvertView(APIView):
    def post(self, request):
        serializer = BaseConvertSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid base conversion input", details=serializer.errors)

        d = serializer.validated_data
        res = ComputerScienceService.base_convert(value=d['value'], from_base=d['from_base'], to_base=d['to_base'])
        return calculation_success_response(
            result=res['result'],
            exact_result=res['result'],
            steps=[{"step": 1, "description": f"Converted {d['value']} (base {d['from_base']}) to base {d['to_base']}"}],
            method=res['method']
        )

class BitwiseView(APIView):
    def post(self, request):
        serializer = BitwiseSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid bitwise parameters", details=serializer.errors)

        d = serializer.validated_data
        res = ComputerScienceService.bitwise_operation(a=d['a'], b=d['b'], operation=d['operation'], bit_width=d['bit_width'])
        return calculation_success_response(
            result=res['result'],
            numeric_result=res['unsigned_decimal'],
            exact_result=res['binary'],
            formula=f"{d['operation'].upper()}({d['a']}, {d['b']})",
            method=res['method']
        )

class AsciiView(APIView):
    def post(self, request):
        serializer = AsciiSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid ASCII text", details=serializer.errors)

        res = ComputerScienceService.ascii_inspect(serializer.validated_data['text'])
        return calculation_success_response(
            result=res['result'],
            steps=res['characters'],
            method=res['method']
        )

class TruthTableView(APIView):
    def post(self, request):
        serializer = TruthTableSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid truth table inputs", details=serializer.errors)

        d = serializer.validated_data
        res = ComputerScienceService.generate_truth_table(variables=d['variables'], expression_str=d['expression'])
        return calculation_success_response(
            result=res['result'],
            steps=res['truth_table'],
            formula=d['expression'],
            method=res['method']
        )
