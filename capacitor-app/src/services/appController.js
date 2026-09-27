/**
 * Main Application Controller
 * Orchestrates all services and business logic
 */

import HomeAssistantClient from './haClient.js';
import { Device, getControllableDevices } from './deviceModels.js';
import authService from './auth.js';
import stateManager from './stateManager.js';
import { getApiBaseUrl, IS_LOCAL_MODE } from '../config/config.js';

class AppController {
  constructor() {
    this.haClient = null;
    this.authService = authService;
    this.stateManager = stateManager;
    this.pollInterval = null;
    this.isInitialized = false;
  }

  /**
   * Initialize app with stored credentials
   */
  async initialize() {
    try {
      this.authService.load();

      if (this.authService.isAuthenticated()) {
        const apiBaseUrl = getApiBaseUrl(this.authService);
        this.initializeClient(apiBaseUrl, this.authService.haToken);
        await this.testConnection();
      }

      this.isInitialized = true;
      return true;
    } catch (error) {
      console.error('Initialization error:', error);
      this.stateManager.setError(error);
      return false;
    }
  }

  /**
   * Initialize Home Assistant client
   */
  initializeClient(baseUrl, token) {
    this.haClient = new HomeAssistantClient({
      baseUrl,
      token,
    });
  }

  /**
   * Test connection to Home Assistant
   */
  async testConnection() {
    try {
      if (!this.haClient) {
        throw new Error('Client not initialized');
      }

      const result = await this.haClient.testConnection();

      if (result.success) {
        this.stateManager.setConnected(true);
        return true;
      } else {
        this.stateManager.setError(new Error('Connection failed'));
        return false;
      }
    } catch (error) {
      console.error('Connection test failed:', error);
      this.stateManager.setConnected(false);
      this.stateManager.setError(error);
      return false;
    }
  }

  /**
   * Setup credentials and test connection
   */
  async setupCredentials(haUrl, haToken, useBackend = false, backendUrl = null) {
    try {
      this.authService.setHaCredentials(haUrl, haToken);

      if (useBackend) {
        this.authService.setUseBackend(true, backendUrl);
      }

      const apiBaseUrl = getApiBaseUrl(this.authService);
      this.initializeClient(apiBaseUrl, haToken);

      const isConnected = await this.testConnection();

      if (isConnected) {
        await this.loadDevices();
        this.startPolling();
      }

      return isConnected;
    } catch (error) {
      console.error('Setup credentials error:', error);
      this.stateManager.setError(error);
      return false;
    }
  }

  /**
   * Load all devices from Home Assistant
   */
  async loadDevices() {
    try {
      this.stateManager.setLoading(true);
      this.stateManager.clearError();

      if (!this.haClient) {
        throw new Error('Client not initialized');
      }

      const states = await this.haClient.getStates();

      // Convert states to Device objects
      const devices = states
        .map(state => new Device(state.entity_id, state.state, state.attributes))
        .filter(device => device.isControllable);

      this.stateManager.setDevices(devices);
      this.stateManager.setConnected(true);

      return devices;
    } catch (error) {
      console.error('Load devices error:', error);
      this.stateManager.setError(error);
      this.stateManager.setConnected(false);
      throw error;
    } finally {
      this.stateManager.setLoading(false);
    }
  }

  /**
   * Turn on a device
   */
  async turnOn(entityId) {
    try {
      this.stateManager.clearError();

      if (!this.haClient) {
        throw new Error('Client not initialized');
      }

      await this.haClient.turnOn(entityId);
      await this.loadDevices(); // Refresh device list

      return true;
    } catch (error) {
      console.error('Turn on error:', error);
      this.stateManager.setError(error);
      throw error;
    }
  }

  /**
   * Turn off a device
   */
  async turnOff(entityId) {
    try {
      this.stateManager.clearError();

      if (!this.haClient) {
        throw new Error('Client not initialized');
      }

      await this.haClient.turnOff(entityId);
      await this.loadDevices(); // Refresh device list

      return true;
    } catch (error) {
      console.error('Turn off error:', error);
      this.stateManager.setError(error);
      throw error;
    }
  }

  /**
   * Toggle a device
   */
  async toggle(entityId) {
    try {
      this.stateManager.clearError();

      if (!this.haClient) {
        throw new Error('Client not initialized');
      }

      await this.haClient.toggle(entityId);
      await this.loadDevices(); // Refresh device list

      return true;
    } catch (error) {
      console.error('Toggle error:', error);
      this.stateManager.setError(error);
      throw error;
    }
  }

  /**
   * Set brightness for a light
   */
  async setBrightness(entityId, brightness) {
    try {
      this.stateManager.clearError();

      if (!this.haClient) {
        throw new Error('Client not initialized');
      }

      await this.haClient.setBrightness(entityId, brightness);
      await this.loadDevices(); // Refresh device list

      return true;
    } catch (error) {
      console.error('Set brightness error:', error);
      this.stateManager.setError(error);
      throw error;
    }
  }

  /**
   * Start polling for device updates
   */
  startPolling(interval = 5000) {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
    }

    this.pollInterval = setInterval(async () => {
      try {
        await this.loadDevices();
      } catch (error) {
        console.error('Polling error:', error);
      }
    }, interval);
  }

  /**
   * Stop polling
   */
  stopPolling() {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
      this.pollInterval = null;
    }
  }

  /**
   * Logout and clear credentials
   */
  logout() {
    this.stopPolling();
    this.authService.logout();
    this.stateManager.reset();
    this.haClient = null;
  }

  /**
   * Toggle between local and backend mode
   */
  async switchMode(useBackend, backendUrl = null) {
    try {
      if (!this.authService.isAuthenticated()) {
        throw new Error('Not authenticated');
      }

      this.authService.setUseBackend(useBackend, backendUrl);
      const apiBaseUrl = getApiBaseUrl(this.authService);

      this.initializeClient(apiBaseUrl, this.authService.haToken);
      await this.testConnection();

      if (this.stateManager.getState().isConnected) {
        await this.loadDevices();
        this.startPolling();
      }

      return true;
    } catch (error) {
      console.error('Mode switch error:', error);
      this.stateManager.setError(error);
      return false;
    }
  }

  /**
   * Get current app state
   */
  getState() {
    return this.stateManager.getState();
  }

  /**
   * Subscribe to state changes
   */
  subscribe(callback) {
    return this.stateManager.subscribe(callback);
  }
}

export default new AppController();

