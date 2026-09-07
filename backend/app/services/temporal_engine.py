import numpy as np
from typing import List, Dict, Tuple
from sqlalchemy.orm import Session
from app.models.domain import RiskAssessment, BehavioralTelemetry

class TemporalEngine:
    @staticmethod
    def evaluate_persistence(db: Session, user_id: str, current_z_scores: Dict[str, float]) -> Tuple[int, str]:
        """
        Evaluates temporal persistence over recent history (3-7 days).
        Returns: (persistence_days: int, trend: str)
        """
        recent_assessments = db.query(RiskAssessment).filter(
            RiskAssessment.user_id == user_id
        ).order_by(RiskAssessment.timestamp.desc()).limit(7).all()

        if not recent_assessments:
            return 1, "stable"

        recent_scores = [r.risk_score for r in recent_assessments]
        
        # Calculate persistence of elevated deviation
        elevated_days = 0
        for score in recent_scores:
            if score >= 35.0: # mild or higher threshold
                elevated_days += 1
            else:
                break
        
        # Current day counts if current z-score implies deviation
        avg_abs_z = np.mean([abs(z) for z in current_z_scores.values()]) if current_z_scores else 0
        if avg_abs_z >= 1.2:
            elevated_days = max(1, elevated_days + 1)
        else:
            elevated_days = 0

        # Determine trend
        if len(recent_scores) >= 3:
            recent_avg = np.mean(recent_scores[:3])
            older_avg = np.mean(recent_scores[3:]) if len(recent_scores) > 3 else recent_scores[-1]
            if recent_avg - older_avg > 10.0:
                trend = "increasing"
            elif older_avg - recent_avg > 10.0:
                trend = "decreasing"
            else:
                trend = "stable"
        else:
            trend = "stable"

        return elevated_days, trend
