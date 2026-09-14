import React from 'react';
import { Activity, ShieldCheck, UserCheck, BarChart3, HeartHandshake, User, Smartphone, LogIn, Shield } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab, onOpenSelfCheck, currentUser, onOpenLogin }) {
  const tabs = [
    { id: 'dashboard', label: 'Dashboard', icon: Activity },
    { id: 'telemetry', label: 'Live Telemetry', icon: Smartphone },
    { id: 'interventions', label: 'Interventions', icon: HeartHandshake },
    { id: 'consultant', label: currentUser?.role === 'admin' ? '🛡️ Admin Center' : 'Consultant', icon: UserCheck },
    { id: 'privacy', label: 'Privacy', icon: ShieldCheck },
    { id: 'account', label: 'Account', icon: User },
    { id: 'analytics', label: 'Research', icon: BarChart3 },
  ];

  const isAdmin = currentUser?.role === 'admin';

  return (
    <header style={{
      background: 'var(--bg-neo)',
      boxShadow: 'var(--neo-raised-sm)',
      position: 'sticky',
      top: 0,
      zIndex: 100,
      padding: '12px 0',
      borderBottom: '1px solid rgba(255, 255, 255, 0.5)'
    }}>
      <div style={{
        maxWidth: '1400px',
        margin: '0 auto',
        padding: '0 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '16px'
      }}>
        {/* Brand Logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{
            width: '42px',
            height: '42px',
            borderRadius: '14px',
            background: 'var(--bg-neo)',
            boxShadow: 'var(--neo-raised-sm)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Activity size={24} color="var(--accent-violet)" />
          </div>
          <div>
            <div style={{ fontFamily: 'var(--font-heading)', fontWeight: 800, fontSize: '1.35rem', color: 'var(--text-main)' }}>
              Behavioral<span style={{ color: 'var(--accent-violet)' }}>Well</span>
            </div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 600, letterSpacing: '0.04em' }}>
              NEOMORPHIC HEALTHCARE ENGINE
            </div>
          </div>
        </div>

        {/* View Tabs */}
        <nav style={{ display: 'flex', gap: '8px', overflowX: 'auto', padding: '4px' }}>
          {tabs.map(tab => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`nav-tab ${isActive ? 'active' : ''}`}
              >
                <Icon size={18} color={isActive ? 'var(--accent-violet)' : 'var(--text-muted)'} />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </nav>

        {/* Action Controls & Role Badge */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {/* Active User / Role Badge */}
          <button
            onClick={onOpenLogin}
            className="btn"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 14px',
              borderRadius: '12px',
              fontSize: '0.85rem'
            }}
          >
            {isAdmin ? <Shield size={16} color="var(--accent-violet)" /> : <User size={16} color="var(--accent-blue)" />}
            <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>{currentUser?.name || 'Guest User'}</span>
            <span style={{
              fontSize: '0.7rem',
              fontWeight: 800,
              padding: '3px 8px',
              borderRadius: '8px',
              background: isAdmin ? 'var(--accent-violet)' : 'var(--accent-teal)',
              color: '#FFFFFF',
              marginLeft: '4px'
            }}>
              {isAdmin ? 'ADMIN' : 'USER'}
            </span>
            <LogIn size={14} color="var(--text-muted)" />
          </button>

          <button className="btn btn-secondary" onClick={onOpenSelfCheck} style={{ fontSize: '0.85rem' }}>
            <HeartHandshake size={16} color="var(--accent-rose)" />
            Self Check-in
          </button>
        </div>
      </div>
    </header>
  );
}
