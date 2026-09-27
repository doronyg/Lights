# Architecture Overview

## Current State: Native Android App

The Home Assistant Lights Controller is currently a native Android application built with:

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **HTTP Client:** OkHttp (via Ktor in future)
- **State Management:** StateFlow (reactive state)
- **API Integration:** Home Assistant REST API

### Current Architecture

```
┌─────────────────────────────────────────┐
│       Android App (Jetpack Compose)     │
│  ┌─────────────────────────────────┐    │
│  │  UI Layer                       │    │
│  │  ├─ Device Grid                 │    │
│  │  ├─ Settings                    │    │
│  │  └─ Onboarding                  │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │  ViewModel/State Management     │    │
│  │  ├─ AppViewModel                │    │
│  │  └─ StateFlow publishers        │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │  Service Layer                  │    │
│  │  ├─ HomeAssistantApi            │    │
│  │  ├─ AuthService                 │    │
│  │  └─ ConfigService               │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
              │
              │ HTTP/REST
              ▼
  ┌──────────────────────────┐
  │  Home Assistant (NAS)    │
  │  ├─ Light Control        │
  │  ├─ Device States        │
  │  └─ Automation/Triggers  │
  └──────────────────────────┘
```

### Key Components

**HomeAssistantApi** (`app/src/main/java/.../ha/HomeAssistantApi.kt`)
- Handles all HA REST API communication
- Device state fetching
- Command execution (turn on/off, brightness adjustment)
- Event listener pattern for state updates
- OkHttp-based HTTP client with Bearer token auth

**AuthService** (`app/src/main/java/.../services/auth.js` [TODO: Kotlin location])
- Manages Home Assistant credentials
- Stores/retrieves URL and access token
- Provides authentication state

**UI Components** (Compose)
- Responsive device grid layout
- Brightness controls
- Settings/configuration screens
- Accessibility-aware layouts for large system fonts

## Future: Kotlin Multiplatform Architecture

The architecture will evolve to support multiple platforms while maintaining code sharing.

### Proposed Multi-Platform Structure

```
┌─────────────────────────────────────────────────┐
│  Shared Business Logic (commonMain)             │
│  ┌───────────────────────────────────────────┐  │
│  │  API Client                               │  │
│  │  ├─ HomeAssistantClient (Ktor)            │  │
│  │  ├─ Request/Response serialization        │  │
│  │  └─ Error handling                        │  │
│  └───────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────┐  │
│  │  Data Models                              │  │
│  │  ├─ HaDevice, HaLight, HaSwitch           │  │
│  │  ├─ AppState                              │  │
│  │  └─ Configuration                        │  │
│  └───────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────┐  │
│  │  Business Logic (ViewModels)              │  │
│  │  ├─ Device state management               │  │
│  │  ├─ Authentication flow                   │  │
│  │  └─ Command execution & polling           │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
       │                              │
       │                              │
       ▼                              ▼
┌─────────────────────┐    ┌─────────────────────┐
│  Android (Native)   │    │  Web (WASM)         │
│  ┌───────────────┐  │    │  ┌───────────────┐  │
│  │ Compose UI    │  │    │  │ Compose UI    │  │
│  │               │  │    │  │ (Canvas based)│  │
│  │ androidMain/  │  │    │  │               │  │
│  │ ├─ Main screen│  │    │  │ wasmJsMain/   │  │
│  │ ├─ Grid view  │  │    │  │ (Skia render) │  │
│  │ └─ Settings   │  │    │  └───────────────┘  │
│  └───────────────┘  │    │                     │
│  HTTP client:       │    │  Static web app     │
│  Direct OkHttp      │    │  Served from NAS    │
│  (no CORS issues)   │    │  HTTP client:       │
│                     │    │  Ktor/Browser APIs  │
└─────────────────────┘    └─────────────────────┘
       │                              │
       └──────────┬───────────────────┘
                  │
                  │ HTTP/REST (Bearer token auth)
                  ▼
      ┌──────────────────────────┐
      │  Home Assistant (NAS)    │
      │  ├─ Light Control        │
      │  ├─ Device States        │
      │  └─ Automation/Triggers  │
      └──────────────────────────┘
```

### Code Organization

```
app/src/main/
├── commonMain/                    # Shared across all platforms
│   ├── kotlin/ha/
│   │   ├── HomeAssistantClient.kt (Ktor-based)
│   │   ├── ApiModels.kt           (Device, Light, Switch, etc.)
│   │   └── ApiEndpoints.kt        (API definitions)
│   ├── kotlin/services/
│   │   ├── AuthService.kt
│   │   ├── StateManager.kt
│   │   └── DeviceManager.kt
│   └── kotlin/models/
│       ├── AppState.kt
│       ├── Configuration.kt
│       └── Device.kt
│
├── androidMain/                   # Android-specific (Jetpack Compose)
│   ├── kotlin/ui/
│   │   ├── screens/
│   │   │   ├─ MainScreen.kt
│   │   │   ├─ DeviceGridScreen.kt
│   │   │   └─ SettingsScreen.kt
│   │   └─ components/
│   │       ├─ DeviceCard.kt
│   │       ├─ BrightnessControl.kt
│   │       └─ OnboardingFlow.kt
│   └── kotlin/platform/
│       └─ AndroidHttpClient.kt    (OkHttp integration)
│
└── wasmJsMain/                    # Web/WASM target (Compose MP)
    ├── kotlin/ui/
    │   ├─ screens/
    │   └─ components/
    └── kotlin/platform/
        └─ WasmHttpClient.kt       (Browser fetch integration)
```

## Design Decisions

### 1. Ktor for HTTP Clients

**Decision:** Use Ktor Client for all API communication (replaces OkHttp in shared code)

