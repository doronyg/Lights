/**
 * Settings Screen UI
 * Handles app settings, mode switching, and logout
 */

import appController from '../services/appController.js';
import { IS_LOCAL_MODE, CONFIG } from '../config/config.js';

export class SettingsUI {
  constructor(containerId) {
    this.container = document.getElementById(containerId);
    this.isOpen = false;
  }

  /**
   * Render settings panel
   */
  render() {
    const { haUrl, useBackend, backendUrl } = appController.authService.getCredentials();
    const modeDisplay = useBackend ? 'Backend' : 'Local';
    const modeUrl = useBackend ? backendUrl : haUrl;

    this.container.innerHTML = `
      <div class="settings-overlay" id="settingsOverlay">
        <div class="settings-panel">
          <div class="settings-header">
            <h2>⚙️ Settings</h2>
            <button class="close-btn" id="closeSettings">&times;</button>
          </div>

          <div class="settings-content">
            <div class="settings-section">
              <h3>Connection</h3>
              <div class="setting-item">
                <label>Home Assistant URL</label>
                <p class="setting-value">${haUrl}</p>
              </div>
            </div>

            ${!IS_LOCAL_MODE ? `
              <div class="settings-section">
                <h3>Mode</h3>
                <div class="setting-item">
                  <label>
                    <input type="radio" name="mode" value="local" ${!useBackend ? 'checked' : ''} />
                    Local (Direct Connection)
                  </label>
                  <small>Connect directly to Home Assistant</small>
                </div>
                <div class="setting-item">
                  <label>
                    <input type="radio" name="mode" value="backend" ${useBackend ? 'checked' : ''} />
                    Backend (Server)
                  </label>
                  <small>Connect through backend server</small>
                </div>

                ${useBackend ? `
                  <div class="setting-item">
                    <label>Backend URL</label>
                    <input type="url" id="backendUrlInput" value="${backendUrl}" />
                  </div>
                ` : ''}
              </div>
            ` : ''}

            <div class="settings-section">
              <h3>App Info</h3>
              <div class="setting-item">
                <label>Version</label>
                <p class="setting-value">1.0.0</p>
              </div>
              <div class="setting-item">
                <label>Mode</label>
                <p class="setting-value">${IS_LOCAL_MODE ? 'Local Only' : 'Local + Backend'}</p>
              </div>
              <div class="setting-item">
                <label>Current Connection</label>
                <p class="setting-value">${modeDisplay}</p>
              </div>
            </div>

            <div class="settings-section">
              <h3>Danger Zone</h3>
              <button class="btn btn-danger" id="logoutBtn">Logout</button>
            </div>
          </div>
        </div>
      </div>
    `;

    this.setupEventListeners();
  }

  /**
   * Setup event listeners
   */
  setupEventListeners() {
    document.getElementById('closeSettings').addEventListener('click', () => {
      this.close();
    });

    document.getElementById('settingsOverlay').addEventListener('click', (e) => {
      if (e.target.id === 'settingsOverlay') {
        this.close();
      }
    });

    document.getElementById('logoutBtn').addEventListener('click', () => {
      this.handleLogout();
    });

    if (!IS_LOCAL_MODE) {
      const modeRadios = document.querySelectorAll('input[name="mode"]');
      modeRadios.forEach(radio => {
        radio.addEventListener('change', (e) => {
          this.handleModeChange(e.target.value);
        });
      });
    }
  }

  /**
   * Handle mode change
   */
  async handleModeChange(mode) {
    try {
      const useBackend = mode === 'backend';
      let backendUrl = null;

      if (useBackend) {
        backendUrl = document.getElementById('backendUrlInput')?.value || CONFIG.BACKEND.defaultBackendUrl;
      }

      const success = await appController.switchMode(useBackend, backendUrl);

      if (success) {
        this.showNotification(`✓ Switched to ${mode} mode`);
      } else {
        this.showNotification('✗ Failed to switch mode', 'error');
      }
    } catch (error) {
      console.error('Mode switch error:', error);
      this.showNotification('Error: ' + error.message, 'error');
    }
  }

  /**
   * Handle logout
   */
  handleLogout() {
    if (confirm('Are you sure you want to logout?')) {
      appController.logout();
      window.dispatchEvent(new CustomEvent('logout-complete'));
    }
  }

  /**
   * Show notification
   */
  showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
      notification.remove();
    }, 3000);
  }

  /**
   * Open settings panel
   */
  open() {
    this.isOpen = true;
    this.render();
    this.container.style.display = 'block';
  }

  /**
   * Close settings panel
   */
  close() {
    this.isOpen = false;
    this.container.style.display = 'none';
  }

  /**
   * Toggle settings panel
   */
  toggle() {
    if (this.isOpen) {
      this.close();
    } else {
      this.open();
    }
  }
}

export default SettingsUI;

