/**
 * 💖 Pookie Web App Entry Point
 * Orchestrates Lobby, Game Arenas, Adaptive Poller, Audio, and Reactions.
 */
import { api } from './api/gasClient.js';
import { RoomPoller } from './api/poller.js';
import { sessionStore } from './state/sessionStore.js';
import { sounds } from './utils/sound.js';
import { reactions } from './components/reactions.js';
import { renderLobby } from './components/lobby.js';
import { renderScoreboard, renderDesktopPanels } from './components/scoreboard.js';
import { renderRematchModal } from './components/rematchModal.js';
import { renderTicTacToe } from './games/tictactoe.js';
import { renderDotsAndBoxes } from './games/dotsandboxes.js';
import { renderCouplesQuiz, DEFAULT_QUIZ_QUESTIONS } from './games/couplesquiz.js';
import { renderSoulSync } from './games/soulsync.js';

import './styles/tokens.css';
import './styles/animations.css';
import './styles/layout.css';
import './styles/games.css';

class PookieApp {
  constructor() {
    this.appEl = document.getElementById('app');
    this.currentRoom = null;
    this.poller = null;
    this.playerId = sessionStore.getPlayerId();
    this.isSubmittingMove = false;
    this.backendStatus = 'CHECKING';
    this.backendLatency = null;

    this.init();
  }

  async init() {
    this.renderHeader();
    this.checkBackendHealth();

    // Check if there's a room parameter in the URL or in sessionStorage
    const urlParams = new URLSearchParams(window.location.search);
    const roomParam = urlParams.get('room');
    const storedRoom = sessionStore.getActiveRoomCode();
    const targetRoomCode = roomParam || storedRoom;

    if (targetRoomCode) {
      await this.tryResumeRoom(targetRoomCode);
    } else {
      this.render();
    }
  }

  async checkBackendHealth() {
    this.backendStatus = 'CHECKING';
    this.renderHeader();
    const start = performance.now();

    try {
      const res = await api.checkHealth();
      if (res && res.status === 'SUCCESS') {
        this.backendStatus = 'ONLINE';
        this.backendLatency = Math.round(performance.now() - start);
      } else {
        this.backendStatus = 'OFFLINE';
      }
    } catch (err) {
      this.backendStatus = 'OFFLINE';
    }
    this.renderHeader();
  }

  renderHeader() {
    let header = document.querySelector('.app-header');
    if (!header) {
      header = document.createElement('header');
      header.className = 'app-header';
      this.appEl.prepend(header);
    }

    const isSoundOn = sessionStore.isSoundEnabled();
    const hasRoom = Boolean(this.currentRoom && this.currentRoom.roomCode);

    const statusTitle = this.backendStatus === 'ONLINE'
      ? `Backend Online (${this.backendLatency}ms) • Tap to re-check`
      : (this.backendStatus === 'CHECKING' ? 'Connecting to backend...' : 'Backend unreachable • Tap to retry');

    header.innerHTML = `
      <div class="brand-badge">
        <span class="heart-logo">💖</span>
        <span>Pookie</span>
      </div>

      <div class="header-actions">
        <!-- Health Status Indicator -->
        <div class="health-pill ${this.backendStatus.toLowerCase()}" id="btn-health-pill" title="${statusTitle}">
          <span class="status-dot"></span>
          <span>${this.backendStatus === 'ONLINE' ? 'Online' : (this.backendStatus === 'CHECKING' ? 'Checking' : 'Offline')}</span>
        </div>

        ${hasRoom ? `
          <div class="room-badge" id="btn-header-code" title="Copy room code">
            <span>🔑</span>
            <span>${this.currentRoom.roomCode}</span>
          </div>
        ` : ''}

        <button class="icon-btn" id="btn-sound-toggle" title="Toggle Sound">
          ${isSoundOn ? '🔊' : '🔇'}
        </button>

        ${hasRoom ? `
          <button class="icon-btn" id="btn-header-exit" title="Leave Game" style="color: var(--primary);">
            ✕
          </button>
        ` : ''}
      </div>
    `;

    header.querySelector('#btn-health-pill')?.addEventListener('click', () => {
      sounds.playTap();
      this.checkBackendHealth();
    });


    header.querySelector('#btn-sound-toggle').addEventListener('click', () => {
      const nextSound = !sessionStore.isSoundEnabled();
      sessionStore.setSoundEnabled(nextSound);
      sounds.playTap();
      this.renderHeader();
    });

    const codeBtn = header.querySelector('#btn-header-code');
    if (codeBtn) {
      codeBtn.addEventListener('click', () => {
        navigator.clipboard.writeText(this.currentRoom.roomCode);
        sounds.playTap();
        alert(`Room Code ${this.currentRoom.roomCode} copied! 💖`);
      });
    }

    const exitBtn = header.querySelector('#btn-header-exit');
    if (exitBtn) {
      exitBtn.addEventListener('click', () => {
        if (confirm('Are you sure you want to exit to the lobby?')) {
          this.handleLeaveRoom();
        }
      });
    }
  }

