/**
 * Main App Entry Point
 * Orchestrates UI and app lifecycle
 */

// Optional Capacitor import (may not be available during development)
let App = null;

import appController from './services/appController.js';
import OnboardingUI from './ui/onboarding.js';
import DeviceGridUI from './ui/deviceGrid.js';
import SettingsUI from './ui/settings.js';
import { APP_MODE } from './config/config.js';

class MyLightsControllerApp {
  constructor() {
    this.onboardingUI = new OnboardingUI('app-container');
    this.deviceGridUI = new DeviceGridUI('device-grid');
    this.settingsUI = new SettingsUI('settings-container');
    this.currentScreen = 'onboarding';
    this.unsubscribe = null;
  }

   /**
    * Initialize app
    */
   async init() {
     try {
       console.log(`[App] Starting MyLightsController in ${APP_MODE} mode`);

       // Initialize app controller
       const isInitialized = await appController.initialize();
       console.log('[App] AppController initialized:', isInitialized);

       // Setup global event listeners
       this.setupGlobalListeners();

       // Setup state subscription
       this.subscribeToStateChanges();

       // Navigate to appropriate screen
       if (isInitialized && appController.authService.isAuthenticated()) {
         this.showMainApp();
       } else {
         this.showOnboarding();
       }

       // Setup Capacitor listeners
       this.setupCapacitorListeners();
     } catch (error) {
       console.error('[App] Initialization error:', error);
       this.showOnboarding();
     }
   }

  /**
   * Setup global event listeners
   */
  setupGlobalListeners() {
    // Onboarding complete
    window.addEventListener('onboarding-complete', () => {
      this.showMainApp();
    });

    // Logout complete
    window.addEventListener('logout-complete', () => {
      this.showOnboarding();
    });

    // Settings toggle
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && this.settingsUI.isOpen) {
        this.settingsUI.close();
      }
    });
  }

  /**
   * Subscribe to app state changes
   */
  subscribeToStateChanges() {
    this.unsubscribe = appController.subscribe((state) => {
      this.updateUI(state);
    });
  }

  /**
   * Update UI based on state
   */
  updateUI(state) {
    if (this.currentScreen === 'main') {
      this.deviceGridUI.setLoading(state.isLoading);
      this.deviceGridUI.refresh(state.devices);

      if (state.error) {
        this.showNotification(`Error: ${state.error}`, 'error');
      }
    }
  }

   /**
    * Show onboarding screen
    */
   showOnboarding() {
     try {
       this.currentScreen = 'onboarding';
       const appContainer = document.getElementById('app-container');
       const mainApp = document.getElementById('main-app');

       if (appContainer) appContainer.style.display = 'block';
       if (mainApp) mainApp.style.display = 'none';

       this.onboardingUI.render();
     } catch (error) {
       console.error('[App] Error showing onboarding:', error);
     }
   }

  /**
   * Show main app
   */
  async showMainApp() {
    this.currentScreen = 'main';
    document.getElementById('app-container').style.display = 'none';
    document.getElementById('main-app').style.display = 'flex';

    // Load devices
    try {
      await appController.loadDevices();
      const state = appController.getState();
      this.deviceGridUI.render(state.devices);
    } catch (error) {
      console.error('Load devices error:', error);
      this.showNotification('Failed to load devices', 'error');
    }
  }

   /**
    * Setup Capacitor listeners
    */
   setupCapacitorListeners() {
     // Only setup if Capacitor is available
     if (!App) {
       console.log('[App] Capacitor not available, skipping listeners');
       return;
     }

     // Handle app pause
     App.addListener('pause', () => {
       console.log('[App] App paused');
       appController.stopPolling();
     });

     // Handle app resume
     App.addListener('resume', () => {
       console.log('[App] App resumed');
       if (appController.authService.isAuthenticated()) {
         appController.startPolling();
       }
     });

     // Handle back button (Android)
     App.addListener('backButton', ({ canGoBack }) => {
       if (this.settingsUI.isOpen) {
         this.settingsUI.close();
       } else if (!canGoBack) {
         App.exitApp();
       }
     });
   }

  /**
   * Show notification
   */
  showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
      notification.classList.add('notification-fade-out');
      setTimeout(() => notification.remove(), 300);
    }, 3000);
  }

  /**
   * Toggle settings
   */
  toggleSettings() {
    this.settingsUI.toggle();
  }

  /**
   * Cleanup on app destroy
   */
  destroy() {
    appController.stopPolling();
    if (this.unsubscribe) {
      this.unsubscribe();
    }
  }
}

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', async () => {
  try {
    // Try to load Capacitor
    try {
      const capacitorApp = await import('@capacitor/app');
      App = capacitorApp.App;
      console.log('[App] Capacitor loaded');
    } catch (e) {
      console.log('[App] Capacitor not available (development mode)');
    }

    const app = new MyLightsControllerApp();
    await app.init();

    // Make app accessible globally for debugging
    window.myLightsApp = app;
  } catch (error) {
    console.error('[App] Fatal error:', error);
    document.body.innerHTML = `
      <div style="color: red; padding: 20px; font-family: monospace;">
        <h1>Error loading app</h1>
        <pre>${error.message}\n${error.stack}</pre>
      </div>
    `;
  }
});

export default MyLightsControllerApp;







