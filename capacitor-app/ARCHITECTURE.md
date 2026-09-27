# Architecture & Design Guide

## Overview

MyLightsController (Capacitor) is a **single-codebase, multi-platform** Home Assistant controller. It demonstrates how to build a cross-platform app without platform-specific UI code.

```
┌─────────────────────────────────────────────────────────────┐
│                       User Interfaces                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Web Browser │  │     iOS      │  │    Android App   │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└────────────┬──────────────┬──────────────────┬───────────────┘
             │              │                  │
             └──────────────┼──────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│              Capacitor Framework Bridge                       │
│  (Handles native APIs: camera, device, status bar, etc)     │
└────────────┬──────────────┬──────────────────┬───────────────┘
             │              │                  │
             └──────────────┼──────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                          │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐  │
│  │                    app.js                           │  │
│  │  Main app controller & lifecycle management         │  │
│  └────┬──────────────────────────────────────────────┬─┘  │
│       │                                              │     │
│  ┌────▼──────────┐  ┌──────────────┐  ┌──────────────▼──┐ │
│  │  UI Screens   │  │   Services   │  │  Config/Mode    │ │
│  │               │  │              │  │                 │ │
│  │ • Onboarding  │  │ • AppCtrl    │  │ • LOCAL mode    │ │
│  │ • DeviceGrid  │  │ • HAClient   │  │ • BACKEND mode  │ │
│  │ • Settings    │  │ • StateMgr   │  │ • Credentials   │ │
│  │               │  │ • Auth       │  │                 │ │
│  └───────────────┘  └──────────────┘  └─────────────────┘ │
│                                                              │
└──────────────────────────┬─────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                  Home Assistant REST API                     │
│                                                              │
│  Direct Connection (Local Mode)                            │
│  └─> http://192.168.x.x:8123/api/states                  │
│                                                              │
│  OR Backend Proxy (Backend Mode)                           │
│  └─> https://backend.example.com/api/states               │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## Core Design Principles

### 1. **Single Codebase**
- Write once, deploy everywhere
- No platform-specific UI code
- Shared business logic across all platforms
- Reduce maintenance burden by 50%+

### 2. **Local-First**
- Direct Home Assistant communication by default
- No external dependency for core functionality
- Better privacy: token never leaves your network
- Better performance: no extra hops

### 3. **Optional Backend**
- Build-time flag: `VITE_APP_MODE=local|backend`
- Runtime switching via settings (if backend mode enabled)
- Backward compatible with local-only setup
- Future-proof for scaling

### 4. **Observable State Pattern**
Similar to Kotlin Flow / RxJS:
```javascript
// Subscribe to state changes
const unsubscribe = appController.subscribe((state) => {
    updateUI(state.devices, state.isLoading, state.error);
});

// When model changes, all subscribers notified automatically
appController.setState({ devices: newDevices });
```

### 5. **Separation of Concerns**
```
UI Screens (onboarding.js, deviceGrid.js, settings.js)
        ↓ (calls)
AppController (orchestrator, business logic)
        ↓ (uses)
Services (haClient, auth, stateManager, etc)
        ↓ (communicate via)
