/**
 * Onboarding Screen UI
 * Handles initial setup of Home Assistant credentials
 */

import appController from '../services/appController.js';
import { IS_LOCAL_MODE, CONFIG } from '../config/config.js';
import { QRScannerUI } from './qrScanner.js';

export class OnboardingUI {
  constructor(containerId) {
    this.container = document.getElementById(containerId);
    this.qrScanner = new QRScannerUI();
    this.haUrlInput = null;
    this.haTokenInput = null;
    this.backendUrlInput = null;
    this.useBackendCheckbox = null;
    this.connectButton = null;
    this.testButton = null;
    this.qrButton = null;
    this.errorMessage = null;
    this.isLoading = false;
  }

  /**
   * Render onboarding screen
   */
  render() {
    this.container.innerHTML = `
      <div class="onboarding-container">
        <div class="onboarding-header">
          <h1>🏠 MyLightsController</h1>
          <p>Connect to Home Assistant</p>
          <p class="mode-badge">Mode: ${IS_LOCAL_MODE ? 'LOCAL' : 'BACKEND'}</p>
        </div>

        <form class="onboarding-form" id="setupForm">
          <div class="form-group">
            <label for="haUrl">Home Assistant URL</label>
            <input
              type="url"
              id="haUrl"
              placeholder="http://192.168.1.100:8123"
              required
            />
            <small>e.g., http://192.168.1.100:8123</small>
          </div>

          <div class="form-group">
            <label for="haToken">Access Token</label>
            <input
              type="password"
              id="haToken"
              placeholder="Enter your Home Assistant token"
              required
            />
            <small>Generate from Home Assistant Settings → Developer Tools → Create Long-Lived Token</small>
          </div>

          ${!IS_LOCAL_MODE ? `
            <div class="form-group">
              <label>
                <input type="checkbox" id="useBackend" />
                Use Backend Server
              </label>
              <input
                type="url"
                id="backendUrl"
                placeholder="${CONFIG.BACKEND.defaultBackendUrl}"
                style="display:none;"
              />
            </div>
          ` : ''}

          <div class="error-message" id="errorMessage" style="display:none;"></div>

          <div class="button-group">
            <button type="button" id="qrButton" class="btn btn-secondary">
              📸 Scan QR
            </button>
            <button type="button" id="testButton" class="btn btn-secondary">
              Test Connection
            </button>
            <button type="submit" id="connectButton" class="btn btn-primary">
              Connect
            </button>
          </div>
        </form>

        <div class="onboarding-info">
          <h3>About this app</h3>
          <p>MyLightsController is a lightweight Home Assistant controller for controlling your lights and devices.</p>
          <p><strong>Mode:</strong> ${IS_LOCAL_MODE ? 'Direct connection to Home Assistant' : 'Connection through backend server'}</p>
        </div>
      </div>
    `;

    this.setupEventListeners();
    this.restoreFormValues();
  }

  /**
   * Setup event listeners
   */
   setupEventListeners() {
     this.haUrlInput = document.getElementById('haUrl');
     this.haTokenInput = document.getElementById('haToken');
     this.connectButton = document.getElementById('connectButton');
     this.testButton = document.getElementById('testButton');
     this.qrButton = document.getElementById('qrButton');
     this.errorMessage = document.getElementById('errorMessage');

     if (!IS_LOCAL_MODE) {
       this.useBackendCheckbox = document.getElementById('useBackend');
       this.backendUrlInput = document.getElementById('backendUrl');

       this.useBackendCheckbox.addEventListener('change', (e) => {
         this.backendUrlInput.style.display = e.target.checked ? 'block' : 'none';
       });
     }

     document.getElementById('setupForm').addEventListener('submit', (e) => {
       e.preventDefault();
       this.handleConnect();
     });

     this.testButton.addEventListener('click', () => {
       this.handleTestConnection();
     });

     if (this.qrButton) {
       this.qrButton.addEventListener('click', () => {
         this.handleQRScan();
       });
     }
   }

