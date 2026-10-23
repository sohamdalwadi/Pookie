/**
 * 💖 Adaptive Room Poller
 * Efficient, quota-friendly polling with exponential backoff on error
 * and version-based ETag checks.
 */
import { api } from './gasClient.js';

export class RoomPoller {
  constructor({ roomCode, playerId, onUpdate, onError }) {
    this.roomCode = roomCode;
    this.playerId = playerId;
    this.onUpdate = onUpdate;
    this.onError = onError;

    this.isRunning = false;
    this.timerId = null;
    this.lastVersion = null;
    this.consecutiveErrors = 0;
    this.currentRoom = null;

    this.handleVisibilityChange = this.handleVisibilityChange.bind(this);
  }

  start() {
    if (this.isRunning) return;
    this.isRunning = true;
    document.addEventListener('visibilitychange', this.handleVisibilityChange);
    this.pollImmediately();
  }

  stop() {
    this.isRunning = false;
    if (this.timerId) {
      clearTimeout(this.timerId);
      this.timerId = null;
    }
    document.removeEventListener('visibilitychange', this.handleVisibilityChange);
  }

  handleVisibilityChange() {
    if (!document.hidden && this.isRunning) {
      // Woke up from background tab, poll immediately
      this.pollImmediately();
    }
  }

  async pollImmediately() {
    if (!this.isRunning) return;
    if (this.timerId) {
      clearTimeout(this.timerId);
      this.timerId = null;
    }

    try {
      const response = await api.getState(this.roomCode, this.playerId, this.lastVersion);
      this.consecutiveErrors = 0;

      if (response && response.success) {
        const data = response.data;
        if (data.isUpdated === false) {
          // No changes on backend
        } else {
          this.lastVersion = data.version;
          this.currentRoom = data;
          if (this.onUpdate) {
            this.onUpdate(data);
          }
        }
      } else if (response && !response.success) {
        if (this.onError) {
          this.onError(new Error(response.error?.message || 'Failed to sync room state'));
        }
      }
    } catch (err) {
      this.consecutiveErrors++;
      if (this.onError) {
        this.onError(err);
      }
    } finally {
      this.scheduleNext();
    }
  }

  calculateInterval() {
    if (document.hidden) return 6000;
    if (this.consecutiveErrors > 0) {
      // Exponential backoff up to 10s
      return Math.min(10000, 1500 * Math.pow(1.5, this.consecutiveErrors));
    }

    if (!this.currentRoom) return 2000;

    const { status, currentTurn, host, partner } = this.currentRoom;

    if (status === 'WAITING_FOR_PARTNER') return 2500;
    if (status === 'FINISHED') return 3000;

    if (status === 'PLAYING') {
      const isHost = this.playerId === host?.id;
      const myTurn = (isHost && currentTurn === 'HOST') || (!isHost && currentTurn === 'PARTNER');
      // If waiting for partner, poll slightly faster for snappy reaction
      return myTurn ? 2500 : 1500;
    }

    return 3000;
  }

  scheduleNext() {
    if (!this.isRunning) return;
    const interval = this.calculateInterval();
    this.timerId = setTimeout(() => this.pollImmediately(), interval);
  }
}
