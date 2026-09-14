import React from 'react';
import { Fingerprint } from 'lucide-react';

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
    <div className="neo-card">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '18px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ padding: '8px', borderRadius: '10px', background: 'var(--bg-neo)', boxShadow: 'var(--neo-raised-sm)', display: 'flex' }}>
            <Fingerprint size={20} color="var(--accent-violet)" />
          </div>
          <div>
            <h3 style={{ fontSize: '1.05rem', color: 'var(--text-main)', fontWeight: 700 }}>
              PERSONAL BEHAVIORAL FINGERPRINT
            </h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 500 }}>
              Learned Baseline vs Current Activity (N=14 Baseline Days)
            </p>
          </div>
        </div>
        <span className="badge" style={{ color: 'var(--accent-violet)', borderColor: 'rgba(124, 58, 237, 0.3)' }}>
          BASELINE LEARNED
        </span>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '14px' }}>
        {displayMetrics.map(item => {
          const b = baselineMap[item.key];
          const meanVal = b ? b.mean.toFixed(1) : item.defaultMean;
          const stdVal = b ? b.std.toFixed(1) : (item.defaultMean * 0.1).toFixed(1);
          
          return (
            <div key={item.key} className="neo-card-inset" style={{ padding: '14px' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                <span style={{ fontSize: '1.1rem' }}>{item.icon}</span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                  ±{stdVal} {item.unit}
                </span>
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                {item.label}
              </div>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', marginTop: '4px' }}>
                <span style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--text-main)' }}>
                  {meanVal}
                </span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
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
