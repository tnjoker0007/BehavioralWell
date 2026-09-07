import React, { useState } from 'react';
import { ShieldCheck, Lock, Trash2, CheckCircle2, AlertTriangle, EyeOff } from 'lucide-react';

export default function PrivacyConsentView({ consentData, onUpdateConsent, onDeleteData }) {
  const [consent, setConsent] = useState(consentData || {
    keyboard_enabled: true,
    usage_enabled: true,
    motion_enabled: true,
    work_enabled: true,
    mobility_enabled: true
  });

  const [message, setMessage] = useState('');

  const togglePermission = async (key) => {
    const next = { ...consent, [key]: !consent[key] };
    setConsent(next);
    await onUpdateConsent(next);
    setMessage('Consent permissions updated immediately.');
    setTimeout(() => setMessage(''), 3000);
  };

  const handleDeleteAll = async () => {
    if (window.confirm("Are you sure you want to permanently delete all your behavioral telemetry and personal baseline data? This action cannot be undone.")) {
      await onDeleteData();
      setMessage('All behavioral data and baseline records permanently deleted.');
    }
  };

  const categories = [
    { key: 'keyboard_enabled', label: 'Keyboard Dynamics', desc: 'Typing speed (WPM), inter-key pauses, dwell time, backspace correction rate.', icon: '⌨️', safe: 'NO typed text or keystroke content stored.' },
    { key: 'usage_enabled', label: 'Screen & App Usage', desc: 'Active screen time, phone unlock count, late-night usage hours, app category switching.', icon: '📱', safe: 'App categories only; no message or app content.' },
    { key: 'motion_enabled', label: 'Motion Sensors', desc: 'Accelerometer & gyroscope movement intensity, motion variance, sedentary duration.', icon: '🏃', safe: 'Aggregated physical movement intensity metrics.' },
    { key: 'work_enabled', label: 'Work Performance', desc: 'Explicitly permitted task completion time, accuracy rate, and error frequency.', icon: '🎯', safe: 'Productivity metrics only when integrated.' },
    { key: 'mobility_enabled', label: 'Mobility & Driving', desc: 'Travel speed variance, route consistency, and motion smoothness.', icon: '🚘', safe: 'Coarse movement trends without GPS track logs.' }
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '800px', margin: '0 auto' }}>
      <div className="glass-card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
          <ShieldCheck size={26} color="var(--primary)" />
          <h2 style={{ fontSize: '1.4rem', color: '#fff', fontWeight: 800 }}>
            PRIVACY & DATA CONSENT HUB
          </h2>
        </div>
        <p style={{ fontSize: '0.9rem', color: 'var(--text-dim)' }}>
          You maintain full ownership of your behavioral data. Toggle modality permissions or wipe your stored data at any time.
        </p>

        {message && (
          <div style={{
            marginTop: '16px',
            padding: '12px 16px',
            borderRadius: '10px',
            background: 'rgba(16, 185, 129, 0.15)',
            border: '1px solid rgba(16, 185, 129, 0.3)',
            color: '#10b981',
            fontSize: '0.85rem',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <CheckCircle2 size={16} />
            <span>{message}</span>
          </div>
        )}
      </div>

      {/* Permission Toggles */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {categories.map(cat => {
          const enabled = consent[cat.key];
          return (
            <div key={cat.key} className="glass-card" style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: '16px'
            }}>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '14px' }}>
                <span style={{ fontSize: '1.5rem', marginTop: '2px' }}>{cat.icon}</span>
                <div>
                  <h4 style={{ fontSize: '1.05rem', color: '#fff', fontWeight: 700 }}>
                    {cat.label}
                  </h4>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginTop: '2px' }}>
                    {cat.desc}
                  </p>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.75rem', color: 'var(--primary)', marginTop: '6px' }}>
                    <Lock size={12} />
                    <span>{cat.safe}</span>
                  </div>
                </div>
              </div>

              {/* Toggle Switch */}
              <button
                onClick={() => togglePermission(cat.key)}
                style={{
                  width: '56px',
                  height: '30px',
                  borderRadius: '9999px',
                  background: enabled ? 'var(--primary)' : 'rgba(255, 255, 255, 0.1)',
                  border: 'none',
                  position: 'relative',
                  cursor: 'pointer',
                  transition: 'background 0.3s ease',
                  flexShrink: 0
                }}
              >
                <div style={{
                  width: '24px',
                  height: '24px',
                  borderRadius: '50%',
                  background: '#ffffff',
                  position: 'absolute',
                  top: '3px',
                  left: enabled ? '29px' : '3px',
                  transition: 'left 0.3s ease'
                }} />
              </button>
            </div>
          );
        })}
      </div>

      {/* Danger Zone: Data Wipe */}
      <div className="glass-card" style={{ border: '1px solid rgba(244, 63, 94, 0.3)', background: 'rgba(244, 63, 94, 0.05)' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '16px', flexWrap: 'wrap' }}>
          <div>
            <h4 style={{ fontSize: '1.05rem', color: '#f43f5e', fontWeight: 700 }}>
              PERMANENT DATA DELETION REQUEST
            </h4>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginTop: '4px' }}>
              Purge all historical behavioral telemetry, baseline statistics, and risk logs permanently.
            </p>
          </div>
          <button className="btn btn-danger" onClick={handleDeleteAll}>
            <Trash2 size={16} />
            <span>Delete All My Data</span>
          </button>
        </div>
      </div>
    </div>
  );
}
