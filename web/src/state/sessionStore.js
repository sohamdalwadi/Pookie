/**
 * 💖 Pookie Local Session Store
 * Manages player identity, preferences, sound settings, and active room context.
 * Uses sessionStorage for player session isolation so two tabs can play against each other locally.
 */

function generateUUID() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return 'pk-' + Math.random().toString(36).substring(2, 11) + '-' + Date.now();
}

export const sessionStore = {
  getPlayerId() {
    let id = sessionStorage.getItem('pookie_player_id');
    if (!id) {
      id = generateUUID();
      sessionStorage.setItem('pookie_player_id', id);
    }
    return id;
  },

  getPlayerName() {
    return sessionStorage.getItem('pookie_player_name') 
      || localStorage.getItem('pookie_player_name') 
      || 'Pookie';
  },

  setPlayerName(name) {
    const clean = (name || 'Pookie').trim();
    sessionStorage.setItem('pookie_player_name', clean);
    localStorage.setItem('pookie_player_name', clean);
  },

  getGender() {
    return sessionStorage.getItem('pookie_gender') 
      || localStorage.getItem('pookie_gender') 
      || 'MALE';
  },

  setGender(gender) {
    const val = gender === 'FEMALE' ? 'FEMALE' : 'MALE';
    sessionStorage.setItem('pookie_gender', val);
    localStorage.setItem('pookie_gender', val);
  },

  isSoundEnabled() {
    const sound = localStorage.getItem('pookie_sound_enabled');
    return sound === null ? true : sound === 'true';
  },

  setSoundEnabled(enabled) {
    localStorage.setItem('pookie_sound_enabled', String(enabled));
  },

  getActiveRoomCode() {
    return sessionStorage.getItem('pookie_active_room') || '';
  },

  setActiveRoomCode(code) {
    if (code) {
      sessionStorage.setItem('pookie_active_room', code.toUpperCase().trim());
    } else {
      sessionStorage.removeItem('pookie_active_room');
    }
  }
};
