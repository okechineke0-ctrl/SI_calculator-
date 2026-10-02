"""
Ocean Math AI - High-Performance Python Backend Server
Powered by FastAPI, SymPy (Symbolic Mathematics), NumPy, and Google Gemini API.
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List, Any
import sympy as sp
import numpy as np
import os
import re

app = FastAPI(
    title="Ocean Math AI Python Backend",
    description="Symbolic mathematics, numerical computation, and AI math reasoning engine",
    version="1.0.0"
)

# Enable CORS for cross-origin requests
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class EvaluateRequest(BaseModel):
    expression: str
    precision: Optional[int] = 10
    angle_mode: Optional[str] = "DEG"

class EvaluateResponse(BaseModel):
    is_success: Boolean = True
    result: str
    exact_fraction: Optional[str] = None
    latex: Optional[str] = None
    error: Optional[str] = None

class CalculusRequest(BaseModel):
    expression: str
    variable: Optional[str] = "x"
    operation: str  # "diff", "integrate", "limit"
    point: Optional[float] = 0.0

class MatrixRequest(BaseModel):
    matrix: List[List[float]]
    operation: str  # "det", "inv", "eigenvalues", "rank"

class AiSolveRequest(BaseModel):
    prompt: str
    mode: Optional[str] = "Step-by-Step"
    api_key: Optional[str] = None

@app.get("/api/health")
def health_check():
    return {
        "status": "healthy",
        "engine": "Python 3.11 + SymPy + NumPy + FastAPI",
        "version": "1.0.0"
    }

@app.post("/api/evaluate")
def evaluate_expression(req: EvaluateRequest):
    """
    Evaluates mathematical expressions symbolically and numerically using SymPy.
    """
    try:
        expr_str = req.expression.strip()
        if not expr_str:
            return {"is_success": True, "result": "0", "exact_fraction": "0"}

        # Normalize symbols for SymPy
        cleaned = expr_str.replace("×", "*").replace("÷", "/").replace("^", "**")
        cleaned = cleaned.replace("π", "pi").replace("e", "E")

        # Parse with SymPy
        sym_expr = sp.sympify(cleaned, evaluate=True)

        # Numerical evaluation with requested precision
        prec = max(2, min(req.precision or 10, 16))
        num_val = float(sym_expr.evalf(prec))

        # Check exact rational representation
        exact_frac = None
        if isinstance(sym_expr, sp.Rational) and sym_expr.q != 1:
            exact_frac = f"{sym_expr.p}/{sym_expr.q}"

        return {
            "is_success": True,
            "result": f"{num_val:.{prec}g}",
            "exact_fraction": exact_frac,
            "latex": sp.latex(sym_expr),
            "error": None
        }
    except Exception as e:
        return {
            "is_success": False,
            "result": "Error",
            "exact_fraction": None,
            "error": str(e)
        }

@app.post("/api/calculus")
def calculus_operation(req: CalculusRequest):
    """
    Performs symbolic calculus operations (differentiation, integration, limits) via SymPy.
    """
    try:
        var = sp.Symbol(req.variable or "x")
        expr_str = req.expression.replace("×", "*").replace("÷", "/").replace("^", "**")
        expr = sp.sympify(expr_str)

        if req.operation == "diff":
            result = sp.diff(expr, var)
        elif req.operation == "integrate":
            result = sp.integrate(expr, var)
        elif req.operation == "limit":
            result = sp.limit(expr, var, req.point or 0.0)
        else:
            raise HTTPException(status_code=400, detail="Invalid operation")

        return {
            "is_success": True,
            "symbolic_result": str(result),
            "latex": sp.latex(result)
        }
    except Exception as e:
        return {"is_success": False, "error": str(e)}

@app.post("/api/matrix")
def matrix_operation(req: MatrixRequest):
    """
    Solves linear algebra and matrix operations using SymPy and NumPy.
    """
    try:
        M = sp.Matrix(req.matrix)
        if req.operation == "det":
            res = float(M.det())
            return {"is_success": True, "result": res}
        elif req.operation == "inv":
            if M.det() == 0:
                return {"is_success": False, "error": "Matrix is singular (det = 0)"}
            inv = M.inv()
            return {"is_success": True, "matrix": [[float(val) for val in row] for row in inv.tolist()]}
        elif req.operation == "rank":
            return {"is_success": True, "result": int(M.rank())}
        else:
            raise HTTPException(status_code=400, detail="Unsupported operation")
    except Exception as e:
        return {"is_success": False, "error": str(e)}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
