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
    { id: 'breathing', label: 'Breathing Reset', icon: Wind, color: '#0D9488' },
    { id: 'reaction', label: 'Reaction Speed', icon: Zap, color: '#7C3AED' },
    { id: 'focus', label: 'Focus Challenge', icon: Target, color: '#10B981' },
    { id: 'micro_goal', label: 'Micro Goals', icon: CheckSquare, color: '#2563EB' },
    { id: 'detox', label: 'Digital Detox', icon: Smartphone, color: '#D97706' }
  ];

  const handleActivityComplete = (resultData) => {
    if (onLogActivity) onLogActivity(activeActivity, resultData);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div className="neo-card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
          <div style={{ padding: '8px', borderRadius: '12px', background: 'var(--bg-neo)', boxShadow: 'var(--neo-raised-sm)', display: 'flex' }}>
            <HeartHandshake size={24} color="var(--accent-rose)" />
          </div>
          <div>
            <h2 style={{ fontSize: '1.4rem', color: 'var(--text-main)', fontWeight: 800 }}>
              PREVENTIVE INTERVENTIONS
            </h2>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', fontWeight: 500 }}>
              Evidence-informed micro-activities tailored to recalibrate physiological stress, focus, and digital routines.
            </p>
          </div>
        </div>

        {/* Activity Selector Tabs */}
        <div style={{ display: 'flex', gap: '12px', marginTop: '20px', overflowX: 'auto', padding: '4px' }}>
          {activities.map(a => {
            const Icon = a.icon;
            const isSelected = activeActivity === a.id;
            return (
              <button
                key={a.id}
                onClick={() => setActiveActivity(a.id)}
                className="btn"
                style={{
                  boxShadow: isSelected ? 'var(--neo-inset)' : 'var(--neo-raised-sm)',
                  color: isSelected ? 'var(--accent-violet)' : 'var(--text-muted)',
                  fontWeight: isSelected ? 800 : 600
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
