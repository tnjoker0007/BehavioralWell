# BehavioralWell — Personalized Behavioral Intelligence Engine Architecture

## Disclaimer & Core Operating Principle
> **IMPORTANT**: BehavioralWell detects significant behavioral deviation relative to an individual's own historical baseline. **It is not a medical diagnostic system**, does not diagnose mental illness or drug use, and does not make clinical claims. All thresholds are engineering thresholds for non-diagnostic supportive interventions.

---

## 1. Pipeline Architecture

```text
    Raw Telemetry Ingestion (Android / Web)
                      │
                      ▼
   14–30 Day Personal Baseline Engine (User-Isolated)
                      │
                      ▼
     Directional Feature Normalization (Z-Score)
                      │
                      ▼
    Temporal Persistence Engine (3–7 Day Window)
                      │
                      ▼
   Multimodal Risk Fusion Layer (BaseRiskEngine Interface)
                      │
                      ▼
 Behavioral Deviation Stage (Stage 0–4) & Confidence Score
                      │
                      ▼
 Non-Diagnostic Explainability & Supportive Interventions
```

---

## 2. Progressive Personal Baseline Engine (`baseline_engine.py`)

Every user's historical telemetry data is isolated per `user_id`. Historical mean, standard deviation ($\sigma$), median, min, max, variability, and sample counts are calculated over a rolling 14–30 day window.

### Progressive Baseline Maturity Stages
- **`< 3 days` (< 5 samples)**: `insufficient` (Confidence 10%–40%, risk conservatively clamped to Stage 0–1).
- **`3–7 days` (5–14 samples)**: `early` (Confidence 40%–65%).
- **`7–14 days` (15–29 samples)**: `developing` (Confidence 65%–85%).
- **`14+ days` (30+ samples)**: `established` (Confidence 85%–98%).

---

## 3. Directional Z-Score Normalization

Z-scores evaluate how far current feature values deviate from the personal mean ($\mu$) using a safe standard deviation floor ($\sigma_{floor} = 1e-4$):

$$\text{Z} = \begin{cases} 
\frac{\mu - x}{\sigma} & \text{for } \text{lower\_is\_deviation (e.g. typing\_speed, task\_accuracy)} \\
\frac{x - \mu}{\sigma} & \text{for } \text{higher\_is\_deviation (e.g. night\_usage, screen\_time, correction\_rate)} \\
\frac{|x - \mu|}{\sigma} & \text{for } \text{bidirectional (e.g. movement\_intensity, speed\_variance)}
\end{cases}$$

Deviations below $0.6\sigma$ are treated as normal baseline noise and filtered out.

---

## 4. Temporal Persistence Engine (`temporal_engine.py`)

To prevent single-day transient spikes from triggering high-risk stages:
- **Single-Day Transient Spike ($D_{consecutive} = 1$)**: Scaled down by $0.75\times$.
- **Persistent Multi-Day Shift ($D_{consecutive} \ge 3$)**: Multiplied by $1.20\times$ persistence weight.
- **Rolling Trends**: Computes 3-day and 7-day rolling averages along with linear trend slope ($m$).

---

## 5. Multimodal Risk Fusion Layer (`ml_risk_engine.py`)

Defines a clean, modular `BaseRiskEngine` interface allowing seamless swapping/augmentation with ML models (Random Forest, XGBoost, Temporal Transformer) without altering API contracts or Android schemas.

### Behavioral Risk Stages
- **Stage 0 — Stable** (0–19)
- **Stage 1 — Early Deviation** (20–39)
- **Stage 2 — Persistent Behavioral Deviation** (40–59)
- **Stage 3 — Elevated Risk** (60–79)
- **Stage 4 — High Concern** (80–100)

### Cross-Modal Gating Rules
- **Stage 3** requires at least 2 active modalities showing elevated deviation.
- **Stage 4** REQUIRES multi-modal evidence AND multi-day persistence ($\ge 2$ days). An isolated single modality cannot trigger Stage 4 alone.

---

## 6. Non-Diagnostic Explainability (`explainability.py`)

All risk assessments output non-stigmatizing natural language explanations focused on pattern shifts relative to the user's personal baseline.

Example:
> *"Late-night screen activity is 2.3 std-dev above your personal baseline routine."*

---

## 7. Model-Ready Architecture

The `BaseRiskEngine` interface allows future ML model expansion:

```python
class BaseRiskEngine(ABC):
    @abstractmethod
    def evaluate_risk(self, z_scores, persistence_days, consent_flags, baseline_status, samples_count):
        pass
```
