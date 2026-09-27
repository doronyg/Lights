# Visual Architecture & Flow Diagrams

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    USER DEVICES / PLATFORMS                     │
├─────────────┬──────────────┬──────────────┬──────────────────────┤
│   Browser   │    iPhone    │   Android    │   Desktop/Electron   │
│  (Web App)  │   App (iOS)  │   App (APK)  │   (Tauri/Electron)   │
└──────┬──────┴──────┬───────┴──────┬───────┴──────────┬───────────┘
       │             │              │                  │
       └─────────────┼──────────────┼──────────────────┘
                     │
       ┌─────────────▼──────────────────────────┐
       │    Capacitor Framework Bridge          │
       │  (Handles native APIs on each platform)│
       └──────────────┬──────────────────────────┘
                      │
       ┌──────────────▼──────────────────────────┐
       │   Shared JavaScript Application Layer   │
       │                                         │
       │  ┌─────────────────────────────────┐   │
       │  │  UI Layer (onboarding, grid)    │   │
       │  └────────────┬────────────────────┘   │
       │               │ calls                   │
       │  ┌────────────▼────────────────────┐   │
       │  │  App Controller (orchestrator)  │   │
       │  │  • setupCredentials()           │   │
       │  │  • loadDevices()                │   │
       │  │  • toggle(), setBrightness()    │   │
       │  └────────────┬────────────────────┘   │
       │               │ uses                    │
       │  ┌────────────▼────────────────────┐   │
       │  │  Services Layer                 │   │
       │  │  • haClient (REST)              │   │
       │  │  • deviceModels (logic)         │   │
       │  │  • auth (credentials)           │   │
       │  │  • stateManager (observable)    │   │
       │  └────────────┬────────────────────┘   │
       │               │ communicates with      │
       └───────────────┼────────────────────────┘
                       │
       ┌───────────────▼────────────────────────────────┐
       │    HOME ASSISTANT (Your Smart Home Hub)       │
       │                                                │
       │  Direct Connection (Local Mode)               │
       │  └─> http://192.168.1.100:8123                │
       │                                                │
       │  OR via Backend Proxy (Backend Mode)          │
       │  └─> https://backend.example.com/api ─────┐  │
       │                                            │  │
       └────────────────────────────────────────────┼──┘
                                                    │
                                    ┌───────────────▼──────┐
                                    │  Backend Server      │
                                    │  (Optional)          │
                                    │  • Proxy HA API      │
                                    │  • Cache/logging     │
                                    │  • User management   │
                                    └──────────────────────┘
```

## Application Data Flow: Toggle a Light

```
User clicks "OFF" button on device card
        │
        ▼
DeviceGridUI.handleToggle(entityId)
        │
        ▼
appController.toggle(entityId)
        │
        ├─ Calls: haClient.toggle(entityId)
        │         │
        │         ▼
        │    axios.post('http://HA_URL/api/services/light/toggle', {
        │                entity_id: "light.living_room"
        │            })
        │         │
        │         ▼
        │    Home Assistant processes command
        │    └─> Physical device turns on
        │
        ├─ Updates state: stateManager.setState({ isLoading: true })
        │ (UI shows loading spinner)
        │
        ├─ Calls: await appController.loadDevices()
        │         │
        │         ▼
        │    haClient.getStates()
        │    └─> axios.get('http://HA_URL/api/states')
        │    └─> Returns updated state list
        │
        ├─ Updates state: stateManager.setState({ devices, isLoading: false })
        │
        ▼
stateManager notifies all subscribers
        │
        ▼
UI components receive new state
        │
        ▼
DeviceGridUI.refresh(devices) updates device card
        │
        ▼
User sees light is now "ON" ✓
```

## State Management Flow

```
Initial State
    │
    ├─ devices: []
    ├─ isLoading: false
    ├─ error: null
    ├─ isConnected: false
    └─ lastUpdate: null
    
    ▼
    
User action (e.g., click toggle)
    │
    ▼
AppController updates state
    stateManager.setState({ isLoading: true })
    
    ▼
    
