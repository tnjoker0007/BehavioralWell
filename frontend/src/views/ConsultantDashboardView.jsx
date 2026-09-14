import React, { useState, useEffect } from 'react';
import { UserCheck, Shield, Activity, Search, ExternalLink, Eye, Smartphone, Clock, Award, AlertTriangle, Sparkles, X, HeartHandshake, User } from 'lucide-react';
import { api } from '../api/client';

export default function ConsultantDashboardView() {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [stageFilter, setStageFilter] = useState('ALL');
  const [selectedUserActivity, setSelectedUserActivity] = useState(null);
  const [loadingActivity, setLoadingActivity] = useState(false);

  useEffect(() => {
    loadPatients();
  }, []);

  async function loadPatients() {
    setLoading(true);
    const data = await api.getConsultantPatients();
    if (data) {
      setPatients(data);
    }
    setLoading(false);
  }

  const handleInspectUser = async (userId) => {
    setLoadingActivity(true);
    const activityData = await api.getUserActivityDetails(userId);
    if (activityData) {
      setSelectedUserActivity(activityData);
    }
    setLoadingActivity(false);
  };

  const filteredPatients = patients.filter(p => {
    const matchesSearch = (p.name || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                          (p.email || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                          (p.anonymous_id || '').toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStage = stageFilter === 'ALL' || p.stage.toString() === stageFilter;
    return matchesSearch && matchesStage;
  });

  const highConcernCount = patients.filter(p => p.stage >= 3).length;
  const totalInterventions = patients.reduce((acc, p) => acc + (p.interventions_completed || 0), 0);
  const totalTelemetries = patients.reduce((acc, p) => acc + (p.telemetries_count || 0), 0);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Admin Title Header */}
      <div className="glass-card" style={{ padding: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{
              width: '48px',
              height: '48px',
              borderRadius: '14px',
              background: 'linear-gradient(135deg, #a855f7 0%, #3b82f6 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 0 20px rgba(168, 85, 247, 0.4)'
            }}>
              <Shield size={26} color="#ffffff" />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <h2 style={{ fontSize: '1.4rem', color: '#fff', fontWeight: 800, margin: 0 }}>
                  ADMIN & CLINICIAN CONTROL CENTER
                </h2>
                <span className="badge badge-stage-4" style={{ fontSize: '0.75rem', padding: '4px 10px' }}>
                  ADMIN ROLE ACTIVE
                </span>
              </div>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', margin: '4px 0 0 0' }}>
                Monitor all registered users' real-time telemetries, risk stages, self-checks, and intervention activities.
              </p>
            </div>
          </div>

          <button className="btn btn-secondary" onClick={loadPatients} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Activity size={16} />
            Refresh All Patient Data
          </button>
        </div>
      </div>

      {/* Summary KPI Cards Bar */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '16px' }}>
        <div className="glass-card" style={{ padding: '20px' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)', fontWeight: 600 }}>TOTAL USERS MONITORED</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#fff', marginTop: '6px' }}>{patients.length} Users</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--secondary)', marginTop: '4px' }}>Active digital phenotyping feeds</div>
        </div>

        <div className="glass-card" style={{ padding: '20px' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)', fontWeight: 600 }}>HIGH CONCERN (STAGE 3 & 4)</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: highConcernCount > 0 ? '#ef4444' : '#10b981', marginTop: '6px' }}>
            {highConcernCount} Patients
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', marginTop: '4px' }}>Requires clinical review</div>
        </div>

        <div className="glass-card" style={{ padding: '20px' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)', fontWeight: 600 }}>TOTAL TELEMETRY SNAPSHOTS</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--primary)', marginTop: '6px' }}>{totalTelemetries} Ingested</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', marginTop: '4px' }}>Derived metadata records</div>
        </div>

        <div className="glass-card" style={{ padding: '20px' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)', fontWeight: 600 }}>INTERVENTIONS COMPLETED</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--secondary)', marginTop: '6px' }}>{totalInterventions} Sessions</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', marginTop: '4px' }}>Guided exercises completed</div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="glass-card" style={{ padding: '16px 24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
        <div style={{ position: 'relative', flex: 1, minWidth: '260px' }}>
          <Search size={16} color="var(--text-dim)" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
          <input
            type="text"
            placeholder="Search by User Name, Email, or Patient ID..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              width: '100%',
              padding: '10px 14px 10px 42px',
              borderRadius: '10px',
              background: 'rgba(255, 255, 255, 0.05)',
              border: '1px solid var(--border-glass)',
              color: '#fff',
              fontSize: '0.9rem',
              boxSizing: 'border-box'
            }}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-dim)' }}>Filter Stage:</span>
          {['ALL', '0', '1', '2', '3', '4'].map(stage => (
            <button
              key={stage}
              onClick={() => setStageFilter(stage)}
              className="btn"
              style={{
                padding: '6px 14px',
                fontSize: '0.8rem',
                borderRadius: '8px',
                background: stageFilter === stage ? 'var(--primary)' : 'rgba(255, 255, 255, 0.05)',
                color: '#fff',
                border: '1px solid var(--border-glass)'
              }}
            >
              {stage === 'ALL' ? 'All Stages' : `Stage ${stage}`}
            </button>
          ))}
        </div>
      </div>

      {/* All Users Activity Roster */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {filteredPatients.map(p => (
          <div key={p.user_id} className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                <div style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: '12px',
                  background: 'rgba(255, 255, 255, 0.08)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 800,
                  color: '#fff'
                }}>
                  <User size={20} color="#a855f7" />
                </div>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span style={{ fontWeight: 800, fontSize: '1.05rem', color: '#fff' }}>{p.name || p.anonymous_id}</span>
                    <span className={`badge badge-stage-${p.stage}`}>
                      {p.stage_label}
                    </span>
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)', marginTop: '2px' }}>
                    {p.email} • {p.occupation_category} ({p.age_group})
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '16px', flexWrap: 'wrap' }}>
                <div style={{ fontSize: '0.85rem' }}>
                  <span style={{ color: 'var(--text-dim)' }}>RISK SCORE: </span>
                  <span style={{ fontWeight: 800, color: p.risk_score > 50 ? '#ef4444' : '#34d399' }}>{p.risk_score} / 100</span>
                </div>

                <div style={{ fontSize: '0.85rem' }}>
                  <span style={{ color: 'var(--text-dim)' }}>LAST SELF-CHECK: </span>
                  <span style={{ fontWeight: 700, color: '#fff' }}>{p.last_self_report?.mood || 'None'}</span>
                </div>

                <button
                  className="btn btn-primary"
                  onClick={() => handleInspectUser(p.user_id)}
                  style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 16px', fontSize: '0.85rem' }}
                >
                  <Eye size={16} />
                  Inspect All User Activity
                </button>
              </div>
            </div>

            {/* Modality Breakdown Bar */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(110px, 1fr))',
              gap: '10px',
              padding: '12px',
              borderRadius: '10px',
              background: 'rgba(0, 0, 0, 0.2)',
              fontSize: '0.8rem'
            }}>
              {Object.entries(p.modality_scores || {}).map(([mod, score]) => (
                <div key={mod} style={{ textAlign: 'center' }}>
                  <div style={{ color: 'var(--text-dim)', textTransform: 'capitalize' }}>{mod}</div>
                  <div style={{ fontWeight: 700, color: score > 50 ? '#f59e0b' : 'var(--text-main)', marginTop: '2px' }}>
                    {score} / 100
                  </div>
                </div>
              ))}
            </div>

            {/* Primary Contributors */}
            {p.top_contributors && p.top_contributors.length > 0 && (
              <div style={{ fontSize: '0.85rem', color: 'var(--text-dim)' }}>
                <span style={{ fontWeight: 600, color: 'var(--primary)' }}>Key Contributors: </span>
                {p.top_contributors.map(c => c.human_explanation).join(' | ')}
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Detailed User Activity Modal / Inspection Drawer */}
      {selectedUserActivity && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.85)',
          backdropFilter: 'blur(10px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1100,
          padding: '24px'
        }}>
          <div className="glass-card" style={{
            maxWidth: '1000px',
            width: '100%',
            maxHeight: '90vh',
            overflowY: 'auto',
            padding: '32px',
            borderRadius: '24px',
            border: '1px solid var(--border-glass)',
            display: 'flex',
            flexDirection: 'column',
            gap: '24px'
          }}>
            {/* Header */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--border-glass)', paddingBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                <div style={{ width: '44px', height: '44px', borderRadius: '12px', background: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <User size={24} color="#fff" />
                </div>
                <div>
                  <h3 style={{ fontSize: '1.3rem', fontWeight: 800, color: '#fff', margin: 0 }}>
                    Detailed User Activity Log: {selectedUserActivity.user?.name}
                  </h3>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginTop: '2px' }}>
                    {selectedUserActivity.user?.email} • Role: <strong style={{ color: 'var(--secondary)' }}>{selectedUserActivity.user?.role?.toUpperCase()}</strong> • ID: {selectedUserActivity.user?.id}
                  </div>
                </div>
              </div>
              <button className="btn" onClick={() => setSelectedUserActivity(null)} style={{ padding: '8px' }}>
                <X size={20} color="#fff" />
              </button>
            </div>

            {/* OpenRouter Claude LLM Supportive Interpretation */}
            {selectedUserActivity.llm_interpretation && (
              <div style={{
                background: 'linear-gradient(135deg, rgba(168, 85, 247, 0.15) 0%, rgba(6, 182, 212, 0.15) 100%)',
                border: '1px solid rgba(168, 85, 247, 0.4)',
                borderRadius: '16px',
                padding: '20px',
                display: 'flex',
                gap: '14px'
              }}>
                <Sparkles size={24} color="#c084fc" style={{ flexShrink: 0, marginTop: '2px' }} />
                <div>
                  <div style={{ fontWeight: 800, fontSize: '0.95rem', color: '#c084fc', marginBottom: '6px' }}>
                    OPENROUTER CLAUDE LLM CLINICAL RISK SUMMARY
                  </div>
                  <div style={{ fontSize: '0.9rem', color: '#f0f6fc', lineHeight: '1.5' }}>
                    {selectedUserActivity.llm_interpretation}
                  </div>
                </div>
              </div>
            )}

            {/* Grid of User Metrics */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px' }}>
              {/* Telemetry Stream */}
              <div className="glass-card" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div style={{ fontWeight: 700, fontSize: '1rem', color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Smartphone size={18} />
                  Recent Ingested Telemetries ({selectedUserActivity.recent_telemetries?.length || 0})
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxHeight: '240px', overflowY: 'auto' }}>
                  {selectedUserActivity.recent_telemetries?.map((t, idx) => (
                    <div key={idx} style={{ padding: '10px', background: 'rgba(0,0,0,0.3)', borderRadius: '8px', fontSize: '0.8rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-dim)' }}>
                        <span>Screen Time: <strong style={{ color: '#fff' }}>{t.screen_time ? t.screen_time.toFixed(2) + ' hrs' : 'N/A'}</strong></span>
                        <span>Unlocks: <strong style={{ color: '#fff' }}>{t.unlock_count || 0}</strong></span>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-dim)', marginTop: '4px' }}>
                        <span>Typing: <strong style={{ color: 'var(--secondary)' }}>{t.typing_speed ? t.typing_speed.toFixed(1) + ' WPM' : 'N/A'}</strong></span>
                        <span>Accel Var: <strong style={{ color: 'var(--secondary)' }}>{t.acceleration_variance ? t.acceleration_variance.toFixed(4) : 'N/A'}</strong></span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Self-Reports */}
              <div className="glass-card" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div style={{ fontWeight: 700, fontSize: '1rem', color: 'var(--secondary)', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <HeartHandshake size={18} />
                  Self-Check-ins & Mood Reports ({selectedUserActivity.self_reports?.length || 0})
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxHeight: '240px', overflowY: 'auto' }}>
                  {selectedUserActivity.self_reports?.map((sr, idx) => (
                    <div key={idx} style={{ padding: '10px', background: 'rgba(0,0,0,0.3)', borderRadius: '8px', fontSize: '0.8rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ fontWeight: 700, color: '#fff' }}>Mood: {sr.mood}</span>
                        <span style={{ color: 'var(--text-dim)' }}>Stress: {sr.stress_level} / 5</span>
                      </div>
                      {sr.note && <div style={{ color: 'var(--text-dim)', marginTop: '4px', fontStyle: 'italic' }}>"{sr.note}"</div>}
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Intervention Sessions */}
            <div className="glass-card" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{ fontWeight: 700, fontSize: '1rem', color: '#c084fc', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Award size={18} />
                Completed Intervention Sessions ({selectedUserActivity.interventions?.length || 0})
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '10px' }}>
                {selectedUserActivity.interventions?.map((item, idx) => (
                  <div key={idx} style={{ padding: '10px', background: 'rgba(0,0,0,0.3)', borderRadius: '8px', fontSize: '0.8rem' }}>
                    <div style={{ fontWeight: 700, color: '#fff', textTransform: 'capitalize' }}>{item.activity_type}</div>
                    <div style={{ color: 'var(--secondary)', marginTop: '2px' }}>Status: Completed</div>
                  </div>
                ))}
              </div>
            </div>

            <div style={{ textAlign: 'right', marginTop: '10px' }}>
              <button className="btn btn-secondary" onClick={() => setSelectedUserActivity(null)}>
                Close Activity Log
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
