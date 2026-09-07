import React from 'react';
import { Activity, ShieldCheck, UserCheck, BarChart3, HeartHandshake, User } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab, onOpenSelfCheck }) {
  const tabs = [
    { id: 'dashboard', label: 'User Dashboard', icon: Activity },
    { id: 'interventions', label: 'Interventions', icon: HeartHandshake },
    { id: 'privacy', label: 'Privacy & Consent', icon: ShieldCheck },
    { id: 'account', label: 'Account', icon: User },
    { id: 'consultant', label: 'Consultant Dashboard', icon: UserCheck },
    { id: 'analytics', label: 'Research Analytics', icon: BarChart3 },
  ];

  return (
    <header style={{
      borderBottom: '1px solid var(--border-glass)',
      background: 'rgba(9, 13, 22, 0.85)',
      backdropFilter: 'blur(20px)',
      position: 'sticky',
      top: 0,
      zIndex: 100
    }}>
      <div style={{
        maxWidth: '1400px',
        margin: '0 auto',
        padding: '16px 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '16px'
      }}>
        {/* Brand Logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{
            width: '40px',
            height: '40px',
            borderRadius: '12px',
            background: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 20px rgba(6, 182, 212, 0.4)'
          }}>
            <Activity size={24} color="#ffffff" />
          </div>
          <div>
            <div style={{ fontFamily: 'var(--font-heading)', fontWeight: 800, fontSize: '1.25rem', color: '#fff' }}>
              Behavioral<span style={{ color: 'var(--primary)' }}>Well</span>
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', letterSpacing: '0.04em' }}>
              PERSONAL BEHAVIORAL FINGERPRINT ENGINE
            </div>
          </div>
        </div>

        {/* View Tabs */}
        <nav style={{ display: 'flex', gap: '6px', overflowX: 'auto', paddingBottom: '4px' }}>
          {tabs.map(tab => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`nav-tab ${isActive ? 'active' : ''}`}
              >
                <Icon size={18} />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </nav>

        {/* Action Controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <button className="btn btn-secondary" onClick={onOpenSelfCheck} style={{ fontSize: '0.85rem' }}>
            <HeartHandshake size={16} color="var(--primary)" />
            Self Check-in
          </button>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '6px 14px',
            borderRadius: '9999px',
            background: 'rgba(16, 185, 129, 0.1)',
            border: '1px solid rgba(16, 185, 129, 0.3)',
            fontSize: '0.8rem',
            color: 'var(--secondary)'
          }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--secondary)', boxShadow: '0 0 8px var(--secondary)' }} />
            Telemetry Active
          </div>
        </div>
      </div>
    </header>
  );
}
