import React, { useState } from 'react';
import { X, Heart, Smile, Meh, Frown, AlertCircle } from 'lucide-react';

export default function SelfCheckModal({ isOpen, onClose, onSubmit }) {
  const [mood, setMood] = useState('Okay');
  const [stress, setStress] = useState(2);
  const [note, setNote] = useState('');
  const [submitted, setSubmitted] = useState(false);

  if (!isOpen) return null;

  const moodOptions = [
    { label: 'Good', icon: Smile, color: '#10B981' },
    { label: 'Okay', icon: Smile, color: '#06B6D4' },
    { label: 'Neutral', icon: Meh, color: '#D97706' },
    { label: 'Low', icon: Frown, color: '#EA580C' },
    { label: 'Stressed', icon: AlertCircle, color: '#E11D48' }
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
      <div className="modal-card" style={{ position: 'relative' }}>
        <button
          onClick={onClose}
          className="btn btn-secondary"
          style={{
            position: 'absolute',
            top: '16px',
            right: '16px',
            padding: '6px'
          }}
        >
          <X size={18} color="var(--text-main)" />
        </button>

        {submitted ? (
          <div style={{ textAlign: 'center', padding: '30px 0' }}>
            <Heart size={48} color="var(--accent-rose)" style={{ margin: '0 auto 12px' }} />
            <h3 style={{ fontSize: '1.25rem', color: 'var(--text-main)', fontWeight: 800 }}>Check-in Logged</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500, marginTop: '6px' }}>
              Your self-reported context has been safely added to your behavioral model.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
              <div style={{ padding: '8px', borderRadius: '10px', background: 'var(--bg-neo)', boxShadow: 'var(--neo-raised-sm)', display: 'flex' }}>
                <Heart size={20} color="var(--accent-rose)" />
              </div>
              <h3 style={{ fontSize: '1.2rem', color: 'var(--text-main)', fontWeight: 800 }}>
                Interactive Self-Check
              </h3>
            </div>
            
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500, marginBottom: '20px' }}>
              How are you feeling right now? Your input helps personalize your baseline model.
            </p>

            {/* Mood selector */}
            <div style={{ marginBottom: '20px' }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-main)', fontWeight: 700, display: 'block', marginBottom: '10px' }}>
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
                      className="btn"
                      style={{
                        padding: '10px 4px',
                        flexDirection: 'column',
                        gap: '6px',
                        boxShadow: isSelected ? 'var(--neo-inset)' : 'var(--neo-raised-sm)',
                        color: isSelected ? 'var(--accent-violet)' : 'var(--text-muted)',
                        fontWeight: isSelected ? 800 : 600,
                        fontSize: '0.75rem'
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
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-main)', fontWeight: 700, marginBottom: '8px' }}>
                <span>STRESS LEVEL</span>
                <span style={{ color: 'var(--accent-violet)' }}>{stress} / 5</span>
              </div>
              <input
                type="range"
                min="1"
                max="5"
                value={stress}
                onChange={(e) => setStress(parseInt(e.target.value))}
                style={{ width: '100%', accentColor: 'var(--accent-violet)' }}
              />
            </div>

            {/* Optional note */}
            <div style={{ marginBottom: '24px' }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-main)', fontWeight: 700, display: 'block', marginBottom: '8px' }}>
                CONTEXT / NOTE (OPTIONAL)
              </label>
              <textarea
                className="neo-input"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="e.g. Preparing for major exams, travel fatigue, late work session..."
                rows={2}
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
