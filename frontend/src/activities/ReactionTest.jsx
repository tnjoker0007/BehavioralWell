import React, { useState } from 'react';
import { Zap, RefreshCw, CheckCircle } from 'lucide-react';

export default function ReactionTest({ onComplete }) {
  const [gameState, setGameState] = useState('idle'); // idle, waiting, ready, result
  const [startTime, setStartTime] = useState(0);
  const [reactionTime, setReactionTime] = useState(null);
  const [attempts, setAttempts] = useState([]);

  const startTest = () => {
    setGameState('waiting');
    const delay = 2000 + Math.random() * 3000;
    setTimeout(() => {
      setGameState('ready');
      setStartTime(Date.now());
    }, delay);
  };

  const handleClick = () => {
    if (gameState === 'waiting') {
      alert('Too early! Wait for green color.');
      setGameState('idle');
    } else if (gameState === 'ready') {
      const elapsed = Date.now() - startTime;
      setReactionTime(elapsed);
      setAttempts(prev => [...prev, elapsed]);
      setGameState('result');
      if (onComplete) onComplete({ activity: 'reaction', reaction_time_ms: elapsed });
    }
  };

  return (
    <div className="glass-card" style={{ textAlign: 'center', padding: '32px 24px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', color: 'var(--accent-purple)', marginBottom: '8px' }}>
        <Zap size={24} />
        <h3 style={{ fontSize: '1.25rem', color: '#fff', fontWeight: 700 }}>Visual Reaction Speed Test</h3>
      </div>
      <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginBottom: '24px' }}>
        Click as fast as possible when the box turns GREEN to assess cognitive response latency.
      </p>

      <div
        onClick={handleClick}
        style={{
          width: '100%',
          maxWidth: '360px',
          height: '160px',
          margin: '0 auto 24px',
          borderRadius: '16px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: gameState === 'idle' ? 'default' : 'pointer',
          background: gameState === 'waiting' ? '#f59e0b' : gameState === 'ready' ? '#10b981' : 'rgba(255, 255, 255, 0.05)',
          border: '1px solid var(--border-glass)',
          transition: 'background 0.2s ease',
          boxShadow: gameState === 'ready' ? '0 0 30px #10b981' : 'none'
        }}
      >
        <div style={{ color: gameState === 'waiting' || gameState === 'ready' ? '#ffffff' : 'var(--text-muted)', fontWeight: 700, fontSize: '1.1rem' }}>
          {gameState === 'idle' && 'Click "Start Test" below'}
          {gameState === 'waiting' && 'Wait for Green...'}
          {gameState === 'ready' && 'TAP / CLICK NOW!'}
          {gameState === 'result' && `${reactionTime} ms`}
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'center', gap: '12px' }}>
        {gameState !== 'ready' && gameState !== 'waiting' && (
          <button className="btn btn-primary" onClick={startTest}>
            <Zap size={18} />
            <span>{gameState === 'result' ? 'Try Again' : 'Start Test'}</span>
          </button>
        )}
      </div>

      {attempts.length > 0 && (
        <div style={{ marginTop: '20px', fontSize: '0.85rem', color: 'var(--text-dim)' }}>
          Attempts Average: {Math.round(attempts.reduce((a, b) => a + b, 0) / attempts.length)} ms
        </div>
      )}
    </div>
  );
}