  async tryResumeRoom(roomCode) {
    try {
      const response = await api.getState(roomCode, this.playerId);
      if (response && response.success && response.data) {
        this.currentRoom = response.data;
        sessionStore.setActiveRoomCode(roomCode);
        this.startPoller();
      } else {
        sessionStore.setActiveRoomCode('');
      }
    } catch (e) {
      sessionStore.setActiveRoomCode('');
    }
    this.render();
  }

  startPoller() {
    if (this.poller) {
      this.poller.stop();
    }

    this.poller = new RoomPoller({
      roomCode: this.currentRoom.roomCode,
      playerId: this.playerId,
      onUpdate: (updatedRoom) => {
        this.currentRoom = updatedRoom;
        if (updatedRoom.gameState?.lastReaction) {
          reactions.handleIncomingReaction(updatedRoom.gameState.lastReaction);
        }
        this.render();
      },
      onError: (err) => {
        console.warn('Poller sync notice:', err.message);
      }
    });

    this.poller.start();
  }

  stopPoller() {
    if (this.poller) {
      this.poller.stop();
      this.poller = null;
    }
  }

  async handleCreateRoom({ hostName, gameType, gridSize, gender, questions }) {
    try {
      const response = await api.createRoom({
        hostName,
        hostId: this.playerId,
        gameType,
        gridSize,
        gender
      });

      if (response && response.success) {
        this.currentRoom = response.data;
        sessionStore.setActiveRoomCode(this.currentRoom.roomCode);
        this.startPoller();
        this.render();
      } else {
        alert(response.error?.message || 'Failed to create room. Please check backend connection.');
      }
    } catch (err) {
      alert(err.message || 'Error communicating with backend.');
    }
  }

  async handleJoinRoom({ roomCode, partnerName, gender }) {
    try {
      const response = await api.joinRoom({
        roomCode,
        partnerName,
        partnerId: this.playerId,
        gender
      });

      if (response && response.success) {
        this.currentRoom = response.data;
        sessionStore.setActiveRoomCode(this.currentRoom.roomCode);
        this.startPoller();
        this.render();
      } else {
        alert(response.error?.message || 'Could not join room.');
      }
    } catch (err) {
      alert(err.message || 'Error communicating with backend.');
    }
  }

  async handleAllowEva(roomCode) {
    try {
      const response = await api.allowEva({ roomCode });
      if (response && response.success) {
        this.currentRoom = response.data;
        this.render();
      } else {
        alert(response.error?.message || 'Could not invite Eva.');
      }
    } catch (err) {
      alert(err.message || 'Error communicating with backend.');
    }
  }

  async handleMakeMove(moveData) {
    if (this.isSubmittingMove || !this.currentRoom) return;
    this.isSubmittingMove = true;

    try {
      const response = await api.makeMove({
        roomCode: this.currentRoom.roomCode,
        playerId: this.playerId,
        expectedVersion: this.currentRoom.version,
        move: moveData
      });

      if (response && response.success) {
        this.currentRoom = response.data;
        this.render();
      } else if (response && response.error?.code === 'CONCURRENCY_CONFLICT') {
        // Fetch fresh state immediately
        await this.poller?.pollImmediately();
      } else {
        alert(response?.error?.message || 'Move failed.');
      }
    } catch (err) {
      console.warn('Move network error:', err);
    } finally {
      this.isSubmittingMove = false;
    }
  }

  async handleResetGame() {
    if (!this.currentRoom) return;
    try {
      const response = await api.resetGame({
        roomCode: this.currentRoom.roomCode,
        playerId: this.playerId
      });
      if (response && response.success) {
        this.currentRoom = response.data;
        this.render();
      }
    } catch (err) {
      console.warn('Rematch reset error:', err);
    }
  }

  async handleSubmitVerdict(verdict) {
    if (!this.currentRoom) return;
    try {
      const response = await api.submitVerdict({
        roomCode: this.currentRoom.roomCode,
        playerId: this.playerId,
        verdict
      });
      if (response && response.success) {
        this.currentRoom = response.data;
        this.render();
      }
    } catch (err) {
      console.warn('Submit verdict error:', err);
    }
  }

  async handleSendReaction(reactionType) {
    if (!this.currentRoom) return;
    reactions.showFloatingReaction(reactionType, true);

    try {
      await api.sendReaction({
        roomCode: this.currentRoom.roomCode,
        playerId: this.playerId,
        reaction: reactionType
      });
    } catch (err) {
      // Non-critical background reaction
    }
  }

  async handleLeaveRoom() {
    this.stopPoller();
    if (this.currentRoom) {
      try {
        await api.leaveRoom({
          roomCode: this.currentRoom.roomCode,
          playerId: this.playerId
        });
      } catch (e) {}
    }

    this.currentRoom = null;
    sessionStore.setActiveRoomCode('');

    // Clear room param from URL without page reload
    const cleanUrl = `${window.location.origin}${window.location.pathname}`;
    window.history.replaceState({}, document.title, cleanUrl);

    this.render();
  }

