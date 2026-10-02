from rest_framework.views import APIView
from apps.core.responses import calculation_success_response, calculation_error_response
from .serializers import KinematicsSerializer, DynamicsSerializer, EnergySerializer
from .services import PhysicsService

class KinematicsView(APIView):
    def post(self, request):
        serializer = KinematicsSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid kinematics inputs", details=serializer.errors)

        d = serializer.validated_data
        res = PhysicsService.solve_kinematics(u=d.get('u'), v=d.get('v'), a=d.get('a'), t=d.get('t'), s=d.get('s'))
        return calculation_success_response(
            result=res['result'],
            numeric_result=res.get('numeric_result'),
            exact_result=res.get('exact_result'),
            unit=res.get('unit'),
            formula=res.get('formula'),
            steps=res.get('steps'),
            method=res.get('method', 'deterministic')
        )

class DynamicsView(APIView):
    def post(self, request):
        serializer = DynamicsSerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid dynamics inputs", details=serializer.errors)

        d = serializer.validated_data
        res = PhysicsService.solve_dynamics(mass=d['mass'], acceleration=d.get('acceleration'), force=d.get('force'))
        return calculation_success_response(
            result=res['result'],
            numeric_result=res.get('numeric_result'),
            exact_result=res.get('exact_result'),
            unit=res.get('unit'),
            formula=res.get('formula'),
            steps=res.get('steps'),
            method=res.get('method', 'deterministic')
        )

class EnergyView(APIView):
    def post(self, request):
        serializer = EnergySerializer(data=request.data)
        if not serializer.is_valid():
            return calculation_error_response("VALIDATION_ERROR", "Invalid energy inputs", details=serializer.errors)

        d = serializer.validated_data
        res = PhysicsService.solve_work_energy(mass=d['mass'], velocity=d['velocity'])
        return calculation_success_response(
            result=res['result'],
            numeric_result=res.get('numeric_result'),
            exact_result=res.get('exact_result'),
            unit=res.get('unit'),
            formula=res.get('formula'),
            steps=res.get('steps'),
            method=res.get('method', 'deterministic')
        )
