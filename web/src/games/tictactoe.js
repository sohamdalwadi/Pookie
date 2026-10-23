/**
 * 💖 Tic-Tac-Toe Component
 */
import { sounds } from '../utils/sound.js';

export function renderTicTacToe({ room, playerId, onMove }) {
  const container = document.createElement('div');
  container.className = 'anim-entrance';

  const isHost = playerId === room.host?.id;
  const playerRole = isHost ? 'HOST' : 'PARTNER';
  const isMyTurn = room.status === 'PLAYING' && room.currentTurn === playerRole;
  const isFinished = room.status === 'FINISHED';

  const gameState = room.gameState || {};
  const grid = gameState.grid || Array(9).fill(null);
  const winningLine = gameState.winningLine || [];

  // Turn status banner
  const turnBanner = document.createElement('div');
  turnBanner.style.textAlign = 'center';
  if (isFinished) {
    let winMsg = 'Match Finished!';
    if (gameState.winner === 'DRAW') {
      winMsg = "It's a Cozy Tie! 🤝";
    } else if (gameState.winner === playerRole) {
      winMsg = '🎉 You Won! 💖';
    } else {
      winMsg = `${gameState.winner === 'HOST' ? room.host.name : (room.partner?.name || 'Partner')} Won! 🥰`;
    }
    turnBanner.innerHTML = `<div class="turn-pill my-turn">${winMsg}</div>`;
  } else {
    turnBanner.innerHTML = isMyTurn
      ? `<div class="turn-pill my-turn anim-turn">💖 Your Turn (${playerRole === 'HOST' ? 'X' : 'O'})</div>`
      : `<div class="turn-pill partner-turn">⏳ Waiting for partner... (${playerRole === 'HOST' ? 'O' : 'X'})</div>`;
  }
  container.appendChild(turnBanner);

  // 3x3 Grid
  const board = document.createElement('div');
  board.className = 'ttt-board';

  grid.forEach((val, idx) => {
    const cell = document.createElement('button');
    cell.className = 'ttt-cell';
    cell.dataset.index = idx;

    if (val === 'X') {
      cell.textContent = '✕';
      cell.classList.add('mark-x');
    } else if (val === 'O') {
      cell.textContent = '◯';
      cell.classList.add('mark-o');
    }

    if (winningLine.includes(idx)) {
      cell.classList.add('winning-cell');
    }

    // Disable if occupied, not my turn, or game finished
    if (val !== null || !isMyTurn || isFinished) {
      cell.disabled = true;
    } else {
      cell.addEventListener('click', () => {
        sounds.playMove();

        // ⚡ Optimistic UI Update (0ms instant visual response)
        const symbol = playerRole === 'HOST' ? 'X' : 'O';
        cell.textContent = symbol === 'X' ? '✕' : '◯';
        cell.classList.add(symbol === 'X' ? 'mark-x' : 'mark-o');
        cell.disabled = true;

        // Immediately disable remaining cells to prevent double clicks
        board.querySelectorAll('.ttt-cell').forEach((c) => {
          c.disabled = true;
        });

        // Immediately switch turn banner to waiting
        turnBanner.innerHTML = `<div class="turn-pill partner-turn">⏳ Waiting for partner... (${playerRole === 'HOST' ? 'O' : 'X'})</div>`;

        onMove({ cellIndex: idx });
      });
    }

    board.appendChild(cell);

  });

  container.appendChild(board);
  return container;
}
