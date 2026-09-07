import React, { useState, useEffect } from 'react';
import { UserCheck, Shield, Activity, Search, ExternalLink, ArrowUpRight } from 'lucide-react';
import { api } from '../api/client';

export default function ConsultantDashboardView() {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    async function loadPatients() {
      const data = await api.getConsultantPatients();
      if (data) {
        setPatients(data);
      } else {
        // Fallback mock patients
        setPatients([
          {
            anonymous_id: 'PAT-8201A',
            occupation_category: 'Healthcare',
            stage: 2,
            stage_label: 'Stage 2 — Persistent Deviation',
            risk_score: 64.5,
            trend: 'increasing',
            persistence_days: 4,
            modality_scores: { keyboard: 62, usage: 78, motion: 45, work: 70, mobility: 20 },
            top_contributors: [
              { human_explanation: 'Late-night activity increased by 2.4 std-dev relative to personal baseline.' },
              { human_explanation: 'Work task accuracy shows a 2.1 std-dev deviation from baseline.' }
            ],
            last_self_report: { mood: 'Stressed', stress_level: 4 },
            interventions_completed: 3
          },
          {
            anonymous_id: 'PAT-4932B',
            occupation_category: 'Technology',
            stage: 0,
            stage_label: 'Stage 0 — Stable',
            risk_score: 14.2,
            trend: 'stable',
            persistence_days: 1,
            modality_scores: { keyboard: 12, usage: 15, motion: 10, work: 8, mobility: 5 },
            top_contributors: [],
            last_self_report: { mood: 'Good', stress_level: 1 },
            interventions_completed: 8
          },
          {
            anonymous_id: 'PAT-1029C',
            occupation_category: 'Education',
            stage: 3,
            stage_label: 'Stage 3 — Elevated Risk',
            risk_score: 78.0,
            trend: 'increasing',
            persistence_days: 6,
            modality_scores: { keyboard: 82, usage: 85, motion: 60, work: 74, mobility: 40 },
            top_contributors: [
              { human_explanation: 'Typing speed is 3.1 std-dev slower than personal baseline.' },
              { human_explanation: 'Backspace & correction rate is 2.8 std-dev higher than usual.' }
            ],
            last_self_report: { mood: 'Low', stress_level: 5 },
            interventions_completed: 1
          }
        ]);
      }
      setLoading(false);
    }
    loadPatients();
  }, []);

  const filteredPatients = patients.filter(p => p.anonymous_id.toLowerCase().includes(searchTerm.toLowerCase()));

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div className="glass-card">
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <UserCheck size={26} color="var(--primary)" />
            <div>
              <h2 style={{ fontSize: '1.4rem', color: '#fff', fontWeight: 800 }}>
                AUTHORIZED CONSULTANT & PROFESSIONAL DASHBOARD
              </h2>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)' }}>
                Anonymized patient risk staging, behavioral vector shifts, and intervention logs
              </p>
            </div>
          </div>

          <div style={{ position: 'relative' }}>
            <Search size={16} color="var(--text-dim)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              placeholder="Search Anonymous ID..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                padding: '8px 12px 8px 36px',
                borderRadius: '8px',
                background: 'rgba(255, 255, 255, 0.05)',
                border: '1px solid var(--border-glass)',
                color: '#fff',
                fontSize: '0.85rem'
              }}
            />
          </div>
        </div>
      </div>

      {/* Patient List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {filteredPatients.map(p => (
          <div key={p.anonymous_id} className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{
                  padding: '8px 14px',
                  borderRadius: '8px',
                  background: 'rgba(255, 255, 255, 0.05)',
                  fontWeight: 700,
                  fontSize: '0.95rem',
                  color: '#fff',
                  fontFamily: 'monospace'
                }}>
                  {p.anonymous_id}
                </div>
                <span className={`badge badge-stage-${p.stage}`}>
                  {p.stage_label}
                </span>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '20px', fontSize: '0.85rem' }}>
                <div>
                  <span style={{ color: 'var(--text-dim)' }}>DEVIATION SCORE: </span>
                  <span style={{ fontWeight: 800, color: '#fff' }}>{p.risk_score} / 100</span>
                </div>
                <div>
                  <span style={{ color: 'var(--text-dim)' }}>PERSISTENCE: </span>
                  <span style={{ fontWeight: 700, color: '#fff' }}>{p.persistence_days} Days</span>
                </div>
                <div>
                  <span style={{ color: 'var(--text-dim)' }}>COMPLETED INTERVENTIONS: </span>
                  <span style={{ fontWeight: 700, color: 'var(--secondary)' }}>{p.interventions_completed}</span>
                </div>
              </div>
            </div>

            {/* Modality Vector Breakdown */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(5, 1fr)',
              gap: '10px',
              padding: '12px',
              borderRadius: '10px',
              background: 'rgba(255, 255, 255, 0.02)',
              fontSize: '0.8rem'
            }}>
              {Object.entries(p.modality_scores).map(([mod, score]) => (
                <div key={mod} style={{ textAlign: 'center' }}>
                  <div style={{ color: 'var(--text-dim)', textTransform: 'capitalize' }}>{mod}</div>
                  <div style={{ fontWeight: 700, color: score > 50 ? 'var(--accent-amber)' : 'var(--text-main)', marginTop: '2px' }}>
                    {score} / 100
                  </div>
                </div>
              ))}
            </div>

            {/* Top Contributors */}
            {p.top_contributors.length > 0 && (
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                <span style={{ fontWeight: 600, color: 'var(--primary)' }}>Primary Contributors: </span>
                {p.top_contributors.map(c => c.human_explanation).join(' | ')}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
