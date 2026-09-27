# Interview Story: From Capacitor to Compose Multiplatform

**Project:** Home Assistant Lights Controller  
**Timeline:** 3-month evolution (July-September 2026)  
**Branches:** 
- `main` — Production Android app (Kotlin + Compose)
- `experiment/capacitor-poc` — Capacitor exploration & learnings

---

## The Story You'll Tell

### Setup (2 minutes)

*"I built a Home Assistant controller for my home NAS. The initial version was a native Android app in Kotlin using Jetpack Compose—works great for on-the-go control of lights and devices. But I wanted to extend it to tablets and web browsers so family members could also control lights from anywhere in the house."*

### The Challenge (3 minutes)

*"My first instinct was to use Capacitor to wrap a TypeScript frontend—get web + mobile from one codebase, right? So I built a POC with TypeScript, Vite, and created a backend proxy to handle CORS issues browsers have when talking to local APIs.*

*But as I built it out, I ran into problems:*
- *Maintaining two languages (TypeScript for web, Kotlin for Android) meant duplicating logic*
- *The WebView wrapper was adding latency—not ideal for a local app*
- *The CORS issue taught me something important: browser security isn't a bug to solve, it's a constraint to design around*
- *I had 3+ years of Kotlin expertise I was abandoning for JavaScript*

### The Pivot (2 minutes)

*"Instead of fighting the constraints, I re-evaluated my options:*
- *React Native? Doesn't solve web, still JavaScript-based*
- *Keep Capacitor? Works, but dual-language maintenance*
- *Kotlin Multiplatform? Leverage existing expertise, compile to multiple platforms*

*I chose Kotlin Multiplatform with Compose Multiplatform because:*
- *Single language across Android and web targets*
- *Compose is modern and declarative—I already knew it*
- *Kotlin compiles to WebAssembly for the browser, native code for Android*
- *The business logic (HA API client, ViewModels) is shared via a `commonMain` module*
- *No WebView overhead; native rendering on both platforms*"

### The Lesson (1-2 minutes)

*"This project taught me three things:*
1. *Explore technologies deeply, don't just adopt hype. I discovered that CORS is a security model, not a technical debt to eliminate*
2. *Your expertise matters. I had built Android apps for years; pivoting to JavaScript meant losing that leverage*
3. *Platform-specific solutions beat generic ones. A native Android app + WASM web build is better than a generic wrapper for everything*

*I documented this as an Architecture Decision Record (ADR) because understanding why you choose a tech stack is as important as the code itself."*

---

## What to Show Them

### 1. The ADR Document
**Path:** `docs/ADR-001-capacitor-to-compose-multiplatform.md`

**Why show it:**
- Demonstrates architectural thinking beyond "make it work"
- Shows you can weigh trade-offs (not just picking the fanciest tool)
- Proves you learn from mistakes and iterate

**Key talking points:**
- "I evaluated three approaches, not just picked one"
- "I understood the CORS problem deeply enough to know it wasn't solvable with Capacitor"
- "Leveraging existing expertise was a factor, not just technical merit"

### 2. The Capacitor Code (Archived)
**Branch:** `experiment/capacitor-poc`

**What they see:**
- Working TypeScript frontend with Material Design
- Node.js CORS proxy
- Android Capacitor wrapper
- Demonstrates you can build working code, even when pivoting

**Why it matters:**
- "I didn't theorize—I built it, experienced the pain, and decided"
- Shows incremental learning, not analysis paralysis

### 3. The Native Android App
**Branch:** `main` (production)

**What they see:**
- Well-structured Kotlin code
- Jetpack Compose implementation
- Clean architecture (HA API client, ViewModels, UI separation)
- Voice command integration
- Production-ready patterns (error handling, lifecycle management)

**Why it matters:**
- Shows your native Android expertise
- Proves the "core" product works and is deployable
- The foundation for the Compose Multiplatform migration

---

## How to Use This in Different Interview Scenarios

### "Tell us about a recent project"
Use the 8-minute story above. Interviewers love architecture decisions over feature lists.

### "How do you evaluate technologies?"
Reference the ADR. "I document trade-offs, not just implementation. Here's what I did for this project..."

### "Have you worked with multi-platform frameworks?"
"Yes, I evaluated Capacitor for this project. Learned why it wasn't the right choice, then designed Compose Multiplatform instead."

### "What's your experience with WebAssembly?"
"I'm planning to use Kotlin/WASM to compile shared business logic for the browser. Here's why I chose it over other approaches..."

### "Describe a time you had to learn new technology"
"I started with Capacitor/TypeScript for this, hit constraints, and pivoted to Kotlin Multiplatform. The journey taught me more than shipping one stack would have."

### "How do you handle technical debt?"
"Rather than accumulate dual-language maintenance debt, I recognized it early and reset. Here's the ADR documenting why..."

---

## Strongest Interview Angles

1. **Problem-Solving:** CORS → investigation → backend proxy → architecture rethink
2. **Learning Agility:** Capacitor → React Native evaluation → Kotlin Multiplatform selection
3. **Expertise Leverage:** Recognized when to use native platforms vs. generic wrappers
4. **Documentation:** ADR shows mature engineering practices
5. **Honest Evaluation:** Willing to admit an approach isn't working and pivot
6. **AI Collaboration:** Used AI to explore options and accelerate development (be transparent about this)

---

## Quick Talking Points to Memorize

**"Why Capacitor didn't work:"**
- "Browser CORS is a security constraint, not a problem to engineer around"
- "Dual-language maintenance (TS + Kotlin) was a hidden cost"
- "WebView performance for local apps is worse than native"

**"Why Kotlin Multiplatform:"**
- "Leverages 3+ years of Kotlin expertise"
- "Compose is modern and I already knew it"
- "Shared business logic in commonMain"
- "Native rendering on all platforms"

**"The key lesson:"**
- "Don't fight platform constraints—design around them"
- "Platform-specific > generic wrappers"
- "Your expertise is a tool, not a limitation"

---

## Next Steps on Your Roadmap

1. ✅ **Documented the Capacitor experiment** (done—this commit)
2. ⏳ **Migrate Android app to Compose Multiplatform** (main branch, Phase 1-2)
3. ⏳ **Add wasmJsMain target** for browser (Phase 3)
4. ⏳ **Deploy WASM build to NAS** as static web app
5. ⏳ **Record demo** of unified Android + Web app

When you've completed Phase 3, this becomes a compelling portfolio project:
- *"Native Android app + WASM web dashboard, 85% shared Kotlin code, deployed on personal NAS"*

That's interview gold. 🎯

---

## For More Details

See:
- **Architecture details:** `docs/ADR-001-capacitor-to-compose-multiplatform.md`
- **Experiment overview:** `EXPERIMENT_CAPACITOR_POC.md`
- **Production code:** `app/src/main/java/com/doronyg/mylightscontroller/`

---

**Remember:** The story isn't about the app—it's about how you think, learn, and make decisions. That's what employers actually hire for.

