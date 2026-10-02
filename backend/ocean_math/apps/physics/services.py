import math
from typing import Dict, Any, Optional
from apps.core.exceptions import DimensionError, OceanMathException

try:
    import pint
    ureg = pint.UnitRegistry()
    HAS_PINT = True
except ImportError:
    HAS_PINT = False

class PhysicsService:
    """
    Dedicated physics service layer with dimensional analysis and unit validation.
    """

    @staticmethod
    def solve_kinematics(
        u: Optional[float] = None,
        v: Optional[float] = None,
        a: Optional[float] = None,
        t: Optional[float] = None,
        s: Optional[float] = None
    ) -> Dict[str, Any]:
        """
        Solves uniformly accelerated rectilinear motion:
        v = u + a*t
        s = u*t + 0.5*a*t^2
        v^2 = u^2 + 2*a*s
        """
        # Case 1: find v given u, a, t
        if u is not None and a is not None and t is not None and v is None:
            res_v = u + a * t
            formula = "v = u + at"
            steps = [
                {"step": 1, "description": "Identify kinematics equation", "formula": formula},
                {"step": 2, "description": f"Substitute values: u={u} m/s, a={a} m/s², t={t} s", "substitution": f"v = {u} + ({a})({t})"},
                {"step": 3, "description": "Compute final velocity", "result": f"{res_v} m/s"}
            ]
            return {
                "result": str(res_v),
                "numeric_result": res_v,
                "exact_result": str(res_v),
                "unit": "m/s",
                "formula": formula,
                "steps": steps,
                "method": "deterministic_physics"
            }

        # Case 2: find s given u, a, t
        if u is not None and a is not None and t is not None and s is None:
            res_s = u * t + 0.5 * a * (t ** 2)
            formula = "s = ut + (1/2)at²"
            steps = [
                {"step": 1, "description": "Identify displacement equation", "formula": formula},
                {"step": 2, "description": f"Substitute values: u={u}, a={a}, t={t}", "substitution": f"s = ({u})({t}) + 0.5({a})({t}²)"},
                {"step": 3, "description": "Compute total displacement", "result": f"{res_s} m"}
            ]
            return {
                "result": str(res_s),
                "numeric_result": res_s,
                "exact_result": str(res_s),
                "unit": "m",
                "formula": formula,
                "steps": steps,
                "method": "deterministic_physics"
            }

        # Case 3: find a given v, u, t
        if v is not None and u is not None and t is not None and a is None:
            if t == 0:
                raise OceanMathException("Time interval Δt cannot be zero.")
            res_a = (v - u) / t
            formula = "a = (v - u) / t"
            return {
                "result": str(res_a),
                "numeric_result": res_a,
                "exact_result": str(res_a),
                "unit": "m/s²",
                "formula": formula,
                "method": "deterministic_physics"
            }

        raise OceanMathException("Provide sufficient kinematics parameters (e.g. u, a, t to solve v or s).")

    @staticmethod
    def solve_dynamics(mass: float, acceleration: Optional[float] = None, force: Optional[float] = None) -> Dict[str, Any]:
        """
        Newton's Second Law: F = m * a
        """
        if mass <= 0:
            raise OceanMathException("Mass must be strictly positive.")

        if acceleration is not None and force is None:
            f = mass * acceleration
            return {
                "result": str(f),
                "numeric_result": f,
                "exact_result": str(f),
                "unit": "N",
                "formula": "F = ma",
                "steps": [
                    {"step": 1, "formula": "F = ma"},
                    {"step": 2, "substitution": f"F = ({mass} kg) × ({acceleration} m/s²)"},
                    {"step": 3, "result": f"{f} N"}
                ],
                "method": "deterministic_physics"
            }
        elif force is not None and acceleration is None:
            a = force / mass
            return {
                "result": str(a),
                "numeric_result": a,
                "exact_result": str(a),
                "unit": "m/s²",
                "formula": "a = F / m",
                "method": "deterministic_physics"
            }

        raise OceanMathException("Provide mass and either acceleration or force.")

    @staticmethod
    def solve_work_energy(mass: float, velocity: float) -> Dict[str, Any]:
        """
        Kinetic Energy: KE = 0.5 * m * v^2
        """
        if mass <= 0:
            raise OceanMathException("Mass must be positive.")

        ke = 0.5 * mass * (velocity ** 2)
        return {
            "result": str(ke),
            "numeric_result": ke,
            "exact_result": str(ke),
            "unit": "J",
            "formula": "KE = (1/2)mv²",
            "steps": [
                {"step": 1, "formula": "KE = (1/2)mv²"},
                {"step": 2, "substitution": f"KE = 0.5 × ({mass} kg) × ({velocity} m/s)²"},
                {"step": 3, "result": f"{ke} Joules"}
            ],
            "method": "deterministic_physics"
        }
