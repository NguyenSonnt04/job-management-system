---
name: ui-ux-pro-max
description: Search-backed UI/UX design system generation and implementation guidance for web and mobile products. Use when Codex needs to plan, design, build, review, or refine interfaces; choose layout, style, color, typography, motion, landing-page, chart, icon, UX, or accessibility directions; generate reusable design systems; or pull stack-specific UI guidance for Tailwind, React, Next.js, Vue, Svelte, Flutter, React Native, SwiftUI, shadcn, or Jetpack Compose projects.
---

# UI/UX Pro Max

## Overview

Use this skill to turn a product request into a concrete design system and implementation direction. Start with the bundled search workflow, then run narrower searches only for the gaps that remain.

## Follow This Workflow

1. Extract the design brief first.
- Identify the product type, target audience, industry, desired mood, key constraints, and implementation stack.
- Infer the stack from the repo when it is obvious. If no stack is visible, default to `html-tailwind`.

2. Generate a design system before making detailed UI decisions.
- Run:

```bash
python scripts/search.py "<product type> <industry> <keywords>" --design-system -p "<project name>"
```

- Treat this as the default starting point for new UI work.
- Use the result to anchor layout pattern, palette, typography, effects, and anti-patterns.

3. Persist the design system when the work spans multiple pages or sessions.
- Run:

```bash
python scripts/search.py "<query>" --design-system --persist -p "<project name>"
python scripts/search.py "<query>" --design-system --persist -p "<project name>" --page "<page name>"
```

- Read `design-system/<project>/MASTER.md` as the global source of truth.
- When a page override exists in `design-system/<project>/pages/`, let it override the master rules for that page.

4. Use targeted domain searches only when you need more detail.
- Style exploration:

```bash
python scripts/search.py "<keywords>" --domain style
```

- Color palettes:

```bash
python scripts/search.py "<keywords>" --domain color
```

- Typography:

```bash
python scripts/search.py "<keywords>" --domain typography
```

- Landing-page structure:

```bash
python scripts/search.py "<keywords>" --domain landing
```

- UX and accessibility:

```bash
python scripts/search.py "<keywords>" --domain ux
python scripts/search.py "<keywords>" --domain web
```

- Charts, icons, prompts, and product/category fit:

```bash
python scripts/search.py "<keywords>" --domain chart
python scripts/search.py "<keywords>" --domain icons
python scripts/search.py "<keywords>" --domain prompt
python scripts/search.py "<keywords>" --domain product
```

5. Pull stack-specific guidance before implementation.
- Run:

```bash
python scripts/search.py "<keywords>" --stack html-tailwind
python scripts/search.py "<keywords>" --stack react
python scripts/search.py "<keywords>" --stack nextjs
```

- Other supported stacks: `vue`, `nuxtjs`, `nuxt-ui`, `svelte`, `swiftui`, `react-native`, `flutter`, `shadcn`, `jetpack-compose`.

6. Synthesize the output into concrete design decisions.
- Convert search output into a small decision set: page structure, tokens, typography pair, motion rules, component behavior, and anti-patterns to avoid.
- Prefer a strong visual point of view over a generic template.
- Explain why the chosen direction fits the audience and product.

## Guardrails

- Avoid generic AI defaults such as automatic split heroes, bento grids for simple content, mesh gradients, glassmorphism by reflex, and safe fintech-blue palettes.
- Do not use purple, violet, indigo, or magenta as the primary brand direction unless the user explicitly asks for it.
- Use SVG icons instead of emojis in shipped UI.
- Keep hover states stable; avoid motion that shifts layout.
- Preserve visible focus states, keyboard access, and sufficient text contrast.
- Respect `prefers-reduced-motion` and favor `transform` and `opacity` for animation.
- Test responsive layouts at mobile and desktop breakpoints before delivery.

## Resource Map

- [scripts/search.py](scripts/search.py): Main CLI for design-system, domain, and stack searches
- [scripts/core.py](scripts/core.py): Search engine and domain/stack configuration
- [scripts/design_system.py](scripts/design_system.py): Design-system generator and persistence helpers
- [assets/data](assets/data): Search dataset for styles, colors, UX, product types, icons, charts, and stack guidance
