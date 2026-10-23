/**
 * 💖 Rematch & Game Over Modal
 */
import { sounds } from '../utils/sound.js';

export function renderRematchModal({ room, playerId, onResetGame, onLeaveRoom }) {
  if (room.status !== 'FINISHED') return null;

  const isHost = playerId === room.host?.id;
  const isPartner = room.partner && playerId === room.partner.id;
  const rematchState = room.rematchState || {};

  const myReady = isHost ? rematchState.hostReady : rematchState.partnerReady;
  const partnerReady = isHost ? rematchState.partnerReady : rematchState.hostReady;

  const modal = document.createElement('div');
  modal.className = 'modal-overlay anim-entrance';

  const content = document.createElement('div');
  content.className = 'modal-content';

  const partnerName = room.partner ? room.partner.name : 'Partner';

  content.innerHTML = `
    <div style="font-size: 3rem; margin-bottom: 0.5rem;">🎉</div>
    <h2 style="color: #fff; font-size: 1.6rem; margin-bottom: 0.5rem;">Round Complete!</h2>
    <div style="font-size: 1.15rem; font-weight: 800; color: var(--accent-gold); margin-bottom: 1.5rem;">
      ${room.host.name}: ${room.host.score} • ${partnerName}: ${room.partner ? room.partner.score : 0}
    </div>

    <div style="margin-bottom: 1.5rem;">
      ${myReady ? `
        <div class="turn-pill partner-turn" style="width: 100%; justify-content: center;">
          ${partnerReady ? 'Restarting match...' : 'Rematch requested! Waiting for partner... 🥰'}
        </div>
      ` : `
        <button id="btn-request-rematch" class="btn-primary" style="width: 100%; padding: 1rem; font-size: 1.05rem;">
          Request Rematch 💖
        </button>
      `}
    </div>

    <button id="btn-leave-match" style="background: transparent; color: var(--text-muted); font-size: 0.9rem; text-decoration: underline;">
      Return to Lobby
    </button>
  `;

  const btnRematch = content.querySelector('#btn-request-rematch');
  if (btnRematch) {
    btnRematch.addEventListener('click', () => {
      sounds.playTap();
      btnRematch.disabled = true;
      btnRematch.textContent = 'Requesting...';
      onResetGame();
    });
  }

  content.querySelector('#btn-leave-match').addEventListener('click', () => {
    sounds.playTap();
    onLeaveRoom();
  });

  modal.appendChild(content);
  return modal;
}
