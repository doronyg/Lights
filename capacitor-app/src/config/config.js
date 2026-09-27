/**
 * Build-time Configuration
 * Determines whether app runs in LOCAL or BACKEND mode
 * Set via environment variable: VITE_APP_MODE=local|backend
 */

export const APP_MODE = __APP_MODE__ || 'local'; // 'local' or 'backend'

export const IS_LOCAL_MODE = APP_MODE === 'local';
export const IS_BACKEND_MODE = APP_MODE === 'backend';

export const CONFIG = {
  // Local mode: communicate directly with Home Assistant
  LOCAL: {
    mode: 'local',
    description: 'Connect directly to Home Assistant',
    requiresBackend: false,
  },

  // Backend mode: communicate through a backend server
  BACKEND: {
    mode: 'backend',
    description: 'Connect through backend server',
    requiresBackend: true,
    defaultBackendUrl: import.meta.env.VITE_BACKEND_URL || 'http://localhost:3001',
  },
};

/**
 * Get current configuration
 */
export function getConfig() {
  return IS_LOCAL_MODE ? CONFIG.LOCAL : CONFIG.BACKEND;
}

/**
 * Validate configuration
 */
export function validateConfig() {
  const config = getConfig();
  console.log(`[Config] Running in ${config.mode} mode: ${config.description}`);
  return config;
}

/**
 * Get API base URL based on mode
 */
export function getApiBaseUrl(authService) {
  if (IS_LOCAL_MODE) {
    return authService.haUrl;
  }
  return authService.backendUrl || CONFIG.BACKEND.defaultBackendUrl;
}

/**
 * Check if running in development
 */
export function isDevelopment() {
  return import.meta.env.DEV;
}

/**
 * Check if running in production
 */
export function isProduction() {
  return import.meta.env.PROD;
}

export default validateConfig();



