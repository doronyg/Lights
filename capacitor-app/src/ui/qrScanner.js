/**
 * QR Code Scanner UI Component
 * Allows scanning QR codes to extract Home Assistant credentials
 * QR code should contain: ha_url|ha_token
 *
 * Note: For production, use a library like jsQR, zxing, or QuaggaJS
 * This version uses canvas-based detection
 */

export class QRScannerUI {
  constructor() {
    this.isOpen = false;
    this.video = null;
    this.canvas = null;
    this.stream = null;
    this.scanCallback = null;
    this.animationId = null;
  }

  /**
   * Render QR scanner modal
   */
  render(onScan) {
    this.scanCallback = onScan;

    const html = `
      <div class="qr-scanner-overlay" id="qrScannerOverlay">
        <div class="qr-scanner-panel">
          <div class="qr-scanner-header">
            <h2>📸 Scan QR Code</h2>
            <button class="close-btn" id="closeQRScanner">&times;</button>
          </div>

          <div class="qr-scanner-content">
            <video id="qrVideo" class="qr-video"></video>
            <canvas id="qrCanvas" style="display:none;"></canvas>

            <div class="qr-scanner-info">
              <p>Point your camera at a QR code</p>
              <p class="qr-scanner-status" id="qrStatus">Initializing camera...</p>
            </div>

            <div style="border-top: 1px solid var(--border); padding-top: var(--spacing-md); margin-top: var(--spacing-md);">
              <p style="font-size: 12px; color: var(--text-secondary); margin: 0 0 var(--spacing-sm) 0;"><strong>Or manually enter your QR code data:</strong></p>
              <input
                type="text"
                id="qrManualInput"
                placeholder="Example: http://192.168.1.100:8123|eyJ0eXAiOiJKV1Q..."
                style="width: 100%; padding: var(--spacing-sm); border: 1px solid var(--border); border-radius: var(--border-radius); font-size: 12px; font-family: monospace; box-sizing: border-box;"
              />
              <button class="btn btn-secondary" id="qrManualSubmitBtn" style="width: 100%; margin-top: var(--spacing-sm);">Use This Code</button>
            </div>

            <button class="btn btn-secondary" id="cancelQRScanner" style="margin-top: var(--spacing-md);">Cancel</button>
          </div>
        </div>
      </div>
    `;

    const container = document.createElement('div');
    container.innerHTML = html;
    document.body.appendChild(container);

    this.setupEventListeners();
    this.startScanning();
  }

  /**
   * Setup event listeners
   */
  setupEventListeners() {
    document.getElementById('closeQRScanner').addEventListener('click', () => this.close());
    document.getElementById('cancelQRScanner').addEventListener('click', () => this.close());
    document.getElementById('qrScannerOverlay').addEventListener('click', (e) => {
      if (e.target.id === 'qrScannerOverlay') this.close();
    });

    // Manual QR code input
    const manualBtn = document.getElementById('qrManualSubmitBtn');
    if (manualBtn) {
      manualBtn.addEventListener('click', () => {
        const input = document.getElementById('qrManualInput');
        const value = input.value.trim();
        console.log('[QR Scanner] Manual input submitted:', value);
        if (value) {
          this.handleQRCodeDetected(value);
        } else {
          alert('Please enter QR code data in the format: url|token');
        }
      });
    }
  }

  /**
   * Start scanning for QR codes
   */
  async startScanning() {
    try {
      this.video = document.getElementById('qrVideo');
      this.canvas = document.getElementById('qrCanvas');
      const statusEl = document.getElementById('qrStatus');

      // Request camera access
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' }
      });

      this.video.srcObject = this.stream;
      this.video.setAttribute('playsinline', 'true');
      this.video.setAttribute('autoplay', 'true');
      this.video.play();

      statusEl.textContent = '✓ Camera ready. Point at QR code or paste below.';
      this.isOpen = true;
      console.log('[QR Scanner] Camera started, detection enabled');

