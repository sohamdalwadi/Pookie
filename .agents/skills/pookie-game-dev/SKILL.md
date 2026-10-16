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

### Step 3: Register in Apps Script Backend & Permanent CLI Sync
1. Update backend route handling in `backend/src/Code.js` to validate the `GameType` enum and invoke the server-side validator.
2. **Permanent Single Deployment Rule**:
   - Always push through CLI: `npx -y @google/clasp push -f`
   - **Never create a new deployment ID.** Always update the canonical permanent deployment in-place reading the ID from `local.properties`:
     ```powershell
     # Read deployment ID from local.properties
     $deployId = (Get-Content local.properties | Select-String "POOKIE_DEMO_DEPLOYMENT_ID").ToString().Split("=")[1].Trim()
     npx -y @google/clasp deploy -i $deployId -d "<description>"
     ```
   - The deployment ID and Web App URL stay 100% constant forever, eliminating client configuration churn.
   - Always explicitly notify the user whenever backend code is pushed/updated.
3. **Build-Time Demo Key Encryption**:
   - The permanent deployment ID is stored exclusively in git-ignored `local.properties`: `POOKIE_DEMO_DEPLOYMENT_ID=<YOUR_DEPLOYMENT_ID>`.
   - `app/build.gradle` automatically encrypts this at compile time using XOR mask `0x5C` and injects `BuildConfig.ENCRYPTED_DEMO_KEY_HEX`.
   - Plaintext deployment keys must NEVER be hardcoded in `build.gradle` or any tracked documentation files.
   - Recompile (`cmd /c "set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr&& gradlew.bat assembleDebug"`) and reinstall via ADB.

### Step 4: Add Client Views & Visual Feedback
Implement the view components corresponding to the new game state machine. Ensure animations and haptic feedback provide delightful couples interactions.

---

## 🔍 Validation Checklist
- [ ] Board serializes to valid, compact JSON string (< 500 characters).
- [ ] Handles rapid double-clicks gracefully (optimistic concurrency version check).
- [ ] Disconnection or timeout is cleanly handled.
- [ ] Game over screen triggers rematch negotiation properly.
