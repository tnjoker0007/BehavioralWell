import React from 'react';
import { Smartphone, ExternalLink, Activity, RefreshCw } from 'lucide-react';

export default function LiveTelemetryView() {
  const telemetryUrl = 'http://localhost:8000/dev/telemetry';

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Header Banner */}
      <div className="glass-panel" style={{ padding: '20px 24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{ width: '48px', height: '48px', borderRadius: '14px', background: 'linear-gradient(135deg, #a855f7 0%, #06b6d4 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Smartphone size={26} color="#fff" />
          </div>
          <div>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff', margin: 0 }}>
              Live Phone Telemetry Monitor
            </h2>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', margin: '4px 0 0 0' }}>
              Real-time stream from physical Android device (FastAPI Engine on Port 8000)
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <a
            href={telemetryUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="btn btn-primary"
            style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', textDecoration: 'none' }}
          >
            Open Standalone Telemetry Window <ExternalLink size={16} />
          </a>
        </div>
      </div>

      {/* Embedded Telemetry Monitor View */}
      <div className="glass-panel" style={{ height: '800px', overflow: 'hidden', padding: 0, borderRadius: '16px', border: '1px solid var(--border-glass)' }}>
        <iframe
          src={telemetryUrl}
          title="BehavioralWell Live Telemetry Stream"
          style={{ width: '100%', height: '100%', border: 'none', background: '#0D1117' }}
        />
      </div>
    </div>
  );
}
