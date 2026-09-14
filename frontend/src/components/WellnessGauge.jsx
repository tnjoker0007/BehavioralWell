import React from 'react';
import { ArrowUpRight, ArrowDownRight, Minus } from 'lucide-react';

export default function WellnessGauge({ riskScore = 15, stage = 0, stageLabel = "Stage 0 — Stable", trend = "stable", persistenceDays = 1, confidence = 92 }) {
  // SVG Radial parameters
  const radius = 80;
  const strokeWidth = 14;
  const normalizedRadius = radius - strokeWidth / 2;
  const circumference = normalizedRadius * 2 * Math.PI;
  // Semi-circle arc
  const arcLength = circumference * 0.75;
  const strokeDashoffset = arcLength - (riskScore / 100) * arcLength;

  // Safe confidence score normalization (strictly between 0% and 100%)
  const rawConf = typeof confidence === 'number' ? confidence : parseFloat(confidence) || 92;
  const normalizedConf = rawConf <= 1.0 ? rawConf * 100 : rawConf;
  const clampedConf = Math.min(100, Math.max(0, Math.round(normalizedConf)));

  const getStageColor = (s) => {
    switch (s) {
      case 0: return '#10B981'; // Green
      case 1: return '#06B6D4'; // Cyan
      case 2: return '#D97706'; // Amber
      case 3: return '#EA580C'; // Orange
      case 4: return '#E11D48'; // Rose
      default: return '#10B981';
    }
  };

  const currentColor = getStageColor(stage);

  return (
    <div className="neo-card" style={{ textAlign: 'center', position: 'relative', overflow: 'hidden' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <h3 style={{ fontSize: '1.05rem', color: 'var(--text-muted)', fontWeight: 700 }}>
          BEHAVIORAL RISK
        </h3>
        <span className={`badge badge-stage-${stage}`}>
          {stageLabel}
        </span>
      </div>

      {/* Neomorphic Circular Raised Container */}
      <div style={{
        position: 'relative',
        width: '210px',
        height: '210px',
        margin: '10px auto',
        borderRadius: '50%',
        background: 'var(--bg-neo)',
        boxShadow: 'var(--neo-raised)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}>
        {/* Sunken Inner Ring */}
        <div style={{
          position: 'absolute',
          width: '180px',
          height: '180px',
          borderRadius: '50%',
          boxShadow: 'var(--neo-inset)'
        }} />

        <svg height="210" width="210" style={{ transform: 'rotate(135deg)', position: 'absolute' }}>
          {/* Track background */}
          <circle
            stroke="#C4C1DA"
            fill="transparent"
            strokeDasharray={`${arcLength} ${circumference}`}
            strokeWidth={strokeWidth}
            strokeLinecap="round"
            r={normalizedRadius}
            cx="105"
            cy="105"
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
            cx="105"
            cy="105"
          />
        </svg>

        {/* Center Score Text */}
        <div style={{ zIndex: 2, textAlign: 'center' }}>
          <div style={{ fontSize: '3.2rem', fontFamily: 'var(--font-heading)', fontWeight: 800, color: 'var(--text-main)', lineHeight: 1 }}>
            {Math.round(riskScore)}
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700, marginTop: '4px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Risk Index
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--accent-violet)', fontWeight: 600, marginTop: '2px' }}>
            Confidence {clampedConf}%
          </div>
        </div>
      </div>

      {/* Footer Metrics */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: '12px',
        marginTop: '20px'
      }}>
        <div className="neo-card-inset">
          <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', fontWeight: 700 }}>PATTERN TREND</div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '4px', fontWeight: 700, color: trend === 'increasing' ? '#E11D48' : '#10B981', marginTop: '4px' }}>
            {trend === 'increasing' && <ArrowUpRight size={16} />}
            {trend === 'decreasing' && <ArrowDownRight size={16} />}
            {trend === 'stable' && <Minus size={16} />}
            {trend.toUpperCase()}
          </div>
        </div>

        <div className="neo-card-inset">
          <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', fontWeight: 700 }}>PERSISTENCE</div>
          <div style={{ fontWeight: 700, color: 'var(--text-main)', marginTop: '4px' }}>
            {persistenceDays} {persistenceDays === 1 ? 'Day' : 'Days'}
          </div>
        </div>
      </div>
    </div>
  );
}
