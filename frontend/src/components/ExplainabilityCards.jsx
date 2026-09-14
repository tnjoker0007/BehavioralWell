import React from 'react';
import { Info, TrendingUp, TrendingDown, AlertCircle } from 'lucide-react';

export default function ExplainabilityCards({ topContributors = [] }) {
  if (!topContributors || topContributors.length === 0) {
    return (
      <div className="neo-card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '14px' }}>
          <div style={{ padding: '8px', borderRadius: '10px', background: 'var(--bg-neo)', boxShadow: 'var(--neo-raised-sm)', display: 'flex' }}>
            <Info size={18} color="var(--accent-emerald)" />
          </div>
          <h3 style={{ fontSize: '1.05rem', color: 'var(--text-main)', fontWeight: 700 }}>
            EXPLAINABILITY & PATTERNS
          </h3>
        </div>
        <div className="neo-card-inset" style={{ color: 'var(--accent-emerald)', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '10px', fontSize: '0.9rem' }}>
          <span>✅ Your recent behavioral modalities remain closely aligned with your personal baseline.</span>
        </div>
      </div>
    );
  }

  return (
    <div className="neo-card">
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
        <div style={{ padding: '8px', borderRadius: '10px', background: 'var(--bg-neo)', boxShadow: 'var(--neo-raised-sm)', display: 'flex' }}>
          <AlertCircle size={18} color="var(--accent-violet)" />
        </div>
        <h3 style={{ fontSize: '1.05rem', color: 'var(--text-main)', fontWeight: 700 }}>
          WHAT CHANGED? (EXPLAINABLE ATTRIBUTION)
        </h3>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {topContributors.map((factor, idx) => (
          <div key={idx} className="neo-card-inset" style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '12px 16px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '10px',
                background: 'var(--bg-neo)',
                boxShadow: 'var(--neo-raised-sm)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: factor.direction === 'elevated' ? 'var(--accent-rose)' : 'var(--accent-cyan)'
              }}>
                {factor.direction === 'elevated' ? <TrendingUp size={18} /> : <TrendingDown size={18} />}
              </div>
              <div>
                <div style={{ fontSize: '0.9rem', color: 'var(--text-main)', fontWeight: 700 }}>
                  {factor.human_explanation}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                  Modality: {factor.modality} • Z-Score: {factor.z_score > 0 ? `+${factor.z_score}` : factor.z_score}
                </div>
              </div>
            </div>
            <span style={{
              fontSize: '0.75rem',
              fontWeight: 700,
              padding: '4px 10px',
              borderRadius: '8px',
              background: 'var(--bg-neo)',
              boxShadow: 'var(--neo-raised-sm)',
              color: factor.direction === 'elevated' ? 'var(--accent-rose)' : 'var(--accent-violet)'
            }}>
              {factor.direction.toUpperCase()}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
