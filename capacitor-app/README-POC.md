# Capacitor Proof-of-Concept (PoC)

## Overview

This directory contains a working proof-of-concept implementation of the Home Assistant Lights Controller using Capacitor, a framework for building cross-platform apps with web technologies.

**Status:** Archived - This approach was evaluated but not selected for production. See `docs/ADR-001-multiplatform-architecture-decision.md` for the full decision analysis.

## Purpose

This PoC was created to evaluate Capacitor's viability for multi-platform (Android + Web) development of a Home Assistant control dashboard.

**Key questions addressed:**
1. Can a web-based UI work for controlling local devices?
2. How does Capacitor handle platform-specific features?
3. What are the real constraints when building for both mobile and web from one codebase?

## Architecture

```
Frontend (TypeScript/JavaScript):
├─ UI Components (Compose-like grid layout)
├─ Service Layer (haClient.js for API calls)
├─ State Management (stateManager.js)
└─ Capacitor Integration (platform bridges)

Backend (Optional Node.js proxy):
└─ CORS proxy for browser→HA communication
   (necessary due to browser same-origin policy)

Mobile Wrapper:
├─ Android (Capacitor Cordova bridge)
└─ iOS (Capacitor Cordova bridge)
```

## Key Findings

### What Worked Well

1. **Rapid UI Development** — HTML/CSS/JavaScript allowed fast iteration on UI
2. **Functional Integration** — Successfully integrated with Home Assistant API
3. **Capacitor Abstraction** — Proper separation between web code and mobile bridges
4. **Device Control** — Real-time control of lights and switches demonstrated successfully

### Constraints Discovered

1. **CORS Limitations**
   - Problem: Browser security policy prevents direct XMLHttpRequest to local API
   - Solution implemented: Node.js backend proxy that:
     - Receives browser requests
     - Forwards to Home Assistant on local network
     - Returns response to browser
   - Implication: Requires backend infrastructure, not truly "single codebase"

2. **Language Heterogeneity**
   - Frontend: TypeScript/JavaScript
   - Backend (if used): Node.js/JavaScript
   - Native bridges: Java/Kotlin (Android) or Swift (iOS)
   - Maintenance challenge: Multiple language contexts to keep in sync

3. **WebView Overhead**
   - Android runs app in WebView (browser engine), not native rendering
   - Performance impact: Latency for DOM manipulation + event handling
   - Not suitable for performance-critical local control

4. **Dual Type Systems**
   - Frontend uses TypeScript for some type checking
   - Backend would need separate models/validation
   - No shared type definitions across layers

### Architectural Complexity

Even with Capacitor's clean abstraction, the system requires:

```
Browser ─(CORS-blocked)─X─ Home Assistant
  │
  └─→ Backend Proxy ──→ Home Assistant
```

This is simpler than a full microservices architecture, but adds infrastructure complexity for a local-only app.

## File Structure

```
capacitor-app/
├── src/                          # Web application code (TypeScript/JS)
│   ├── app.js                    # Main app entry point
│   ├── config/
│   │   └── config.js             # Configuration management
│   ├── services/
│   │   ├── haClient.js           # Home Assistant API client
│   │   ├── appController.js      # Business logic orchestration
│   │   ├── stateManager.js       # Reactive state management
│   │   ├── auth.js               # Authentication service
│   │   └── deviceModels.js       # Device data models
│   ├── ui/
│   │   ├── deviceGrid.js         # Device grid UI component
│   │   ├── onboarding.js         # Setup flow UI
│   │   ├── settings.js           # Settings screen
│   │   └── qrScanner.js          # QR code scanning (if device camera available)
│   └── styles.css                # Global styling
├── backend/                       # Optional Node.js proxy server
│   ├── server.js                 # CORS proxy implementation
│   └── package.json              # Backend dependencies
├── android/                       # Capacitor Android wrapper
│   ├── app/                       # Gradle build configuration
│   └── build.gradle              # Android build settings
├── ios/                           # Capacitor iOS wrapper (placeholder)
├── vite-project/                 # Alternative Vite-based build
└── package.json                  # Web dependencies
```

### Key Modules

**haClient.js (186 lines)**
- Axios-based HTTP client for Home Assistant API
- Handles authentication via Bearer token
- Methods: getStates(), callService(), turnOn(), turnOff(), setBrightness()
- Response interceptor for error handling
- Polling-based subscription to state changes (simple, not optimal)

**stateManager.js (107 lines)**
- Reactive state container similar to Android StateFlow
- Subscribe/notify pattern for UI updates
- Holds: devices[], isLoading, error, isConnected, lastUpdate

**appController.js (311 lines)**
- Orchestrates haClient and stateManager
- Business logic: credential setup, device loading, polling
- Bridges between UI layer and services

**server.js (88 lines)**
- Minimal CORS proxy (if needed)
- Routes GET /api/states and POST /api/services/:domain/:service
- Credentials passed as query parameters
- For deployment: Would need environment-based configuration

### UI Implementation

**Compose-like patterns:**
- DeviceGridUI renders list of device cards
- Each card has toggle + brightness slider
- State-driven rendering (no direct DOM manipulation in business logic)
- Event delegation for button/slider interactions

## Why This Branch Exists

### Learning Value
- Demonstrates how CORS constraints arise in practice
- Shows what a web-based HA dashboard looks like
- Provides reference for Kotlin Multiplatform UI patterns

### Comparison Point
- Baseline for evaluating other approaches
- Documents actual constraints (not theoretical)
- Proves feasibility but reveals complexity

### Not Production Use
- Capacitor was evaluated and determined not optimal for this use case
- Kotlin Multiplatform chosen instead
- This code remains as reference, not maintained for new features

## Lessons for Multi-Platform Development

1. **Framework choice has downstream effects** — Capacitor's web-wrapper design caused CORS issues that native development doesn't face

2. **Language consistency matters** — Multiple language contexts (TS frontend, JS backend, Java native) = more maintenance burden

3. **Platform constraints are real** — Browser security policy isn't a limitation to work around; it's a constraint to design for

4. **Direct platform access is valuable** — Native Android can make direct HTTP calls to local HA; browsers cannot (by design)

## Comparison to Selected Approach (Kotlin Multiplatform)

| Aspect | Capacitor PoC | Kotlin Multiplatform |
|--------|---------------|----------------------|
| Languages | TypeScript, Node.js, Java | Kotlin (all platforms) |
| Android Rendering | WebView | Native Compose |
| Web Rendering | Browser | WASM (compiled native code) |
| API Calls | Browser (CORS issue) + Proxy | Direct (native) + Direct (WASM) |
| Code Sharing | UI patterns only | Business logic + Models + ViewModels |
| Type Safety | Partial (TS + JS) | Full (Kotlin across all) |
| Backend Need | Yes (CORS proxy) | No |
| Maintenance Burden | Higher (multiple languages) | Lower (single language) |

## If You're Exploring This Branch

**To run the Capacitor PoC:**

```bash
# Install dependencies
npm install

# Run development server (web)
npm run dev

# Build Android
npm run build:android

# Build web for production
npm run build
```

**To understand the decision process:**

See `docs/ADR-001-multiplatform-architecture-decision.md` for the full evaluation.

**To see Kotlin Multiplatform version:**

Check `main` branch (in progress) or planned migration.

## Conclusion

The Capacitor PoC successfully proved that web-based control of Home Assistant is viable. However, the architecture needed additional infrastructure (backend proxy) and multiple language contexts, which made it less optimal than Kotlin Multiplatform for this specific use case.

This branch documents a legitimate architectural exploration—not a failure, but a learning that informs the better choice.

