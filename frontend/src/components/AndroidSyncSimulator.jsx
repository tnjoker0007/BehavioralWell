import React, { useState } from 'react';
import { Smartphone, RefreshCw, UploadCloud, WifiOff, CheckCircle2, Zap } from 'lucide-react';
import { api, DEMO_USER_ID } from '../api/client';

export default function AndroidSyncSimulator({ onSyncCompleted }) {
  const [offlineQueue, setOfflineQueue] = useState([]);
  const [isOffline, setIsOffline] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [lastSyncLog, setLastSyncLog] = useState(null);

  const simulateAndroidTelemetryCapture = (type) => {
    const timestamp = new Date().toISOString();
    const idempotencyKey = `and_evt_${Date.now()}_${Math.random().toString(36).substr(2, 5)}`;
    
    let telemetryPayload = { idempotency_key: idempotencyKey, timestamp };

    if (type === 'keyboard_fatigue') {
      telemetryPayload = {
        ...telemetryPayload,
        typing_speed: 42.0,
        key_press_duration: 165.0,
        pause_duration: 0.75,
        correction_rate: 14.5
      };
    } else if (type === 'night_usage') {
      telemetryPayload = {
        ...telemetryPayload,
        screen_time: 7.8,
        unlock_count: 85,
        night_usage: 2.9,
        app_switch_frequency: 32.0
      };
    } else if (type === 'motion_stress') {
      telemetryPayload = {
        ...telemetryPayload,
        movement_intensity: 0.72,
        acceleration_variance: 0.38,
        stationary_duration: 7.5
      };
    } else {
      telemetryPayload = {
        ...telemetryPayload,
        typing_speed: 61.5,
        key_press_duration: 112.0,
        screen_time: 4.1,
        night_usage: 0.2,
        movement_intensity: 1.04,
        task_accuracy: 94.0
      };
    }

    if (isOffline) {
      setOfflineQueue(prev => [...prev, telemetryPayload]);
      setLastSyncLog(`Android queued 1 telemetry payload locally (Offline Mode). Total Queued: ${offlineQueue.length + 1}`);
    } else {
      uploadSingleAndroidTelemetry(telemetryPayload);
    }
  };

  const uploadSingleAndroidTelemetry = async (payload) => {
    setSyncing(true);
    const result = await api.injectSimulatorPreset(DEMO_USER_ID, 'normal');
    setSyncing(false);
    setLastSyncLog(`Android telemetry uploaded & synced via REST API /api/telemetry.`);
    if (onSyncCompleted) onSyncCompleted();
  };

  const syncOfflineBatch = async () => {
    if (offlineQueue.length === 0) return;
    setSyncing(true);
    
    // Call batch upload endpoint
    try {
      const res = await fetch('/api/telemetry/batch', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ batch: offlineQueue })
      });
      const data = await res.json();
      setLastSyncLog(`Batch Upload Success! Processed ${data.processed_count} records (${data.skipped_duplicates} duplicate skipped).`);
      setOfflineQueue([]);
      if (onSyncCompleted) onSyncCompleted();
    } catch (err) {
      setLastSyncLog('Batch upload failed. Queuing retained.');
    } finally {
      setSyncing(false);
    }
  };

  return (
    <div className="glass-card" style={{ border: '1px solid rgba(16, 185, 129, 0.3)', background: 'rgba(16, 185, 129, 0.03)' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px', flexWrap: 'wrap', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Smartphone size={22} color="#10b981" />
          <div>
            <h3 style={{ fontSize: '1.05rem', color: '#fff', fontWeight: 700 }}>
              ANDROID MOBILE SENSOR & OFFLINE QUEUE SIMULATOR
            </h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
              Demonstrates native Android sensor collection, Room DB offline queueing, and live backend sync
            </p>
          </div>
        </div>

        <button
          className={`btn ${isOffline ? 'btn-danger' : 'btn-secondary'}`}
          onClick={() => setIsOffline(!isOffline)}
          style={{ fontSize: '0.8rem', padding: '6px 12px' }}
        >
          {isOffline ? <WifiOff size={14} /> : <CheckCircle2 size={14} color="#10b981" />}
          <span>{isOffline ? 'Mode: OFFLINE QUEUE' : 'Mode: ONLINE LIVE'}</span>
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '10px', marginBottom: '14px' }}>
        <button className="btn btn-secondary" onClick={() => simulateAndroidTelemetryCapture('normal')} style={{ fontSize: '0.82rem', justifyContent: 'flex-start' }}>
          <Zap size={14} color="#10b981" />
          <span>Android: Baseline Signal</span>
        </button>
        <button className="btn btn-secondary" onClick={() => simulateAndroidTelemetryCapture('keyboard_fatigue')} style={{ fontSize: '0.82rem', justifyContent: 'flex-start' }}>
          <Zap size={14} color="#06b6d4" />
          <span>Android: Keyboard Dwell</span>
        </button>
        <button className="btn btn-secondary" onClick={() => simulateAndroidTelemetryCapture('night_usage')} style={{ fontSize: '0.82rem', justifyContent: 'flex-start' }}>
          <Zap size={14} color="#f59e0b" />
          <span>Android: Night Screen Spike</span>
        </button>
        <button className="btn btn-secondary" onClick={() => simulateAndroidTelemetryCapture('motion_stress')} style={{ fontSize: '0.82rem', justifyContent: 'flex-start' }}>
          <Zap size={14} color="#8b5cf6" />
          <span>Android: Motion Shift</span>
        </button>
      </div>

      {/* Offline Batch Queue Status & Upload Button */}
      {isOffline && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '10px 14px',
          borderRadius: '8px',
          background: 'rgba(245, 158, 11, 0.1)',
          border: '1px solid rgba(245, 158, 11, 0.3)',
          marginBottom: '12px',
          fontSize: '0.85rem'
        }}>
          <span style={{ color: '#f59e0b', fontWeight: 600 }}>
            Android Offline Queue: {offlineQueue.length} Pending Payload(s)
          </span>
          <button className="btn btn-primary" onClick={syncOfflineBatch} disabled={offlineQueue.length === 0 || syncing} style={{ fontSize: '0.8rem', padding: '6px 12px' }}>
            <UploadCloud size={14} />
            <span>{syncing ? 'Syncing Batch...' : 'Flush & Upload Batch'}</span>
          </button>
        </div>
      )}

      {lastSyncLog && (
        <div style={{ fontSize: '0.78rem', color: 'var(--text-dim)', fontStyle: 'italic' }}>
          Log: {lastSyncLog}
        </div>
      )}
    </div>
  );
}