**Rationale:**
- Ktor works across all Kotlin Multiplatform targets
- Single HTTP API for common code
- Platform-specific engine implementation (OkHttp on Android, HttpClient on WASM)
- Coroutine-based, matches existing async patterns

### 2. Shared State Management

**Decision:** Use StateFlow/MutableStateFlow pattern in common code

**Rationale:**
- Kotlin coroutines work across all platforms
- Matches existing Android patterns
- Reactive updates for both native and web UI

### 3. Composition Over Inheritance

**Decision:** Use service composition for platform-specific features

**Example:** HTTP client abstraction
```kotlin
// commonMain
expect class HttpClient
actual class HttpClient // androidMain: uses OkHttp
actual class HttpClient // wasmJsMain: uses browser fetch
```

### 4. No Framework Dependencies in Common Code

**Decision:** Business logic remains framework-agnostic

**Rationale:**
- Easier to test
- Cleaner separation between platform-specific and shared code
- Allows different UI frameworks on different platforms if needed

## Data Flow: User Interaction

### Turn On a Light (Example)

```
User clicks "ON" button
  │
  ▼
UI Event Handler (Compose onClick callback)
  │
  ▼
ViewModel.turnOnLight(deviceId) [commonMain]
  │
  ▼
StateManager.setLoading(true) [commonMain]
  │
  ├─ Update UI reactively (StateFlow emission)
  │
  ▼
HomeAssistantClient.callService("light", "turn_on", {...}) [commonMain]
  │
  ▼
HttpClient [platform-specific]
  │
  ├─ Android: OkHttp.post(url, body)
  │
  └─ Web: fetch(url, {method: 'POST', body})
  │
  ▼
Home Assistant API endpoint
  │
  ▼
Light turns on
  │
  ▼
Response back through HttpClient
  │
  ▼
StateManager.setDevices(updated) [commonMain]
  │
  ▼
StateFlow emission
  │
  ▼
UI re-renders (Compose recomposition)
```

## API Integration Pattern

### HomeAssistantClient (commonMain)

```kotlin
class HomeAssistantClient {
    suspend fun getStates(): List<HaDevice>
    suspend fun callService(domain: String, service: String, data: Map<String, Any>)
    suspend fun turnOn(entityId: String, brightness: Int? = null)
    suspend fun turnOff(entityId: String)
}
```

### Error Handling

- Network errors caught and converted to user-facing messages
- Retry logic with exponential backoff (in StateManager)
- User notification via UI error states

## Performance Considerations

### Android (Native)

- Direct OkHttp HTTP client (no overhead)
- Jetpack Compose rendering (optimized for Android)
- Coroutines native to platform
- Expected: <100ms response times for local network calls

### Web (WASM)

- Compiled Kotlin bytecode to WebAssembly
- Browser fetch API for HTTP (native in browser sandbox)
- Compose Multiplatform canvas rendering (Skia)
- Expected: Same as native (WASM execution is efficient for CPU-bound work; network I/O is bottleneck)

### Optimization Strategies

1. **Local Caching:** Store device state locally; update optimistically
2. **Polling:** Periodic sync instead of real-time listeners (simpler, sufficient for UI)
3. **Request Batching:** Combine multiple device commands into single API call if possible
4. **UI Responsiveness:** Immediate UI updates before API response (optimistic)

## Platform-Specific Considerations

### Android

- Lifecycle management (pause/resume)
- Background service for polling (if needed)
- Notification integration for state changes
- Permission handling for camera (if QR scanning added)

### Web

- Session storage for credentials (localStorage with security considerations)
- Browser tab visibility API for pause/resume behavior
- Static asset serving from NAS (no backend required)
- Can be served directly from Home Assistant if CORS configured

## Security Model

### Credentials Storage

**Android:**
- Encrypted SharedPreferences via Android KeyStore
- Token stored locally, not transmitted

**Web (WASM):**
- localStorage (browser's local storage, same-origin only)
- Alternative: sessionStorage for single-session use
- Token sent via Bearer header in HTTP requests

**Note:** Both assume local network use (home network). For internet-accessible HA, additional security considerations needed (TLS, secure token refresh, etc.).

### API Authorization

- All requests include Bearer token in Authorization header
- Home Assistant validates token server-side
- Direct API calls (no proxy needed for Android)
- Web frontend calls directly (if CORS enabled) or through backend proxy (if not)

## Testing Strategy

### Unit Tests (Common Code)

- HomeAssistantClient (mock HTTP responses)
- StateManager (state transitions)
- Device models (serialization/deserialization)

### Integration Tests

- Real HA instance (staging environment)
- API contract testing

### UI Tests

- Android: Compose test framework
- Web: Testing Library or Playwright

## Migration Path from Capacitor

The `experiment/capacitor-poc` branch demonstrated:
- ✅ Basic UI/UX patterns (inform Compose Multiplatform design)
- ✅ API client patterns (inform Ktor client structure)
- ✅ Polling mechanism for state sync
- ❌ CORS proxy complexity (eliminated by native/WASM direct access)

The Kotlin Multiplatform approach eliminates the CORS proxy while maintaining all working features.

## Future Enhancements

1. **Real-time Sync:** WebSocket or SSE for event-driven updates (vs. polling)
2. **Automation Rules:** Create/edit automations from UI
3. **Scene Support:** Pre-defined scene activation
4. **Voice Control:** Integration with device voice assistants
5. **Progressive Web App:** Add Service Worker for offline capability (web only)
6. **iOS Target:** Add iosMain with native Swift interop if needed

## Conclusion

Kotlin Multiplatform provides a clean, maintainable path to multi-platform support while leveraging existing Kotlin expertise and eliminating complexity from dual-language codebases. The architecture prioritizes code sharing for business logic while maintaining platform-specific optimizations for UI rendering.

