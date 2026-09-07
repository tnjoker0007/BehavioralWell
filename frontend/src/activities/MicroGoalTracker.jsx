import React, { useState } from 'react';
import { CheckSquare, Check, Plus } from 'lucide-react';

export default function MicroGoalTracker({ onComplete }) {
  const [goals, setGoals] = useState([
    { id: 1, text: 'Drink 500ml water', completed: false },
    { id: 2, text: 'Take a 5-minute outdoor walk', completed: false },
    { id: 3, text: 'Complete one priority task without switching tabs', completed: false },
    { id: 4, text: 'Sleep by 11:00 PM tonight', completed: false }
  ]);

  const toggleGoal = (id) => {
    setGoals(prev => prev.map(g => {
      if (g.id === id) {
        const next = !g.completed;
        if (next && onComplete) onComplete({ activity: 'micro_goal', goal_id: id });
        return { ...g, completed: next };
      }
      return g;
    }));
  };

  return (
    <div className="glass-card" style={{ padding: '32px 24px' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--primary)', marginBottom: '8px' }}>
        <CheckSquare size={24} />
        <h3 style={{ fontSize: '1.25rem', color: '#fff', fontWeight: 700 }}>Daily Micro Goals</h3>
      </div>
      <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginBottom: '20px' }}>
        Small, achievable daily actions to reinforce healthy behavioral momentum without overwhelm.
      </p>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        {goals.map(g => (
          <div
            key={g.id}
            onClick={() => toggleGoal(g.id)}
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '14px 16px',
              borderRadius: '12px',
              background: g.completed ? 'rgba(16, 185, 129, 0.12)' : 'rgba(255, 255, 255, 0.03)',
              border: g.completed ? '1px solid rgba(16, 185, 129, 0.3)' : '1px solid var(--border-glass)',
              cursor: 'pointer',
              transition: 'all 0.2s ease'
            }}
          >
            <span style={{ fontSize: '0.9rem', color: g.completed ? '#10b981' : '#ffffff', textDecoration: g.completed ? 'line-through' : 'none' }}>
              {g.text}
            </span>
            <div style={{
              width: '24px',
              height: '24px',
              borderRadius: '6px',
              background: g.completed ? '#10b981' : 'rgba(255, 255, 255, 0.06)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff'
            }}>
              {g.completed && <Check size={16} />}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
