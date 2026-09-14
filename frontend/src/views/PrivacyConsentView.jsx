import React, { useState } from 'react';
import { ShieldCheck, Lock, Trash2, CheckCircle2 } from 'lucide-react';

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
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '820px', margin: '0 auto' }}>
      <div className="neo-card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
          <div style={{ padding: '8px', borderRadius: '12px', background: 'var(--bg-neo)', boxShadow: 'var(--neo-raised-sm)', display: 'flex' }}>
            <ShieldCheck size={26} color="var(--accent-violet)" />
          </div>
          <div>
            <h2 style={{ fontSize: '1.4rem', color: 'var(--text-main)', fontWeight: 800 }}>
              YOUR DATA. YOUR CONTROL.
            </h2>
            <p style={{ fontSize: '0.85rem', color: 'var(--accent-violet)', fontWeight: 700, letterSpacing: '0.04em' }}>
              PRIVACY & DATA CONSENT HUB
            </p>
          </div>
        </div>
        <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginTop: '8px', fontWeight: 500 }}>
          You maintain full ownership of your behavioral data. Toggle modality permissions or wipe your stored data at any time. Zero raw text, keystrokes, or GPS location records are ever collected.
        </p>

        {message && (
          <div className="neo-card-inset" style={{
            marginTop: '16px',
            color: 'var(--accent-emerald)',
            fontSize: '0.85rem',
            fontWeight: 700,
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
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {categories.map(cat => {
          const enabled = consent[cat.key];
          return (
            <div key={cat.key} className="neo-card" style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: '16px'
            }}>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '14px' }}>
                <span style={{ fontSize: '1.5rem', marginTop: '2px' }}>{cat.icon}</span>
                <div>
                  <h4 style={{ fontSize: '1.05rem', color: 'var(--text-main)', fontWeight: 800 }}>
                    {cat.label}
                  </h4>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '2px', fontWeight: 500 }}>
                    {cat.desc}
                  </p>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.75rem', color: 'var(--accent-violet)', marginTop: '6px', fontWeight: 700 }}>
                    <Lock size={12} />
                    <span>{cat.safe}</span>
                  </div>
                </div>
              </div>

              {/* Neomorphic Tactile Toggle Switch */}
              <label className="neo-toggle">
                <input
                  type="checkbox"
                  checked={enabled}
                  onChange={() => togglePermission(cat.key)}
                />
                <span className="neo-toggle-slider" />
              </label>
            </div>
          );
        })}
      </div>

      {/* Danger Zone: Data Wipe */}
      <div className="neo-card" style={{ border: '1px solid rgba(225, 29, 72, 0.3)' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '16px', flexWrap: 'wrap' }}>
          <div>
            <h4 style={{ fontSize: '1.05rem', color: 'var(--accent-rose)', fontWeight: 800 }}>
              PERMANENT DATA DELETION REQUEST
            </h4>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '4px', fontWeight: 500 }}>
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
