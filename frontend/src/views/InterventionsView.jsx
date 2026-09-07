import React, { useState } from 'react';
import BreathingReset from '../activities/BreathingReset';
import ReactionTest from '../activities/ReactionTest';
import FocusChallenge from '../activities/FocusChallenge';
import MicroGoalTracker from '../activities/MicroGoalTracker';
import DigitalDetoxTimer from '../activities/DigitalDetoxTimer';
import { HeartHandshake, Wind, Zap, Target, CheckSquare, Smartphone } from 'lucide-react';

export default function InterventionsView({ onLogActivity }) {
  const [activeActivity, setActiveActivity] = useState('breathing');

  const activities = [
    { id: 'breathing', label: 'Breathing Reset', icon: Wind, color: '#06b6d4' },
    { id: 'reaction', label: 'Reaction Speed', icon: Zap, color: '#8b5cf6' },
    { id: 'focus', label: 'Focus Challenge', icon: Target, color: '#10b981' },
    { id: 'micro_goal', label: 'Micro Goals', icon: CheckSquare, color: '#3b82f6' },
    { id: 'detox', label: 'Digital Detox', icon: Smartphone, color: '#f59e0b' }
  ];

  const handleActivityComplete = (resultData) => {
    if (onLogActivity) onLogActivity(activeActivity, resultData);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div className="glass-card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
          <HeartHandshake size={24} color="var(--primary)" />
          <h2 style={{ fontSize: '1.4rem', color: '#fff', fontWeight: 800 }}>
            INTERACTIVE PREVENTIVE INTERVENTIONS
          </h2>
        </div>
        <p style={{ fontSize: '0.9rem', color: 'var(--text-dim)' }}>
          Evidence-informed micro-activities tailored to recalibrate physiological stress, focus, and digital routines.
        </p>

        {/* Activity Selector Tabs */}
        <div style={{ display: 'flex', gap: '10px', marginTop: '20px', overflowX: 'auto', paddingBottom: '4px' }}>
          {activities.map(a => {
            const Icon = a.icon;
            const isSelected = activeActivity === a.id;
            return (
              <button
                key={a.id}
                onClick={() => setActiveActivity(a.id)}
                style={{
                  padding: '10px 16px',
                  borderRadius: '10px',
                  background: isSelected ? 'rgba(6, 182, 212, 0.15)' : 'rgba(255, 255, 255, 0.03)',
                  border: isSelected ? `1px solid ${a.color}` : '1px solid var(--border-glass)',
                  color: isSelected ? '#ffffff' : 'var(--text-muted)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  cursor: 'pointer',
                  fontSize: '0.9rem',
                  fontWeight: 600,
                  transition: 'all 0.2s ease'
                }}
              >
                <Icon size={18} color={a.color} />
                <span>{a.label}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Active Activity Component */}
      <div style={{ maxWidth: '640px', margin: '0 auto', width: '100%' }}>
        {activeActivity === 'breathing' && <BreathingReset onComplete={handleActivityComplete} />}
        {activeActivity === 'reaction' && <ReactionTest onComplete={handleActivityComplete} />}
        {activeActivity === 'focus' && <FocusChallenge onComplete={handleActivityComplete} />}
        {activeActivity === 'micro_goal' && <MicroGoalTracker onComplete={handleActivityComplete} />}
        {activeActivity === 'detox' && <DigitalDetoxTimer onComplete={handleActivityComplete} />}
      </div>
    </div>
  );
}
