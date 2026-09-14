import React from 'react';
import WellnessGauge from '../components/WellnessGauge';
import PersonalFingerprintCard from '../components/PersonalFingerprintCard';
import ModalityRadar from '../components/ModalityRadar';
import ExplainabilityCards from '../components/ExplainabilityCards';
import LiveSimulatorControls from '../components/LiveSimulatorControls';
import { HeartHandshake, ShieldAlert, ArrowRight, Sparkles } from 'lucide-react';

export default function UserDashboardView({
  riskData,
  baselineData,
  onInjectPreset,
  onNavigateTab,
  onOpenSelfCheck,
  currentUser
}) {
  const {
    risk_score = 15,
    stage = 0,
    stage_label = "Stage 0 — Stable",
    trend = "stable",
    persistence_days = 1,
    confidence = 92,
    modality_scores = {},
    top_contributors = []
  } = riskData || {};

  const userName = currentUser?.name ? currentUser.name.split(' ')[0] : 'Alex';

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Neomorphic Greeting Header */}
      <div className="neo-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
            <Sparkles size={20} color="var(--accent-violet)" />
            <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--text-main)' }}>
              Good morning, {userName}
            </h2>
          </div>
          <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', fontWeight: 500 }}>
            Your continuous non-invasive behavioral telemetry is active and operating smoothly.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={onOpenSelfCheck}>
            <HeartHandshake size={16} color="var(--accent-rose)" />
            Self Check-in
          </button>
          <button className="btn btn-primary" onClick={() => onNavigateTab('interventions')}>
            <span>Explore Interventions</span>
            <ArrowRight size={16} />
          </button>
        </div>
      </div>

      {/* Live Device Simulator Banner */}
      <LiveSimulatorControls onSelectPreset={onInjectPreset} />

      {/* Primary Dashboard Grid */}
      <div className="dashboard-grid">
        {/* Left Column: Gauge & Radar */}
        <div style={{ gridColumn: 'span 4', display: 'flex', flexDirection: 'column', gap: '24px' }}>
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
        <div style={{ gridColumn: 'span 8', display: 'flex', flexDirection: 'column', gap: '24px' }}>
          <PersonalFingerprintCard baselineFeatures={baselineData?.features || []} />
          <ExplainabilityCards topContributors={top_contributors} />

          {/* Quick Intervention CTA Card */}
          <div className="neo-card" style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '16px',
            flexWrap: 'wrap'
          }}>
            <div>
              <h4 style={{ fontSize: '1.1rem', color: 'var(--text-main)', fontWeight: 800 }}>
                {stage >= 2 ? 'Recommended Preventive Intervention' : 'Daily Behavioral Micro-Reset'}
              </h4>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '4px', fontWeight: 500 }}>
                {stage >= 2
                  ? 'Persistent multimodal deviations detected. Try a 2-minute breathing or focus reset.'
                  : 'Maintain your personal behavioral balance with gentle micro-resets.'}
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
            <div className="neo-card" style={{
              background: '#FFF5F5',
              border: '1px solid rgba(225, 29, 72, 0.3)',
              display: 'flex',
              alignItems: 'center',
              gap: '16px'
            }}>
              <ShieldAlert size={32} color="var(--accent-rose)" />
              <div>
                <h4 style={{ fontSize: '1rem', color: 'var(--accent-rose)', fontWeight: 800 }}>
                  Elevated Pattern Change — Support Recommended
                </h4>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '2px', fontWeight: 500 }}>
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
