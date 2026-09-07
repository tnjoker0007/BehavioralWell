import React, { useState, useEffect } from 'react';
import { Smartphone, Play, Pause, CheckCircle } from 'lucide-react';

export default function DigitalDetoxTimer({ onComplete }) {
  const [seconds, setSeconds] = useState(600); // 10 minutes
  const [active, setActive] = useState(false);
  const [completed, setCompleted] = useState(false);

  useEffect(() => {
    let timer = null;
    if (active && seconds > 0) {
      timer = setInterval(() => setSeconds(prev => prev - 1), 1000);
    } else if (seconds === 0) {
      setActive(false);
      setCompleted(true);
      if (onComplete) onComplete({ activity: 'detox', duration_minutes: 10 });
    }
    return () => clearInterval(timer);
  }, [active, seconds]);

  const formatTime = (sec) => {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div className="glass-card" style={{ textAlign: 'center', padding: '32px 24px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', color: 'var(--accent-amber)', marginBottom: '8px' }}>
        <Smartphone size={24} />
        <h3 style={{ fontSize: '1.25rem', color: '#fff', fontWeight: 700 }}>10-Minute Digital Detox Break</h3>
      </div>
      <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginBottom: '24px' }}>
        Step away from screen notifications to reduce digital stimulation and night usage stress.
      </p>

      <div style={{
        fontSize: '3.5rem',
        fontFamily: 'var(--font-heading)',
        fontWeight: 800,
        color: active ? 'var(--accent-amber)' : '#ffffff',
        marginBottom: '24px'
      }}>
        {formatTime(seconds)}
      </div>

      <div style={{ display: 'flex', justifyContent: 'center', gap: '12px' }}>
        {!completed ? (
          <button className="btn btn-primary" onClick={() => setActive(!active)}>
            {active ? <Pause size={18} /> : <Play size={18} />}
            <span>{active ? 'Pause Break' : 'Start Detox Break'}</span>
          </button>
        ) : (
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--secondary)', fontWeight: 600 }}>
            <CheckCircle size={20} />
            <span>Detox Break Complete!</span>
          </div>
        )}
      </div>
    </div>
  );
}
