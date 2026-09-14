import numpy as np
from typing import List, Dict, Tuple
from sqlalchemy.orm import Session
from app.models.domain import RiskAssessment, BehavioralTelemetry

class TemporalEngine:
    @staticmethod
    def evaluate_persistence(db: Session, user_id: str, current_z_scores: Dict[str, float]) -> Tuple[int, str, float, float, float]:
        """
        Evaluates temporal persistence over 3-7 calendar day rolling windows.
        Prevents multiple events submitted on the same day from falsely inflating persistence days.

        Returns:
        - persistence_days (int): Consecutive calendar days with active behavioral deviation
        - trend (str): 'increasing', 'decreasing', or 'stable'
        - trend_slope (float): Linear trend slope across recent evaluations
        - rolling_3d_avg (float): 3-day average risk score
        - rolling_7d_avg (float): 7-day average risk score
        """
        recent_assessments = db.query(RiskAssessment).filter(
            RiskAssessment.user_id == user_id
        ).order_by(RiskAssessment.timestamp.desc()).limit(100).all()

        if not recent_assessments:
            current_avg_z = float(np.mean([abs(z) for z in current_z_scores.values()])) if current_z_scores else 0.0
            p_days = 1 if current_avg_z >= 1.2 else 0
            return p_days, "stable", 0.0, 0.0, 0.0

        # Group assessments by unique calendar date (most recent score per calendar day)
        daily_scores = []
        seen_dates = set()

        for r in recent_assessments:
            c_date = r.timestamp.date()
            if c_date not in seen_dates:
                seen_dates.add(c_date)
                daily_scores.append((c_date, r.risk_score))
            if len(daily_scores) >= 7:
                break

        scores = [score for _, score in daily_scores]
        
        # Calculate 3-day and 7-day rolling averages across calendar days
        rolling_3d_avg = float(np.mean(scores[:3])) if len(scores) >= 1 else 0.0
        rolling_7d_avg = float(np.mean(scores)) if len(scores) >= 1 else 0.0

        # Calculate consecutive elevated calendar days (score >= 30 or avg Z >= 1.0)
        consecutive_days = 0
        for _, score in daily_scores:
            if score >= 30.0:
                consecutive_days += 1
            else:
                break

        # Check current day Z-scores
        avg_concerning_z = float(np.mean([max(0.0, z) for z in current_z_scores.values()])) if current_z_scores else 0.0
        if avg_concerning_z >= 1.0:
            consecutive_days = max(1, consecutive_days)
        else:
            if daily_scores and daily_scores[0][1] < 30.0:
                consecutive_days = 0

        # Calculate trend slope using linear regression if enough days available
        if len(scores) >= 3:
            y = np.array(scores[::-1]) # Chronological order
            x = np.arange(len(y))
            slope = float(np.polyfit(x, y, 1)[0])
        else:
            slope = 0.0

        if slope > 3.0:
            trend = "increasing"
        elif slope < -3.0:
            trend = "decreasing"
        else:
            trend = "stable"

        return consecutive_days, trend, round(slope, 2), round(rolling_3d_avg, 1), round(rolling_7d_avg, 1)
