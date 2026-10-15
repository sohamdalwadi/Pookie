---
name: pookie-game-dev
description: Comprehensive workflow guide for adding mini-games, extending backend contracts, and building features in the Pookie project using Antigravity without deep Android knowledge.
---

# Pookie Game Development Skill

This skill guides any developer or AI agent through adding new mini-games or extending the Pookie architecture.

## 🌟 Philosophy: Low Barrier to Contribution
Contributors don't need years of Android or GameDev experience. By adhering to the **Pookie Game Interface**, any turn-based game can be implemented through 4 deterministic components:
1. **State Definition (`GameState`)**: The data representation of the board / cards / quiz.
2. **Move Definition (`MovePayload`)**: The action sent by a player during their turn.
3. **Move Reducer (`reduceMove`)**: A pure function `(state, move, player) => newGameResult`.
4. **Win/Terminal Evaluator (`evaluateResult`)**: Calculates winner, draw, or next turn.

---

## 📋 Adding a New Mini-Game in 4 Steps

### Step 1: Document the Game Contract
Create a specification under `docs/games/<GAME_NAME>.md` detailing:
- The JSON serialization format.
- Player roles (e.g., Host = Player 1, Partner = Player 2).
- Valid move coordinate/input types.
- Victory conditions and draw rules.

### Step 2: Implement the Engine Interface
In the core game engine directory:
```typescript
// or Kotlin / pure logic equivalent
interface PookieGameEngine<TState, TMove> {
  getInitialState(): TState;
  validateMove(state: TState, move: TMove, player: 'HOST' | 'PARTNER'): boolean;
  applyMove(state: TState, move: TMove, player: 'HOST' | 'PARTNER'): {
    nextState: TState;
    nextTurn: 'HOST' | 'PARTNER';
    winner: 'HOST' | 'PARTNER' | 'DRAW' | null;
  };
}
```

### Step 3: Register in Apps Script Backend
Update the backend route handling to validate the `GameType` enum and invoke the server-side validator before committing the updated `GameState` string to Google Sheets.

### Step 4: Add Client Views & Visual Feedback
Implement the view components corresponding to the new game state machine. Ensure animations and haptic feedback provide delightful couples interactions.

---

## 🔍 Validation Checklist
- [ ] Board serializes to valid, compact JSON string (< 500 characters).
- [ ] Handles rapid double-clicks gracefully (optimistic concurrency version check).
- [ ] Disconnection or timeout is cleanly handled.
- [ ] Game over screen triggers rematch negotiation properly.
