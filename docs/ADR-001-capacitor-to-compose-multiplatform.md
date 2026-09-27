# ADR-001: Capacitor POC to Compose Multiplatform Migration

**Date:** September 27, 2026  
**Status:** Accepted  
**Context:** Initial multi-platform exploration for Home Assistant Lights Controller  

## Problem Statement

The Home Assistant Lights Controller was initially built as a native Android app using Kotlin and Jetpack Compose. The goal was to extend this to support web browsers (for family tablets) and potentially iOS while maintaining code reuse.

## Options Evaluated

### Option 1: Capacitor (Web Wrapper)
**What:** Use Capacitor (Ionic's framework) to wrap a TypeScript/React web app with native capabilities.

**Pros:**
- Rapid prototyping with web technologies (HTML, CSS, JS)
- Single codebase approach (theory)
- Large ecosystem and community support
- Familiar to web developers

**Cons:**
- WebView wrapper introduces rendering overhead
- JavaScript-to-Native bridge latency for background state syncing
- Dual-language maintenance burden (TypeScript/JavaScript for web + Kotlin for Android)
- CORS restrictions in browser context require backend proxy
- Not leveraging existing Kotlin expertise
- JS bundle size and performance on low-end devices

### Option 2: React Native
**What:** Use React Native for cross-platform mobile development.

**Pros:**
- Good iOS/Android coverage
- JavaScript ecosystem

**Cons:**
- Does not solve web platform (still need separate solution)
- Requires learning React Native bridge layer
- Still JavaScript-based, not leveraging Kotlin knowledge
- Similar bridge latency issues as Capacitor

### Option 3: Kotlin Multiplatform + Compose Multiplatform (Chosen)
**What:** Migrate Android Compose app to Compose Multiplatform, with Kotlin/WASM target for web.

**Pros:**
- Single-language codebase (Kotlin across all platforms)
- Native execution on Android (no WebView overhead)
- Compose for UI—familiar to existing Android implementation
- Direct Kotlin/WASM compilation for browser dashboard
- Shared data models, ViewModels, and networking logic via commonMain
- No dual-language maintenance burden
- Better performance on local NAS deployment
- Strong type safety across platforms

**Cons:**
- Kotlin/WASM ecosystem is newer (but rapidly maturing)
- Smaller community than React Native/Capacitor
- Requires learning Compose Multiplatform APIs

## Decision

**CHOSEN: Kotlin Multiplatform + Compose Multiplatform**

We will:
1. Archive the Capacitor POC in the `experiment/capacitor-poc` branch
2. Migrate the existing Android Compose app to Compose Multiplatform
3. Extract shared logic into `commonMain` (data models, HA API client via Ktor, ViewModels)
4. Maintain `androidMain` for native Android-specific code
5. Add `wasmJsMain` for browser deployment on NAS

## Rationale

**Why not Capacitor?**
- We already have a working native Android implementation
- Capacitor introduces a WebView layer that adds latency and overhead
- The CORS problem Capacitor hits is a browser/security limitation, not a solution gap
- Maintaining two languages (Kotlin + TypeScript) goes against DRY principle
- For a local NAS controller, native performance matters more than "write once, run anywhere"

**Why Compose Multiplatform?**
- Leverages 3+ years of Kotlin and Compose expertise
- Compose is a modern declarative UI framework (not legacy Swing/Android Views)
- Kotlin/WASM is production-ready for our use case (static web builds, not high-traffic services)
- Ktor client works seamlessly across platforms
- Shared business logic = less code duplication and fewer bugs

## Implementation Plan

### Phase 1: Extract Shared Layer (Week 1-2)
- Move Home Assistant API client (Ktor-based) to `commonMain`
- Move data models (HaDevice, HaLightCommand, etc.) to `commonMain`
- Extract ViewModel logic to `commonMain`

### Phase 2: Native Android Target (Week 2-3)
- Verify existing `androidMain` Compose implementation works with extracted shared layer
- Test with actual Home Assistant instance
- Performance benchmarking

### Phase 3: WASM Target (Week 3-4)
- Add `wasmJsMain` source set
- Implement WASM-specific rendering (Skia canvas)
- Create static web build for NAS hosting
- Test browser dashboard functionality

### Phase 4: Documentation & Interview Materials (Week 4)
- Document decision process and learnings
- Create code examples showing shared Kotlin logic across platforms
- Record demo of unified architecture

## Lessons Learned

1. **Web frameworks have inherent limitations** — CORS isn't a technical problem to solve, it's a security model to design around
2. **Language homogeneity matters** — Maintaining a single language across platforms reduces cognitive load and bugs
3. **Platform-specific solutions beat generic ones** — Native Android execution > WebView wrapper for local apps
4. **Expertise alignment matters** — Using familiar tools (Kotlin, Compose) accelerates development

## Risks & Mitigations

| Risk | Mitigation |
|------|-----------|
| Kotlin/WASM immaturity | Start with Compose Multiplatform alpha; use stable Kotlin version |
| Ecosystem fragmentation | Leverage Kotlinx libraries (coroutines, serialization) proven across platforms |
| Learning curve for WASM | Prototype with simple components first |

---

**Approved by:** Doron Yakovlev Golani  
**Reviewed:** Self (AI-assisted exploration)

