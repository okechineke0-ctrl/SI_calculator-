from typing import Dict, Any, List
from apps.core.exceptions import OceanMathException

class ComputerScienceService:
    """
    Deterministic programmer calculator and computer science engine:
    Base conversions, bitwise arithmetic, Two's complement, ASCII decoding, and truth tables.
    """

    @staticmethod
    def base_convert(value: str, from_base: int, to_base: int) -> Dict[str, Any]:
        """
        Converts between base-2 to base-36.
        """
        try:
            # Parse integer in source base
            decimal_val = int(value.strip(), from_base)
            
            # Format to target base
            if to_base == 2:
                target_str = bin(decimal_val)[2:]
            elif to_base == 8:
                target_str = oct(decimal_val)[2:]
            elif to_base == 10:
                target_str = str(decimal_val)
            elif to_base == 16:
                target_str = hex(decimal_val)[2:].upper()
            else:
                # Custom base converter
                digits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                n = abs(decimal_val)
                res = []
                while n:
                    res.append(digits[n % to_base])
                    n //= to_base
                target_str = "".join(reversed(res)) if res else "0"
                if decimal_val < 0:
                    target_str = "-" + target_str

            return {
                "result": target_str,
                "decimal_value": decimal_val,
                "binary": bin(decimal_val)[2:],
                "octal": oct(decimal_val)[2:],
                "hexadecimal": hex(decimal_val)[2:].upper(),
                "method": "deterministic_base_conversion"
            }
        except Exception as e:
            raise OceanMathException(f"Base conversion failed: {str(e)}")

    @staticmethod
    def bitwise_operation(a: int, b: int, operation: str, bit_width: int = 32) -> Dict[str, Any]:
        """
        Performs bitwise logic and arithmetic with signed/unsigned wrap.
        """
        mask = (1 << bit_width) - 1

        op_lower = operation.lower()
        if op_lower == "and":
            val = (a & b) & mask
        elif op_lower == "or":
            val = (a | b) & mask
        elif op_lower == "xor":
            val = (a ^ b) & mask
        elif op_lower == "not":
            val = (~a) & mask
        elif op_lower == "nand":
            val = (~(a & b)) & mask
        elif op_lower == "nor":
            val = (~(a | b)) & mask
        elif op_lower == "shl":
            val = (a << b) & mask
        elif op_lower == "shr":
            val = (a >> b) & mask
        else:
            raise OceanMathException(f"Unsupported bitwise operation: {operation}")

        # Formatted representations
        bin_str = bin(val)[2:].zfill(bit_width)
        hex_str = hex(val)[2:].upper().zfill(bit_width // 4)

        # Signed two's complement interpretation
        is_negative = bool(val & (1 << (bit_width - 1)))
        signed_val = val - (1 << bit_width) if is_negative else val

        return {
            "result": str(val),
            "unsigned_decimal": val,
            "signed_decimal": signed_val,
            "binary": bin_str,
            "hexadecimal": hex_str,
            "bit_width": bit_width,
            "method": "deterministic_bitwise"
        }

    @staticmethod
    def ascii_inspect(text: str) -> Dict[str, Any]:
        """
        Character <-> ASCII decimal, binary, octal, hex conversion.
        """
        characters = []
        for ch in text:
            code = ord(ch)
            characters.append({
                "char": ch,
                "decimal": code,
                "binary": bin(code)[2:].zfill(8),
                "octal": oct(code)[2:].zfill(3),
                "hex": hex(code)[2:].upper().zfill(2)
            })

        return {
            "result": f"{len(text)} characters analyzed",
            "characters": characters,
            "method": "deterministic_ascii"
        }

    @staticmethod
    def generate_truth_table(variables: List[str], expression_str: str) -> Dict[str, Any]:
        """
        Generates complete Boolean truth table for logical expression (e.g. A and not B).
        """
        num_vars = len(variables)
        if num_vars > 5:
            raise OceanMathException("Truth table generator supports up to 5 variables.")

        rows = []
        import itertools
        for combo in itertools.product([False, True], repeat=num_vars):
            env = dict(zip(variables, combo))
            # Safe eval of boolean expression
            try:
                # Replace logic operators with python
                clean_expr = expression_str.lower().replace("∧", " and ").replace("∨", " or ").replace("¬", " not ")
                eval_res = bool(eval(clean_expr, {"__builtins__": None}, env))
                row_data = {var: int(env[var]) for var in variables}
                row_data["output"] = int(eval_res)
                rows.append(row_data)
            except Exception as e:
                raise OceanMathException(f"Failed to evaluate boolean logic: {str(e)}")

        return {
            "result": f"{len(rows)} truth table rows generated",
            "variables": variables,
            "expression": expression_str,
            "truth_table": rows,
            "method": "deterministic_boolean_table"
        }
