import React from 'react';
import { Activity, ShieldCheck, UserCheck, BarChart3, HeartHandshake, User, Smartphone, LogIn, Shield } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab, onOpenSelfCheck, currentUser, onOpenLogin }) {
  const tabs = [
    { id: 'dashboard', label: 'User Dashboard', icon: Activity },
    { id: 'telemetry', label: 'Live Phone Telemetry', icon: Smartphone },
    { id: 'interventions', label: 'Interventions', icon: HeartHandshake },
    { id: 'consultant', label: currentUser?.role === 'admin' ? '🛡️ Admin Control Center' : 'Consultant Dashboard', icon: UserCheck },
    { id: 'privacy', label: 'Privacy & Consent', icon: ShieldCheck },
    { id: 'account', label: 'Account', icon: User },
    { id: 'analytics', label: 'Research Analytics', icon: BarChart3 },
  ];

  const isAdmin = currentUser?.role === 'admin';

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
            const isTabAdmin = tab.id === 'consultant' && isAdmin;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`nav-tab ${isActive ? 'active' : ''}`}
                style={isTabAdmin ? { border: '1px solid rgba(168, 85, 247, 0.5)', background: isActive ? 'var(--primary)' : 'rgba(168, 85, 247, 0.15)', color: '#d8b4fe' } : {}}
              >
                <Icon size={18} />
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
              padding: '6px 14px',
              borderRadius: '10px',
              background: isAdmin ? 'rgba(168, 85, 247, 0.2)' : 'rgba(255, 255, 255, 0.05)',
              border: isAdmin ? '1px solid #a855f7' : '1px solid var(--border-glass)',
              color: '#fff',
              fontSize: '0.85rem'
            }}
          >
            {isAdmin ? <Shield size={16} color="#c084fc" /> : <User size={16} color="#60a5fa" />}
            <span>{currentUser?.name || 'Guest User'}</span>
            <span style={{
              fontSize: '0.7rem',
              fontWeight: 800,
              padding: '2px 6px',
              borderRadius: '4px',
              background: isAdmin ? '#a855f7' : 'var(--primary)',
              color: '#fff',
              marginLeft: '4px'
            }}>
              {isAdmin ? 'ADMIN' : 'USER'}
            </span>
            <LogIn size={14} color="var(--text-dim)" />
          </button>

          <button className="btn btn-secondary" onClick={onOpenSelfCheck} style={{ fontSize: '0.85rem' }}>
            <HeartHandshake size={16} color="var(--primary)" />
            Self Check-in
          </button>
        </div>
      </div>
    </header>
  );
}
