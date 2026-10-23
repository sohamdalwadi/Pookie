/**
 * 💖 Pookie Web Configuration
 * Injected at build time from GitHub Actions secret POOKIE_DEMO_DEPLOYMENT_ID
 * or read locally from local.properties.
 * Players NEVER need to manually enter this deployment key!
 */

const RAW_INJECTED_KEY = typeof __POOKIE_DEMO_DEPLOYMENT_ID__ !== 'undefined' 
  ? __POOKIE_DEMO_DEPLOYMENT_ID__ 
  : '';

export const CONFIG = {
  INJECTED_DEPLOYMENT_ID: RAW_INJECTED_KEY,

  /**
   * Sanitizes and produces the canonical Google Apps Script Web App exec URL.
   */
  sanitizeEndpoint(input) {
    if (!input) return '';
    let cleaned = input.trim().replace(/["']/g, '');

    // If it's a full Google Apps Script URL, extract the deployment ID
    if (cleaned.includes('macros/s/')) {
      const match = cleaned.match(/macros\/s\/([^/?#]+)/);
      if (match && match[1]) {
        return `https://script.google.com/macros/s/${match[1]}/exec`;
      }
    }

    if (cleaned.startsWith('http://') || cleaned.startsWith('https://')) {
      return cleaned.replace(/\/dev$/, '/exec');
    }

    // Bare deployment ID (e.g. AKfycb...)
    const cleanId = cleaned.split('?')[0].split('/')[0].trim();
    return `https://script.google.com/macros/s/${cleanId}/exec`;
  },

  /**
   * Resolves the active backend URL.
   * Priority:
   * 1. URL search param ?key=... (for developer override/testing)
   * 2. Injected secret POOKIE_DEMO_DEPLOYMENT_ID
   */
  getBackendUrl() {
    try {
      const params = new URLSearchParams(window.location.search);
      const overrideKey = params.get('key');
      if (overrideKey && overrideKey.trim()) {
        return this.sanitizeEndpoint(overrideKey.trim());
      }
    } catch (e) {
      // Ignore URL parsing errors
    }

    if (this.INJECTED_DEPLOYMENT_ID && this.INJECTED_DEPLOYMENT_ID.trim()) {
      return this.sanitizeEndpoint(this.INJECTED_DEPLOYMENT_ID.trim());
    }

    return '';
  },

  hasValidKey() {
    return Boolean(this.getBackendUrl());
  }
};
