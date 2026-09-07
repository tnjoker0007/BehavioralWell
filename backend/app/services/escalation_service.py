from typing import Dict, Any

class EscalationService:
    @staticmethod
    def get_escalation_resources(stage: int) -> Dict[str, Any]:
        return {
            "disclaimer": "BehavioralWell provides behavioral pattern analytics, not medical diagnoses. If you are experiencing distress, please reach out to professional healthcare resources.",
            "stage": stage,
            "escalation_recommended": stage >= 3,
            "resources": [
                {
                    "title": "National Crisis & Helpline",
                    "phone": "988 (USA/Canada) / 112 (EU/UK)",
                    "description": "24/7 Free and confidential emotional support line.",
                    "type": "crisis"
                },
                {
                    "title": "Student & Employee Wellness Counseling",
                    "description": "Connect with an authorized institutional counselor or therapist.",
                    "type": "counseling"
                },
                {
                    "title": "Consultant Data Sharing Report",
                    "description": "Generate an anonymized, consent-protected behavioral trend summary to share with your personal healthcare professional.",
                    "type": "shareable_report"
                }
            ]
        }
