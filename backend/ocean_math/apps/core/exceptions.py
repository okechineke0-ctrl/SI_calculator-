from rest_framework.views import exception_handler
from rest_framework.response import Response
from rest_framework import status

class OceanMathException(Exception):
    def __init__(self, message: str, error_code: str = "CALCULATION_ERROR", status_code: int = 400):
        super().__init__(message)
        self.message = message
        self.error_code = error_code
        self.status_code = status_code

class DimensionError(OceanMathException):
    def __init__(self, message: str = "Incompatible units in dimensional analysis"):
        super().__init__(message=message, error_code="DIMENSION_ERROR", status_code=400)

class MathSyntaxError(OceanMathException):
    def __init__(self, message: str = "Invalid mathematical expression syntax"):
        super().__init__(message=message, error_code="SYNTAX_ERROR", status_code=400)

class ChemicalBalanceError(OceanMathException):
    def __init__(self, message: str = "Could not balance chemical equation"):
        super().__init__(message=message, error_code="CHEMISTRY_BALANCE_ERROR", status_code=400)

def custom_exception_handler(exc, context):
    if isinstance(exc, OceanMathException):
        return Response({
            "success": False,
            "error_code": exc.error_code,
            "message": exc.message
        }, status=exc.status_code)

    response = exception_handler(exc, context)
    if response is not None:
        return Response({
            "success": False,
            "error_code": "API_ERROR",
            "message": str(exc),
            "details": response.data
        }, status=response.status_code)

    return Response({
        "success": False,
        "error_code": "INTERNAL_SERVER_ERROR",
        "message": str(exc)
    }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
