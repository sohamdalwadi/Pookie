/**
 * 💖 Floating Reaction Visualizer & Sound Manager
 */
import { sounds } from '../utils/sound.js';

const EMOJI_MAP = {
  HEART: '💖',
  KISS: '💋',
  HUG: '🥺',
  FIRE: '🔥',
  LAUGH: '😂'
};

export class ReactionManager {
  constructor() {
    this.stage = document.createElement('div');
    this.stage.className = 'reaction-floating-stage';
    document.body.appendChild(this.stage);
    this.lastReactionTimestamp = null;
  }

  showFloatingReaction(reactionType, isLocal = false) {
    const emoji = EMOJI_MAP[reactionType] || reactionType || '💖';
    const item = document.createElement('div');
    item.className = 'floating-reaction-item';
    item.textContent = emoji;

    // Randomize horizontal spawn position (between 25% and 75% screen width)
    const randomLeft = Math.floor(25 + Math.random() * 50);
    item.style.left = `${randomLeft}%`;
    item.style.bottom = '80px';

    this.stage.appendChild(item);
    sounds.playReaction();

    setTimeout(() => {
      item.remove();
    }, 2200);
  }

  handleIncomingReaction(lastReaction) {
    if (!lastReaction || !lastReaction.timestamp) return;
    if (this.lastReactionTimestamp === lastReaction.timestamp) return;

    this.lastReactionTimestamp = lastReaction.timestamp;
    this.showFloatingReaction(lastReaction.type);
  }
}

export const reactions = new ReactionManager();
