import React from 'react';
import { Smartphone, ExternalLink } from 'lucide-react';

export default function LiveTelemetryView() {
  const telemetryUrl = 'http://localhost:8000/dev/telemetry';

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header Banner */}
      <div className="neo-card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{
            width: '48px',
            height: '48px',
            borderRadius: '14px',
            background: 'var(--bg-neo)',
            boxShadow: 'var(--neo-raised-sm)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Smartphone size={26} color="var(--accent-violet)" />
          </div>
          <div>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--text-main)', margin: 0 }}>
              Live Telemetry Stream Monitor
            </h2>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500, margin: '4px 0 0 0' }}>
              Real-time stream from physical Android device (FastAPI SSE Engine on Port 8000)
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
      <div className="neo-card" style={{ height: '800px', overflow: 'hidden', padding: 0, borderRadius: '20px' }}>
        <iframe
          src={telemetryUrl}
          title="BehavioralWell Live Telemetry Stream"
          style={{ width: '100%', height: '100%', border: 'none', background: 'var(--bg-neo)' }}
        />
      </div>
    </div>
  );
}
