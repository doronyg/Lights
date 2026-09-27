# Capacitor POC Experiment Branch

**Branch:** `experiment/capacitor-poc`  
**Date Created:** September 27, 2026  
**Status:** Archived (see ADR-001)  

## Overview

This branch contains the Capacitor proof-of-concept for cross-platform (Android + Web) control of Home Assistant devices. After evaluation, this approach was **not** selected for production. See [`docs/ADR-001-capacitor-to-compose-multiplatform.md`](../docs/ADR-001-capacitor-to-compose-multiplatform.md) for decision rationale.

## What's Here

- **`capacitor-app/`** — Full Capacitor project with TypeScript frontend, Vite bundling, and mobile wrapper
  - `src/` — TypeScript/HTML/CSS UI code
  - `android/` — Capacitor Android wrapper
  - `ios/` — Capacitor iOS wrapper (placeholder)
  - `backend/` — Node.js backend proxy (addresses CORS issues in browser)
  - `vite-project/` — Modern build tooling

## Key Findings

### ✅ What Worked
- Rapid UI development with web technologies
- Good for visual iteration
- Simple API integration with Home Assistant

### ❌ What Didn't Fit
- **WebView overhead** — Rendering through browser wrapper added latency
- **Dual-language maintenance** — TypeScript + Kotlin → context switching
- **CORS complexity** — Browser security model required backend proxy workaround
- **Platform-agnostic performance loss** — Unnecessary abstraction for local NAS app
- **Divergence from expertise** — Built on JavaScript, not leveraging Kotlin depth

## Why This Branch Exists

This is **learning material** for:
1. **Interview storytelling** — Shows how to evaluate technologies and make informed decisions
2. **Architecture thinking** — Demonstrates weighing pros/cons of different approaches
3. **Problem-solving** — CORS problem → proxy solution → re-evaluating the whole approach
4. **Not repeating mistakes** — Documents why Capacitor wasn't chosen

## Next Steps

**Do not use this branch for production.** Instead:

1. Check out `main` branch
2. Review the Android native app in `app/src/main/java/com/doronyg/mylightscontroller/`
3. Follow the Compose Multiplatform migration plan in ADR-001

## Reference Commits

- **Initial Capacitor setup** — Explored rapid web prototyping
- **Backend proxy implementation** — Addressed CORS with Node.js server
- **Architecture investigation** — Evaluated React Native and other options

## Learning Resources from This Experiment

### CORS Problem Analysis
See `capacitor-app/backend/server.js` for a working CORS proxy. The lesson: **CORS isn't a bug, it's a feature.** Don't fight browser security; design around it.

### API Integration Patterns
See `capacitor-app/src/services/haClient.js` for HA API client in TypeScript. Useful for understanding Ktor equivalents in Kotlin.

### UI Components
See `capacitor-app/vite-project/src/` for Compose-like component patterns in React. These concepts map to Compose Multiplatform.

---

## Quick Start (if exploring)

```bash
# Install dependencies
cd capacitor-app
npm install

# Run web dev server
npm run dev

# Build web
npm run build

# Build Android
npm run build:android
```

⚠️ **Note:** This is a historical artifact. Capacitor may have received updates since this POC was created. For production work, see the main branch.

