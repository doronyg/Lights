/**
 * State Management Service
 * Manages app state similar to StateFlow in Android
 */

class StateManager {
  constructor() {
    this.state = {
      devices: [],
      isLoading: false,
      error: null,
      isConnected: false,
      lastUpdate: null,
    };
    this.listeners = [];
  }

  /**
   * Subscribe to state changes
   */
  subscribe(callback) {
    this.listeners.push(callback);
    // Return unsubscribe function
    return () => {
      this.listeners = this.listeners.filter(listener => listener !== callback);
    };
  }

  /**
   * Notify all listeners of state change
   */
  notifyListeners() {
    this.listeners.forEach(listener => listener(this.state));
  }

  /**
   * Update state
   */
  setState(updates) {
    this.state = {
      ...this.state,
      ...updates,
      lastUpdate: new Date().toISOString(),
    };
    this.notifyListeners();
  }

  /**
   * Get current state
   */
  getState() {
    return { ...this.state };
  }

  /**
   * Set devices list
   */
  setDevices(devices) {
    this.setState({ devices });
  }

  /**
   * Set loading state
   */
  setLoading(isLoading) {
    this.setState({ isLoading });
  }

  /**
   * Set error
   */
  setError(error) {
    this.setState({ error: error ? error.message : null });
  }

  /**
   * Set connection status
   */
  setConnected(isConnected) {
    this.setState({ isConnected });
  }

  /**
   * Clear error
   */
  clearError() {
    this.setState({ error: null });
  }

  /**
   * Reset state
   */
  reset() {
    this.state = {
      devices: [],
      isLoading: false,
      error: null,
      isConnected: false,
      lastUpdate: null,
    };
    this.notifyListeners();
  }
}

export default new StateManager();

