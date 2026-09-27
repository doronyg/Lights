# ADR-001: Multi-Platform Architecture Decision

**Date:** September 2026  
**Status:** Proposed  
**Context:** Extending native Android app to support web browser access  

## Problem Statement

The Home Assistant Lights Controller was built as a native Android app using Kotlin and Jetpack Compose, providing an efficient control interface for managing lights and devices on a local NAS.

**Challenge:** Support for web browsers (desktop, tablet) in a family context requires either:
- Duplicating the entire feature set in a separate web framework
- Finding a framework that allows code sharing across platforms

This decision evaluates approaches to multi-platform development.

## Options Evaluated

### Option 1: Capacitor (Web Wrapper)

**Approach:** Wrap a TypeScript/JavaScript frontend using Capacitor's iOS/Android bridge.

**Implementation:**
- Single TypeScript codebase for UI/logic
- Capacitor handles native API bridges (permissions, device features)
- Web version runs via browser without wrapper

**Advantages:**
- Rapid UI development using familiar web technologies (HTML, CSS, JS)
- Shared codebase between mobile and web
- Large ecosystem with Ionic community support
- Can leverage existing web development knowledge
- Familiar tooling (npm, Vite, etc.)

**Disadvantages:**
- WebView wrapper introduces rendering overhead
- JavaScript-to-native bridge latency for state synchronization
- Dual-language maintenance: TypeScript/JavaScript for frontend vs. Kotlin for any native functionality
- Browser-based frontend encounters CORS restrictions when accessing local APIs
  - Requires separate backend proxy to forward requests
  - Adds architectural complexity
