import React, { useState } from 'react';
import { X, Heart, Smile, Meh, Frown, AlertCircle } from 'lucide-react';

export default function SelfCheckModal({ isOpen, onClose, onSubmit }) {
  const [mood, setMood] = useState('Okay');
  const [stress, setStress] = useState(2);
  const [note, setNote] = useState('');
  const [submitted, setSubmitted] = useState(false);

  if (!isOpen) return null;

  const moodOptions = [
    { label: 'Good', icon: Smile, color: '#10b981' },
    { label: 'Okay', icon: Smile, color: '#06b6d4' },
    { label: 'Neutral', icon: Meh, color: '#f59e0b' },
    { label: 'Low', icon: Frown, color: '#f97316' },
    { label: 'Stressed', icon: AlertCircle, color: '#f43f5e' }
  ];

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ mood, stress_level: stress, note });
    setSubmitted(true);
    setTimeout(() => {
      setSubmitted(false);
      onClose();
    }, 1200);
  };

  return (
    <div className="modal-overlay">
      <div className="glass-card" style={{ maxWidth: '480px', width: '100%', position: 'relative' }}>
        <button
          onClick={onClose}
          style={{
            position: 'absolute',
            top: '16px',
            right: '16px',
            background: 'none',
            border: 'none',
            color: 'var(--text-muted)',
            cursor: 'pointer'
          }}
        >
          <X size={20} />
        </button>

        {submitted ? (
          <div style={{ textAlign: 'center', padding: '30px 0' }}>
            <Heart size={48} color="var(--primary)" style={{ margin: '0 auto 12px' }} />
            <h3 style={{ fontSize: '1.25rem', color: '#fff', fontWeight: 700 }}>Check-in Logged</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginTop: '6px' }}>
              Your self-reported context has been safely added to your behavioral model.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
              <Heart size={22} color="var(--primary)" />
              <h3 style={{ fontSize: '1.2rem', color: '#fff', fontWeight: 700 }}>
                Interactive Self-Check
              </h3>
            </div>
            
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '20px' }}>
              How are you feeling right now? Your input helps personalize your baseline model.
            </p>

            {/* Mood selector */}
            <div style={{ marginBottom: '20px' }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-main)', fontWeight: 600, display: 'block', marginBottom: '10px' }}>
                CURRENT MOOD
              </label>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '8px' }}>
                {moodOptions.map(m => {
                  const Icon = m.icon;
                  const isSelected = mood === m.label;
                  return (
                    <button
                      type="button"
                      key={m.label}
                      onClick={() => setMood(m.label)}
                      style={{
                        padding: '10px 4px',
                        borderRadius: '10px',
                        background: isSelected ? 'rgba(6, 182, 212, 0.2)' : 'rgba(255, 255, 255, 0.04)',
                        border: isSelected ? `1px solid ${m.color}` : '1px solid var(--border-glass)',
                        color: isSelected ? '#ffffff' : 'var(--text-muted)',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        gap: '6px',
                        cursor: 'pointer',
                        fontSize: '0.75rem',
                        fontWeight: 600
                      }}
                    >
                      <Icon size={20} color={m.color} />
                      <span>{m.label}</span>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Stress level */}
            <div style={{ marginBottom: '20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-main)', fontWeight: 600, marginBottom: '8px' }}>
                <span>STRESS LEVEL</span>
                <span style={{ color: 'var(--primary)' }}>{stress} / 5</span>
              </div>
              <input
                type="range"
                min="1"
                max="5"
                value={stress}
                onChange={(e) => setStress(parseInt(e.target.value))}
                style={{ width: '100%', accentColor: 'var(--primary)' }}
              />
            </div>

            {/* Optional note */}
            <div style={{ marginBottom: '24px' }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-main)', fontWeight: 600, display: 'block', marginBottom: '8px' }}>
                CONTEXT / NOTE (OPTIONAL)
              </label>
              <textarea
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="e.g. Preparing for major exams, travel fatigue, late work session..."
                rows={2}
                style={{
                  width: '100%',
                  padding: '10px',
                  borderRadius: '8px',
                  background: 'rgba(255, 255, 255, 0.04)',
                  border: '1px solid var(--border-glass)',
                  color: '#ffffff',
                  fontFamily: 'var(--font-body)',
                  fontSize: '0.85rem'
                }}
              />
            </div>

            <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
              Submit Check-in
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
