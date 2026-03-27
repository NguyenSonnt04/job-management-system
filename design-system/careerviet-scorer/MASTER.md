# CareerViet Home UI Master File

> LOGIC: When building a specific page, first check `design-system/careerviet-scorer/pages/[page-name].md`.
> If that file exists, its rules override this master file.
> If not, follow the rules below.

---

Project: CareerViet
Source of truth: `src/main/resources/static/index.html` + `src/main/resources/static/css/style.css` + shared `includes/header.html` / `includes/footer.html`
Updated: 2026-03-28
Category: Job portal / recruitment marketplace

---

## Design Intent

This UI is not a generic SaaS dashboard.
It is a high-energy recruitment homepage that combines:

- trust and structure from a corporate job portal
- strong conversion focus around search and registration
- promotional hero storytelling for featured employers
- dense but readable listing surfaces for job discovery

The visual language should feel:

- energetic
- commercial
- recruitment-focused
- modern but familiar
- information-rich without looking cluttered

Do not drift into minimalist startup SaaS styling.
Do not replace the current recruitment-marketplace feel with flat, sparse, app-like layouts.

---

## Global Rules

### Color Palette

| Role | Hex | CSS Variable |
|------|-----|--------------|
| Primary Orange | `#FF6B00` | `--primary-orange` |
| Warm Orange | `#FF8C00` | custom gradient stop |
| Gold Accent | `#FFB800` | custom gradient stop |
| Primary Teal | `#00BFA5` | `--primary-teal` |
| Primary Blue | `#2E3B8E` | `--primary-blue` |
| Dark Blue | `#1A2456` | `--dark-blue` |
| Light Gray Surface | `#F5F7FA` | `--light-gray` |
| Medium Gray Border | `#E8ECEF` | `--medium-gray` |
| Main Text | `#2C3E50` | `--text-dark` |
| Secondary Text | `#6C757D` | `--text-gray` |
| White | `#FFFFFF` | `--white` |
| Highlight Badge | `#FFD700` | `--yellow-badge` |
| Urgent Badge | `#FF4444` | `--red-urgent` |

### Color Usage Rules

- Orange is the primary commercial accent for tabs, links, highlights, badges, and hero emphasis.
- Teal is used for the main action path for candidates, especially search and success states.
- Dark blue and primary blue are used for employer or account-related actions and trust-building UI.
- Light gray is the default background for secondary sections and footer surfaces.
- White cards sit on top of tinted or gray section backgrounds.
- Avoid introducing purple, neon tones, or low-saturation pastel palettes.

### Gradients

- Hero page band: `linear-gradient(90deg, #FF8C00 0%, #FFB800 50%, #FF6B00 100%)`
- Featured employer banner: `linear-gradient(135deg, #FF8C00 0%, #FF6B00 100%)`
- Search panel background: soft peach gradient, not a flat fill

Gradients are a core part of the homepage identity.
Do not flatten the hero into a single-color block unless the page context requires it.

### Typography

- Heading font: Inter
- Body font: Inter
- Fallback stack: `-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif`

### Typography Rules

- Use large, bold, sales-oriented headings in hero and section titles.
- Use medium to strong font weight for interactive text, tabs, buttons, and job titles.
- Job titles should read as the primary text in listing cards.
- Company and metadata text should be one level lower in contrast and weight.
- Avoid decorative display fonts and avoid overly editorial serif treatment.

### Spacing Scale

| Token | Value |
|------|-------|
| `--space-xs` | `4px` |
| `--space-sm` | `8px` |
| `--space-md` | `12px` |
| `--space-lg` | `16px` |
| `--space-xl` | `20px` |
| `--space-2xl` | `30px` |
| `--space-3xl` | `40px` |
| `--space-4xl` | `50px` |

Use compact spacing inside cards and controls.
Use larger spacing only between major sections and hero modules.

### Radius

| Token | Value |
|------|-------|
| Small | `6px` |
| Medium | `8px` |
| Large | `10px` |
| XL | `12px` |
| Pill | `20px` |

### Shadow Depths