      // Start detection loop (slower to reduce CPU)
      this.detectQRCode();
    } catch (error) {
      console.error('[QR Scanner] Camera access error:', error);
      document.getElementById('qrStatus').textContent =
        '❌ Camera access denied or not available.';
    }
  }

  /**
   * Detect QR code in video stream
   * Simple fallback: mainly for manual input via text field
   */
  async detectQRCode() {
    if (!this.isOpen || !this.video) return;

    try {
      const ctx = this.canvas.getContext('2d');
      this.canvas.width = this.video.videoWidth;
      this.canvas.height = this.video.videoHeight;

      if (this.canvas.width === 0) {
        this.animationId = requestAnimationFrame(() => this.detectQRCode());
        return;
      }

      // Draw video frame to canvas
      ctx.drawImage(this.video, 0, 0);

      // Log that scanning is active
      console.log('[QR Scanner] Scanning frame...');

      // If Quagga is available, try it (but don't rely on it)
      if (window.Quagga && window.Quagga.decodeSingle) {
        try {
          window.Quagga.decodeSingle({
            src: this.canvas.toDataURL(),
            config: {
              frequency: 10,
              numOfWorkers: 1,
              locate: true
            }
          }, (result) => {
            if (result && result.codeResult && result.codeResult.code) {
              console.log('[QR Scanner] QR detected via Quagga:', result.codeResult.code);
              this.handleQRCodeDetected(result.codeResult.code);
            }
          });
        } catch (e) {
          console.log('[QR Scanner] Quagga detection skipped');
        }
      }

      // Continue scanning every 500ms to reduce CPU
      this.animationId = setTimeout(() => this.detectQRCode(), 500);
    } catch (error) {
      console.error('[QR Scanner] Detection error:', error);
      this.animationId = setTimeout(() => this.detectQRCode(), 500);
    }
  }

  /**
   * Handle detected QR code data
   */
  handleQRCodeDetected(rawValue) {
    console.log('[QR Scanner] Processing QR code:', rawValue);

    try {
      // Parse QR code data format: "ha_url|ha_token"
      // Example: "http://192.168.1.100:8123|eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
      const parts = rawValue.split('|');
      console.log('[QR Scanner] Parts:', parts.length);

      if (parts.length === 2) {
        const [haUrl, haToken] = parts;
        console.log('[QR Scanner] URL:', haUrl, 'Token length:', haToken.length);

        if (haUrl && haToken && haUrl.startsWith('http')) {
          console.log('[QR Scanner] ✓ Valid format detected');
          document.getElementById('qrStatus').textContent = '✓ QR Code read successfully!';

          this.close();

          // Call the callback with parsed data
          if (this.scanCallback) {
            this.scanCallback({
              haUrl: haUrl.trim(),
              haToken: haToken.trim()
            });
          }
          return;
        }
      }

      // If format doesn't match, try to detect URL
      console.log('[QR Scanner] ✗ Invalid format');
      if (rawValue.startsWith('http')) {
        document.getElementById('qrStatus').textContent =
          'Found URL but missing token. Format should be: url|token';
      } else {
        document.getElementById('qrStatus').textContent =
          'QR code format invalid. Expected: url|token';
      }
    } catch (error) {
      console.error('[QR Scanner] Error parsing QR code:', error);
      document.getElementById('qrStatus').textContent = 'Error parsing QR code data';
    }
  }

  /**
   * Close scanner
   */
  close() {
    this.isOpen = false;

    // Stop camera stream
    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
      this.stream = null;
    }

    // Cancel animation/timeout
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
      clearTimeout(this.animationId);
    }

    // Remove overlay
    const overlay = document.getElementById('qrScannerOverlay');
    if (overlay) {
      overlay.remove();
    }

    console.log('[QR Scanner] Scanner closed');
  }
}

export default QRScannerUI;













