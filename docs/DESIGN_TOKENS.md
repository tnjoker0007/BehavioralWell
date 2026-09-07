# BehavioralWell — Shared Design System & Design Tokens

Both the **Web Application (React)** and **Native Android Application (Jetpack Compose / Material 3)** must visually belong to the exact same product aesthetic: **Dark, Glassmorphic, Premium, Calm, Modern, AI/Healthcare-adjacent**.

---

## 1. Color Tokens

| Token Name | Hex Code | Jetpack Compose Value | Usage |
|---|---|---|---|
| `bg-dark` | `#090D16` | `Color(0xFF090D16)` | Primary application background |
| `bg-card` | `#111827` (0.7 opacity) | `Color(0xB3111827)` | Glassmorphic card surface |
| `bg-card-hover` | `#1E293B` (0.8 opacity) | `Color(0xCC1E293B)` | Interactive card hover/press |
| `border-glass` | `rgba(255,255,255,0.08)` | `Color(0x14FFFFFF)` | Subtle glass card border |
| `border-glass-glow` | `rgba(6,182,212,0.3)` | `Color(0x4D06B6D4)` | Active card border glow |
| `primary` | `#06B6D4` | `Color(0xFF06B6D4)` | Electric Cyan (Main Brand & Gauges) |
| `secondary` | `#10B981` | `Color(0xFF10B981)` | Emerald Teal (Success & Stable Stage) |
| `accent-purple` | `#8B5CF6` | `Color(0xFF8B5CF6)` | Electric Purple (Cognitive & Motion) |
| `accent-amber` | `#F59E0B` | `Color(0xFFF59E0B)` | Warm Amber (Warning & Persistent Stage) |
| `accent-orange` | `#F97316` | `Color(0xFFF97316)` | Vivid Orange (Elevated Risk Stage) |
| `accent-rose` | `#F43F5E` | `Color(0xFFF43F5E)` | Deep Rose (High Concern Stage & Crisis) |
| `text-main` | `#F8FAFC` | `Color(0xFFF8FAFC)` | Primary text |
| `text-muted` | `#94A3B8` | `Color(0xFF94A3B8)` | Subtitle & body text |
| `text-dim` | `#64748B` | `Color(0xFF64748B)` | Footers & metadata text |

---

## 2. Risk Stage Colors & Labels

| Stage Level | Stage Name | Badge Background | Badge Text |
|---|---|---|---|
| **Stage 0** | `Stage 0 — Stable` | `rgba(16, 185, 129, 0.15)` | `#10B981` |
| **Stage 1** | `Stage 1 — Early Deviation` | `rgba(6, 182, 212, 0.15)` | `#06B6D4` |
| **Stage 2** | `Stage 2 — Persistent Deviation` | `rgba(245, 158, 11, 0.15)` | `#F59E0B` |
| **Stage 3** | `Stage 3 — Elevated Risk` | `rgba(249, 115, 22, 0.15)` | `#F97316` |
| **Stage 4** | `Stage 4 — High Concern` | `rgba(244, 63, 94, 0.15)` | `#F43F5E` |

---

## 3. Typography Tokens

- **Heading Font:** `Outfit` (Android: `FontFamily(Font(R.font.outfit_bold))`)
  - H1 Header: `32sp`, Bold (800)
  - H2 Section Header: `22sp`, Bold (700)
  - H3 Card Header: `17sp`, SemiBold (600)
- **Body Font:** `Inter` (Android: `FontFamily(Font(R.font.inter_regular))`)
  - Body Main: `14sp`, Normal (400)
  - Caption / Label: `12sp`, Medium (500)

---

## 4. Layout & Shape Tokens

- **Card Corner Radius:** `16dp` (`RoundedCornerShape(16.dp)`)
- **Button Corner Radius:** `10dp` (`RoundedCornerShape(10.dp)`)
- **Pill / Badge Radius:** `9999dp` (`CircleShape`)
- **Card Padding:** `24dp`
- **Grid Spacing:** `16dp` to `20dp`