| Level | Value | Usage |
|------|-------|-------|
| `--shadow-sm` | `0 2px 8px rgba(0, 0, 0, 0.08)` | header, small buttons, cards |
| `--shadow-md` | `0 4px 16px rgba(0, 0, 0, 0.12)` | buttons, floating labels, logo pills |
| `--shadow-lg` | `0 8px 24px rgba(0, 0, 0, 0.16)` | hero cards, hover state, chatbot, QR panel |

Shadows are used intentionally.
This system is not shadowless flat design.

### Motion

- Standard transition: `all 0.3s cubic-bezier(0.4, 0, 0.2, 1)`
- Dropdown and hover transitions: `0.2s` to `0.3s`
- Entry animations are allowed in hero, section titles, and cards
- Hover motion should be subtle lift, not dramatic scale

Allowed motion patterns:

- slide down for sticky header reveal
- fade in from left or right for hero modules
- fade in up for job cards
- soft bounce for floating chatbot trigger
- pulse for branded hero labels

Do not use exaggerated elastic animations or parallax-heavy scenes.

---

## Layout System

### Container

- Use full-width sections with padded inner container
- Desktop container padding: `40px`
- Large desktop: `30px`
- Tablet: `20px`
- Mobile: `12px` to `15px`

### Header

- Sticky top navigation
- White background
- Subtle shadow
- Dense horizontal nav with business-like wording
- Utility actions on the right: notifications, auth, employer CTA

The header should feel functional and commerce-oriented, not editorial or lifestyle-oriented.

### Homepage Section Order

Follow this pattern for the landing page:

1. Sticky header
2. Hero band with search module and employer promotion banner
3. Top employer heading / brand block
4. Featured job listings with tabs
5. Category-driven job listings with chips or tabs
6. Dense multi-column footer

### Section Rhythm

- Hero is the most visually intense zone
- Listing sections should calm the layout slightly with white cards and gray background alternation
- Footer returns to utility-first dense information architecture

---

## Component Specs

### Primary CTAs

Two CTA families must exist:

- Candidate CTA: teal, high-contrast, direct action
- Employer or account CTA: blue/navy, trust-oriented

#### Candidate CTA

```css
.btn-search {
  background: #00BFA5;
  color: #FFFFFF;
  border-radius: 6px;
  font-weight: 700;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.btn-search:hover {
  background: #00A88F;
  transform: translateY(-2px);
}
```

#### Employer CTA

```css
.btn-employer {
  background: #1A2456;
  color: #FFFFFF;
  border-radius: 6px;
  font-weight: 600;
}

.btn-employer:hover {
  background: #2E3B8E;
  transform: translateY(-2px);
}
```

### Secondary Text Buttons

- Use transparent background
- Use icon + label when helpful
- Add hover fill with a low-contrast tinted background
- Keep radius at `6px`

### Inputs

```css
.search-input,
.login-form input {
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background: #FFFFFF;
}

.search-input:focus,
.login-form input:focus {
  outline: none;
  border-color: #00BFA5;
  box-shadow: 0 0 0 3px rgba(0, 191, 165, 0.1);
}
```

Inputs should feel clean and transactional.
Avoid oversized rounded app-style fields.

### Tabs

Two tab styles are used:

- underline tabs for ranking / listing context
- pill tabs for category browsing

#### Underline Tabs

- Transparent background
- Muted label by default
- Orange active state
- Orange underline bar on active

#### Pill Tabs

- Light gray background by default
- Blue active state with white text
- Orange hover state

### Job Cards

```css
.job-card {
  background: #FFFFFF;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.job-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.16);
}
```

Job card rules:

- logo on the left in a bordered square
- title first, strong and readable
- company, salary, location stacked beneath
- badges sit at the top-right corner
- hover should highlight the job title in orange
- keep cards dense; this is not a spacious editorial card layout

### Badges

- `TOP`: yellow background, dark text
- `URGENT`: red background, white text
- badges are compact, rectangular, and clearly commercial

Do not convert these into outlined tags or soft pastel chips.

### Hero Search Panel

