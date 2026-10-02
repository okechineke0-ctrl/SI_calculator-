from typing import Dict, Any
from apps.core.exceptions import DimensionError, OceanMathException

try:
    import pint
    ureg = pint.UnitRegistry()
    HAS_PINT = True
except ImportError:
    HAS_PINT = False

class UnitsService:
    """
    Deterministic unit conversion and strict dimensional analysis engine.
    """

    # Built-in standard conversion factors if Pint is not yet installed in container
    FACTORS: Dict[str, Dict[str, float]] = {
        "length": {
            "m": 1.0, "km": 1000.0, "cm": 0.01, "mm": 0.001,
            "mi": 1609.344, "yd": 0.9144, "ft": 0.3048, "in": 0.0254,
            "nmi": 1852.0
        },
        "mass": {
            "kg": 1.0, "g": 0.001, "mg": 1e-6, "t": 1000.0,
            "lb": 0.45359237, "oz": 0.028349523125
        },
        "time": {
            "s": 1.0, "ms": 0.001, "min": 60.0, "h": 3600.0, "d": 86400.0
        },
        "speed": {
            "m/s": 1.0, "km/h": 1.0 / 3.6, "mph": 0.44704, "knot": 0.514444
        },
        "energy": {
            "J": 1.0, "kJ": 1000.0, "cal": 4.184, "kcal": 4184.0, "Wh": 3600.0, "kWh": 3.6e6, "eV": 1.602176634e-19
        },
        "pressure": {
            "Pa": 1.0, "kPa": 1000.0, "bar": 100000.0, "atm": 101325.0, "psi": 6894.757, "mmHg": 133.322
        }
    }

    @classmethod
    def convert(cls, value: float, from_unit: str, to_unit: str) -> Dict[str, Any]:
        """
        Converts between compatible units or raises DimensionError.
        """
        from_u = from_unit.strip()
        to_u = to_unit.strip()

        if HAS_PINT:
            try:
                quantity = value * ureg(from_u)
                target = quantity.to(to_u)
                num_val = float(target.magnitude)
                return {
                    "result": f"{round(num_val, 8)}",
                    "numeric_result": num_val,
                    "exact_result": f"{num_val}",
                    "unit": to_u,
                    "formula": f"{value} {from_u} = {num_val} {to_u}",
                    "method": "deterministic_pint_units"
                }
            except pint.DimensionalityError:
                raise DimensionError(f"Cannot convert '{from_u}' to '{to_u}' (incompatible physical dimensions).")
            except Exception as e:
                raise OceanMathException(f"Unit conversion failed: {str(e)}")

        # Fallback dictionary check
        for category, units in cls.FACTORS.items():
            if from_u in units:
                if to_u not in units:
                    raise DimensionError(f"Dimension error: '{from_u}' ({category}) cannot be converted to '{to_u}'.")
                
                # Standard conversion via base unit
                base_val = value * units[from_u]
                target_val = base_val / units[to_u]
                return {
                    "result": f"{round(target_val, 8)}",
                    "numeric_result": target_val,
                    "exact_result": f"{target_val}",
                    "unit": to_u,
                    "formula": f"{value} {from_u} = {target_val} {to_u}",
                    "method": "deterministic_standards_table"
                }

        raise OceanMathException(f"Unknown or unsupported unit: '{from_u}'")
