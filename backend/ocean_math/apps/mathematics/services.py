import math
from typing import Dict, Any, Optional, List
from decimal import Decimal, getcontext
from fractions import Fraction
from apps.core.exceptions import MathSyntaxError, OceanMathException

try:
    import sympy as sp
    from sympy import sympify, latex, diff, integrate, limit, Matrix, factor, expand, simplify, solve
    HAS_SYMPY = True
except ImportError:
    HAS_SYMPY = False

class MathematicsService:
    """
    Deterministic scientific mathematics engine with exact symbolic and arbitrary precision capabilities.
    """

    @staticmethod
    def evaluate(expression: str, precision: int = 15, angle_mode: str = "DEG") -> Dict[str, Any]:
        """
        Evaluates mathematical expression symbolically and numerically.
        """
        expr_str = expression.strip()
        if not expr_str:
            return {"result": "0", "numeric_result": 0.0, "exact_result": "0"}

        # Normalize visual math characters
        cleaned = expr_str.replace("×", "*").replace("÷", "/").replace("^", "**")
        cleaned = cleaned.replace("π", "pi")

        if HAS_SYMPY:
            try:
                sym_expr = sp.sympify(cleaned, evaluate=True)
                
                # Check for exact rational number
                exact_res = str(sym_expr)
                if isinstance(sym_expr, sp.Rational) and sym_expr.q != 1:
                    exact_res = f"{sym_expr.p}/{sym_expr.q}"

                num_val = float(sym_expr.evalf(precision))
                
                return {
                    "result": f"{num_val:.{precision}g}",
                    "numeric_result": num_val,
                    "exact_result": exact_res,
                    "latex": sp.latex(sym_expr),
                    "method": "deterministic_sympy",
                    "steps": [
                        {"step": 1, "description": "Parsed symbolic expression", "expression": str(sym_expr)},
                        {"step": 2, "description": "Evaluated numerical result", "value": str(num_val)}
                    ]
                }
            except Exception as e:
                raise MathSyntaxError(f"Expression evaluation failed: {str(e)}")
        else:
            # Fallback to Python standard math and Decimal
            try:
                getcontext().prec = precision
                val = eval(cleaned, {"__builtins__": None, "math": math, "pi": math.pi, "e": math.e})
                num_val = float(val)
                return {
                    "result": f"{num_val:.{precision}g}",
                    "numeric_result": num_val,
                    "exact_result": str(val),
                    "method": "deterministic_python_math",
                    "steps": [{"step": 1, "description": "Evaluated with standard math library", "value": str(num_val)}]
                }
            except Exception as e:
                raise MathSyntaxError(f"Math evaluation error: {str(e)}")

    @staticmethod
    def factor_or_expand(expression: str, operation: str = "factor") -> Dict[str, Any]:
        """
        Symbolic polynomial factorization, expansion, and simplification.
        """
        if not HAS_SYMPY:
            raise OceanMathException("SymPy is required for symbolic algebraic factorization.")

        try:
            expr = sp.sympify(expression.replace("^", "**"))
            if operation == "factor":
                res = sp.factor(expr)
                desc = "Factorized polynomial"
            elif operation == "expand":
                res = sp.expand(expr)
                desc = "Expanded polynomial"
            else:
                res = sp.simplify(expr)
                desc = "Simplified expression"

            return {
                "result": str(res),
                "exact_result": str(res),
                "latex": sp.latex(res),
                "method": "deterministic_sympy",
                "steps": [
                    {"step": 1, "description": "Input expression", "value": str(expr)},
                    {"step": 2, "description": desc, "value": str(res)}
                ]
            }
        except Exception as e:
            raise MathSyntaxError(f"Algebraic operation failed: {str(e)}")

    @staticmethod
    def differentiate(expression: str, variable: str = "x", order: int = 1) -> Dict[str, Any]:
        """
        Symbolic derivative d^n/dx^n.
        """
        if not HAS_SYMPY:
            raise OceanMathException("SymPy is required for symbolic calculus.")

        try:
            var = sp.Symbol(variable)
            expr = sp.sympify(expression.replace("^", "**"))
            res = sp.diff(expr, var, order)
            return {
                "result": str(res),
                "exact_result": str(res),
                "formula": f"d^{order}/d{variable}^{order} ({expression})",
                "latex": sp.latex(res),
                "method": "deterministic_sympy"
            }
        except Exception as e:
            raise MathSyntaxError(f"Differentiation failed: {str(e)}")

    @staticmethod
    def integrate_expr(expression: str, variable: str = "x", lower_limit: Optional[float] = None, upper_limit: Optional[float] = None) -> Dict[str, Any]:
        """
        Symbolic definite or indefinite integral.
        """
        if not HAS_SYMPY:
            raise OceanMathException("SymPy is required for symbolic calculus.")

        try:
            var = sp.Symbol(variable)
            expr = sp.sympify(expression.replace("^", "**"))
            if lower_limit is not None and upper_limit is not None:
                res = sp.integrate(expr, (var, lower_limit, upper_limit))
                num_val = float(res.evalf()) if hasattr(res, 'evalf') else float(res)
                return {
                    "result": str(num_val),
                    "numeric_result": num_val,
                    "exact_result": str(res),
                    "formula": f"∫[{lower_limit}, {upper_limit}] ({expression}) d{variable}",
                    "latex": sp.latex(res),
                    "method": "deterministic_sympy"
                }
            else:
                res = sp.integrate(expr, var)
                return {
                    "result": f"{str(res)} + C",
                    "exact_result": str(res),
                    "formula": f"∫ ({expression}) d{variable}",
                    "latex": sp.latex(res) + " + C",
                    "method": "deterministic_sympy"
                }
        except Exception as e:
            raise MathSyntaxError(f"Integration failed: {str(e)}")

    @staticmethod
    def matrix_ops(matrix_data: List[List[float]], operation: str = "det") -> Dict[str, Any]:
        """
        Matrix linear algebra (determinant, inverse, rank, eigenvalues).
        """
        if not HAS_SYMPY:
            raise OceanMathException("SymPy is required for matrix computations.")

        try:
            M = sp.Matrix(matrix_data)
            if operation == "det":
                val = float(M.det())
                return {"result": str(val), "numeric_result": val, "exact_result": str(M.det())}
            elif operation == "inv":
                if M.det() == 0:
                    raise OceanMathException("Matrix is singular (determinant is 0), cannot invert.")
                inv = M.inv()
                return {"result": str(inv), "exact_result": str(inv), "matrix": inv.tolist(), "latex": sp.latex(inv)}
            elif operation == "rank":
                r = int(M.rank())
                return {"result": str(r), "numeric_result": r, "exact_result": str(r)}
            else:
                raise OceanMathException(f"Unsupported matrix operation: {operation}")
        except Exception as e:
            raise OceanMathException(f"Matrix operation error: {str(e)}")
