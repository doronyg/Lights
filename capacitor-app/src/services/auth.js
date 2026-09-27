/**
 * Authentication Service
 * Handles Home Assistant credentials storage and management
 */

const STORAGE_KEYS = {
  HA_URL: 'ha_url',
  HA_TOKEN: 'ha_token',
  USE_BACKEND: 'use_backend',
  BACKEND_URL: 'backend_url',
};

export class AuthService {
  constructor() {
    this.haUrl = null;
    this.haToken = null;
    this.useBackend = false;
    this.backendUrl = null;
    this.load();
  }

  /**
   * Load credentials from local storage
   */
  load() {
    this.haUrl = localStorage.getItem(STORAGE_KEYS.HA_URL) || '';
    this.haToken = localStorage.getItem(STORAGE_KEYS.HA_TOKEN) || '';
    this.useBackend = localStorage.getItem(STORAGE_KEYS.USE_BACKEND) === 'true';
    this.backendUrl = localStorage.getItem(STORAGE_KEYS.BACKEND_URL) || '';
  }

  /**
   * Save credentials to local storage
   */
  save() {
    localStorage.setItem(STORAGE_KEYS.HA_URL, this.haUrl);
    localStorage.setItem(STORAGE_KEYS.HA_TOKEN, this.haToken);
    localStorage.setItem(STORAGE_KEYS.USE_BACKEND, this.useBackend.toString());
    if (this.backendUrl) {
      localStorage.setItem(STORAGE_KEYS.BACKEND_URL, this.backendUrl);
    }
  }

  /**
   * Set Home Assistant credentials
   */
  setHaCredentials(url, token) {
    this.haUrl = url;
    this.haToken = token;
    this.save();
  }

  /**
   * Set backend mode
   */
  setUseBackend(useBackend, backendUrl = null) {
    this.useBackend = useBackend;
    if (backendUrl) {
      this.backendUrl = backendUrl;
    }
    this.save();
  }

  /**
   * Get the API base URL (either Home Assistant or backend)
   */
  getApiBaseUrl() {
    if (this.useBackend && this.backendUrl) {
      return this.backendUrl;
    }
    return this.haUrl;
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated() {
    return !!this.haUrl && !!this.haToken;
  }

  /**
   * Clear credentials
   */
  logout() {
    this.haUrl = '';
    this.haToken = '';
    this.useBackend = false;
    this.backendUrl = '';
    localStorage.clear();
  }

  /**
   * Get credentials object
   */
  getCredentials() {
    return {
      haUrl: this.haUrl,
      haToken: this.haToken,
      useBackend: this.useBackend,
      backendUrl: this.backendUrl,
      apiBaseUrl: this.getApiBaseUrl(),
    };
  }
}

export default new AuthService();

