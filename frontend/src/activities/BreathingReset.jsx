import React, { useState, useEffect } from 'react';
import { Wind, Play, Pause, RotateCcw, CheckCircle } from 'lucide-react';

export default function BreathingReset({ onComplete }) {
  const [active, setActive] = useState(false);
  const [phase, setPhase] = useState('Inhale'); // Inhale (4s), Hold (7s), Exhale (8s)
  const [secondsLeft, setSecondsLeft] = useState(120);
  const [completed, setCompleted] = useState(false);

  useEffect(() => {
    let timer = null;
    if (active && secondsLeft > 0) {
      timer = setInterval(() => {
        setSecondsLeft(prev => prev - 1);
      }, 1000);
    } else if (secondsLeft === 0) {
      setActive(false);
      setCompleted(true);
      if (onComplete) onComplete({ activity: 'breathing', duration_seconds: 120 });
    }
    return () => clearInterval(timer);
  }, [active, secondsLeft]);

  // Breathing cycle phase sync
  useEffect(() => {
    if (!active) return;
    const cycleTimer = setInterval(() => {
      const elapsed = 120 - secondsLeft;
      const cycle = elapsed % 19;
      if (cycle < 4) setPhase('Inhale');
      else if (cycle < 11) setPhase('Hold');
      else setPhase('Exhale');
    }, 1000);
    return () => clearInterval(cycleTimer);
  }, [active, secondsLeft]);

  const toggleTimer = () => setActive(!active);
  const resetTimer = () => {
    setActive(false);
    setSecondsLeft(120);
    setPhase('Inhale');
    setCompleted(false);
  };

  const formatTime = (sec) => {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div className="glass-card" style={{ textAlign: 'center', padding: '32px 24px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', color: 'var(--primary)', marginBottom: '8px' }}>
        <Wind size={24} />
        <h3 style={{ fontSize: '1.25rem', color: '#fff', fontWeight: 700 }}>2-Minute Breathing Reset</h3>
      </div>
      <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginBottom: '24px' }}>
        4-7-8 Rhythmic Breathing to reduce physiological tension and stabilize heart rate variability.
      </p>

      {/* Breathing Ring Animation */}
      <div style={{ position: 'relative', width: '180px', height: '180px', margin: '0 auto 24px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div className={active ? 'animate-breathe' : ''} style={{
          position: 'absolute',
          width: '160px',
          height: '160px',
          borderRadius: '50%',
          background: phase === 'Inhale' ? 'rgba(6, 182, 212, 0.25)' : phase === 'Hold' ? 'rgba(245, 158, 11, 0.25)' : 'rgba(16, 185, 129, 0.25)',
          border: `2px solid ${phase === 'Inhale' ? '#06b6d4' : phase === 'Hold' ? '#f59e0b' : '#10b981'}`,
          boxShadow: '0 0 30px rgba(6, 182, 212, 0.3)',
          transition: 'all 0.5s ease'
        }} />

        <div style={{ zIndex: 2, textAlign: 'center' }}>
          <div style={{ fontSize: '1.4rem', fontFamily: 'var(--font-heading)', fontWeight: 800, color: '#fff' }}>
            {active ? phase : completed ? 'Complete!' : 'Ready'}
          </div>
          <div style={{ fontSize: '1rem', color: 'var(--text-muted)', marginTop: '4px' }}>
            {formatTime(secondsLeft)}
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'center', gap: '12px' }}>
        {!completed ? (
          <button className="btn btn-primary" onClick={toggleTimer}>
            {active ? <Pause size={18} /> : <Play size={18} />}
            <span>{active ? 'Pause' : 'Start Reset'}</span>
          </button>
        ) : (
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--secondary)', fontWeight: 600 }}>
            <CheckCircle size={20} />
            <span>Reset Completed!</span>
          </div>
        )}
        <button className="btn btn-secondary" onClick={resetTimer}>
          <RotateCcw size={18} />
          <span>Reset</span>
        </button>
      </div>
    </div>
  );
}