REST API / Local Storage
```

## Directory Structure & Responsibilities

### `/src/app.js` - Main Entry Point
- Initializes Capacitor and app controller
- Manages screen navigation (onboarding ↔ main app)
- Subscribes to state changes
- Handles app lifecycle (pause, resume)
- ~150 lines

### `/src/services/appController.js` - Business Logic Orchestrator
- Central hub for all operations
- Coordinates: haClient, auth, stateManager
- Methods: `setupCredentials()`, `loadDevices()`, `toggle()`, `setBrightness()`, etc.
- Handles errors and state transitions
- ~300 lines
- **This is what you'd port from Android Services**

### `/src/services/haClient.js` - Home Assistant API Client
- Low-level REST API communication
- Methods: `getStates()`, `turnOn()`, `turnOff()`, `callService()`
- Axios for HTTP, with auth headers
- Response interceptors for error handling
- ~150 lines
- **Direct port of Android HomeAssistantApi.kt**

### `/src/services/deviceModels.js` - Device Types & Utilities
- Device class definition
- Utility functions: `isControllable()`, `getDeviceIcon()`, `parseEntityId()`
- Constants: `DEVICE_DOMAINS`, `DEVICE_STATES`
- ~200 lines
- **Direct port of Android HaDeviceModels.kt**

### `/src/services/auth.js` - Credential Management
- LocalStorage persistence
- Manages: HA URL, token, backend mode, backend URL
- Methods: `setHaCredentials()`, `isAuthenticated()`, `logout()`
- ~100 lines

### `/src/services/stateManager.js` - Observable State
- Similar to Kotlin StateFlow / RxJS BehaviorSubject
- Subscribers get notified on state changes
- Single source of truth for app state
- ~100 lines

### `/src/config/config.js` - Build-Time Configuration
- Determines if app is LOCAL or BACKEND mode
- Uses `__APP_MODE__` injected by Vite at build time
- Helpers: `getApiBaseUrl()`, `isDevelopment()`, `isProduction()`
- ~60 lines

### `/src/ui/onboarding.js` - Setup Screen
- Initial login/configuration screen
- Collects: HA URL, access token, optional backend URL
- "Test Connection" button to verify before saving
- ~250 lines
- **Similar to Android onboarding/OnboardingActivity**

### `/src/ui/deviceGrid.js` - Main Device Control Screen
- Responsive grid of device cards
- Each card has: toggle button, brightness slider
- Listens to user interactions
- Updates devices list when state changes
- ~200 lines
- **Similar to Android ui/DeviceGrid Compose**

### `/src/ui/settings.js` - Settings Panel
- Bottom sheet / modal with settings
- Shows connection info, current mode
- Logout button
- Mode switcher (if backend mode available)
- ~250 lines
- **Similar to Android ui/SettingsScreen**

### `/src/styles.css` - Global Styling
- Material Design 3 CSS variables
- Responsive layouts with CSS Grid/Flexbox
- Dark mode support via `prefers-color-scheme`
- No CSS framework (pure CSS)
- ~1000 lines

### `/capacitor.config.ts` - Capacitor Configuration
- App ID, name, web directory
- Plugin configuration
- Platform-specific settings

### `/vite.config.js` - Build Configuration
- Vite bundler setup
- Injects `__APP_MODE__` at build time
- Output directory: `www/`

### `/index.html` - Entry Point HTML
- Minimal HTML structure
- Three main containers:
  - `#app-container` - Onboarding screen
  - `#main-app` - Main app (header + device grid)
  - `#settings-container` - Settings overlay

## Data Flow Example: Toggling a Device

```
User clicks toggle button
        ↓
deviceGrid.js handleToggle() event listener fires
        ↓
appController.toggle(entityId)
        ↓
appController.haClient.toggle(entityId)
        ↓
axios.post('http://HA_URL/api/services/light/toggle', { entity_id })
        ↓
Home Assistant processes command
        ↓
appController.loadDevices() (refresh state)
        ↓
haClient.getStates()
        ↓
axios.get('http://HA_URL/api/states')
        ↓
stateManager.setDevices(devices)
        ↓
All subscribers notified via callback
        ↓
deviceGrid.render(devices) updates UI
        ↓
User sees light toggled on/off
```

## Build-Time Mode Switching

### How It Works

**Build Configuration:**
```bash
# Local mode build
VITE_APP_MODE=local npm run build
# → Generates app that only works locally

# Backend mode build
VITE_APP_MODE=backend npm run build
# → Generates app with backend URL switcher
```

**In Code:**
```javascript
// config.js
export const IS_LOCAL_MODE = __APP_MODE__ === 'local';
export const IS_BACKEND_MODE = __APP_MODE__ === 'backend';

// Usage
if (IS_LOCAL_MODE) {
    // Hide backend URL input
}

if (IS_BACKEND_MODE) {
    // Show backend URL input
}
```

**Why Build-Time?**
- No runtime overhead for unused features
- Smaller bundle for local-only users
- Simple environment variable configuration
- No conditional logic at runtime

## Local Mode vs Backend Mode

### Local Mode (Default)
```
Client App
    ↓
Home Assistant (direct REST API)
    ↓
Your devices
```

**Pros:**
- No server to maintain
- Direct communication
- Lower latency
- Better privacy (token stays local)

**Cons:**
- Home Assistant must be network-accessible
- Token stored in app
- No request logging/analytics

### Backend Mode
```
Client App
    ↓
Backend Server
    ↓
Home Assistant (via server)
    ↓
Your devices
```

**Pros:**
- Home Assistant can be private
- Backend handles auth
- Can add features (logging, caching, aggregation)
- More secure (token on server only)

**Cons:**
- Server maintenance
- Higher latency
- Single point of failure
- Backend complexity

## State Management Details

### StateManager Pattern
```javascript
// Subscribe to changes
const unsubscribe = stateManager.subscribe((newState) => {
    console.log('State changed:', newState);
});

// Update state
stateManager.setState({
    devices: [...],
    isLoading: false,
    error: null
});

// Unsubscribe when done
unsubscribe();
```

**Why not Redux/Vuex?**
- App state is simple (devices, loading, error, connection)
- Overkill for 4 state properties
- Reduces dependencies
- Easier to understand for new developers

**Similar to:**
- Kotlin: `StateFlow<AppState>`
- RxJS: `BehaviorSubject<AppState>`
- React: `useState` + context

## Error Handling

### Network Errors
```javascript
try {
    await appController.toggle(entityId);
} catch (error) {
    stateManager.setError(error);
    // UI shows error notification automatically
}
```