Updated State
    ├─ devices: [...same...]
    ├─ isLoading: true ◄─── Changed
    ├─ error: null
    ├─ isConnected: true
    └─ lastUpdate: "2024-01-01T12:00:00Z" ◄─── Auto-updated
    
    ▼
    
State Manager notifies all subscribers
    callback(newState)
    callback(newState)
    callback(newState)
    
    ▼
    
UI Components receive new state
    onboarding.js: ignored (not visible)
    deviceGrid.js: re-renders with isLoading=true
    settings.js: ignored (not open)
    
    ▼
    
Device Grid renders loading spinner
    
    ▼
    
(Meanwhile, API request completes)
    
    ▼
    
AppController receives response
    stateManager.setState({
        devices: updatedDevices,
        isLoading: false,
        error: null,
        isConnected: true
    })
    
    ▼
    
State Manager notifies subscribers again
    
    ▼
    
Device Grid receives updated devices
    Re-renders with new state
    Spinner disappears
    Device shows new state
```

## Build Modes

```
┌──────────────────────────────────────────────────────────────┐
│                    npm run build                              │
│              VITE_APP_MODE environment variable               │
└──────────────────────────────────────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ▼                             ▼
┌───────────────────────┐   ┌──────────────────────┐
│   LOCAL MODE          │   │   BACKEND MODE       │
│ (Default)             │   │ (Advanced)           │
├───────────────────────┤   ├──────────────────────┤
│ VITE_APP_MODE=local   │   │ VITE_APP_MODE=backend│
│                       │   │                      │
│ Build Output:         │   │ Build Output:        │
│ • index.html          │   │ • index.html         │
│ • app.js (~150KB)     │   │ • app.js (~180KB)    │
│ • styles.css          │   │ • styles.css         │
│                       │   │                      │
│ Features Hidden:      │   │ Features Visible:    │
│ • Backend URL input   │   │ • Backend URL input  │
│ • Mode switcher       │   │ • Mode switcher      │
│                       │   │                      │
│ Runtime:              │   │ Runtime:             │
│ App → Home Assistant  │   │ App → Backend Server │
│ (Direct connection)   │   │    → Home Assistant  │
└───────────────────────┘   └──────────────────────┘
```

## Component Hierarchy

```
AppController (Singleton)
├─ Manages: haClient, auth, stateManager
├─ Methods: initialize, setupCredentials, toggle, setBrightness, etc.
└─ Emits: State changes via stateManager
    
    ├─ haClient (Singleton)
    │   ├─ axios instance (configured)
    │   ├─ Methods: getStates, turnOn, turnOff, toggle, setBrightness
    │   └─ API: REST calls to Home Assistant
    │
    ├─ auth (Singleton)
    │   ├─ localStorage (persistent)
    │   ├─ Properties: haUrl, haToken, useBackend, backendUrl
    │   └─ Methods: setCredentials, isAuthenticated, logout
    │
    └─ stateManager (Singleton)
        ├─ state: { devices, isLoading, error, isConnected, lastUpdate }
        ├─ subscribers: [callback1, callback2, ...]
        └─ Methods: subscribe, setState, getState, reset
        
            UI Components (Instances created per screen)
            
            ├─ OnboardingUI
            │   ├─ Listens to: user input (form submit)
            │   ├─ Calls: appController.setupCredentials()
            │   └─ Shows: onboarding form or success
            │
            ├─ DeviceGridUI
            │   ├─ Subscribes to: stateManager (devices, isLoading)
            │   ├─ Listens to: toggle button, brightness slider
            │   ├─ Calls: appController.toggle(), setBrightness()
            │   └─ Shows: device cards grid
            │
            └─ SettingsUI
                ├─ Shows: connection info, mode toggle, logout
                ├─ Listens to: mode radio buttons, logout button
                ├─ Calls: appController.switchMode(), logout()
                └─ Uses: auth.getCredentials()
