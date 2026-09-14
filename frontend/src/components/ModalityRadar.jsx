import React from 'react';
import { Layers, Keyboard, Smartphone, Activity, Briefcase, Car } from 'lucide-react';

export default function ModalityRadar({ modalityScores = {} }) {
  const modalities = [
    { key: 'keyboard', label: 'Keyboard Dynamics', icon: Keyboard, color: '#0D9488', score: modalityScores.keyboard || 10 },
    { key: 'usage', label: 'Smartphone Usage', icon: Smartphone, color: '#2563EB', score: modalityScores.usage || 15 },
    { key: 'motion', label: 'Physical Motion', icon: Activity, color: '#7C3AED', score: modalityScores.motion || 10 },
    { key: 'work', label: 'Work Performance', icon: Briefcase, color: '#10B981', score: modalityScores.work || 12 },
    { key: 'mobility', label: 'Mobility & Driving', icon: Car, color: '#D97706', score: modalityScores.mobility || 5 }
  ];

  return (
    <div className="neo-card">
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '18px' }}>
        <div style={{
          padding: '8px',
          borderRadius: '10px',
          background: 'var(--bg-neo)',
          boxShadow: 'var(--neo-raised-sm)',
          display: 'flex'
        }}>
          <Layers size={18} color="var(--accent-violet)" />
        </div>
        <h3 style={{ fontSize: '1.05rem', color: 'var(--text-main)', fontWeight: 700 }}>
          MODALITY BREAKDOWN
        </h3>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {modalities.map(m => {
          const Icon = m.icon;
          const pct = Math.min(100, Math.max(0, m.score));
          
          return (
            <div key={m.key}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.85rem', marginBottom: '6px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-main)', fontWeight: 600 }}>
                  <Icon size={16} color={m.color} />
                  <span>{m.label}</span>
                </div>
                <span style={{ fontWeight: 700, color: pct > 45 ? m.color : 'var(--text-muted)' }}>
                  {pct.toFixed(0)} / 100
                </span>
              </div>
              
              {/* Sunken Inset Progress bar */}
              <div style={{
                height: '10px',
                width: '100%',
                background: 'var(--bg-neo)',
                borderRadius: '9999px',
                boxShadow: 'var(--neo-inset)',
                overflow: 'hidden',
                padding: '2px'
              }}>
                <div style={{
                  height: '100%',
                  width: `${pct}%`,
                  background: m.color,
                  borderRadius: '9999px',
                  boxShadow: `0 0 8px ${m.color}`,
                  transition: 'width 0.6s ease'
                }} />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
