import React from 'react';
import { Layers, Keyboard, Smartphone, Activity, Briefcase, Car } from 'lucide-react';

export default function ModalityRadar({ modalityScores = {} }) {
  const modalities = [
    { key: 'keyboard', label: 'Keyboard Dynamics', icon: Keyboard, color: '#06b6d4', score: modalityScores.keyboard || 10 },
    { key: 'usage', label: 'Smartphone Usage', icon: Smartphone, color: '#3b82f6', score: modalityScores.usage || 15 },
    { key: 'motion', label: 'Physical Motion', icon: Activity, color: '#8b5cf6', score: modalityScores.motion || 10 },
    { key: 'work', label: 'Work Performance', icon: Briefcase, color: '#10b981', score: modalityScores.work || 12 },
    { key: 'mobility', label: 'Mobility & Driving', icon: Car, color: '#f59e0b', score: modalityScores.mobility || 5 }
  ];

  return (
    <div className="glass-card">
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
        <Layers size={20} color="var(--primary)" />
        <h3 style={{ fontSize: '1.05rem', color: '#fff', fontWeight: 700 }}>
          MULTIMODAL DEVIATION FUSION
        </h3>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {modalities.map(m => {
          const Icon = m.icon;
          const pct = Math.min(100, Math.max(0, m.score));
          
          return (
            <div key={m.key}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.85rem', marginBottom: '6px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-main)' }}>
                  <Icon size={16} color={m.color} />
                  <span>{m.label}</span>
                </div>
                <span style={{ fontWeight: 600, color: pct > 45 ? m.color : 'var(--text-muted)' }}>
                  {pct.toFixed(0)} / 100
                </span>
              </div>
              
              {/* Progress bar */}
              <div style={{
                height: '8px',
                width: '100%',
                background: 'rgba(255, 255, 255, 0.06)',
                borderRadius: '9999px',
                overflow: 'hidden'
              }}>
                <div style={{
                  height: '100%',
                  width: `${pct}%`,
                  background: m.color,
                  borderRadius: '9999px',
                  boxShadow: `0 0 10px ${m.color}`,
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
