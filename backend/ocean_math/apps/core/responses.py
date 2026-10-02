from typing import Any, List, Optional
from rest_framework.response import Response
from rest_framework import status

def calculation_success_response(
    result: str,
    numeric_result: Optional[float] = None,
    exact_result: Optional[str] = None,
    unit: Optional[str] = None,
    formula: Optional[str] = None,
    steps: Optional[List[Any]] = None,
    method: str = "deterministic",
    precision: int = 15,
    warnings: Optional[List[str]] = None,
    latex: Optional[str] = None,
    status_code: int = status.HTTP_200_OK
) -> Response:
    """
    Standard Ocean Math calculation response specification.
    """
    payload = {
        "success": True,
        "result": str(result),
        "numeric_result": numeric_result,
        "exact_result": exact_result if exact_result is not None else str(result),
        "unit": unit,
        "formula": formula,
        "steps": steps or [],
        "method": method,
        "precision": precision,
        "warnings": warnings or [],
    }
    if latex:
        payload["latex"] = latex

    return Response(payload, status=status_code)

def calculation_error_response(
    error_code: str,
    message: str,
    status_code: int = status.HTTP_400_BAD_REQUEST,
    details: Optional[Any] = None
) -> Response:
    """
    Standard Ocean Math error response specification.
    """
    payload = {
        "success": False,
        "error_code": error_code,
        "message": message,
    }
    if details:
        payload["details"] = details
        
    return Response(payload, status=status_code)
