import React from 'react';
import { Info, TrendingUp, TrendingDown, AlertCircle } from 'lucide-react';

export default function ExplainabilityCards({ topContributors = [] }) {
  if (!topContributors || topContributors.length === 0) {
    return (
      <div className="glass-card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
          <Info size={20} color="var(--secondary)" />
          <h3 style={{ fontSize: '1.05rem', color: '#fff', fontWeight: 700 }}>
            WHAT CHANGED?
          </h3>
        </div>
        <div style={{
          padding: '16px',
          borderRadius: '12px',
          background: 'rgba(16, 185, 129, 0.08)',
          border: '1px solid rgba(16, 185, 129, 0.2)',
          fontSize: '0.9rem',
          color: 'var(--secondary)',
          display: 'flex',
          alignItems: 'center',
          gap: '10px'
        }}>
          <span>✅ Your recent behavioral modalities remain closely aligned with your personal baseline.</span>
        </div>
      </div>
    );
  }

  return (
    <div className="glass-card">
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
        <AlertCircle size={20} color="var(--primary)" />
        <h3 style={{ fontSize: '1.05rem', color: '#fff', fontWeight: 700 }}>
          WHAT CHANGED? (EXPLAINABLE ATTRIBUTION)
        </h3>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        {topContributors.map((factor, idx) => (
          <div key={idx} style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '12px 16px',
            borderRadius: '12px',
            background: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid var(--border-glass)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{
                width: '32px',
                height: '32px',
                borderRadius: '8px',
                background: factor.direction === 'elevated' ? 'rgba(244, 63, 94, 0.15)' : 'rgba(6, 182, 212, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: factor.direction === 'elevated' ? '#f43f5e' : '#06b6d4'
              }}>
                {factor.direction === 'elevated' ? <TrendingUp size={18} /> : <TrendingDown size={18} />}
              </div>
              <div>
                <div style={{ fontSize: '0.9rem', color: '#ffffff', fontWeight: 600 }}>
                  {factor.human_explanation}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', textTransform: 'capitalize' }}>
                  Modality: {factor.modality} • Z-Score: {factor.z_score > 0 ? `+${factor.z_score}` : factor.z_score}
                </div>
              </div>
            </div>
            <span style={{
              fontSize: '0.75rem',
              fontWeight: 600,
              padding: '4px 10px',
              borderRadius: '6px',
              background: 'rgba(255, 255, 255, 0.06)',
              color: 'var(--text-muted)'
            }}>
              {factor.direction.toUpperCase()}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