### Connection Errors
- Caught during `testConnection()`
- Displayed in onboarding screen
- User can retry with different credentials

### API Errors
- Axios interceptor catches responses
- 4xx: Auth/validation error
- 5xx: Server error
- Network timeout: Connection error

## Performance Optimizations

### Bundle Size
- No React/Vue/Angular: ~50KB smaller
- Vite tree-shaking: Removes unused code
- Gzipped: ~100KB total
- Compared to React equivalent: ~250KB

### Load Time
- Lazy load device grid only when needed
- Debounced input handlers
- Throttled polling (5 seconds default)
- Efficient DOM updates (full render, not VDOM)

### Network
- Configurable polling interval
- Early error detection
- Batch device updates
- Cache device list in localStorage (future)

## Testing Strategy

### Unit Tests (Future)
- Test `deviceModels.js` utilities
- Test `auth.js` localStorage
- Test `haClient.js` API formatting

### Integration Tests (Future)
- Mock Home Assistant API
- Test `appController` workflows
- Test state transitions

### E2E Tests (Future)
- Mock HA server
- Test full user journeys
- Test on multiple devices/browsers

## Extensibility

### Adding a New Device Type
1. Add domain to `DEVICE_DOMAINS` in deviceModels.js
2. Add icon/logic to `getDeviceIcon()`
3. Update `isControllable()` if needed
4. Add UI styling if needed

### Adding a New Screen
1. Create class in `src/ui/newScreen.js`
2. Implement `render()` method
3. Add to `app.js` navigation
4. Add route handling

### Adding a Backend Feature
1. Add endpoint to `backend/server.js`
2. Call from haClient with backend URL
3. Handle in UI layer

### Adding a Service
1. Create in `src/services/myService.js`
2. Export singleton instance
3. Integrate into `appController.js`
4. Use in UI components

## Capacitor Specifics

### What Capacitor Provides
- Native bridge for platform APIs
- Plugin system (camera, geolocation, etc)
- App lifecycle (pause, resume, back button)
- Platform-native builds from single web code

### What We're Using
- `@capacitor/app` - App lifecycle
- `@capacitor/device` - Device info

### What We Could Add
- `@capacitor/camera` - QR scanning
- `@capacitor/share` - Share device status
- `@capacitor/local-notifications` - Device alerts

## Future Enhancements

### High Priority
- [ ] WebSocket support for real-time updates
- [ ] Unit tests for services
- [ ] Error recovery strategies

### Medium Priority
- [ ] QR code setup (QR scanner → auto-fill credentials)
- [ ] Button-activated voice commands
- [ ] Device favorites/grouping
- [ ] Dark mode override toggle

### Low Priority
- [ ] Multi-user accounts
- [ ] Device automations/scenes
- [ ] Analytics dashboard
- [ ] Push notifications

## Comparison with Original Android App

| Aspect | Android (Kotlin) | Capacitor (JS) |
|---|---|---|
| Languages | Kotlin | JavaScript |
| UI Framework | Jetpack Compose | Vanilla HTML/CSS |
| Platforms | Android only | Web, iOS, Android, Desktop |
| Bundle Size | 10-20MB (APK) | 2-5MB (web) |
| Development Speed | Slower | Faster |
| Codebase | 2000+ lines Kotlin | 1500+ lines JS |
| Platforms | 1 | 4+ |
| Maintenance | High (Java ecosystem) | Lower (web standard) |

## Architecture Decisions & Trade-offs

### ✅ Chosen: Vanilla JavaScript
- **Why:** No build complexity, web standard
- **Trade-off:** No type safety (but could add JSDoc)

### ✅ Chosen: LocalStorage for Auth
- **Why:** Simple, reliable, cross-platform
- **Trade-off:** Not encrypted (but secure enough for local token)

### ✅ Chosen: Polling for Updates
- **Why:** Simple, no WebSocket complexity
- **Trade-off:** More network traffic, slight delay

### ✅ Chosen: CSS-in-file vs CSS Framework
- **Why:** No external dependency, full control
- **Trade-off:** More CSS to write, maintain

### ✅ Chosen: Build-time Mode vs Runtime Flag
- **Why:** Simpler UX, smaller bundle for local users
- **Trade-off:** Two builds instead of one

## Debugging Tips

### Browser DevTools
```javascript
// Access app globally
window.myLightsApp

// Get current state
window.myLightsApp.appController.getState()

// Check auth
window.myLightsApp.appController.authService.getCredentials()

// Trigger device load
await window.myLightsApp.appController.loadDevices()
```

### Logs
- All errors logged to console
- API calls logged with method/endpoint
- State changes logged with timestamp
- App lifecycle events logged

### Network Inspector
- All HTTP requests visible in Network tab
- Auth headers included
- Response times shown
- Errors with status codes

---

This architecture is designed to be simple, maintainable, and scalable. It prioritizes clarity over cleverness and separates concerns to make testing and future changes easier.

