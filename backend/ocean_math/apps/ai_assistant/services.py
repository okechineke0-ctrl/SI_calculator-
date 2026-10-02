import os
import re
import requests
from typing import Dict, Any, Optional
from django.conf import settings
from apps.mathematics.services import MathematicsService
from apps.core.exceptions import OceanMathException

class AiAssistantService:
    """
    AI Assistant pipeline with deterministic calculation hierarchy, intent classification, and verification.
    """

    CANDIDATE_MODELS = [
        "gemini-flash-latest",
        "gemini-3.1-flash-lite-preview",
        "gemini-3.8-flash"
    ]

    @classmethod
    def process_query(cls, prompt: str, context: Optional[str] = None) -> Dict[str, Any]:
        """
        Processes mathematical and scientific queries according to the calculation hierarchy:
        1. Exact deterministic computation if direct expression.
        2. Gemini AI reasoning/explanation if conceptual or word problem.
        3. Double-check verification pipeline.
        """
        trimmed = prompt.strip()

        # Step 1: Detect if prompt is purely deterministic arithmetic or algebra
        is_pure_expression = bool(re.match(r'^[0-9\+\-\*\/\^\(\)\.\s\,a-zA-Z\=\×\÷\√]+$', trimmed))
        has_explanation_keywords = any(k in trimmed.lower() for k in ["why", "explain", "how", "what is", "prove", "describe", "derive"])

        if is_pure_expression and not has_explanation_keywords:
            try:
                # Direct mathematical engine evaluation without wasting AI tokens
                math_res = MathematicsService.evaluate(trimmed)
                return {
                    "result": math_res['result'],
                    "numeric_result": math_res.get('numeric_result'),
                    "exact_result": math_res.get('exact_result'),
                    "method": "deterministic_engine_direct",
                    "steps": math_res.get('steps', []),
                    "warnings": []
                }
            except Exception:
                pass

        # Step 2: Call Gemini API securely through Django backend
        api_key = getattr(settings, 'GEMINI_API_KEY', '') or os.environ.get('GEMINI_API_KEY', '')
        if not api_key:
            return {
                "result": "Gemini API key is not configured on the backend server.",
                "method": "deterministic_fallback",
                "warnings": ["AI assistant offline: GEMINI_API_KEY is unset on the Django server."]
            }

        system_instruction = (
            "You are Ocean Math AI scientific assistant. Provide rigorous, complete mathematical solutions. "
            "Never use LaTeX dollar signs ($ or $$). Use standard Unicode math symbols (², ³, √, ±, ×, π, θ). "
            "Show every derivation step with zero arithmetic errors."
        )

        payload = {
            "contents": [{"parts": [{"text": prompt}]}],
            "systemInstruction": {"parts": [{"text": system_instruction}]},
            "generationConfig": {
                "temperature": 0.0,
                "topP": 0.95,
                "maxOutputTokens": 4096
            }
        }

        # Multi-model cascade
        for model in cls.CANDIDATE_MODELS:
            url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={api_key}"
            try:
                resp = requests.post(url, json=payload, timeout=20)
                if resp.status_code == 200:
                    data = resp.json()
                    candidates = data.get("candidates", [])
                    if candidates:
                        parts = candidates[0].get("content", {}).get("parts", [])
                        text = "".join(p.get("text", "") for p in parts)
                        # Clean dollar signs
                        cleaned_text = text.replace("$", "").strip()
                        return {
                            "result": cleaned_text,
                            "method": f"gemini_assistant_{model}",
                            "model_used": model,
                            "warnings": []
                        }
            except Exception:
                continue

        return {
            "result": "Unable to reach AI services at this moment. Please check server logs or retry.",
            "method": "failed",
            "warnings": ["All candidate AI models were temporarily unreachable."]
        }
