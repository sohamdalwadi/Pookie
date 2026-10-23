/**
 * 💖 Dots and Boxes Component (Dynamic 3x3, 4x4, 5x5)
 */
import { sounds } from '../utils/sound.js';

export function renderDotsAndBoxes({ room, playerId, onMove }) {
  const container = document.createElement('div');
  container.className = 'anim-entrance dnb-container';

  const isHost = playerId === room.host?.id;
  const playerRole = isHost ? 'HOST' : 'PARTNER';
  const isMyTurn = room.status === 'PLAYING' && room.currentTurn === playerRole;
  const isFinished = room.status === 'FINISHED';

  const gameState = room.gameState || {};
  const gridSize = gameState.gridSize || 3;
  const claimedLines = gameState.claimedLines || {};
  const claimedBoxes = gameState.claimedBoxes || {};
  const boxNumbers = gameState.boxNumbers || {};

  // Status banner
  const turnBanner = document.createElement('div');
  turnBanner.style.textAlign = 'center';
  if (isFinished) {
    let winMsg = 'Match Finished!';
    if (gameState.winner === 'DRAW') {
      winMsg = `Cozy Tie! (${gameState.hostBoxesCount} - ${gameState.partnerBoxesCount}) 🤝`;
    } else if (gameState.winner === playerRole) {
      winMsg = '🎉 You Won the Territory! 💖';
    } else {
      winMsg = `${gameState.winner === 'HOST' ? room.host.name : (room.partner?.name || 'Partner')} Won! 🥰`;
    }
    turnBanner.innerHTML = `<div class="turn-pill my-turn">${winMsg}</div>`;
  } else {
    turnBanner.innerHTML = isMyTurn
      ? `<div class="turn-pill my-turn anim-turn">💖 Your Turn (${playerRole === 'HOST' ? 'Pink' : 'Lavender'})</div>`
      : `<div class="turn-pill partner-turn">⏳ Partner's Turn...</div>`;
  }
  container.appendChild(turnBanner);

  // Boxes count indicator
  const boxesScoreBar = document.createElement('div');
  boxesScoreBar.style.display = 'flex';
  boxesScoreBar.style.gap = '1.5rem';
  boxesScoreBar.style.marginBottom = '1rem';
  boxesScoreBar.style.fontWeight = '700';
  boxesScoreBar.innerHTML = `
    <span style="color: var(--primary);">💖 ${room.host.name}: ${gameState.hostBoxesCount || 0}</span>
    <span style="color: var(--secondary);">💜 ${room.partner ? room.partner.name : 'Partner'}: ${gameState.partnerBoxesCount || 0}</span>
  `;
  container.appendChild(boxesScoreBar);

  // Board Builder
  const board = document.createElement('div');
  board.className = 'dnb-board';

  // For gridSize boxes, we have:
  // (gridSize + 1) rows of dots & horizontal lines
  // gridSize rows of vertical lines & boxes
  for (let r = 0; r <= gridSize; r++) {
    // 1. Horizontal row (Dot -> H-Line -> Dot -> H-Line -> Dot ...)
    const hRow = document.createElement('div');
    hRow.className = 'dnb-row';

    for (let c = 0; c <= gridSize; c++) {
      const dot = document.createElement('div');
      dot.className = 'dnb-dot';
      hRow.appendChild(dot);

      if (c < gridSize) {
        const lineKey = `H_${r}_${c}`;
        const hLine = document.createElement('div');
        hLine.className = 'dnb-line-h';

        if (claimedLines[lineKey]) {
          hLine.classList.add('claimed', claimedLines[lineKey].toLowerCase());
        } else if (isMyTurn && !isFinished) {
          hLine.addEventListener('click', () => {
            sounds.playMove();

            // ⚡ Optimistic UI Update (0ms instant line fill)
            hLine.classList.add('claimed', playerRole.toLowerCase());
            board.querySelectorAll('.dnb-line-h:not(.claimed), .dnb-line-v:not(.claimed)').forEach((l) => {
              l.style.pointerEvents = 'none';
            });
            turnBanner.innerHTML = `<div class="turn-pill partner-turn">⏳ Partner's Turn...</div>`;

            onMove({ lineKey });
          });
        }
        hRow.appendChild(hLine);
      }
    }
    board.appendChild(hRow);

    // 2. Vertical row & boxes (if not the last row)
    if (r < gridSize) {
      const vRow = document.createElement('div');
      vRow.className = 'dnb-row';

      for (let c = 0; c <= gridSize; c++) {
        const lineKey = `V_${r}_${c}`;
        const vLine = document.createElement('div');
        vLine.className = 'dnb-line-v';

        if (claimedLines[lineKey]) {
          vLine.classList.add('claimed', claimedLines[lineKey].toLowerCase());
        } else if (isMyTurn && !isFinished) {
          vLine.addEventListener('click', () => {
            sounds.playMove();

            // ⚡ Optimistic UI Update (0ms instant line fill)
            vLine.classList.add('claimed', playerRole.toLowerCase());
            board.querySelectorAll('.dnb-line-h:not(.claimed), .dnb-line-v:not(.claimed)').forEach((l) => {
              l.style.pointerEvents = 'none';
            });
            turnBanner.innerHTML = `<div class="turn-pill partner-turn">⏳ Partner's Turn...</div>`;

            onMove({ lineKey });
          });
        }
        vRow.appendChild(vLine);


        if (c < gridSize) {
          const boxKey = `box_${r}_${c}`;
          const box = document.createElement('div');
          box.className = 'dnb-box';

          if (claimedBoxes[boxKey]) {
            const owner = claimedBoxes[boxKey];
            box.classList.add('claimed', owner.toLowerCase());
            const num = boxNumbers[boxKey] || '';
            box.textContent = `${owner === 'HOST' ? 'H' : 'P'}${num}`;
          }
          vRow.appendChild(box);
        }
      }
      board.appendChild(vRow);
    }
  }

  container.appendChild(board);
  return container;
}