```

## File Dependency Graph

```
index.html (entry point)
    │
    └─ app.js
        │
        ├─ @capacitor/app (Capacitor framework)
        │
        ├─ services/appController.js (main orchestrator)
        │   │
        │   ├─ services/haClient.js (API client)
        │   │   └─ axios (HTTP library)
        │   │
        │   ├─ services/deviceModels.js (models & utilities)
        │   │   └─ (no external dependencies)
        │   │
        │   ├─ services/auth.js (authentication)
        │   │   └─ localStorage (browser API)
        │   │
        │   └─ services/stateManager.js (state management)
        │       └─ (no external dependencies)
        │
        ├─ ui/onboarding.js (screen component)
        │   └─ services/appController.js (calls methods)
        │
        ├─ ui/deviceGrid.js (screen component)
        │   └─ services/appController.js (calls methods)
        │
        ├─ ui/settings.js (screen component)
        │   └─ services/appController.js (calls methods)
        │
        └─ config/config.js (configuration)
            └─ __APP_MODE__ (injected by Vite)

styles.css (stylesheet)
    └─ (no dependencies, standalone CSS)

package.json (manifest)
    ├─ @capacitor/app@^6.0.0
    ├─ @capacitor/core@^6.0.0
    └─ axios@^1.6.0
```

## Platform Build Process

```
Source Code (JavaScript)
    │
    ├─────────┬──────────┬──────────┐
    │         │          │          │
    ▼         ▼          ▼          ▼
  Vite      Vite      Vite       Vite
 Build     Build     Build      Build
 LOCAL    BACKEND     iOS      ANDROID
    │         │          │        │
    ▼         ▼          ▼        ▼
  www/    www/       Capacitor  Capacitor
  (300KB) (350KB)    sync       sync
    │         │          │        │
    ├─────────┼──────────┼────────┤
    │         │          │        │
    ▼         ▼          ▼        ▼
  Deploy   Deploy     Xcode    Android
  to HTTP  to HTTP    Build    Studio
  Server   Server   (iOS App)  (APK/AAB)
    │         │          │        │
    ▼         ▼          ▼        ▼
  WebApp   WebApp+    AppStore  PlayStore
  (Local)  Backend    (iPhone)  (Android)
```

## State Subscription Pattern

```
┌─ stateManager (Observable)
│
│   Internal State
│   ├─ devices: Device[]
│   ├─ isLoading: boolean
│   ├─ error: string | null
│   ├─ isConnected: boolean
│   └─ lastUpdate: ISO string
│
│
├─ Subscriber 1: DeviceGridUI
│   └─ callback(state) {
│       updateDeviceCards(state.devices)
│       showSpinner(state.isLoading)
│       showError(state.error)
│   }
│
├─ Subscriber 2: App Main
│   └─ callback(state) {
│       if (!state.isConnected) showError()
│   }
│
└─ Subscriber N: ...
    └─ callback(state) { ... }


When state changes:
appController.setState({ devices: [...] })
    │
    ▼
stateManager.state = { ... }
    │
    ▼
for each subscriber, call callback(newState)
    │
    ├─ Subscriber1.callback() triggered
    │├─ Re-render device grid
    │└─ Update UI
    │
    ├─ Subscriber2.callback() triggered
    │├─ Check connection status
    │└─ Show/hide error
    │
    └─ SubscriberN.callback() triggered
     └─ ...
```

## Error Handling Flow

```
Try Operation (e.g., toggle light)
    │
    ├─ Step 1: Call appController.toggle(entityId)
    │
    ├─ Step 2: Call haClient.toggle(entityId)
    │   │
    │   ├─ Step 2a: axios.post(...)
    │   │   │
    │   │   └─ Axios interceptor catches response
    │   │       │
    │   │       ├─ 2xx Success → return data
    │   │       └─ 4xx/5xx Error → throw Error
    │   │
    │   └─ Catch block in haClient
    │       └─ throw Error with message
    │
    ├─ Catch block in appController
    │   │
    │   ├─ Log error: console.error()
    │   └─ Update state: stateManager.setError(error)
    │
    └─ UI receives error in state
        │
        ├─ DeviceGrid sees error
        └─ Shows error notification to user
           (Automatically clears after 3 seconds)
```

These diagrams show:
1. **System Architecture** - How all components connect
2. **Data Flow** - How user actions trigger changes
3. **State Management** - How state flows through app
4. **Build Process** - How source becomes deployed app
5. **Error Handling** - How errors are caught and displayed

Each diagram can help you understand different aspects of the implementation.

