/**
 * 💖 Pookie Lobby Component
 * Create Room, Join Room, QR Code sharing, and Eva AI opponent trigger.
 */
import QRCode from 'qrcode';
import { sessionStore } from '../state/sessionStore.js';
import { sounds } from '../utils/sound.js';
import { DEFAULT_QUIZ_QUESTIONS } from '../games/couplesquiz.js';

export function renderLobby({
  activeRoom,
  onCreateRoom,
  onJoinRoom,
  onAllowEva,
  onLeaveRoom
}) {
  const container = document.createElement('div');
  container.className = 'anim-entrance';
  container.style.maxWidth = '520px';
  container.style.margin = '0 auto';

  // 1. If currently in a room WAITING FOR PARTNER
  if (activeRoom && activeRoom.status === 'WAITING_FOR_PARTNER') {
    return renderWaitingRoom({ activeRoom, onAllowEva, onLeaveRoom });
  }

  // 2. Normal Lobby: Profile + Create or Join
  let selectedTab = 'CREATE'; // 'CREATE' or 'JOIN'
  let selectedGame = 'TIC_TAC_TOE';
  let selectedGridSize = 3;

  const card = document.createElement('div');
  card.className = 'glass-panel';

  // Player Profile Pill
  const profileSection = document.createElement('div');
  profileSection.style.display = 'flex';
  profileSection.style.alignItems = 'center';
  profileSection.style.gap = '0.75rem';
  profileSection.style.marginBottom = '1.5rem';
  profileSection.style.paddingBottom = '1.25rem';
  profileSection.style.borderBottom = '1px solid var(--glass-border)';

  const currentName = sessionStore.getPlayerName();
  const currentGender = sessionStore.getGender();

  profileSection.innerHTML = `
    <div class="player-avatar" id="avatar-toggle" style="cursor: pointer;" title="Toggle Gender">
      ${currentGender === 'FEMALE' ? '👧' : '👦'}
    </div>
    <div style="flex: 1;">
      <label style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700;">YOUR NAME</label>
      <input type="text" id="player-name-input" value="${currentName}" 
        style="background: transparent; color: #fff; font-size: 1.1rem; font-weight: 700; width: 100%; border-bottom: 1px dashed var(--glass-border);" 
        maxlength="20" placeholder="Enter nickname..." />
    </div>
  `;

  const nameInput = profileSection.querySelector('#player-name-input');
  nameInput.addEventListener('change', () => {
    sessionStore.setPlayerName(nameInput.value);
  });

  const avatarToggle = profileSection.querySelector('#avatar-toggle');
  avatarToggle.addEventListener('click', () => {
    const nextGender = sessionStore.getGender() === 'MALE' ? 'FEMALE' : 'MALE';
    sessionStore.setGender(nextGender);
    avatarToggle.textContent = nextGender === 'FEMALE' ? '👧' : '👦';
    sounds.playTap();
  });

  card.appendChild(profileSection);

  // Mode Tabs (Create Game vs Join Room)
  const tabsRow = document.createElement('div');
  tabsRow.style.display = 'flex';
  tabsRow.style.gap = '0.5rem';
  tabsRow.style.marginBottom = '1.5rem';

  const btnCreateTab = document.createElement('button');
  btnCreateTab.className = 'btn-primary';
  btnCreateTab.style.flex = '1';
  btnCreateTab.textContent = '✨ Create Room';

  const btnJoinTab = document.createElement('button');
  btnJoinTab.className = 'btn-secondary';
  btnJoinTab.style.flex = '1';
  btnJoinTab.textContent = '🔗 Join Room';

  tabsRow.appendChild(btnCreateTab);
  tabsRow.appendChild(btnJoinTab);
  card.appendChild(tabsRow);


  // Tab Content Container
  const tabContent = document.createElement('div');
  card.appendChild(tabContent);

  function updateTabContent() {
    tabContent.innerHTML = '';

    if (selectedTab === 'CREATE') {
      btnCreateTab.className = 'btn-primary';
      btnJoinTab.className = 'btn-secondary';

      const createForm = document.createElement('div');
      createForm.innerHTML = `
        <label style="font-weight: 700; font-size: 0.85rem; color: var(--text-muted); display: block; margin-bottom: 0.75rem;">
          SELECT MINI-GAME:
        </label>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-bottom: 1.25rem;">
          <button type="button" class="game-pick-btn ${selectedGame === 'TIC_TAC_TOE' ? 'active' : ''}" data-game="TIC_TAC_TOE">
            <span style="font-size: 1.5rem;">✕◯</span>
            <b>Tic-Tac-Toe</b>
          </button>
          <button type="button" class="game-pick-btn ${selectedGame === 'DOTS_AND_BOXES' ? 'active' : ''}" data-game="DOTS_AND_BOXES">
            <span style="font-size: 1.5rem;">⚄</span>
            <b>Dots & Boxes</b>
          </button>
          <button type="button" class="game-pick-btn ${selectedGame === 'COUPLES_QUIZ' ? 'active' : ''}" data-game="COUPLES_QUIZ">
            <span style="font-size: 1.5rem;">💭</span>
            <b>Couples Quiz</b>
          </button>
          <button type="button" class="game-pick-btn ${selectedGame === 'SOUL_SYNC' ? 'active' : ''}" data-game="SOUL_SYNC">
            <span style="font-size: 1.5rem;">💖</span>
            <b>SoulSync</b>
          </button>
        </div>

        ${selectedGame === 'DOTS_AND_BOXES' ? `
          <div style="margin-bottom: 1.25rem;">
            <label style="font-weight: 700; font-size: 0.85rem; color: var(--text-muted); display: block; margin-bottom: 0.5rem;">
              GRID SIZE:
            </label>
            <div style="display: flex; gap: 0.5rem;">
              <button type="button" class="size-btn ${selectedGridSize === 3 ? 'active' : ''}" data-size="3">3x3 (Fast)</button>
              <button type="button" class="size-btn ${selectedGridSize === 4 ? 'active' : ''}" data-size="4">4x4 (Medium)</button>
              <button type="button" class="size-btn ${selectedGridSize === 5 ? 'active' : ''}" data-size="5">5x5 (Deep)</button>
            </div>
          </div>
        ` : ''}

        <button id="start-create-btn" class="btn-primary" style="width: 100%; padding: 0.95rem 1.25rem; font-size: clamp(0.95rem, 3vw, 1.05rem); white-space: nowrap;">
          Create Room ➔
        </button>

      `;

      // Game selector listeners
      createForm.querySelectorAll('.game-pick-btn').forEach((btn) => {
        btn.addEventListener('click', () => {
          sounds.playTap();
          selectedGame = btn.dataset.game;
          updateTabContent();
        });
      });

      // Grid size listeners
      createForm.querySelectorAll('.size-btn').forEach((btn) => {
        btn.addEventListener('click', () => {
          sounds.playTap();
          selectedGridSize = parseInt(btn.dataset.size, 10);
          updateTabContent();
        });
      });

      createForm.querySelector('#start-create-btn').addEventListener('click', () => {
        sounds.playTap();
        const hostName = nameInput.value.trim() || 'Host';
        sessionStore.setPlayerName(hostName);
        onCreateRoom({
          hostName,
          gameType: selectedGame,
          gridSize: selectedGridSize,
          gender: sessionStore.getGender(),
          questions: selectedGame === 'COUPLES_QUIZ' ? DEFAULT_QUIZ_QUESTIONS : undefined
        });
      });

      tabContent.appendChild(createForm);
    } else {
      btnCreateTab.className = 'btn-secondary';
      btnJoinTab.className = 'btn-primary';

      // Check URL query param ?room=... for instant join
      const urlParams = new URLSearchParams(window.location.search);
      const prefilledCode = (urlParams.get('room') || '').toUpperCase().trim();

      const joinForm = document.createElement('div');
      joinForm.innerHTML = `
        <label style="font-weight: 700; font-size: 0.85rem; color: var(--text-muted); display: block; margin-bottom: 0.75rem;">
          ENTER 6-CHARACTER ROOM CODE:
        </label>
        <input type="text" id="join-room-code" class="soulsync-input" 
          placeholder="e.g. PK8392" maxlength="6" value="${prefilledCode}"
          style="text-transform: uppercase; text-align: center; font-size: 1.5rem; letter-spacing: 0.2em; font-weight: 800; margin-bottom: 1.25rem;" />
        <button id="start-join-btn" class="btn-primary" style="width: 100%; padding: 1rem; font-size: 1.05rem;">
          Join Room & Play 💖
        </button>
      `;

      const codeInput = joinForm.querySelector('#join-room-code');
      const startJoinBtn = joinForm.querySelector('#start-join-btn');

      const executeJoin = () => {
        const code = codeInput.value.trim().toUpperCase();
        if (code.length < 4) {
          codeInput.focus();
          return;
        }
        sounds.playTap();
        const partnerName = nameInput.value.trim() || 'Partner';
        sessionStore.setPlayerName(partnerName);
        onJoinRoom({
          roomCode: code,
          partnerName,
          gender: sessionStore.getGender()
        });
      };

      startJoinBtn.addEventListener('click', executeJoin);
      codeInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') executeJoin();
      });

      tabContent.appendChild(joinForm);
    }
  }

  btnCreateTab.addEventListener('click', () => {
    selectedTab = 'CREATE';
    sounds.playTap();
    updateTabContent();
  });

  btnJoinTab.addEventListener('click', () => {
    selectedTab = 'JOIN';
    sounds.playTap();
    updateTabContent();
  });

  // Check if URL has ?room=... already
  const urlRoom = new URLSearchParams(window.location.search).get('room');
  if (urlRoom) {
    selectedTab = 'JOIN';
  }

  updateTabContent();
  container.appendChild(card);
  return container;
}

