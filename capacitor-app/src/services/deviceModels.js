/**
 * Device Models and Types
 * Defines Home Assistant device structure and utilities
 */

export const DEVICE_DOMAINS = {
  LIGHT: 'light',
  SWITCH: 'switch',
  BINARY_SENSOR: 'binary_sensor',
  COVER: 'cover',
  CLIMATE: 'climate',
  FAN: 'fan',
};

export const DEVICE_STATES = {
  ON: 'on',
  OFF: 'off',
  OPEN: 'open',
  CLOSED: 'closed',
  UNKNOWN: 'unknown',
};

/**
 * Parses entity_id and extracts domain and name
 * Example: "light.living_room" -> { domain: "light", name: "living_room" }
 */
export function parseEntityId(entityId) {
  const [domain, name] = entityId.split('.');
  return { domain, name };
}

/**
 * Converts friendly name from entity_id
 * Example: "living_room_lamp" -> "Living Room Lamp"
 */
export function getFriendlyName(entityId) {
  const { name } = parseEntityId(entityId);
  return name
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

/**
 * Determines if a device is controllable (can be turned on/off)
 */
export function isControllable(domain) {
  return [
    DEVICE_DOMAINS.LIGHT,
    DEVICE_DOMAINS.SWITCH,
    DEVICE_DOMAINS.COVER,
    DEVICE_DOMAINS.FAN,
  ].includes(domain);
}

/**
 * Checks if a device is currently on
 */
export function isDeviceOn(state) {
  return state === DEVICE_STATES.ON || state === DEVICE_STATES.OPEN;
}

/**
 * Gets icon for device based on domain and state
 */
export function getDeviceIcon(domain, state, attributes = {}) {
  switch (domain) {
    case DEVICE_DOMAINS.LIGHT:
      return isDeviceOn(state) ? '💡' : '💡';
    case DEVICE_DOMAINS.SWITCH:
      return isDeviceOn(state) ? '✓' : '✗';
    case DEVICE_DOMAINS.COVER:
      return state === DEVICE_STATES.OPEN ? '▲' : '▼';
    case DEVICE_DOMAINS.FAN:
      return '🌀';
    case DEVICE_DOMAINS.CLIMATE:
      return '🌡️';
    case DEVICE_DOMAINS.BINARY_SENSOR:
      return isDeviceOn(state) ? '●' : '○';
    default:
      return '⚙️';
  }
}

/**
 * Extracts brightness from attributes
 */
export function getBrightness(attributes = {}) {
  return attributes.brightness || 0;
}

/**
 * Extracts color temperature from attributes
 */
export function getColorTemp(attributes = {}) {
  return attributes.color_temp || 0;
}

/**
 * Normalizes brightness to 0-100 scale
 */
export function normalizeBrightness(brightness) {
  return Math.round((brightness / 255) * 100);
}

/**
 * Converts 0-100 brightness to 0-255
 */
export function denormalizeBrightness(normalizedBrightness) {
  return Math.round((normalizedBrightness / 100) * 255);
}

/**
 * Device class for structured device data
 */
export class Device {
  constructor(entityId, state, attributes = {}) {
    this.entityId = entityId;
    this.state = state;
    this.attributes = attributes;
    const { domain, name } = parseEntityId(entityId);
    this.domain = domain;
    this.name = name;
    this.friendlyName = this.attributes.friendly_name || getFriendlyName(entityId);
    this.icon = getDeviceIcon(domain, state, attributes);
    this.isOn = isDeviceOn(state);
    this.isControllable = isControllable(domain);
    this.brightness = domain === DEVICE_DOMAINS.LIGHT ? getBrightness(attributes) : null;
  }

  toJSON() {
    return {
      entityId: this.entityId,
      state: this.state,
      friendlyName: this.friendlyName,
      icon: this.icon,
      isOn: this.isOn,
      isControllable: this.isControllable,
      brightness: this.brightness,
      domain: this.domain,
    };
  }
}

/**
 * Groups devices by domain
 */
export function groupDevicesByDomain(devices) {
  const grouped = {};
  devices.forEach(device => {
    if (!grouped[device.domain]) {
      grouped[device.domain] = [];
    }
    grouped[device.domain].push(device);
  });
  return grouped;
}

/**
 * Filters devices to only controllable ones
 */
export function getControllableDevices(devices) {
  return devices.filter(device => device.isControllable);
}