  /**
   * Restore form values from storage
   */
  restoreFormValues() {
    const { haUrl, useBackend, backendUrl } = appController.authService.getCredentials();

    if (haUrl) {
      this.haUrlInput.value = haUrl;
    }

    if (!IS_LOCAL_MODE && useBackend) {
      this.useBackendCheckbox.checked = true;
      this.backendUrlInput.style.display = 'block';
      this.backendUrlInput.value = backendUrl || '';
    }
  }

  /**
   * Handle test connection
   */
  async handleTestConnection() {
    try {
      this.showLoading(this.testButton);
      this.clearError();

      const haUrl = this.haUrlInput.value.trim();
      const haToken = this.haTokenInput.value.trim();

      if (!haUrl || !haToken) {
        this.showError('Please enter both URL and token');
        return;
      }

      appController.initializeClient(haUrl, haToken);
      const isConnected = await appController.testConnection();

      if (isConnected) {
        this.showSuccess('✓ Connection successful!');
      } else {
        this.showError('Connection failed. Check your URL and token.');
      }
    } catch (error) {
      this.showError(error.message);
    } finally {
      this.hideLoading(this.testButton);
    }
  }

  /**
   * Handle connect form submission
   */
  async handleConnect() {
    try {
      this.showLoading(this.connectButton);
      this.clearError();

      const haUrl = this.haUrlInput.value.trim();
      const haToken = this.haTokenInput.value.trim();

      if (!haUrl || !haToken) {
        this.showError('Please enter both URL and token');
        return;
      }

      let useBackend = false;
      let backendUrl = null;

      if (!IS_LOCAL_MODE && this.useBackendCheckbox?.checked) {
        useBackend = true;
        backendUrl = this.backendUrlInput.value.trim() || CONFIG.BACKEND.defaultBackendUrl;
      }

      const isConnected = await appController.setupCredentials(
        haUrl,
        haToken,
        useBackend,
        backendUrl
      );

      if (isConnected) {
        // Trigger navigation to main app
        window.dispatchEvent(new CustomEvent('onboarding-complete'));
      } else {
        this.showError('Failed to connect. Please check your credentials.');
      }
    } catch (error) {
      this.showError(error.message);
    } finally {
      this.hideLoading(this.connectButton);
    }
  }

  /**
   * Show error message
   */
  showError(message) {
    this.errorMessage.textContent = message;
    this.errorMessage.style.display = 'block';
  }

  /**
   * Show success message
   */
  showSuccess(message) {
    this.errorMessage.textContent = message;
    this.errorMessage.style.color = '#4caf50';
    this.errorMessage.style.display = 'block';
  }

  /**
   * Clear error message
   */
  clearError() {
    this.errorMessage.style.display = 'none';
    this.errorMessage.textContent = '';
    this.errorMessage.style.color = '#f44336';
  }

  /**
   * Show loading state
   */
  showLoading(button) {
    button.disabled = true;
    button.textContent = 'Loading...';
    this.isLoading = true;
  }

   /**
    * Hide loading state
    */
   hideLoading(button) {
     button.disabled = false;
     const originalText = button.id === 'testButton' ? 'Test Connection' : button.id === 'qrButton' ? '📸 Scan QR' : 'Connect';
     button.textContent = originalText;
     this.isLoading = false;
   }

   /**
    * Handle QR code scan
    */
   handleQRScan() {
     try {
       this.clearError();
       this.qrScanner.render((qrData) => {
         if (qrData && qrData.haUrl && qrData.haToken) {
           this.haUrlInput.value = qrData.haUrl;
           this.haTokenInput.value = qrData.haToken;
           this.showSuccess('✓ QR code scanned! URL and token filled.');
         }
       });
     } catch (error) {
       console.error('QR scan error:', error);
       this.showError('QR scanner not supported on this browser');
     }
   }
}

export default OnboardingUI;






