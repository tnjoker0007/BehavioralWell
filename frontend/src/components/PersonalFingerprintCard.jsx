import React from 'react';
import { Fingerprint, CheckCircle2, AlertTriangle, HelpCircle } from 'lucide-react';

export default function PersonalFingerprintCard({ baselineFeatures = [] }) {
  // Key fingerprint feature metrics
  const displayMetrics = [
    { key: 'typing_speed', label: 'Typing Speed', unit: 'WPM', defaultMean: 61, defaultCurrent: 60, icon: '⌨️' },
    { key: 'correction_rate', label: 'Backspace / Edits', unit: '%', defaultMean: 4.2, defaultCurrent: 4.5, icon: '⌫' },
    { key: 'screen_time', label: 'Screen Usage', unit: 'hrs/day', defaultMean: 4.1, defaultCurrent: 4.3, icon: '📱' },
    { key: 'night_usage', label: 'Late Night Screen', unit: 'hrs', defaultMean: 0.3, defaultCurrent: 0.2, icon: '🌙' },
    { key: 'task_accuracy', label: 'Work Accuracy', unit: '%', defaultMean: 94.5, defaultCurrent: 93.8, icon: '🎯' },
    { key: 'acceleration_variance', label: 'Motion Stability', unit: 'var', defaultMean: 0.08, defaultCurrent: 0.09, icon: '🏃' }
  ];

  const baselineMap = {};
  baselineFeatures.forEach(f => {
    baselineMap[f.feature_name] = f;
  });

  return (
    <div className="glass-card">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Fingerprint size={22} color="var(--primary)" />
          <div>
            <h3 style={{ fontSize: '1.05rem', color: '#fff', fontWeight: 700 }}>
              PERSONAL BEHAVIORAL FINGERPRINT
            </h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
              Learned Baseline vs Current Activity (N=14 Baseline Days)
            </p>
          </div>
        </div>
        <span style={{
          fontSize: '0.75rem',
          padding: '4px 10px',
          borderRadius: '9999px',
          background: 'rgba(6, 182, 212, 0.15)',
          color: 'var(--primary)',
          border: '1px solid rgba(6, 182, 212, 0.3)'
        }}>
          BASELINE LEARNED
        </span>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '12px' }}>
        {displayMetrics.map(item => {
          const b = baselineMap[item.key];
          const meanVal = b ? b.mean.toFixed(1) : item.defaultMean;
          const stdVal = b ? b.std.toFixed(1) : (item.defaultMean * 0.1).toFixed(1);
          
          return (
            <div key={item.key} style={{
              background: 'rgba(255, 255, 255, 0.03)',
              border: '1px solid var(--border-glass)',
              borderRadius: '12px',
              padding: '14px',
              transition: 'all 0.2s ease'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                <span style={{ fontSize: '1.1rem' }}>{item.icon}</span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', fontWeight: 500 }}>
                  ±{stdVal} {item.unit}
                </span>
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>
                {item.label}
              </div>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', marginTop: '4px' }}>
                <span style={{ fontSize: '1.25rem', fontWeight: 700, color: '#ffffff' }}>
                  {meanVal}
                </span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                  {item.unit} baseline
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
