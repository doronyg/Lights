# Quick Start Guide

Get MyLightsController running in 5 minutes.

## Prerequisites

- **Node.js 16+** - [Download](https://nodejs.org)
- **Home Assistant** - Running with REST API access
- **Access Token** - Generated from HA Settings

## 1. Install & Run (Web)

```bash
# Navigate to capacitor-app directory
cd /Users/dorong/AndroidStudioProjects/MyLightsController/capacitor-app

# Install dependencies
npm install

# Start development server
npm run dev

# Open browser: http://localhost:3000
```

## 2. Get Your Home Assistant Credentials

### Find Your Home Assistant URL
- Local network: `http://192.168.1.XX:8123`
- Check your router or HA documentation

### Create Long-Lived Access Token
1. In Home Assistant, go to **Settings** → **Developer Tools**
2. Scroll to **Long-Lived Access Tokens**
3. Click **Create Token**, give it a name (e.g., "Mobile App")
4. Copy the token and save it somewhere safe

## 3. Connect to Home Assistant

In the app:

1. **Enter Home Assistant URL**
   - Example: `http://192.168.1.100:8123`

2. **Enter Access Token**
   - Paste the token you created

3. **Click "Test Connection"**
   - If successful, you'll see ✓

4. **Click "Connect"**
   - You'll see your devices!

## 4. Control Your Devices

### Toggle Lights On/Off
- Click any device card to toggle

### Adjust Brightness
- Drag the brightness slider below the device

### Refresh Devices
- Click 🔄 refresh button in header

### View Settings
- Click ⚙️ settings button in header

## 5. Common Issues

### "Connection failed"
- ✅ Check Home Assistant URL is correct
- ✅ Check token is not expired
- ✅ Ensure your phone/computer can reach HA URL
- ✅ Try "Test Connection" first

### "No devices found"
- ✅ Verify devices are configured in Home Assistant
- ✅ Devices must be lights, switches, or fans
- ✅ Binary sensors and climate devices won't appear

### "Settings not saved"
- ✅ Check browser localStorage is enabled
- ✅ Clear cache and try again
- ✅ Check browser console for errors

## 6. Deploy to Mobile (Optional)

### iOS
```bash
npm run build
npx cap add ios
npx cap open ios
# Build in Xcode
```

### Android
```bash
npm run build
npx cap add android
npx cap open android
# Build in Android Studio
```

### Web (Production)
```bash
npm run build
# Upload www/ directory to your web server
```

## 7. Next Steps

- [📖 Full README](./README.md)
- [🏗️ Architecture Guide](./ARCHITECTURE.md)
- [🔄 Migration from Android](./MIGRATION.md)
- [⚙️ Configuration](./README.md#configuration)

## Keyboard Shortcuts

| Shortcut | Action |
|---|---|
| `ESC` | Close settings panel |
| `Ctrl+R` | Refresh (if not touch) |

## Tips & Tricks

**Faster Development:**
```bash
# Run in local mode explicitly
VITE_APP_MODE=local npm run dev

# Run in backend mode
VITE_APP_MODE=backend npm run dev
```

**Debug in Console:**
```javascript
// Check app state
window.myLightsApp.appController.getState()

// Force reload devices
await window.myLightsApp.appController.loadDevices()

// Check auth
window.myLightsApp.appController.authService.getCredentials()
```

**Dark Mode:**
- Automatically follows system preference
- Can be overridden in browser dev tools: `prefers-color-scheme`

## Troubleshooting

### Port 3000 already in use?
```bash
# Use different port
npx vite --port 3001
```

### Module not found?
```bash
# Clear and reinstall
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### Build fails?
```bash
# Clean and rebuild
npm run clean  # (if script exists)
npm run build
# or
VITE_APP_MODE=local npm run build
```

## Support

- 📖 Check [README.md](./README.md) for more details
- 🏗️ Review [ARCHITECTURE.md](./ARCHITECTURE.md) for design
- 🔄 See [MIGRATION.md](./MIGRATION.md) for Android migration
- 🐛 Check browser console for error messages

---

**That's it!** You should now have MyLightsController running. Enjoy controlling your lights! 💡