- Performance degradation on lower-end devices due to WebView overhead
- Type safety is weaker (JavaScript vs. Kotlin's strong typing)
- Does not leverage existing Kotlin codebase—starts from scratch

**Constraints Discovered:**
- CORS in browser context prevents direct API calls to local NAS
- Solution requires backend proxy (Node.js server) to bridge browser→HA communication
- This means the "single codebase" advantage is offset by backend infrastructure complexity

### Option 2: React Native

**Approach:** Rewrite both Android and iOS apps using React Native, with separate web solution.

**Implementation:**
- React Native for Android/iOS
- Separate web framework (React, Vue, etc.) for browser
- Share data models via TypeScript types

**Advantages:**
- Strong ecosystem and community
- Good mobile platform coverage
- Share component logic via libraries

**Disadvantages:**
- Does not solve web platform—still requires separate codebase
- JavaScript-based, not leveraging Kotlin expertise
- Similar performance implications as Capacitor (WebView for Android)
- Requires learning React Native-specific patterns
- Still ends up with dual-language maintenance (TypeScript + Kotlin if native bridges needed)
- Does not reduce overall complexity; adds to it

**Assessment:** Solves multi-platform mobile, but not multi-platform (mobile + web) without additional layers.

### Option 3: Kotlin Multiplatform + Compose Multiplatform

**Approach:** Migrate Android app to Compose Multiplatform, sharing business logic across Android and web (WASM) targets.

**Implementation:**
- Extract business logic (HA client, state management, data models) to `commonMain`
- Keep platform-specific rendering in `androidMain` (Jetpack Compose)
- Add `wasmJsMain` for browser deployment (Compose Multiplatform → WebAssembly)
- Use Ktor client across all platforms for HTTP communication

**Architecture:**
```
commonMain/
  ├── services/       (HA API client, authentication)
  ├── models/         (Device, State, etc.)
  └── viewmodels/     (Business logic)

androidMain/
  └── ui/             (Jetpack Compose native UI)

wasmJsMain/
  └── ui/             (Compose Multiplatform for browser)
```

**Advantages:**
- **Single language:** Kotlin across all platforms (Android, Web via WASM)
- **Code reuse:** Business logic, API clients, data models all shared
- **Native rendering:** 
  - Android uses native Compose (no WebView)
  - WASM compiles to Skia canvas rendering in browser
- **Type safety:** Strong typing across entire codebase
- **Familiar patterns:** Uses Kotlin coroutines and Jetpack patterns already understood
- **No language context switching:** Developer maintains single language expertise
- **Direct API access:** Android app makes direct HTTP calls (no CORS)
- **Performance:** No WebView wrapper; native execution on Android

**Disadvantages:**
- Kotlin/WASM ecosystem is newer than React/Vue
- Smaller community compared to mainstream web frameworks
- Requires learning Compose Multiplatform APIs (though Compose knowledge transfers)
- Build infrastructure more complex than single-platform

**Ecosystem Maturity:**
- Kotlin 2.0+ is stable and production-ready
- Compose Multiplatform is in rapid development but suitable for new projects
- Ktor client is mature and cross-platform
- WASM support in Kotlin is production-ready for non-performance-critical applications (local dashboards)

## Constraints & Learnings

### Browser CORS Problem
**Discovery:** A web frontend accessing a local Home Assistant API directly encounters CORS (Cross-Origin Resource Sharing) restrictions.

**Why:** Browsers enforce same-origin policy to prevent unauthorized cross-domain requests. This is a security feature, not a bug.

**Solutions:**
1. Backend proxy (used in Capacitor PoC)—forwards requests from browser to HA
2. CORS headers on HA itself—if you control the server, enable CORS
3. Server-to-server communication—web backend calls HA directly, not browser
4. Native platforms only—bypass CORS entirely by avoiding browser restrictions

**Implication:** Any browser-based solution requires either infrastructure (backend proxy/server) or configuration changes to HA. This is inherent to the web security model, not a framework limitation.

### Language & Cognitive Load
**Discovery:** Maintaining two languages for a single logical application increases bugs and context-switching overhead.

**Capacitor PoC:** 
- Frontend: TypeScript (270+ lines across multiple UI modules)
- Backend: Node.js JavaScript (88 lines)
- Mobile wrapper: Kotlin (Capacitor-generated, not user-modified)
- Total: 3 languages for a single logical system

**Kotlin Multiplatform:**
- All business logic: Kotlin (common)
- All UI: Kotlin (Compose)
- Total: 1 language for entire system

**Implication:** Reduces cognitive overhead and potential for inconsistencies.

### Performance Implications
**WebView (Capacitor):** Browser engine rendering, bridge latency for native calls
**Native (Kotlin Multiplatform on Android):** Direct native rendering, no bridge
**WASM (Kotlin Multiplatform on Web):** Compiled to machine code running in browser sandbox

For local NAS control (low-latency, local network), the performance difference may be negligible for UI interaction, but native always has less overhead.

## Decision

**CHOSEN: Kotlin Multiplatform + Compose Multiplatform**

### Rationale

1. **Language Consistency:** Single Kotlin codebase reduces maintenance overhead
2. **Leverages Expertise:** 3+ years of Kotlin and Compose knowledge already invested
3. **True Code Sharing:** Business logic, data models, and API clients shared across platforms
4. **Native Performance:** No WebView wrapper; direct rendering on Android
5. **Type Safety:** Strong typing across all platforms prevents entire classes of bugs
6. **Ecosystem:** Mature Kotlin ecosystem (coroutines, Ktor, serialization)
7. **Future-Proof:** Kotlin Multiplatform is the official multi-platform strategy for JetBrains

### Why Not Capacitor

While Capacitor works and the PoC is functional, it introduces:
- Language overhead (TypeScript + backend)
- Architectural complexity (browser can't call HA directly; needs proxy)
- WebView performance penalty
- Divergence from core expertise area

**Capacitor is better suited for:** Rapid prototyping when you're an expert in web technologies and want mobile presence without native development.

**For this project:** Native development with Kotlin is the better fit.

## Implementation Plan

### Phase 1: Extract Shared Layer (2 weeks)
- Move `HomeAssistantApi` (Ktor-based) to `commonMain`
- Move data models (`HaDevice`, `HaLightCommand`, etc.) to `commonMain`
- Move `ViewModels` and state management to `commonMain`
- Verify Android app still builds and functions

### Phase 2: Native Android Target (1 week)
- Ensure `androidMain` contains only Compose UI code
- Verify on actual Home Assistant instance
- Performance validation

### Phase 3: WASM Target (2 weeks)
- Add `wasmJsMain` source set
- Implement Compose Multiplatform UI for web
- Build static WASM bundle
- Deploy to NAS as static web application

### Phase 4: Integration & Documentation (1 week)
- Finalize architecture documentation
- Code examples for future maintainers
- Build/deployment scripts

**Total Timeline:** 6 weeks to production-ready multi-platform system

## Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|-----------|
| Kotlin/WASM ecosystem gaps | Medium | High | Start with basic features; fallback to JavaScript for complex features |
| Build complexity | Medium | Medium | Use Gradle Multiplatform template; document build process |
| IDE/tooling immaturity | Low | Medium | Monitor Kotlin/WASM releases; report issues upstream |
| Performance issues on WASM | Low | Medium | Profile early; optimize hot paths |

## References

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/help/compose-multiplatform/)
- [Kotlin/WASM](https://kotlinlang.org/docs/wasm-overview.html)
- [Ktor Client](https://ktor.io/docs/client-overview.html)

## Alternatives Considered But Not Evaluated

- **Flutter:** Great option if investing in Dart; doesn't leverage existing Kotlin knowledge
- **Native development (separate codebases):** Would duplicate 70% of code; not practical
- **Progressive Web App (PWA):** Doesn't solve CORS without backend; doesn't add multi-platform value

## Appendix: Capacitor PoC Artifacts

The `experiment/capacitor-poc` branch contains a working Capacitor implementation that was used to validate this architecture decision. Key files:

- `capacitor-app/src/services/haClient.js` — Kotlin equivalent exists in commonMain
- `capacitor-app/backend/server.js` — Demonstrates CORS proxy pattern; not needed with Kotlin Multiplatform
- `capacitor-app/src/ui/` — UI patterns inform Compose Multiplatform implementation

These are preserved for reference and learning purposes, not for production use.

