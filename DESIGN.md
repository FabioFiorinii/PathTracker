# Design System: Fitness App de Treinos

## 1. Definição do Estilo

- **Nome:** Fitness App de Treinos
- **Tipo:** Energetic, Bold, Motivational
- **Keywords:** fitness app landing, workouts, before-after, progress tracking, red and orange, energetic, mobile app, gym, health
- **Era:** 2020s Fitness
- **Light/Dark:** ✓ Full / ✗ No

## 2. Paleta de Cores

- **Primárias:** Red #E53E3E, Orange #FF6B00, Dark #1A1A1A
- **Secundárias:** White #FFFFFF, Light Grey #F7FAFC, Yellow #FFC107

## 3. Efeitos Visuais

Hero com grande CTA para download, cards de planos com destaque, slider antes/depois (CSS/JS), animações de números de progresso, layout modular mobile-first.

## 4. AI Prompt Keywords

fitness app landing, workouts, before-after, progress tracking, red and orange, energetic, mobile app, gym, health.

## 5. CSS Technical

```css
background: #FFFFFF, color: #1A1A1A, border-radius: 12px, box-shadow: 0 4px 12px rgba(0,0,0,0.1), font-family: 'Poppins, sans-serif', gradient CTAs red-to-orange, counter animations, before/after slider CSS.
```

## 6. Design System Variables

```css
--red: #E53E3E, --orange: #FF6B00, --dark: #1A1A1A, --white: #FFFFFF, --radius-card: 12px, --font-fitness: 'Poppins, sans-serif'.
```

## 7. Checklist de Implementação

- ☐ Navbar + Hero
- ☐ Benefícios + Planos
- ☐ Progresso/Resultados + App Preview
- ☐ CTA download
- ☐ Meta tags SEO
- ☐ Tom motivacional PT-BR
- ☐ Ícones SVG (treinos
- alimentação)
- ☐ Animações suaves
- ☐ Overlays em imagens para legibilidade.

## 8. Visual Theme & Atmosphere

Fitness App de Treinos — Design thematic com fitness app landing, workouts, before-after. Template e prompt pronto para IA. Estilo Fitness App de Treinos representa uma tendência moderna em design UI/UX web com foco em thematic.

- Density: 5/10 — Balanced
- Variance: 4/10 — Moderate
- Motion: 4/10 — Subtle

## 9. Color Palette & Roles

- **Red** (#E53E3E) — Error states, destructive actions
- **Orange** (#FF6B00) — Warm accent, call-to-action secondary
- **Dark** (#1A1A1A) — Dark surface, primary background
- **White** (#FFFFFF) — Secondary surface
- **Light Grey** (#F7FAFC) — Secondary text, borders, muted elements
- **Yellow** (#FFC107) — Warning states, attention indicators

## 10. Typography Rules

- **Display / Hero:** Poppins — Weight 700, tight tracking, used for headline impact
- **Body:** Poppins — Weight 400, 16px/1.6 line-height, max 72ch per line
- **UI Labels / Captions:** Poppins — 0.875rem, weight 500, slight letter-spacing
- **Monospace:** JetBrains Mono — Used for code, metadata, and technical values

Scale:
- Hero: clamp(2.5rem, 5vw, 4rem)
- H1: 2.25rem
- H2: 1.5rem
- Body: 1rem / 1.6
- Small: 0.875rem

## 11. Component Stylings

- **Primary Button:** Rounded (12px) shape. Accent color fill. Hover: 8% darken + subtle lift shadow. Active: -1px translate tactile press. Font weight 600. No outer glows.
- **Secondary / Ghost Button:** Outline variant. 1.5px border in muted color. Text in primary color. Hover: subtle background fill.
- **Cards:** Rounded (12px) corners. Surface background. Subtle shadow (0 2px 12px rgba(0,0,0,0.06)). 1px border stroke.
- **Inputs:** Label above input. 1px border stroke. Focus ring: 2px accent color offset 2px. Error text below in semantic red. No floating labels.
- **Navigation:** Primary surface background. Active item: accent color indicator. Font weight 500 when active.
- **Skeletons:** Shimmer animation matching component dimensions. No circular spinners.
- **Empty States:** Icon-based composition with descriptive text and action button.

## 12. Layout Principles

- **Grid:** CSS Grid primary. Max-width containment: 1280px centered with 1.5rem side padding.
- **Spacing rhythm:** Balanced. Base unit: 0.5rem (8px).
- **Section vertical gaps:** clamp(4rem, 8vw, 8rem).
- **Hero layout:** Split-screen (text left, visual right).
- **Feature sections:** Zig-zag alternating text+image rows. No 3-equal-columns.
- **Mobile collapse:** All multi-column layouts collapse below 768px. No horizontal overflow.
- **z-index contract:** base (0) / sticky-nav (100) / overlay (200) / modal (300) / toast (500).

## 13. Motion & Interaction

- **Physics:** Ease-out curves, 200-300ms duration. Smooth and predictable.
- **Entry animations:** Fade + translate-Y (16px → 0) over 420ms ease-out. Staggered cascades for lists: 80ms between items.
- **Hover states:** Subtle color shift + shadow adjustment over 200ms.
- **Page transitions:** Fade only (200ms).
- **Performance:** Only transform and opacity animated. No layout-triggering properties.

## 14. Anti-Patterns (Banned)

- No emojis in UI — use icon system only (Lucide, Heroicons)
- No pure black (#000000) — use off-black or charcoal variants
- No oversaturated accent colors (saturation cap: 80%)
- No 3-column equal-width feature layouts — use zig-zag or asymmetric grid
- No `h-screen` — use `min-h-[100dvh]`
- No AI copywriting clichés: "Elevate", "Seamless", "Unleash", "Next-Gen"
- No broken external image links — use picsum.photos or inline SVG
- No generic lorem ipsum in demos

## Contexto Histórico

Estilo Fitness App de Treinos representa uma tendência moderna em design UI/UX web com foco em thematic.

## Caso de Uso

Landing pages, Websites modernas
