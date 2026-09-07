import React, { useState } from 'react';
import { PlayCircle, Cpu, RefreshCw, Zap } from 'lucide-react';

export default function LiveSimulatorControls({ onSelectPreset }) {
  const [activePreset, setActivePreset] = useState('normal');
  const [loading, setLoading] = useState(false);

  const presets = [
    { id: 'normal', name: 'Normal Baseline', desc: '60 WPM, 4h screen, 94% accuracy', badge: 'Stage 0' },
    { id: 'mild_fatigue', name: 'Mild Fatigue', desc: '51 WPM, 6.4h screen, 85% accuracy', badge: 'Stage 1' },
    { id: 'acute_stress', name: 'Stress & Sleep Shift', desc: '37 WPM, 8.8h screen, 71% accuracy, night usage', badge: 'Stage 2/3' },
    { id: 'recovery', name: 'Post-Activity Recovery', desc: '60 WPM, 4.3h screen, 93% accuracy', badge: 'Stage 0' }
  ];

  const handleTrigger = async (presetId) => {
    setActivePreset(presetId);
    setLoading(true);
    await onSelectPreset(presetId);
    setLoading(false);
  };

  return (
    <div className="glass-card" style={{ border: '1px solid var(--primary-glow)', background: 'rgba(6, 182, 212, 0.04)' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Cpu size={22} color="var(--primary)" />
          <div>
            <h3 style={{ fontSize: '1.05rem', color: '#fff', fontWeight: 700 }}>
              LIVE DEVICE SIMULATOR & TELEMETRY INJECTOR
            </h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
              Simulate sensor & digital activity states to observe real-time ML score updates
            </p>
          </div>
        </div>
        {loading && <RefreshCw className="animate-spin" size={18} color="var(--primary)" />}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '12px' }}>
        {presets.map(p => {
          const isActive = activePreset === p.id;
          return (
            <button
              key={p.id}
              onClick={() => handleTrigger(p.id)}
              disabled={loading}
              style={{
                textAlign: 'left',
                padding: '14px',
                borderRadius: '12px',
                background: isActive ? 'rgba(6, 182, 212, 0.15)' : 'rgba(255, 255, 255, 0.03)',
                border: isActive ? '1px solid var(--primary)' : '1px solid var(--border-glass)',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                gap: '8px'
              }}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                  <span style={{ fontSize: '0.95rem', fontWeight: 700, color: '#ffffff' }}>{p.name}</span>
                  <span style={{ fontSize: '0.7rem', padding: '2px 6px', borderRadius: '4px', background: 'rgba(255, 255, 255, 0.1)', color: 'var(--text-muted)' }}>
                    {p.badge}
                  </span>
                </div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-dim)' }}>
                  {p.desc}
                </div>
              </div>
              
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.8rem', color: isActive ? 'var(--primary)' : 'var(--text-muted)', fontWeight: 600, marginTop: '6px' }}>
                <Zap size={14} />
                <span>{isActive ? 'Active State' : 'Inject Telemetry'}</span>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}
