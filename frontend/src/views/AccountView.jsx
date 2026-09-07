import React, { useState } from 'react';
import { User, Smartphone, Monitor, ShieldCheck, Download, Trash2, LogOut, CheckCircle2, Lock } from 'lucide-react';

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
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '800px', margin: '0 auto' }}>
      <div className="glass-card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px', marginBottom: '16px' }}>
          <div style={{
            width: '48px',
            height: '48px',
            borderRadius: '50%',
            background: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontWeight: 800,
            fontSize: '1.2rem'
          }}>
            {user?.name ? user.name[0].toUpperCase() : 'A'}
          </div>
          <div>
            <h2 style={{ fontSize: '1.4rem', color: '#fff', fontWeight: 800 }}>
              {user?.name || 'Alex Morgan'}
            </h2>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)' }}>
              User ID: <code style={{ color: 'var(--primary)', fontFamily: 'monospace' }}>{user?.id || 'usr_demo12345'}</code>
            </p>
          </div>
        </div>

        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
          gap: '12px',
          paddingTop: '16px',
          borderTop: '1px solid var(--border-glass)',
          fontSize: '0.88rem'
        }}>
          <div>
            <div style={{ color: 'var(--text-dim)', fontSize: '0.75rem' }}>EMAIL ADDRESS</div>
            <div style={{ color: '#fff', fontWeight: 600, marginTop: '2px' }}>{user?.email || 'demo@behavioralwell.ai'}</div>
          </div>
          <div>
            <div style={{ color: 'var(--text-dim)', fontSize: '0.75rem' }}>AGE GROUP</div>
            <div style={{ color: '#fff', fontWeight: 600, marginTop: '2px' }}>{user?.age_group || '25-34'}</div>
          </div>
          <div>
            <div style={{ color: 'var(--text-dim)', fontSize: '0.75rem' }}>OCCUPATION</div>
            <div style={{ color: '#fff', fontWeight: 600, marginTop: '2px' }}>{user?.occupation_category || 'Software Engineer'}</div>
          </div>
        </div>
      </div>

      {/* Connected Platform Clients (Android + Web) */}
      <div className="glass-card">
        <h3 style={{ fontSize: '1.1rem', color: '#fff', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Smartphone size={20} color="var(--primary)" />
          CONNECTED PLATFORM CLIENTS
        </h3>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '14px 16px',
            borderRadius: '12px',
            background: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid var(--border-glass)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <Smartphone size={24} color="#10b981" />
              <div>
                <div style={{ color: '#fff', fontWeight: 600, fontSize: '0.95rem' }}>Native Android Mobile Client</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Jetpack Compose • Sensor Collector Active</div>
              </div>
            </div>
            <span style={{ fontSize: '0.75rem', padding: '4px 10px', borderRadius: '9999px', background: 'rgba(16, 185, 129, 0.15)', color: '#10b981', border: '1px solid rgba(16, 185, 129, 0.3)' }}>
              CONNECTED & SYNCED
            </span>
          </div>

          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '14px 16px',
            borderRadius: '12px',
            background: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid var(--border-glass)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <Monitor size={24} color="#06b6d4" />
              <div>
                <div style={{ color: '#fff', fontWeight: 600, fontSize: '0.95rem' }}>React Web Dashboard Client</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Web Browser • Active Session</div>
              </div>
            </div>
            <span style={{ fontSize: '0.75rem', padding: '4px 10px', borderRadius: '9999px', background: 'rgba(6, 182, 212, 0.15)', color: '#06b6d4', border: '1px solid rgba(6, 182, 212, 0.3)' }}>
              CURRENT SESSION
            </span>
          </div>
        </div>
      </div>

      {/* Account Actions: Data Export, Delete Data, Logout */}
      <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px' }}>
          <div>
            <h4 style={{ fontSize: '1rem', color: '#fff', fontWeight: 700 }}>Data Export & Ownership</h4>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>Download your complete profile and consent configuration in JSON format.</p>
          </div>
          <button className="btn btn-secondary" onClick={handleExportData}>
            <Download size={16} />
            <span>{exported ? 'Data Exported!' : 'Export My Data'}</span>
          </button>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px', paddingTop: '14px', borderTop: '1px solid var(--border-glass)' }}>
          <div>
            <h4 style={{ fontSize: '1rem', color: '#f43f5e', fontWeight: 700 }}>Delete Stored Data</h4>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>Permanently purge telemetry, baseline models, and risk history.</p>
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