The search panel is a separate card sitting on top of the hero gradient.

Rules:

- use soft peach gradient background
- medium padding
- rounded corners
- strong box shadow
- stacked actions
- search CTA spans full width
- registration prompt follows search flow, not a separate detached block

### Featured Employer Banner

This is a promotional surface, not a neutral carousel.

Rules:

- orange gradient background
- bold white headline
- branded pill or logo block
- visual storytelling image
- QR or side promo card allowed
- supporting benefit chips can wrap underneath

### Header Dropdowns

- white floating panels
- rounded 8px to 14px corners
- stronger shadow than base cards
- short vertical motion on open
- compact list rows with icon + label

### Footer

- light gray background
- multi-column link system
- uppercase footer headings
- smaller body text
- app badges and social icons grouped in utility column

The footer should remain information-dense and corporate.
Do not simplify it into a minimal 3-link footer.

### Floating Chat Trigger

- fixed circular button at bottom-right
- prominent shadow
- playful motion is acceptable
- must stay secondary to page CTAs

---

## Content and Hierarchy Rules

### Homepage Messaging

- Speak directly to job seekers and employers
- Emphasize opportunity volume, urgency, trust, and speed
- Headlines should sound commercial and action-oriented
- Metadata should be scannable in under 2 seconds per card

### Density Rules

- Dense is acceptable when grouped and aligned
- Keep each card internally structured and readable
- Avoid empty white space for its own sake
- Prioritize scan speed over minimalist purity

### Iconography

- Use inline SVG icons
- Stroke icons for utility areas
- Filled shapes are acceptable in promotional feature chips
- Keep icon style simple and consistent within each component family

---

## Responsive Rules

Primary breakpoints:

- `1400px`
- `1024px`
- `768px`
- `480px`
- `360px`

### Responsive Behavior

- Collapse hero two-column layout into one column below `1024px`
- Convert desktop nav into toggle menu below `768px`
- Hide or simplify decorative hero image on smaller screens
- Preserve CTA clarity on mobile
- Allow tabs to scroll horizontally on narrow screens
- Move floating or side modules into inline stacked layout on mobile
- Footer compresses from 6 columns to 3, then 2, then 1

Mobile priorities:

- no horizontal scroll
- preserve readable job card metadata
- keep search and apply actions large enough to tap
- do not let sticky or floating elements cover primary content

---

## Style Guidelines

Style direction:

- recruitment marketplace
- branded portal
- conversion-first
- high-contrast calls to action
- layered surfaces
- clear commercial hierarchy

Keywords:

- energetic
- promotional
- trustworthy
- structured
- scan-friendly
- dense
- modern corporate

Best for:

- homepages
- job search pages
- employer listing pages
- category landing pages
- account dropdowns
- utility-heavy navigation

---

## Anti-Patterns

Do not use:

- generic SaaS blue-white minimalism as the default
- soft pastel-only palettes
- oversized empty whitespace that reduces listing density
- giant rounded cards with weak hierarchy
- centered single-column landing page layouts for recruitment pages
- low-contrast text on gradients
- hidden or ambiguous CTAs
- visual styles that resemble fintech dashboards more than job portals
- glassmorphism-heavy UI across the full page
- luxury brand aesthetics or editorial magazine styling

Avoid these specific regressions:

- replacing orange-teal-blue contrast with one brand color only
- removing badge urgency language
- flattening hero promotion into a plain banner
- turning listing tabs into generic segmented controls without hierarchy
- weakening footer information architecture

---

## Pre-Delivery Checklist

Before delivering UI work, verify:

- homepage still feels like a recruitment marketplace, not a generic web app
- orange, teal, and navy roles are used consistently
- hero contains clear search and registration paths
- major CTAs remain high contrast
- job cards are dense, readable, and scannable
- hover states use lift or color emphasis without causing layout shift
- sticky header remains readable and functional
- dropdowns and floating panels have clear elevation
- mobile layout has no horizontal overflow
- tabs remain usable on touch devices
- footer still supports dense navigation and corporate trust signals
- focus states remain visible
- motion is present but restrained
