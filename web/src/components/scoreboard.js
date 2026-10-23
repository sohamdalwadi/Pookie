/**
 * 💖 Scoreboard & Player Status Component
 */
import QRCode from 'qrcode';

export function renderScoreboard({ room, playerId }) {
  const isHost = playerId === room.host?.id;
  const currentTurn = room.currentTurn;

  const hostGender = (room.gameState?.playerGenders && room.gameState.playerGenders[room.host.id]) 
    || room.host.gender || 'MALE';
  const partnerGender = (room.partner && room.gameState?.playerGenders && room.gameState.playerGenders[room.partner.id])
    || (room.partner ? room.partner.gender : 'FEMALE');

  const hostAvatar = hostGender === 'FEMALE' ? '👧' : '👦';
  const partnerAvatar = room.partner?.id === 'EVA_BOT' ? '🤖' : (partnerGender === 'MALE' ? '👦' : '👧');

  // Mobile duel header
  const mobileBar = document.createElement('div');
  mobileBar.className = 'scoreboard-bar';

  mobileBar.innerHTML = `
    <div class="player-card ${currentTurn === 'HOST' ? 'active-turn' : ''}">
      <div class="player-avatar">${hostAvatar}</div>
      <div class="player-info">
        <span class="player-name">${room.host.name} ${isHost ? '(You)' : ''}</span>
        <span class="player-role-tag">HOST</span>
      </div>
      <div class="player-score">${room.host.score}</div>
    </div>

    <div class="vs-badge">VS</div>

    <div class="player-card partner ${currentTurn === 'PARTNER' ? 'active-turn' : ''}">
      <div class="player-score">${room.partner ? room.partner.score : 0}</div>
      <div class="player-info" style="text-align: right;">
        <span class="player-name">${room.partner ? room.partner.name : 'Waiting...'} ${!isHost && room.partner ? '(You)' : ''}</span>
        <span class="player-role-tag">${room.partner ? 'PARTNER' : 'OPEN'}</span>
      </div>
      <div class="player-avatar">${partnerAvatar}</div>
    </div>
  `;

  return mobileBar;
}

export function renderDesktopPanels({ room, playerId }) {
  const isHost = playerId === room.host?.id;
  const currentTurn = room.currentTurn;

  const hostGender = (room.gameState?.playerGenders && room.gameState.playerGenders[room.host.id]) 
    || room.host.gender || 'MALE';
  const partnerGender = (room.partner && room.gameState?.playerGenders && room.gameState.playerGenders[room.partner.id])
    || (room.partner ? room.partner.gender : 'FEMALE');

  const hostAvatar = hostGender === 'FEMALE' ? '👧' : '👦';
  const partnerAvatar = room.partner?.id === 'EVA_BOT' ? '🤖' : (partnerGender === 'MALE' ? '👦' : '👧');

  // Left Panel: Host
  const leftPanel = document.createElement('div');
  leftPanel.className = 'desktop-player-panel';
  leftPanel.innerHTML = `
    <div class="glass-panel" style="${currentTurn === 'HOST' ? 'border-color: var(--primary); box-shadow: 0 0 20px var(--primary-glow);' : ''}">
      <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 1rem;">
        <div class="player-avatar" style="width: 52px; height: 52px; font-size: 1.8rem;">${hostAvatar}</div>
        <div>
          <h3 style="color: #fff; font-size: 1.15rem; font-weight: 800;">${room.host.name}</h3>
          <span style="font-size: 0.75rem; color: var(--primary); font-weight: 700;">HOST ${isHost ? '• (YOU)' : ''}</span>
        </div>
      </div>
      <div style="background: rgba(13, 11, 24, 0.6); padding: 0.85rem; border-radius: var(--radius-md); text-align: center;">
        <div style="font-size: 0.75rem; color: var(--text-muted); font-weight: 700;">TOTAL WINS</div>
        <div style="font-size: 2rem; font-weight: 900; color: var(--accent-gold);">${room.host.score}</div>
      </div>
      ${currentTurn === 'HOST' ? '<div class="turn-pill my-turn" style="width: 100%; justify-content: center; margin-top: 1rem;">Thinking... 💭</div>' : ''}
    </div>
  `;

  // Right Panel: Partner + QR
  const rightPanel = document.createElement('div');
  rightPanel.className = 'desktop-player-panel';

  const partnerName = room.partner ? room.partner.name : 'Waiting...';
  const partnerScore = room.partner ? room.partner.score : 0;

  rightPanel.innerHTML = `
    <div class="glass-panel" style="${currentTurn === 'PARTNER' ? 'border-color: var(--secondary); box-shadow: 0 0 20px var(--secondary-glow);' : ''}">
      <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 1rem;">
        <div class="player-avatar" style="width: 52px; height: 52px; font-size: 1.8rem; border-color: var(--secondary);">${partnerAvatar}</div>
        <div>
          <h3 style="color: #fff; font-size: 1.15rem; font-weight: 800;">${partnerName}</h3>
          <span style="font-size: 0.75rem; color: var(--secondary); font-weight: 700;">PARTNER ${!isHost && room.partner ? '• (YOU)' : ''}</span>
        </div>
      </div>
      <div style="background: rgba(13, 11, 24, 0.6); padding: 0.85rem; border-radius: var(--radius-md); text-align: center;">
        <div style="font-size: 0.75rem; color: var(--text-muted); font-weight: 700;">TOTAL WINS</div>
        <div style="font-size: 2rem; font-weight: 900; color: var(--accent-gold);">${partnerScore}</div>
      </div>
      ${currentTurn === 'PARTNER' ? '<div class="turn-pill partner-turn" style="width: 100%; justify-content: center; margin-top: 1rem;">Thinking... 💭</div>' : ''}
    </div>
  `;

  return { leftPanel, rightPanel };
}
