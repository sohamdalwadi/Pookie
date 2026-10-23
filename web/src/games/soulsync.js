/**
 * 💖 SoulSync Component (Mutual Bonding Quiz & Judging)
 */
import { sounds } from '../utils/sound.js';
import confetti from 'canvas-confetti';

export function renderSoulSync({ room, playerId, onMove, onSubmitVerdict }) {
  const container = document.createElement('div');
  container.className = 'anim-entrance soulsync-container';

  const isHost = playerId === room.host?.id;
  const playerRole = isHost ? 'HOST' : 'PARTNER';

  const gameState = room.gameState || {};
  const currentRound = gameState.currentRound || 1;
  const totalRounds = gameState.totalRounds || 10;
  const promptText = gameState.promptText || 'What is our favorite inside joke?';
  const coupleScore = gameState.coupleScore || 0;
  const phase = gameState.phase || 'ANSWERING';

  const myAnswer = isHost ? gameState.hostAnswer : gameState.partnerAnswer;
  const partnerAnswered = isHost ? gameState.hasPartnerAnswered : gameState.hasHostAnswered;
  const myVerdict = gameState.verdicts ? (isHost ? gameState.verdicts.host : gameState.verdicts.partner) : null;

  // Header card with prompt
  const promptCard = document.createElement('div');
  promptCard.className = 'soulsync-prompt-card';

  promptCard.innerHTML = `
    <div class="soulsync-round-tag">✨ ROUND ${currentRound} OF ${totalRounds} • SYNC SCORE: ${coupleScore} PTS</div>
    <h2 class="soulsync-prompt-title">"${promptText}"</h2>
  `;
  container.appendChild(promptCard);

  // PHASE 1: ANSWERING
  if (phase === 'ANSWERING') {
    if (!myAnswer) {
      const inputCard = document.createElement('div');
      inputCard.className = 'glass-panel soulsync-input-group';

      inputCard.innerHTML = `
        <label style="font-weight: 700; color: #fff;">Type your honest secret answer:</label>
        <input type="text" class="soulsync-input" placeholder="e.g. Eating noodles at 2am..." maxlength="120" />
        <button class="btn-primary" style="margin-top: 0.5rem;">Lock In Secret Answer 🔒</button>
      `;

      const input = inputCard.querySelector('input');
      const submitBtn = inputCard.querySelector('button');

      const submitAction = () => {
        const val = input.value.trim();
        if (!val) {
          input.focus();
          return;
        }
        sounds.playTap();
        submitBtn.disabled = true;
        input.disabled = true;
        onMove({ text: val });
      };

      submitBtn.addEventListener('click', submitAction);
      input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') submitAction();
      });

      container.appendChild(inputCard);
    } else {
      // Already answered, waiting for partner
      const waitingBox = document.createElement('div');
      waitingBox.className = 'glass-panel';
      waitingBox.style.textAlign = 'center';
      waitingBox.innerHTML = `
        <div style="font-size: 2.2rem; margin-bottom: 0.5rem;">🤫</div>
        <div style="font-weight: 700; font-size: 1.1rem; color: #fff; margin-bottom: 0.25rem;">Answer Locked!</div>
        <div style="color: var(--text-muted); font-size: 0.9rem;">
          ${partnerAnswered ? 'Unlocking partner answer...' : 'Waiting for your partner to type their secret answer...'}
        </div>
      `;
      container.appendChild(waitingBox);
    }
  }

  // PHASE 2: JUDGING & VERDICT
  if (phase === 'JUDGING') {
    const judgingSection = document.createElement('div');
    judgingSection.className = 'anim-pop';

    // Reveal side by side
    const revealGrid = document.createElement('div');
    revealGrid.className = 'soulsync-reveal-grid';

    revealGrid.innerHTML = `
      <div class="reveal-answer-card host-side">
        <div class="reveal-author">💖 ${room.host.name}</div>
        <div class="reveal-text">"${gameState.hostAnswer || '...'}"</div>
      </div>
      <div class="reveal-answer-card partner-side">
        <div class="reveal-author">💜 ${room.partner ? room.partner.name : 'Partner'}</div>
        <div class="reveal-text">"${gameState.partnerAnswer || '...'}"</div>
      </div>
    `;
    judgingSection.appendChild(revealGrid);

    // Verdict prompt
    const verdictBox = document.createElement('div');
    verdictBox.className = 'glass-panel';
    verdictBox.style.textAlign = 'center';

    if (!myVerdict) {
      verdictBox.innerHTML = `
        <div style="font-weight: 700; font-size: 1.1rem; margin-bottom: 0.5rem; color: #fff;">
          Did your answers match each other?
        </div>
        <div class="verdict-actions-row">
          <button class="btn-primary" id="btn-match" style="background: linear-gradient(135deg, #10b981, #059669);">
            💖 It's a Match! (+2)
          </button>
          <button class="btn-secondary" id="btn-miss" style="border-color: var(--primary);">
            💔 Miss / Different (-1)
          </button>
        </div>
      `;

      verdictBox.querySelector('#btn-match').addEventListener('click', () => {
        sounds.playTap();
        try {
          confetti({ particleCount: 35, spread: 50, origin: { y: 0.8 } });
        } catch (e) {}

        // ⚡ Optimistic UI Update (0ms instant verdict response)
        verdictBox.innerHTML = `
          <div style="font-size: 1.8rem; margin-bottom: 0.25rem;">✨</div>
          <div style="font-weight: 700; color: #fff;">You voted: <b>MATCH</b></div>
          <div style="color: var(--text-muted); font-size: 0.85rem; margin-top: 0.25rem;">
            Waiting for partner's mutual verdict...
          </div>
        `;

        onSubmitVerdict('MATCH');
      });

      verdictBox.querySelector('#btn-miss').addEventListener('click', () => {
        sounds.playTap();

        // ⚡ Optimistic UI Update (0ms instant verdict response)
        verdictBox.innerHTML = `
          <div style="font-size: 1.8rem; margin-bottom: 0.25rem;">✨</div>
          <div style="font-weight: 700; color: #fff;">You voted: <b>MISS</b></div>
          <div style="color: var(--text-muted); font-size: 0.85rem; margin-top: 0.25rem;">
            Waiting for partner's mutual verdict...
          </div>
        `;

        onSubmitVerdict('MISS');
      });

    } else {
      verdictBox.innerHTML = `
        <div style="font-size: 1.8rem; margin-bottom: 0.25rem;">✨</div>
        <div style="font-weight: 700; color: #fff;">You voted: <b>${myVerdict}</b></div>
        <div style="color: var(--text-muted); font-size: 0.85rem; margin-top: 0.25rem;">
          Waiting for partner's mutual verdict...
        </div>
      `;
    }

    judgingSection.appendChild(verdictBox);
    container.appendChild(judgingSection);
  }

  return container;
}