  render() {
    this.renderHeader();

    let mainStage = document.querySelector('.main-stage');
    if (!mainStage) {
      mainStage = document.createElement('main');
      mainStage.className = 'main-stage';
      this.appEl.appendChild(mainStage);
    }
    mainStage.innerHTML = '';

    // Remove any leftover bottom reaction bar or modal
    document.querySelectorAll('.bottom-reactions-bar, .modal-overlay').forEach((el) => el.remove());

    // 1. LOBBY OR WAITING ROOM
    if (!this.currentRoom || this.currentRoom.status === 'WAITING_FOR_PARTNER' || this.currentRoom.status === 'ABANDONED') {
      const lobbyEl = renderLobby({
        activeRoom: this.currentRoom,
        onCreateRoom: this.handleCreateRoom.bind(this),
        onJoinRoom: this.handleJoinRoom.bind(this),
        onAllowEva: this.handleAllowEva.bind(this),
        onLeaveRoom: this.handleLeaveRoom.bind(this)
      });
      mainStage.appendChild(lobbyEl);
      return;
    }

    // 2. ACTIVE GAME ROOM (PLAYING or FINISHED)
    // Desktop 3-column wrapper or standard mobile column
    const desktopPanels = renderDesktopPanels({
      room: this.currentRoom,
      playerId: this.playerId
    });

    const mobileScoreboard = renderScoreboard({
      room: this.currentRoom,
      playerId: this.playerId
    });

    const gridContainer = document.createElement('div');
    gridContainer.className = 'game-container-grid';

    // Left Panel (Desktop only)
    gridContainer.appendChild(desktopPanels.leftPanel);

    // Center Arena (Active Game Board)
    const centerArena = document.createElement('div');
    centerArena.appendChild(mobileScoreboard);

    // Render corresponding game component
    const gameType = this.currentRoom.gameType;
    let gameEl = null;

    if (gameType === 'TIC_TAC_TOE') {
      gameEl = renderTicTacToe({
        room: this.currentRoom,
        playerId: this.playerId,
        onMove: this.handleMakeMove.bind(this)
      });
    } else if (gameType === 'DOTS_AND_BOXES') {
      gameEl = renderDotsAndBoxes({
        room: this.currentRoom,
        playerId: this.playerId,
        onMove: this.handleMakeMove.bind(this)
      });
    } else if (gameType === 'COUPLES_QUIZ') {
      gameEl = renderCouplesQuiz({
        room: this.currentRoom,
        playerId: this.playerId,
        onMove: this.handleMakeMove.bind(this),
        onNextQuestion: (nextIdx) => {
          this.handleMakeMove({
            advanceQuestion: true,
            nextIndex: nextIdx
          });
        }
      });
    } else if (gameType === 'SOUL_SYNC') {
      gameEl = renderSoulSync({
        room: this.currentRoom,
        playerId: this.playerId,
        onMove: this.handleMakeMove.bind(this),
        onSubmitVerdict: this.handleSubmitVerdict.bind(this)
      });
    }

    if (gameEl) {
      centerArena.appendChild(gameEl);
    }

    gridContainer.appendChild(centerArena);

    // Right Panel (Desktop only)
    gridContainer.appendChild(desktopPanels.rightPanel);

    mainStage.appendChild(gridContainer);

    // 3. FIXED BOTTOM REACTIONS BAR
    this.renderBottomReactions();

    // 4. REMATCH MODAL (if game finished)
    if (this.currentRoom.status === 'FINISHED') {
      const rematchModal = renderRematchModal({
        room: this.currentRoom,
        playerId: this.playerId,
        onResetGame: this.handleResetGame.bind(this),
        onLeaveRoom: this.handleLeaveRoom.bind(this)
      });
      if (rematchModal) {
        document.body.appendChild(rematchModal);
      }
    }
  }

  renderBottomReactions() {
    const bar = document.createElement('div');
    bar.className = 'bottom-reactions-bar';

    const reactionsList = [
      { type: 'HEART', emoji: '💖' },
      { type: 'KISS', emoji: '💋' },
      { type: 'HUG', emoji: '🥺' },
      { type: 'FIRE', emoji: '🔥' },
      { type: 'LAUGH', emoji: '😂' }
    ];

    reactionsList.forEach(({ type, emoji }) => {
      const btn = document.createElement('button');
      btn.className = 'reaction-btn';
      btn.textContent = emoji;
      btn.title = `Send ${type}`;
      btn.addEventListener('click', () => {
        this.handleSendReaction(type);
      });
      bar.appendChild(btn);
    });

    document.body.appendChild(bar);
  }
}

// Boot application
window.addEventListener('DOMContentLoaded', () => {
  new PookieApp();
});
