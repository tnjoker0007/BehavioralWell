import json
import requests
from typing import Dict, Any
from app.config import settings

class LLMInterpreter:
    @staticmethod
    def interpret_risk(assessment: Dict[str, Any]) -> str:
        """
        Generates a short, supportive, non-diagnostic natural-language explanation
        of the user's latest risk assessment via OpenRouter API.
        """
        api_key = settings.OPENROUTER_API_KEY
        if not api_key:
            return LLMInterpreter._get_fallback_interpretation(assessment)

        model = settings.OPENROUTER_MODEL or "anthropic/claude-sonnet-4.6"
        site_url = settings.SITE_URL or "http://localhost:8000"
        site_name = settings.SITE_NAME or "BehavioralWell"

        stage_label = assessment.get("stage_label", "Stage 0 — Stable")
        risk_score = assessment.get("risk_score", 0.0)
        confidence = assessment.get("confidence", 50.0)
        trend = assessment.get("trend", "stable")
        persistence_days = assessment.get("persistence_days", 0)

        contributors = assessment.get("top_contributors", [])
        contributor_lines = []
        if isinstance(contributors, list):
            for item in contributors:
                if isinstance(item, dict):
                    explanation = item.get("human_explanation") or item.get("explanation")
                    if explanation:
                        contributor_lines.append(f"- {explanation}")
                elif isinstance(item, str):
                    contributor_lines.append(f"- {item}")

        contributors_summary = "\n".join(contributor_lines) if contributor_lines else "No major feature shifts detected."

        system_prompt = (
            "You are an empathetic, supportive behavioral wellbeing assistant for BehavioralWell.\n"
            "Your task is to provide a brief (2 to 3 sentences max), supportive, non-diagnostic natural language summary "
            "of the user's current behavioral risk assessment and recent routine shifts.\n\n"
            "CRITICAL MANDATES:\n"
            "1. DO NOT diagnose any medical, clinical, or psychiatric condition (e.g. do NOT mention depression, anxiety, ADHD, or disorders).\n"
            "2. Use non-diagnostic, supportive, human-centered language focused on daily routines, rest, pacing, and wellness habits.\n"
            "3. Keep the tone calm, empowering, empathetic, and constructive.\n"
            "4. Keep your output concise (2-3 sentences maximum)."
        )

        user_prompt = (
            f"Assessment Summary:\n"
            f"- Status: {stage_label}\n"
            f"- Risk Index: {risk_score}/100\n"
            f"- Data Confidence: {confidence}%\n"
            f"- Recent Trend: {trend} (Persistent for {persistence_days} days)\n\n"
            f"Observed Routine Shifts:\n{contributors_summary}\n\n"
            f"Please provide a short, supportive, non-diagnostic explanation and gentle wellness guidance."
        )

        headers = {
            "Authorization": f"Bearer {api_key}",
            "HTTP-Referer": site_url,
            "X-Title": site_name,
            "Content-Type": "application/json"
        }

        payload = {
            "model": model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            "max_tokens": 250,
            "temperature": 0.4
        }

        try:
            response = requests.post(
                "https://openrouter.ai/api/v1/chat/completions",
                headers=headers,
                json=payload,
                timeout=12
            )
            if response.status_code == 200:
                data = response.json()
                choices = data.get("choices", [])
                if choices and len(choices) > 0:
                    text = choices[0].get("message", {}).get("content", "").strip()
                    if text:
                        return text
            print(f"[LLM INTERPRETER] OpenRouter returned status {response.status_code}: {response.text}")
        except Exception as e:
            print(f"[LLM INTERPRETER] Exception invoking OpenRouter: {e}")

        return LLMInterpreter._get_fallback_interpretation(assessment)

    @staticmethod
    def _get_fallback_interpretation(assessment: Dict[str, Any]) -> str:
        stage = assessment.get("stage", 0)
        if stage == 0:
            return "Your daily routines and behavioral patterns appear steady and balanced relative to your baseline."
        elif stage == 1:
            return "Recent activity shows minor variations from your normal routines. Simple rest and consistent breaks can help maintain balance."
        elif stage == 2:
            return "Noticeable shifts in your screen time or typing dynamics have persisted for a few days. Taking structured micro-breaks and prioritizing restful sleep is recommended."
        elif stage == 3:
            return "Multiple behavioral signals show elevated deviation from your baseline over several days. Consider reaching out to your support network or engaging in a reset intervention."
        else:
            return "Elevated behavioral pattern changes detected. Prioritize rest, reduce digital overload, and consider consulting a healthcare professional."
