/**
 * Device Grid UI
 * Displays and controls devices in a responsive grid
 */

import appController from '../services/appController.js';

export class DeviceGridUI {
  constructor(containerId) {
    this.container = document.getElementById(containerId);
    this.devices = [];
    this.isLoading = false;
  }

  /**
   * Render device grid
   */
  render(devices = []) {
    this.devices = devices;

    if (this.isLoading) {
      this.container.innerHTML = `
        <div class="loading-container">
          <div class="spinner"></div>
          <p>Loading devices...</p>
        </div>
      `;
      return;
    }

    if (devices.length === 0) {
      this.container.innerHTML = `
        <div class="empty-state">
          <p>No controllable devices found</p>
          <small>Make sure you have lights or switches configured in Home Assistant</small>
        </div>
      `;
      return;
    }

    const deviceHTML = devices.map(device => this.createDeviceCard(device)).join('');

    this.container.innerHTML = `
      <div class="device-grid">
        ${deviceHTML}
      </div>
    `;

    this.setupEventListeners(devices);
  }

  /**
   * Create a device card
   */
  createDeviceCard(device) {
    const brightnessControl = device.brightness !== null ? `
      <div class="brightness-control">
        <input
          type="range"
          min="0"
          max="255"
          value="${device.brightness}"
          class="brightness-slider"
          data-entity="${device.entityId}"
        />
        <span class="brightness-value">${Math.round((device.brightness / 255) * 100)}%</span>
      </div>
    ` : '';

    return `
      <div class="device-card ${device.isOn ? 'device-on' : 'device-off'}" data-entity="${device.entityId}">
        <div class="device-icon">${device.icon}</div>
        <div class="device-info">
          <h3 class="device-name">${device.friendlyName}</h3>
          <p class="device-state">${device.isOn ? 'On' : 'Off'}</p>
        </div>
        <div class="device-controls">
          <button class="device-toggle-btn" data-entity="${device.entityId}">
            ${device.isOn ? 'Off' : 'On'}
          </button>
        </div>
        ${brightnessControl}
      </div>
    `;
  }

  /**
   * Setup event listeners for device cards
   */
  setupEventListeners(devices) {
    // Toggle button listeners
    document.querySelectorAll('.device-toggle-btn').forEach(button => {
      button.addEventListener('click', (e) => {
        e.preventDefault();
        const entityId = button.dataset.entity;
        this.handleToggle(entityId);
      });
    });

    // Brightness slider listeners
    document.querySelectorAll('.brightness-slider').forEach(slider => {
      slider.addEventListener('change', (e) => {
        const entityId = slider.dataset.entity;
        const brightness = parseInt(slider.value);
        this.handleBrightnessChange(entityId, brightness);
      });

      slider.addEventListener('input', (e) => {
        const valueDisplay = e.target.parentElement.querySelector('.brightness-value');
        const brightnessPercent = Math.round((parseInt(e.target.value) / 255) * 100);
        valueDisplay.textContent = `${brightnessPercent}%`;
      });
    });
  }

  /**
   * Handle device toggle
   */
  async handleToggle(entityId) {
    try {
      await appController.toggle(entityId);
    } catch (error) {
      console.error('Toggle error:', error);
      alert('Failed to toggle device: ' + error.message);
    }
  }

  /**
   * Handle brightness change
   */
  async handleBrightnessChange(entityId, brightness) {
    try {
      await appController.setBrightness(entityId, brightness);
    } catch (error) {
      console.error('Brightness error:', error);
      alert('Failed to set brightness: ' + error.message);
    }
  }

  /**
   * Set loading state
   */
  setLoading(isLoading) {
    this.isLoading = isLoading;
  }

  /**
   * Update a single device
   */
  updateDevice(device) {
    const index = this.devices.findIndex(d => d.entityId === device.entityId);
    if (index !== -1) {
      this.devices[index] = device;
    }
  }

  /**
   * Refresh display
   */
  refresh(devices) {
    this.render(devices);
  }
}

export default DeviceGridUI;

