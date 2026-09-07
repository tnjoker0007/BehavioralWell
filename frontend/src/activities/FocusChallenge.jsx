import React, { useState, useEffect } from 'react';
import { Target, CheckCircle2 } from 'lucide-react';

export default function FocusChallenge({ onComplete }) {
  const [targetNumber, setTargetNumber] = useState(1);
  const [grid, setGrid] = useState([]);
  const [completed, setCompleted] = useState(false);
  const [startTime, setStartTime] = useState(null);

  const initGrid = () => {
    const numbers = Array.from({ length: 16 }, (_, i) => i + 1);
    numbers.sort(() => Math.random() - 0.5);
    setGrid(numbers);
    setTargetNumber(1);
    setCompleted(false);
    setStartTime(Date.now());
  };

  useEffect(() => {
    initGrid();
  }, []);

  const handleGridClick = (num) => {
    if (num === targetNumber) {
      if (targetNumber === 16) {
        setCompleted(true);
        const elapsed = (Date.now() - startTime) / 1000;
        if (onComplete) onComplete({ activity: 'focus', completion_time_sec: elapsed });
      } else {
        setTargetNumber(prev => prev + 1);
      }
    }
  };

  return (
    <div className="glass-card" style={{ textAlign: 'center', padding: '32px 24px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', color: 'var(--secondary)', marginBottom: '8px' }}>
        <Target size={24} />
        <h3 style={{ fontSize: '1.25rem', color: '#fff', fontWeight: 700 }}>Visual Focus Challenge</h3>
      </div>
      <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginBottom: '20px' }}>
        Tap numbers in ascending order from 1 to 16 to recalibrate visual concentration.
      </p>

      {!completed ? (
        <div>
          <div style={{ fontSize: '1rem', color: 'var(--primary)', fontWeight: 600, marginBottom: '16px' }}>
            FIND NUMBER: <span style={{ fontSize: '1.5rem', fontWeight: 800 }}>{targetNumber}</span>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '10px', maxWidth: '320px', margin: '0 auto 24px' }}>
            {grid.map((num) => (
              <button
                key={num}
                onClick={() => handleGridClick(num)}
                style={{
                  height: '60px',
                  borderRadius: '12px',
                  background: num < targetNumber ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                  border: num < targetNumber ? '1px solid #10b981' : '1px solid var(--border-glass)',
                  color: num < targetNumber ? '#10b981' : '#ffffff',
                  fontSize: '1.2rem',
                  fontWeight: 700,
                  cursor: 'pointer',
                  transition: 'all 0.2s ease'
                }}
              >
                {num}
              </button>
            ))}
          </div>
        </div>
      ) : (
        <div style={{ padding: '30px 0' }}>
          <CheckCircle2 size={48} color="var(--secondary)" style={{ margin: '0 auto 12px' }} />
          <h4 style={{ fontSize: '1.2rem', color: '#fff', fontWeight: 700 }}>Focus Grid Completed!</h4>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginTop: '6px' }}>
            Visual attention baseline recalibrated.
          </p>
          <button className="btn btn-secondary" onClick={initGrid} style={{ marginTop: '16px' }}>
            Play Again
          </button>
        </div>
      )}
    </div>
  );
}
