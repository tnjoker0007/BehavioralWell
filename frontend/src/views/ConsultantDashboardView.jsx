import React, { useState, useEffect } from 'react';
import { Shield, Activity, Search, Eye, Smartphone, Award, Sparkles, X, HeartHandshake, User } from 'lucide-react';
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
      <div className="neo-card">
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
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
              <Shield size={26} color="var(--accent-violet)" />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <h2 style={{ fontSize: '1.4rem', color: 'var(--text-main)', fontWeight: 800, margin: 0 }}>
                  ADMIN CONTROL CENTER
                </h2>
                <span className="badge badge-stage-4" style={{ fontSize: '0.75rem', padding: '4px 10px' }}>
                  ADMIN ROLE ACTIVE
                </span>
              </div>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500, margin: '4px 0 0 0' }}>
                Monitor all registered users' real-time telemetries, risk stages, self-checks, and intervention activities.
              </p>
            </div>
          </div>

          <button className="btn btn-secondary" onClick={loadPatients} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Activity size={16} color="var(--accent-violet)" />
            Refresh All Patient Data
          </button>
        </div>
      </div>

      {/* Summary KPI Cards Bar */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '18px' }}>
        <div className="neo-card" style={{ padding: '20px' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700 }}>TOTAL USERS MONITORED</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--text-main)', marginTop: '6px' }}>{patients.length} Users</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--accent-emerald)', fontWeight: 600, marginTop: '4px' }}>Active digital phenotyping feeds</div>
        </div>

        <div className="neo-card" style={{ padding: '20px' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700 }}>HIGH CONCERN (STAGE 3 & 4)</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: highConcernCount > 0 ? 'var(--accent-rose)' : 'var(--accent-emerald)', marginTop: '6px' }}>
            {highConcernCount} Patients
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600, marginTop: '4px' }}>Requires clinical review</div>
        </div>

        <div className="neo-card" style={{ padding: '20px' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700 }}>TOTAL TELEMETRY SNAPSHOTS</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--accent-cyan)', marginTop: '6px' }}>{totalTelemetries} Ingested</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600, marginTop: '4px' }}>Derived metadata records</div>
        </div>

        <div className="neo-card" style={{ padding: '20px' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700 }}>INTERVENTIONS COMPLETED</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--accent-emerald)', marginTop: '6px' }}>{totalInterventions} Sessions</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600, marginTop: '4px' }}>Guided exercises completed</div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="neo-card" style={{ padding: '18px 24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
        <div style={{ position: 'relative', flex: 1, minWidth: '260px' }}>
          <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
          <input
            type="text"
            className="neo-input"
            placeholder="Search by User Name, Email, or Patient ID..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ paddingLeft: '42px' }}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 700 }}>Filter Stage:</span>
          {['ALL', '0', '1', '2', '3', '4'].map(stage => (
            <button
              key={stage}
              onClick={() => setStageFilter(stage)}
              className="btn"
              style={{
                padding: '6px 14px',
                fontSize: '0.8rem',
                borderRadius: '10px',
                boxShadow: stageFilter === stage ? 'var(--neo-inset)' : 'var(--neo-raised-sm)',
                color: stageFilter === stage ? 'var(--accent-violet)' : 'var(--text-muted)',
                fontWeight: stageFilter === stage ? 800 : 600
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
          <div key={p.user_id} className="neo-card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                <div style={{
                  width: '44px',
                  height: '44px',
                  borderRadius: '12px',
                  background: 'var(--bg-neo)',
                  boxShadow: 'var(--neo-raised-sm)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 800,
                  color: 'var(--accent-violet)'
                }}>
                  <User size={20} color="var(--accent-violet)" />
                </div>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span style={{ fontWeight: 800, fontSize: '1.05rem', color: 'var(--text-main)' }}>{p.name || p.anonymous_id}</span>
                    <span className={`badge badge-stage-${p.stage}`}>
                      {p.stage_label}
                    </span>
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 500, marginTop: '2px' }}>
                    {p.email} • {p.occupation_category} ({p.age_group})
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '16px', flexWrap: 'wrap' }}>
                <div style={{ fontSize: '0.85rem' }}>
                  <span style={{ color: 'var(--text-muted)', fontWeight: 600 }}>RISK SCORE: </span>
                  <span style={{ fontWeight: 800, color: p.risk_score > 50 ? 'var(--accent-rose)' : 'var(--accent-emerald)' }}>{p.risk_score} / 100</span>
                </div>

                <div style={{ fontSize: '0.85rem' }}>
                  <span style={{ color: 'var(--text-muted)', fontWeight: 600 }}>LAST SELF-CHECK: </span>
                  <span style={{ fontWeight: 700, color: 'var(--text-main)' }}>{p.last_self_report?.mood || 'None'}</span>
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
              borderRadius: '12px',
              background: 'var(--bg-neo)',
              boxShadow: 'var(--neo-inset)',
              fontSize: '0.8rem'
            }}>
              {Object.entries(p.modality_scores || {}).map(([mod, score]) => (
                <div key={mod} style={{ textAlign: 'center' }}>
                  <div style={{ color: 'var(--text-muted)', textTransform: 'capitalize', fontWeight: 600 }}>{mod}</div>
                  <div style={{ fontWeight: 800, color: score > 50 ? 'var(--accent-amber)' : 'var(--text-main)', marginTop: '2px' }}>
                    {score} / 100
                  </div>
                </div>
              ))}
            </div>

            {/* Primary Contributors */}
            {p.top_contributors && p.top_contributors.length > 0 && (
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>
                <span style={{ fontWeight: 700, color: 'var(--accent-violet)' }}>Key Contributors: </span>
                {p.top_contributors.map(c => c.human_explanation).join(' | ')}
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Detailed User Activity Modal / Inspection Drawer */}
      {selectedUserActivity && (
        <div className="modal-overlay">
          <div className="modal-card" style={{ maxWidth: '960px', maxHeight: '90vh', overflowY: 'auto' }}>
            {/* Header */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid rgba(196, 193, 218, 0.4)', paddingBottom: '16px', marginBottom: '20px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                <div style={{ width: '44px', height: '44px', borderRadius: '12px', background: 'var(--accent-violet)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <User size={24} color="#FFFFFF" />
                </div>
                <div>
                  <h3 style={{ fontSize: '1.3rem', fontWeight: 800, color: 'var(--text-main)', margin: 0 }}>
                    Detailed User Activity Log: {selectedUserActivity.user?.name}
                  </h3>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600, marginTop: '2px' }}>
                    {selectedUserActivity.user?.email} • Role: <strong style={{ color: 'var(--accent-violet)' }}>{selectedUserActivity.user?.role?.toUpperCase()}</strong> • ID: {selectedUserActivity.user?.id}
                  </div>
                </div>
              </div>
              <button className="btn btn-secondary" onClick={() => setSelectedUserActivity(null)} style={{ padding: '8px' }}>
                <X size={20} color="var(--text-main)" />
              </button>
            </div>

            {/* OpenRouter Claude LLM Supportive Interpretation */}
            {selectedUserActivity.llm_interpretation && (
              <div className="neo-card-inset" style={{
                marginBottom: '20px',
                padding: '20px',
                display: 'flex',
                gap: '14px'
              }}>
                <Sparkles size={24} color="var(--accent-violet)" style={{ flexShrink: 0, marginTop: '2px' }} />
                <div>
                  <div style={{ fontWeight: 800, fontSize: '0.95rem', color: 'var(--accent-violet)', marginBottom: '6px' }}>
                    OPENROUTER CLAUDE LLM CLINICAL RISK SUMMARY
                  </div>
                  <div style={{ fontSize: '0.9rem', color: 'var(--text-main)', lineHeight: '1.5', fontWeight: 500 }}>
                    {selectedUserActivity.llm_interpretation}
                  </div>
                </div>
              </div>
            )}

            {/* Grid of User Metrics */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px', marginBottom: '20px' }}>
              {/* Telemetry Stream */}
              <div className="neo-card" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--accent-violet)', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Smartphone size={18} />
                  Recent Ingested Telemetries ({selectedUserActivity.recent_telemetries?.length || 0})
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxHeight: '240px', overflowY: 'auto' }}>
                  {selectedUserActivity.recent_telemetries?.map((t, idx) => (
                    <div key={idx} className="neo-card-inset" style={{ padding: '10px', fontSize: '0.8rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-muted)', fontWeight: 600 }}>
                        <span>Screen Time: <strong style={{ color: 'var(--text-main)' }}>{t.screen_time ? t.screen_time.toFixed(2) + ' hrs' : 'N/A'}</strong></span>
                        <span>Unlocks: <strong style={{ color: 'var(--text-main)' }}>{t.unlock_count || 0}</strong></span>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-muted)', marginTop: '4px', fontWeight: 600 }}>
                        <span>Typing: <strong style={{ color: 'var(--accent-teal)' }}>{t.typing_speed ? t.typing_speed.toFixed(1) + ' WPM' : 'N/A'}</strong></span>
                        <span>Accel Var: <strong style={{ color: 'var(--accent-teal)' }}>{t.acceleration_variance ? t.acceleration_variance.toFixed(4) : 'N/A'}</strong></span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Self-Reports */}
              <div className="neo-card" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--accent-emerald)', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <HeartHandshake size={18} />
                  Self-Check-ins & Mood Reports ({selectedUserActivity.self_reports?.length || 0})
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxHeight: '240px', overflowY: 'auto' }}>
                  {selectedUserActivity.self_reports?.map((sr, idx) => (
                    <div key={idx} className="neo-card-inset" style={{ padding: '10px', fontSize: '0.8rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ fontWeight: 800, color: 'var(--text-main)' }}>Mood: {sr.mood}</span>
                        <span style={{ color: 'var(--text-muted)', fontWeight: 600 }}>Stress: {sr.stress_level} / 5</span>
                      </div>
                      {sr.note && <div style={{ color: 'var(--text-muted)', marginTop: '4px', fontStyle: 'italic', fontWeight: 500 }}>"{sr.note}"</div>}
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Intervention Sessions */}
            <div className="neo-card" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--accent-violet)', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Award size={18} />
                Completed Intervention Sessions ({selectedUserActivity.interventions?.length || 0})
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '10px' }}>
                {selectedUserActivity.interventions?.map((item, idx) => (
                  <div key={idx} className="neo-card-inset" style={{ padding: '10px', fontSize: '0.8rem' }}>
                    <div style={{ fontWeight: 800, color: 'var(--text-main)', textTransform: 'capitalize' }}>{item.activity_type}</div>
                    <div style={{ color: 'var(--accent-emerald)', marginTop: '2px', fontWeight: 600 }}>Status: Completed</div>
                  </div>
                ))}
              </div>
            </div>

            <div style={{ textAlign: 'right', marginTop: '16px' }}>
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
