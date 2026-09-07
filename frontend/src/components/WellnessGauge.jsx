import React from 'react';
import { ArrowUpRight, ArrowDownRight, Minus, ShieldAlert } from 'lucide-react';

export default function WellnessGauge({ riskScore = 15, stage = 0, stageLabel = "Stage 0 — Stable", trend = "stable", persistenceDays = 1, confidence = 0.92 }) {
  // SVG Radial parameters
  const radius = 80;
  const strokeWidth = 14;
  const normalizedRadius = radius - strokeWidth / 2;
  const circumference = normalizedRadius * 2 * Math.PI;
  // Semi-circle arc
  const arcLength = circumference * 0.75;
  const strokeDashoffset = arcLength - (riskScore / 100) * arcLength;

  const getStageColor = (s) => {
    switch (s) {
      case 0: return '#10b981'; // Green
      case 1: return '#06b6d4'; // Cyan
      case 2: return '#f59e0b'; // Amber
      case 3: return '#f97316'; // Orange
      case 4: return '#f43f5e'; // Rose
      default: return '#10b981';
    }
  };

  const currentColor = getStageColor(stage);

  return (
    <div className="glass-card" style={{ textAlign: 'center', position: 'relative', overflow: 'hidden' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <h3 style={{ fontSize: '1.05rem', color: 'var(--text-muted)', fontWeight: 600 }}>
          BEHAVIORAL WELLBEING INDEX
        </h3>
        <span className={`badge badge-stage-${stage}`}>
          {stageLabel}
        </span>
      </div>

      {/* Radial Gauge */}
      <div style={{ position: 'relative', width: '220px', height: '180px', margin: '0 auto' }}>
        <svg height="200" width="220" style={{ transform: 'rotate(135deg)', overflow: 'visible' }}>
          {/* Track background */}
          <circle
            stroke="rgba(255, 255, 255, 0.08)"
            fill="transparent"
            strokeDasharray={`${arcLength} ${circumference}`}
            strokeWidth={strokeWidth}
            strokeLinecap="round"
            r={normalizedRadius}
            cx="110"
            cy="110"
          />
          {/* Active progress */}
          <circle
            stroke={currentColor}
            fill="transparent"
            strokeDasharray={`${arcLength} ${circumference}`}
            style={{ strokeDashoffset, transition: 'stroke-dashoffset 0.8s ease-in-out, stroke 0.5s ease' }}
            strokeWidth={strokeWidth}
            strokeLinecap="round"
            r={normalizedRadius}
            cx="110"
            cy="110"
          />
        </svg>

        {/* Center Score Text */}
        <div style={{
          position: 'absolute',
          top: '42%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          textAlign: 'center'
        }}>
          <div style={{ fontSize: '3rem', fontFamily: 'var(--font-heading)', fontWeight: 800, color: '#ffffff', lineHeight: 1 }}>
            {Math.round(riskScore)}
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '4px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Deviation Score
          </div>
        </div>
      </div>

      {/* Footer Metrics */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: '12px',
        marginTop: '16px',
        paddingTop: '16px',
        borderTop: '1px solid var(--border-glass)',
        fontSize: '0.85rem'
      }}>
        <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '10px', borderRadius: '10px' }}>
          <div style={{ color: 'var(--text-dim)', fontSize: '0.75rem' }}>TREND</div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '4px', fontWeight: 600, color: trend === 'increasing' ? '#f43f5e' : '#10b981', marginTop: '4px' }}>
            {trend === 'increasing' && <ArrowUpRight size={16} />}
            {trend === 'decreasing' && <ArrowDownRight size={16} />}
            {trend === 'stable' && <Minus size={16} />}
            {trend.toUpperCase()}
          </div>
        </div>

        <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '10px', borderRadius: '10px' }}>
          <div style={{ color: 'var(--text-dim)', fontSize: '0.75rem' }}>PERSISTENCE</div>
          <div style={{ fontWeight: 600, color: 'var(--text-main)', marginTop: '4px' }}>
            {persistenceDays} {persistenceDays === 1 ? 'Day' : 'Days'}
          </div>
        </div>
      </div>
    </div>
  );
}
