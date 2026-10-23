/**
 * 💖 Pookie Google Apps Script REST Client
 * Handles CORS simple-requests, 302 redirect following, and optimistic concurrency.
 */
import { CONFIG } from '../config.js';

class GasClient {
  constructor() {
    this.timeoutMs = 15000;
  }

  getUrl() {
    const url = CONFIG.getBackendUrl();
    if (!url) {
      throw new Error(
        'Pookie backend is not configured. Please ensure POOKIE_DEMO_DEPLOYMENT_ID is set in CI secrets or local.properties.'
      );
    }
    return url;
  }

  async get(action, queryParams = {}) {
    const base = this.getUrl();
    const url = new URL(base);
    url.searchParams.set('action', action);

    for (const [key, value] of Object.entries(queryParams)) {
      if (value !== undefined && value !== null) {
        url.searchParams.set(key, String(value));
      }
    }

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.timeoutMs);

    try {
      const response = await fetch(url.toString(), {
        method: 'GET',
        redirect: 'follow',
        signal: controller.signal
      });

      if (!response.ok) {
        throw new Error(`HTTP Error ${response.status}: ${response.statusText}`);
      }

      const json = await response.json();
      json.success = json.status === 'SUCCESS';
      return json;
    } catch (err) {
      if (err.name === 'AbortError') {
        throw new Error('Network request timed out. Please check your internet connection.');
      }
      throw err;
    } finally {
      clearTimeout(timeoutId);
    }
  }

  async post(action, payload = {}) {
    const base = this.getUrl();
    const bodyData = JSON.stringify({
      action: action,
      payload: payload
    });

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.timeoutMs);

    try {
      // NOTE: Using text/plain prevents CORS OPTIONS preflight, which GAS Web Apps do not support.
      // GAS parses e.postData.contents directly as JSON.
      const response = await fetch(base, {
        method: 'POST',
        redirect: 'follow',
        headers: {
          'Content-Type': 'text/plain;charset=utf-8'
        },
        body: bodyData,
        signal: controller.signal
      });

      if (!response.ok) {
        throw new Error(`HTTP Error ${response.status}: ${response.statusText}`);
      }

      const json = await response.json();
      json.success = json.status === 'SUCCESS';
      return json;
    } catch (err) {
      if (err.name === 'AbortError') {
        throw new Error('Action timed out. Server might be busy, please retry.');
      }
      throw err;
    } finally {
      clearTimeout(timeoutId);
    }
  }

  // --- Specific Backend Action Wrappers ---

  async checkHealth() {
    return this.get('HEALTH_CHECK');
  }

  async getState(roomCode, playerId, version = null) {
    const params = { roomCode, playerId };
    if (version !== null && version !== undefined) {
      params.v = version;
    }
    return this.get('GET_STATE', params);
  }

  async createRoom({ hostName, hostId, gameType, gender, gridSize }) {
    return this.post('CREATE_ROOM', { hostName, hostId, gameType, gender, gridSize });
  }

  async joinRoom({ roomCode, partnerName, partnerId, gender }) {
    return this.post('JOIN_ROOM', { roomCode, partnerName, partnerId, gender });
  }

  async allowEva({ roomCode }) {
    return this.post('ALLOW_EVA', { roomCode });
  }

  async makeMove({ roomCode, playerId, expectedVersion, move, reaction }) {
    return this.post('MAKE_MOVE', { roomCode, playerId, expectedVersion, move, reaction });
  }

  async resetGame({ roomCode, playerId }) {
    return this.post('RESET_GAME', { roomCode, playerId });
  }

  async startQuiz({ roomCode, questions }) {
    return this.post('START_QUIZ', { roomCode, questions });
  }

  async sendReaction({ roomCode, playerId, reaction }) {
    return this.post('SEND_REACTION', { roomCode, playerId, reaction });
  }

  async submitVerdict({ roomCode, playerId, verdict }) {
    return this.post('SUBMIT_VERDICT', { roomCode, playerId, verdict });
  }

  async leaveRoom({ roomCode, playerId }) {
    return this.post('LEAVE_ROOM', { roomCode, playerId });
  }
}

export const api = new GasClient();
