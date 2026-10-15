# 🎨 Pookie UI/UX Design Specification (Telegram Mini-App / EdChess Aesthetic)

**Author:** Soham Dalwadi  
**Role:** Principal Architect & Product Designer  
**Inspiration:** [Telegram Mini App UI/UX Showcase (EdChess Style)](https://dribbble.com/shots/24700933-Telegram-Mini-App-UI-UX-New-Design-EdChess)  
**Design Philosophy:** Sleek Dark Mode • Tactile Cards • Vector Iconography • Zero-Emoji Policy (Tags Only)  

---

## 📌 Executive Design Principles

1. **Telegram Mini-App (TMA) Mobile-First Form Factor:**
   - Designed for seamless single-hand mobile interactions.
   - Glassmorphic headers and floating bottom navigation docks with soft boundary blur (`backdrop-filter: blur(16px)`).
   - Deep tactile cards with subtle hairline borders (`1px solid rgba(255, 255, 255, 0.08)`).

2. **Strict Vector Iconography Policy (Zero Cheesy Emojis):**
   - **NO EMOJIS anywhere in the UI** (no emoji buttons, no emoji titles, no emoji dialogs).
   - All navigation, actions, game markers, and indicators use clean, crisp **Vector SVGs** (Lucide/Heroicons standard: 24px/20px geometric stroke vectors).
   - **The Sole Exception:** Vibe Tags on the profile screen (e.g., `🍕 Foodies`, `🎮 Gamers`, `🐱 Pet Parents`), where emojis serve as visual tag glyphs.

3. **Color Palette (Sleek Dark Romance):**
   - **Canvas Background:** Deep Charcoal (`#0B0D13`)
   - **Card / Surface Layer 1:** Midnight Slate (`#131722`)
   - **Card / Surface Layer 2:** Elevated Obsidian (`#1B2130`)
   - **Accent / Primary Brand:** Vibrant Rose-Coral (`#FF4E78`) with electric violet gradient highlights (`#8A4FFF`)
   - **Success / Online Turn:** Mint Emerald (`#00E599`)
   - **Typography:** Inter / Outfit font family (crisp `#F4F6FB` primary text, `#8A96A8` secondary text).

---

## 1. First-Open Onboarding Flow: "Welcome to Pookie"

When a couple opens Pookie for the very first time, they are welcomed by a clean, gamified onboarding card to configure their local profile.

```
┌──────────────────────────────────────────────┐
│  POOKIE                      [Skip Vector]   │
├──────────────────────────────────────────────┤
│                                              │
│  ✨ Set Your Couple Identity                 │
│  Let's personalize your private mini-games.  │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │ Nickname                               │  │
│  │ [ Bubba                         ] [🎲] │  │ ◄── Animated Vector Dice Button
│  └────────────────────────────────────────┘  │     (Rolls random cute names!)
│                                              │
│  Pronoun                                     │
│  [ He/Him ✓ ]  [ She/Her ]  [ They/Them ]    │
│                                              │
│  Relationship Status                         │
│  [ Dating ]  [ Engaged ]  [ Married ✓ ]      │
│  [ Long-Distance ]  [ Best Friends ]         │
│                                              │
│  How long have you been together?            │
│  [ <6m ]  [ 6-12m ]  [ 1-3y ✓ ]  [ 3-5y ]    │
│                                              │
│  Select Your Vibe Tags (Max 4)               │
│  ┌────────────────────────────────────────┐  │
│  │ [🍕 Foodies ✓]   [🐱 Pet Parents ✓]    │  │ ◄── Sole permitted emoji area
│  │ [🎮 Gamers ✓]    [☕ Coffee Addicts]    │  │
│  │ [🛋️ Homebodies] [🔥 Chaotic Energy ✓]  │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │         ENTER LOBBY  [→ Vector]        │  │ ◄── High-contrast tactile CTA
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
```

### The Interactive Dice Vector Engine (`🎲`):
- Next to the Nickname text field sits an animated **SVG Dice Vector Button**.
- Tapping the dice triggers a subtle 360° spring-spin animation and randomly selects from a curated couple nickname bank:
  - *Pookie, Bubba, Buttercup, Mochi, Nugget, Honeybun, Dumpling, Snickerdoodle, Sweetpea, Marshmallow, Cinnamon, Sunshine, Cupcake, Bean*.

---

## 2. Main Lobby: EdChess Style Game Selection

The Lobby takes direct inspiration from the EdChess game hub: clean rounded cards displaying active game options with player stats, active room creation, and quick-join via code.

```
┌──────────────────────────────────────────────┐
│  [User Vector] Bubba (1-3y)       [Gear Vec] │
├──────────────────────────────────────────────┤
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │  QUICK PLAY                            │  │
│  │  Create Room or enter a 6-letter code  │  │
│  │                                        │  │
│  │  [ CREATE ROOM (Vector) ]              │  │
│  │  [ Enter Room Code...   ] [ JOIN (Vec) ]│  │
│  └────────────────────────────────────────┘  │
│                                              │
│  CHOOSE YOUR GAME                            │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │ [Grid Vector]                          │  │
│  │ Tic-Tac-Toe                            │  │
│  │ Classic 3x3 • Quick banters            │  │
│  │ ── Best of 3 Rounds ──       [Play →]  │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │ [Dots Matrix Vector]                   │  │
│  │ Dots & Boxes                           │  │
│  │ 4x4 Grid • Capture territory           │  │
│  │ ── Extra turn on box claim ── [Play →] │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │ [Sparkle Brain Vector]                 │  │
│  │ Couples Icebreaker Quiz                │  │
│  │ Gemini AI Powered • Blind Reveal       │  │
│  │ ── Based on your Vibe Tags ── [Play →] │  │
│  └────────────────────────────────────────┘  │
│                                              │
├──────────────────────────────────────────────┤
│  [Home Vec]     [Games Vec]     [Stats Vec]  │ ◄── Glassmorphic Bottom Dock
└──────────────────────────────────────────────┘
```

---

## 3. In-Game Match View: Dual Player PvP HUD

During active games (Tic-Tac-Toe, Dots & Boxes, or Quiz), the screen uses an EdChess-inspired split PvP card header:

```
┌──────────────────────────────────────────────┐
│  [← Back Vector]   Room: POOK99   [Sync Vec] │
├──────────────────────────────────────────────┤
│                                              │
│  ┌────────────────────┐┌───────────────────┐ │
│  │ Bubba (You)        ││ Pookie (Partner)  │ │
│  │ [Avatar Vector]    ││ [Avatar Vector]   │ │
│  │ Score: 2           ││ Score: 1          │ │
│  │ [● Active Turn]    ││ [○ Waiting]       │ │
│  └────────────────────┘└───────────────────┘ │
│                                              │
│  ┌─────────────────────────────────────────┐ │
│  │               GAME BOARD                │ │
│  │                                         │ │
│  │            [ X ] | [   ] | [ O ]        │ │
│  │           -------+-------+-------       │ │
│  │            [   ] | [ X ] | [   ]        │ │
│  │           -------+-------+-------       │ │
│  │            [ O ] | [   ] | [   ]        │ │
│  │                                         │ │
│  └─────────────────────────────────────────┘ │
│                                              │
│  [● Mint Glow] YOUR TURN - Tap a cell        │
│                                              │
├──────────────────────────────────────────────┤
│  [Reset Rematch Vector]      [Share Link Vec]│
└──────────────────────────────────────────────┘
```

---

## 4. UI Vector Iconography Manifest

To preserve design excellence and avoid visual clutter, all UI glyphs are defined in SVG vector formats:

| Action / Element | Vector Icon Description | Emojis Permitted? |
| :--- | :--- | :---: |
| **Dice Randomizer** | 6-face isometric die with rounded dot pips | **NO (Vector Only)** |
| **Create Room** | Plus sign inside a rounded polygon | **NO (Vector Only)** |
| **Join Room** | Keyhole or entrance door arrow vector | **NO (Vector Only)** |
| **Tic-Tac-Toe** | Clean geometric # grid lines | **NO (Vector Only)** |
| **Dots & Boxes** | 4-dot connected square line vector | **NO (Vector Only)** |
| **Quiz** | Radiant lightbulb / chat bubble vector | **NO (Vector Only)** |
| **Sync / Ping** | Dual revolving arc arrows | **NO (Vector Only)** |
| **Rematch** | Circular reload arrow | **NO (Vector Only)** |
| **Vibe Tags** | Tag chip with emoji prefix (`🍕 Foodies`) | **YES (Only in Tags)** |
