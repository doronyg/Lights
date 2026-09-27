# MyLightsController - Capacitor Version

A cross-platform Home Assistant lights and devices controller built with Capacitor, vanilla JavaScript, and HTML/CSS.

## Features

- 🏠 **Home Assistant Integration** - Direct REST API connection
- 📱 **Cross-platform** - Web, iOS, Android, and desktop
- 🎨 **Material Design 3 UI** - Modern, responsive interface
- 🔧 **Local-First Architecture** - Works without external backend
- 🔌 **Optional Backend Mode** - Switchable at runtime via settings
- 🌗 **Dark Mode Support** - Automatic dark mode detection
- ⚡ **Real-time Device Updates** - Polling-based state synchronization
- 📱 **Responsive Design** - Works on phones, tablets, and web

## Build Modes

This project supports two modes controlled via build-time environment variables:

### Local Mode (Default)
```bash
npm run build:local
# or
VITE_APP_MODE=local npm run build
```
App communicates directly with Home Assistant. Backend URL switch hidden in settings.

### Backend Mode
```bash
npm run build:backend
# or
VITE_APP_MODE=backend npm run build
```
App can switch between direct Home Assistant connection and backend server proxy. Backend URL toggle visible in settings.

## Project Structure

```
capacitor-app/
├── src/
│   ├── app.js                    # Main app entry point & lifecycle
│   ├── styles.css                # Global styles (Material Design 3)
│   ├── config/
│   │   └── config.js             # Build-time configuration
│   ├── services/
│   │   ├── appController.js      # Main app controller (orchestrator)
│   │   ├── haClient.js           # Home Assistant REST API client
│   │   ├── deviceModels.js       # Device models & utilities
│   │   ├── auth.js               # Authentication & credential management
│   │   └── stateManager.js       # Observable state management
│   └── ui/
│       ├── onboarding.js         # Setup/login screen
│       ├── deviceGrid.js         # Main device control grid
│       └── settings.js           # Settings panel
├── index.html                    # Main HTML entry point
├── package.json                  # Dependencies & scripts
├── vite.config.js                # Vite build configuration
├── capacitor.config.ts           # Capacitor configuration
└── www/                          # Build output (generated)
```

## Installation

### Prerequisites
- Node.js 16+
- npm or yarn
- Capacitor CLI: `npm install -g @capacitor/cli`

### Setup

1. **Install dependencies**
   ```bash
   npm install
   ```

2. **Run development server**
   ```bash
   npm run dev
   # or with specific mode
   VITE_APP_MODE=local npm run dev
   ```
   Access at `http://localhost:3000`

3. **Build for web**
   ```bash
   npm run build
   # or with specific mode
   npm run build:local
   npm run build:backend
   ```

## Deployment

### Web Deployment
```bash
npm run build
# Files in www/ directory ready for deployment
```

### iOS Deployment
```bash
npm run build
npm install @capacitor/ios
npx cap add ios
npx cap sync ios
npx cap open ios
# Then build & sign in Xcode
```

### Android Deployment
```bash
npm run build
npm install @capacitor/android
npx cap add android
npx cap sync android
npx cap open android
# Then build in Android Studio
```

### Desktop (Electron)
```bash
npm run build
# Can be wrapped with Electron or Tauri
```

## Configuration

### Home Assistant Setup

1. Get your Home Assistant URL (e.g., `http://192.168.1.100:8123`)
2. Create a Long-Lived Access Token:
   - Go to Settings → Developer Tools
   - Create Token (copy it somewhere safe)
3. Launch the app and enter URL + token on onboarding screen

### Environment Variables

```bash
# Set build mode (local or backend)
VITE_APP_MODE=local

# Set backend URL for backend mode
VITE_BACKEND_URL=http://localhost:3001/api
```

## Usage

### Onboarding
1. Enter Home Assistant URL (e.g., `http://192.168.1.100:8123`)
2. Enter Long-Lived Access Token
3. Click "Test Connection" to verify
4. Click "Connect"

### Device Control
- **Toggle Device**: Click device card to turn on/off
- **Adjust Brightness**: Drag brightness slider
- **Refresh**: Click refresh button in header
- **Settings**: Click settings button for connection options

### Settings
- View current connection info
- Toggle between Local/Backend mode (if available)
- Logout and reset credentials

## API Architecture

### HomeAssistantClient
Handles all communication with Home Assistant REST API:
- `getStates()` - Fetch all entity states
- `turnOn(entityId)` - Turn on a device
- `turnOff(entityId)` - Turn off a device
- `toggle(entityId)` - Toggle device state
- `setBrightness(entityId, brightness)` - Set light brightness
- `testConnection()` - Verify connection

### AppController
Orchestrates services and business logic:
- `initialize()` - Initialize app with stored credentials
- `setupCredentials()` - Configure HA connection
- `loadDevices()` - Fetch controllable devices
- `switchMode()` - Toggle between local/backend
- `logout()` - Clear credentials

### StateManager
Observable state management (similar to RxJS/Kotlin Flow):
- `subscribe(callback)` - Listen to state changes
- `setState(updates)` - Update state
- `getState()` - Get current state

### Device Models
Utilities for device handling:
- `Device` class - Typed device representation
- `isControllable()` - Check if device can be controlled
- `getDeviceIcon()` - Get emoji icon for device type
- `groupDevicesByDomain()` - Organize devices by type

## Styling

Built with CSS custom properties (variables) for easy theming:
- Material Design 3 color palette
- Automatic dark mode support
- Responsive grid layouts
- Touch-friendly controls (48px minimum tap targets)

## Build-time Logic

The app uses `process.env.VITE_APP_MODE` to determine behavior at build time:

```javascript
// Local mode only
if (IS_LOCAL_MODE) {
  // Hide backend URL toggle
}

// Backend mode only
if (IS_BACKEND_MODE) {
  // Show backend URL toggle
}

// Both modes
const apiBaseUrl = getApiBaseUrl(authService);
```

## Developing

### Adding a new screen
1. Create UI class in `src/ui/`
2. Add to main app navigation
3. Subscribe to state changes for updates

### Adding a new service
1. Create service in `src/services/`
2. Export singleton instance
3. Integrate into `appController.js`

### Styling new components
- Use CSS custom properties from `:root`
- Follow Material Design 3 guidelines
- Test dark mode compatibility

## Architecture Benefits

### Single Codebase
- One React-less JavaScript codebase
- No UI duplication across platforms
- Reduces maintenance burden

### Local-First
- Works without external server
- Direct Home Assistant communication
- Better privacy and performance

### Flexible Deployment
- Web: Any HTTP server
- Mobile: Capacitor bridges native APIs
- Desktop: Electron/Tauri wrapper

### Optional Backend
- Build-time toggle between modes
- No code change needed
- Runtime switching via settings

## Future Enhancements

- [ ] WebSocket support for real-time updates
- [ ] Camera QR code setup
- [ ] Voice commands (button-activated)
- [ ] Translation support
- [ ] More device types (climate, covers, etc.)
- [ ] Automation/scene triggers
- [ ] Device grouping and favorites
- [ ] Battery level monitoring

## License

© 2026 Doron Yakovlev Golani. All rights reserved.
This software is private and may not be copied, modified, distributed, or used without explicit permission.

