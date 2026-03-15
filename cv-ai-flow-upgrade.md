# CV AI Flow Upgrade

## Goal
Refactor cv-editor.html to implement the new flexible 4-entry-point flow with live preview and better post-gen UX.

## Tasks
- [x] Task 1: Add Onboarding Screen (4 entry points: Profile, Quick AI, Quick Form, Chat)
- [x] Task 2: "Use Profile" flow — call /api/user/me and pre-fill cvData, skip to generate
- [x] Task 3: "Quick AI" flow — show inline mini-form (name, target role, industry, exp), then generate
- [x] Task 4: Live preview — render CV progressively after basic info collected (name/subtitle/contact)
- [x] Task 5: Post-gen G-branch UI — show 4 action cards (Interview, Block-edit, Optimize, Change template)
- [x] Task 6: Backend — add /api/cv-ai/parse-profile to convert user profile → cvJson quickly

## Done When
- [ ] User lands on cv-editor.html, sees onboarding overlay with 4 choices
- [ ] Choosing "Profile" auto-fills and generates CV immediately
- [ ] Choosing "Quick AI" shows 4-field form and generates in 1 step
- [ ] After generation, user sees 4 branching options clearly
