import React from 'react';
import WellnessGauge from '../components/WellnessGauge';
import PersonalFingerprintCard from '../components/PersonalFingerprintCard';
import ModalityRadar from '../components/ModalityRadar';
import ExplainabilityCards from '../components/ExplainabilityCards';
import LiveSimulatorControls from '../components/LiveSimulatorControls';
import { HeartHandshake, ShieldAlert, ArrowRight } from 'lucide-react';

export default function UserDashboardView({
  riskData,
  baselineData,
  onInjectPreset,
  onNavigateTab,
  onOpenSelfCheck
}) {
  const {
    risk_score = 15,
    stage = 0,
    stage_label = "Stage 0 — Stable",
    trend = "stable",
    persistence_days = 1,
    confidence = 0.92,
    modality_scores = {},
    top_contributors = []
  } = riskData || {};

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Live Device Simulator Banner */}
      <LiveSimulatorControls onSelectPreset={onInjectPreset} />

      {/* Primary Dashboard Grid */}
      <div className="dashboard-grid">
        {/* Left Column: Gauge & Radar */}
        <div style={{ gridColumn: 'span 4', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <WellnessGauge
            riskScore={risk_score}
            stage={stage}
            stageLabel={stage_label}
            trend={trend}
            persistenceDays={persistence_days}
            confidence={confidence}
          />
          <ModalityRadar modalityScores={modality_scores} />
        </div>

        {/* Right Column: Personal Fingerprint & Explainability */}
        <div style={{ gridColumn: 'span 8', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <PersonalFingerprintCard baselineFeatures={baselineData?.features || []} />
          <ExplainabilityCards topContributors={top_contributors} />

          {/* Quick Intervention CTA Card */}
          <div className="glass-card" style={{
            background: 'linear-gradient(135deg, rgba(6, 182, 212, 0.1) 0%, rgba(59, 130, 246, 0.1) 100%)',
            border: '1px solid rgba(6, 182, 212, 0.25)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '16px',
            flexWrap: 'wrap'
          }}>
            <div>
              <h4 style={{ fontSize: '1.1rem', color: '#fff', fontWeight: 700 }}>
                {stage >= 2 ? 'Recommended Preventive Intervention' : 'Daily Wellness Reset'}
              </h4>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginTop: '4px' }}>
                {stage >= 2
                  ? 'Persistent multimodal deviations detected. Try a 2-minute breathing or focus reset.'
                  : 'Maintain your personal behavioral balance with micro-resets.'}
              </p>
            </div>

            <div style={{ display: 'flex', gap: '10px' }}>
              <button className="btn btn-secondary" onClick={onOpenSelfCheck}>
                Self Check-in
              </button>
              <button className="btn btn-primary" onClick={() => onNavigateTab('interventions')}>
                <span>Start Activity</span>
                <ArrowRight size={16} />
              </button>
            </div>
          </div>

          {/* Crisis / Professional Support Banner for Stage 3 & 4 */}
          {stage >= 3 && (
            <div className="glass-card" style={{
              background: 'rgba(244, 63, 94, 0.1)',
              border: '1px solid rgba(244, 63, 94, 0.3)',
              display: 'flex',
              alignItems: 'center',
              gap: '16px'
            }}>
              <ShieldAlert size={32} color="#f43f5e" />
              <div>
                <h4 style={{ fontSize: '1rem', color: '#f43f5e', fontWeight: 700 }}>
                  Elevated Pattern Change — Support Recommended
                </h4>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                  BehavioralWell does not diagnose medical conditions. If you are experiencing distress, reach out to confidential crisis lines (988 / 112) or share an anonymized report with your healthcare counselor.
                </p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
