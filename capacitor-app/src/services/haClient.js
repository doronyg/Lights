/**
 * Home Assistant API Client
 * Handles all communication with Home Assistant REST API
 */

import axios from 'axios';

class HomeAssistantClient {
  constructor(config = {}) {
    this.baseUrl = config.baseUrl || '';
    this.token = config.token || '';
    this.axiosInstance = null;
    this.initializeClient();
  }

  initializeClient() {
    console.log('[HA Client] Initializing with baseURL:', this.baseUrl);

    this.axiosInstance = axios.create({
      baseURL: this.baseUrl,
      headers: {
        'Authorization': `Bearer ${this.token}`,
        'Content-Type': 'application/json',
      },
      timeout: 30000,
    });

    // Add response interceptor for error handling
    this.axiosInstance.interceptors.response.use(
      response => response.data,
      error => {
        console.error('[HA Client] API Error:', {
          status: error.response?.status,
          data: error.response?.data,
          message: error.message,
          url: error.config?.url,
          code: error.code
        });

        const errorMessage = error.response?.data?.message ||
                             error.response?.statusText ||
                             error.message ||
                             'Unknown error';
        throw new Error(errorMessage);
      }
    );
  }

  setCredentials(baseUrl, token) {
    this.baseUrl = baseUrl;
    this.token = token;
    this.initializeClient();
  }

  /**
   * Fetch all states (devices, lights, etc.)
   */
  async getStates() {
    try {
      console.log('[HA Client] Fetching states from:', this.baseUrl);
      const states = await this.axiosInstance.get('/api/states');
      console.log('[HA Client] Got states:', states.length, 'devices');
      return states;
    } catch (error) {
      console.error('[HA Client] Failed to fetch states:', error.message);
      throw error;
    }
  }

  /**
   * Fetch a specific entity state
   */
  async getState(entityId) {
    try {
      const state = await this.axiosInstance.get(`/api/states/${entityId}`);
      return state;
    } catch (error) {
      console.error(`[HA Client] Failed to fetch state for ${entityId}:`, error);
      throw error;
    }
  }

  /**
   * Call a service (turn on/off, brightness, etc.)
   */
  async callService(domain, service, serviceData = {}) {
    try {
      const result = await this.axiosInstance.post(
        `/api/services/${domain}/${service}`,
        serviceData
      );
      return result;
    } catch (error) {
      console.error(`[HA Client] Failed to call service ${domain}.${service}:`, error);
      throw error;
    }
  }

  /**
   * Turn on a light or device
   */
  async turnOn(entityId, brightness = null, color = null) {
    const data = { entity_id: entityId };
    if (brightness !== null) {
      data.brightness = brightness;
    }
    if (color !== null) {
      data.rgb_color = color;
    }
    return this.callService('light', 'turn_on', data);
  }

  /**
   * Turn off a light or device
   */
  async turnOff(entityId) {
    return this.callService('light', 'turn_off', { entity_id: entityId });
  }

  /**
   * Toggle a light or device
   */
  async toggle(entityId) {
    return this.callService('light', 'toggle', { entity_id: entityId });
  }

  /**
   * Set brightness (0-255)
   */
  async setBrightness(entityId, brightness) {
    return this.turnOn(entityId, brightness);
  }

  /**
   * Test connection to Home Assistant
   */
  async testConnection() {
    try {
      const result = await this.makeRequest('/api/');
      return { success: true, data: result };
    } catch (error) {
      return { success: false, error: error.message };
    }
  }

  /**
   * Get config/info about Home Assistant
   */
  async getConfig() {
    try {
      const config = await this.makeRequest('/api/config');
      return config;
    } catch (error) {
      console.error('[HA Client] Failed to fetch config:', error);
      throw error;
    }
  }

  /**
   * Subscribe to state changes via webhook (fallback: polling)
   */
  async subscribeToStateChanges(callback) {
    // Note: Real implementation would use WebSocket or SSE
    // For now, we'll poll every 2 seconds
    this.pollInterval = setInterval(async () => {
      try {
        const states = await this.getStates();
        callback(states);
      } catch (error) {
        console.error('Polling error:', error);
      }
    }, 2000);
  }

  unsubscribeFromStateChanges() {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
    }
  }
}

export default HomeAssistantClient;













