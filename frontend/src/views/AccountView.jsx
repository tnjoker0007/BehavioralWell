import React, { useState } from 'react';
import { User, Smartphone, Monitor, ShieldCheck, Download, Trash2, LogOut, CheckCircle2, Lock, KeyRound, Activity } from 'lucide-react';

export default function AccountView({ user, consent, onDeleteData, onLogout }) {
  const [exported, setExported] = useState(false);

  const handleExportData = () => {
    const exportData = {
      profile: user,
      consent: consent,
      export_timestamp: new Date().toISOString(),
      disclaimer: "BehavioralWell privacy-first metadata export."
    };

    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `behavioralwell_data_export_${user?.id || 'demo'}.json`;
    a.click();
    setExported(true);
    setTimeout(() => setExported(false), 3000);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '820px', margin: '0 auto' }}>
      
      {/* 1. Account Profile Card */}
      <div className="neo-card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '20px' }}>
          <div style={{
            width: '52px',
            height: '52px',
            borderRadius: '50%',
            background: 'var(--bg-neo)',
            boxShadow: 'var(--neo-raised-sm)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--accent-violet)',
            fontWeight: 800,
            fontSize: '1.4rem'
          }}>
            {user?.name ? user.name[0].toUpperCase() : 'A'}
          </div>
          <div>
            <h2 style={{ fontSize: '1.4rem', color: 'var(--text-main)', fontWeight: 800 }}>
              {user?.name || 'Alex Morgan'}
            </h2>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>
              User ID: <code style={{ color: 'var(--accent-violet)', fontFamily: 'monospace' }}>{user?.id || 'usr_demo12345'}</code>
            </p>
          </div>
        </div>

        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
          gap: '14px',
          fontSize: '0.88rem'
        }}>
          <div className="neo-card-inset">
            <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', fontWeight: 700 }}>EMAIL ADDRESS</div>
            <div style={{ color: 'var(--text-main)', fontWeight: 700, marginTop: '2px' }}>{user?.email || 'demo@behavioralwell.ai'}</div>
          </div>
          <div className="neo-card-inset">
            <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', fontWeight: 700 }}>AGE GROUP</div>
            <div style={{ color: 'var(--text-main)', fontWeight: 700, marginTop: '2px' }}>{user?.age_group || '25-34'}</div>
          </div>
          <div className="neo-card-inset">
            <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', fontWeight: 700 }}>OCCUPATION</div>
            <div style={{ color: 'var(--text-main)', fontWeight: 700, marginTop: '2px' }}>{user?.occupation_category || 'Software Engineer'}</div>
          </div>
        </div>
      </div>

      {/* 2. Security & Credentials Card */}
      <div className="neo-card">
        <h3 style={{ fontSize: '1.1rem', color: 'var(--text-main)', fontWeight: 800, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ padding: '6px', borderRadius: '10px', background: 'var(--bg-neo)', boxShadow: 'var(--neo-raised-sm)', display: 'flex' }}>
            <KeyRound size={18} color="var(--accent-violet)" />
          </div>
          SECURITY & ROLE ACCESS
        </h3>

        <div className="neo-card-inset" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div style={{ fontWeight: 800, color: 'var(--text-main)', fontSize: '0.95rem' }}>Active Role Permissions</div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 500, marginTop: '2px' }}>
              {user?.role === 'admin' ? 'Full Administrator Access — View all patients & telemetries.' : 'Standard User Access — View personal fingerprint only.'}
            </div>
          </div>
          <span className="badge" style={{ color: user?.role === 'admin' ? 'var(--accent-violet)' : 'var(--accent-teal)' }}>
            {user?.role?.toUpperCase() || 'USER'}
          </span>
        </div>
      </div>

      {/* 3. Diagnostics & Connected Platform Clients */}
      <div className="neo-card">
        <h3 style={{ fontSize: '1.1rem', color: 'var(--text-main)', fontWeight: 800, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ padding: '6px', borderRadius: '10px', background: 'var(--bg-neo)', boxShadow: 'var(--neo-raised-sm)', display: 'flex' }}>
            <Activity size={18} color="var(--accent-teal)" />
          </div>
          DIAGNOSTICS & CONNECTED CLIENTS
        </h3>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div className="neo-card-inset" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <Smartphone size={22} color="var(--accent-emerald)" />
              <div>
                <div style={{ color: 'var(--text-main)', fontWeight: 700, fontSize: '0.95rem' }}>Native Android Mobile Collector</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 500 }}>Jetpack Compose • Derived Sensor Feature Engine</div>
              </div>
            </div>
            <span className="badge" style={{ color: 'var(--accent-emerald)' }}>
              CONNECTED & SYNCED
            </span>
          </div>

          <div className="neo-card-inset" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <Monitor size={22} color="var(--accent-cyan)" />
              <div>
                <div style={{ color: 'var(--text-main)', fontWeight: 700, fontSize: '0.95rem' }}>React Neomorphic Web Dashboard</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 500 }}>Web Browser • Active Session</div>
              </div>
            </div>
            <span className="badge" style={{ color: 'var(--accent-cyan)' }}>
              CURRENT SESSION
            </span>
          </div>
        </div>
      </div>

      {/* 4. Privacy & Data Export Card */}
      <div className="neo-card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px' }}>
          <div>
            <h4 style={{ fontSize: '1rem', color: 'var(--text-main)', fontWeight: 800 }}>Data Export & Ownership</h4>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>Download your complete profile and consent configuration in JSON format.</p>
          </div>
          <button className="btn btn-secondary" onClick={handleExportData}>
            <Download size={16} color="var(--accent-violet)" />
            <span>{exported ? 'Data Exported!' : 'Export My Data'}</span>
          </button>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px', paddingTop: '16px', borderTop: '1px solid rgba(196, 193, 218, 0.4)' }}>
          <div>
            <h4 style={{ fontSize: '1rem', color: 'var(--accent-rose)', fontWeight: 800 }}>Delete Stored Data</h4>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>Permanently purge telemetry, baseline models, and risk history.</p>
          </div>
          <button className="btn btn-danger" onClick={onDeleteData}>
            <Trash2 size={16} />
            <span>Purge Stored Data</span>
          </button>
        </div>
      </div>
    </div>
  );
}