/**
 * Waiting for Partner Screen (QR Code, Share link, Eva bot trigger)
 */
function renderWaitingRoom({ activeRoom, onAllowEva, onLeaveRoom }) {
  const container = document.createElement('div');
  container.className = 'glass-panel anim-entrance';
  container.style.textAlign = 'center';
  container.style.maxWidth = '460px';
  container.style.margin = '0 auto';

  const roomCode = activeRoom.roomCode;
  const joinUrl = `${window.location.origin}${window.location.pathname}?room=${roomCode}`;

  container.innerHTML = `
    <div style="font-size: 2.2rem; margin-bottom: 0.25rem;">💌</div>
    <h2 style="color: #fff; font-size: 1.5rem; margin-bottom: 0.35rem;">Room Created!</h2>
    <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem;">
      Share this code or scan the QR code to connect with your partner.
    </p>

    <div style="background: rgba(13, 11, 24, 0.9); border: 2px dashed var(--primary); border-radius: var(--radius-md); padding: 1.25rem; margin-bottom: 1.5rem;">
      <div style="font-size: 0.75rem; color: var(--primary); font-weight: 800; letter-spacing: 0.1em; margin-bottom: 0.25rem;">ROOM CODE</div>
      <div id="room-code-display" style="font-size: 2.5rem; font-weight: 900; letter-spacing: 0.15em; color: #fff; cursor: pointer;">
        ${roomCode}
      </div>
      <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">Tap to copy</div>
    </div>

    <canvas id="qr-canvas" style="background: #fff; padding: 12px; border-radius: var(--radius-md); margin: 0 auto 1.5rem auto; display: block; max-width: 180px; box-shadow: 0 4px 20px rgba(0,0,0,0.5);"></canvas>

    <div style="display: flex; gap: 0.75rem; justify-content: center; margin-bottom: 1.5rem;">
      <button id="btn-share-link" class="btn-primary" style="flex: 1;">
        📤 Share Invite Link
      </button>
      <button id="btn-allow-eva" class="btn-secondary" style="flex: 1;" title="Play instantly with AI">
        🤖 Play with Eva (AI)
      </button>
    </div>

    <button id="btn-cancel-room" style="background: transparent; color: var(--text-subtle); font-size: 0.85rem; text-decoration: underline;">
      Cancel & Return to Lobby
    </button>
  `;

  // Generate QR Code
  setTimeout(() => {
    const canvas = container.querySelector('#qr-canvas');
    if (canvas) {
      QRCode.toCanvas(canvas, joinUrl, { width: 180, margin: 1 }, (err) => {
        if (err) console.warn('QR code generation failed', err);
      });
    }
  }, 50);

  // Copy room code on click
  container.querySelector('#room-code-display').addEventListener('click', () => {
    navigator.clipboard.writeText(roomCode);
    sounds.playTap();
    alert(`Room Code ${roomCode} copied to clipboard! 💖`);
  });

  // Share link via Web Share API or Clipboard
  container.querySelector('#btn-share-link').addEventListener('click', async () => {
    sounds.playTap();
    if (navigator.share) {
      try {
        await navigator.share({
          title: 'Play Pookie with me! 💖',
          text: `Join my Pookie room ${roomCode} for couple mini-games!`,
          url: joinUrl
        });
        return;
      } catch (e) {}
    }
    navigator.clipboard.writeText(joinUrl);
    alert('Invite link copied! Send it to your partner. 💌');
  });

  // Allow Eva AI Bot
  container.querySelector('#btn-allow-eva').addEventListener('click', () => {
    sounds.playTap();
    onAllowEva(roomCode);
  });

  // Cancel Room
  container.querySelector('#btn-cancel-room').addEventListener('click', () => {
    sounds.playTap();
    onLeaveRoom();
  });

  return container;
}
