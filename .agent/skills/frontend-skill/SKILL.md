---
name: frontend-skill
description: Frontend UI design and UX decision-making for web products, with reusable guidance for layouts, color systems, typography, motion, visual effects, and frontend audits. Use when Codex needs to design or refine pages/components, choose a visual direction, explain UI tradeoffs, avoid generic AI-looking layouts, review usability/accessibility, or run frontend audit scripts against HTML, CSS, JSX, or TSX code.
---

# Frontend Skill

## Overview

Use this skill to make frontend design decisions from principles instead of trends. Start from audience, content, brand, and task constraints; then read only the reference file that matches the decision you need to make.

## Follow This Workflow

1. Clarify constraints first.
- Identify the audience, brand constraints, content readiness, tech stack, and delivery timeline.
- If the request is open-ended, ask about palette, style direction, and layout preference before choosing a visual direction.

2. Read the core psychology reference.
- Always read [references/ux-psychology.md](references/ux-psychology.md) first.
- Use it to shape hierarchy, CTA emphasis, trust cues, cognitive load, and emotional tone.

3. Read only the specialist reference that fits the task.
- Read [references/color-system.md](references/color-system.md) for palette, contrast, dark mode, and color psychology.
- Read [references/typography-system.md](references/typography-system.md) for scale, pairing, line length, and hierarchy.
- Read [references/visual-effects.md](references/visual-effects.md) for shadows, gradients, glass, overlays, and depth.
- Read [references/animation-guide.md](references/animation-guide.md) for interaction timing, loading states, and transitions.
- Read [references/motion-graphics.md](references/motion-graphics.md) for Lottie, GSAP, SVG, 3D, particles, and scroll-driven motion.
- Read [references/decision-trees.md](references/decision-trees.md) when you need a product-type or audience-specific decision path.

4. Execute with intention.
- Explain why major design choices fit the audience and the task.
- Prefer clarity, trust, rhythm, and contrast over visual noise.
- Treat motion and effects as tools for hierarchy, feedback, or emotion, not decoration.

5. Audit implemented code when useful.
- Run [scripts/ux_audit.py](scripts/ux_audit.py) for broad UX and design heuristics.
- Run [scripts/accessibility_checker.py](scripts/accessibility_checker.py) for focused accessibility checks.
- Treat script output as heuristics that still need human review.

## Avoid Default AI Patterns

- Do not default to bento grids, split heroes, aurora or mesh gradients, generic glassmorphism, fintech blue, rounded-everything, or filler copy.
- Do not assume a safe modern SaaS style if the user has not asked for it.
- Do not choose colors, typography, or layout from personal preference when the prompt is vague; ask first.

## Use These Commands

```bash
python scripts/ux_audit.py <project_path>
python scripts/ux_audit.py <project_path> --json
python scripts/accessibility_checker.py <project_path>
```

## Read This Reference Map

- [references/ux-psychology.md](references/ux-psychology.md): UX laws, trust building, cognitive load, personas, and emotional design
- [references/color-system.md](references/color-system.md): palette strategy, contrast, dark mode, and color selection
- [references/typography-system.md](references/typography-system.md): type scale, pairing, readability, and hierarchy
- [references/visual-effects.md](references/visual-effects.md): gradients, shadows, overlays, glows, and effect tradeoffs
- [references/animation-guide.md](references/animation-guide.md): micro-interactions, loading states, page transitions, and motion timing
- [references/motion-graphics.md](references/motion-graphics.md): Lottie, GSAP, SVG, 3D, particles, and advanced motion
- [references/decision-trees.md](references/decision-trees.md): decision templates for landing pages, dashboards, e-commerce, portfolios, and complexity planning

## Apply This Framework Note

- In Next.js 16+ projects, prefer `next/form` for GET-based search, filter, sort, and pagination forms.
- Use a standard HTML `<form>` for mutations and Server Actions.
