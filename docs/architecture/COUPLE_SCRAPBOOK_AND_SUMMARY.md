# 📸 Couple Scrapbook & Post-Match Memory Card Engine

**Author:** Soham Dalwadi  
**Role:** Principal Architect & Product Designer  
**Project:** Pookie  

---

## 📌 Executive Summary
Standard multiplayer games end abruptly with a generic "Winner / Loser" dialog. For a couple's game, **the journey and shared banter are what matter most.**

Pookie introduces the **Couple Scrapbook & Memory Card Engine**. Whenever a couple finishes a gaming session (or taps "Leave Room"), the app generates a beautiful, shareable dark-mode **"Date Night Memory Card"** celebrating their dynamic.

---

## 1. Scrapbook Memory Card Layout (ASCII Wireframe)

```
┌──────────────────────────────────────────────┐
│  POOKIE DATE NIGHT RECAP       [Share Vector]│
├──────────────────────────────────────────────┤
│                                              │
│         💖 Bubba & Pookie 💖                 │
│         Thursday, Date Night Session         │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │  MATCH STATS                           │  │
│  │  • 4 Games Played                      │  │
│  │  • Tic-Tac-Toe: Bubba 2 — Pookie 1     │  │
│  │  • Dots & Boxes: Pookie took 6 boxes!  │  │
│  │  • Quiz Telepathy Score: 80% Soulmates │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  🏆 TONIGHT'S SUPERLATIVE                    │
│  ┌────────────────────────────────────────┐  │
│  │  "Most Likely to Raid the Fridge       │  │
│  │   at 1 AM Without Remorse"             │  │
│  │  (Agreed unanimously on Quiz Q2!)      │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  RELATIONSHIP VIBE EVOLUTION                 │
│  [🍕 Foodies] [⚔️ Playful Rivals] [🔥 Chaotic]│
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │      [Share Card to WhatsApp / IG]     │  │ ◄── Export as PNG
│  │      [Save to Local Love Scrapbook]    │  │ ◄── Persist in app
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
```

---

## 2. Dynamic Superlative Generator Logic

Based on the couple's answers in the Couples Quiz and game rivalries, the app selects a fun, custom superlative:

| Condition | Superlative Awarded |
| :--- | :--- |
| **Quiz answers matched on food question** | *"Midnight Snack Partners in Crime"* |
| **Player made a comeback in Dots & Boxes** | *"The Comeback Master (Never count them out!)"* |
| **Both chose "Destiny" on romantic prompt** | *"Officially Written in the Stars"* |
| **3 consecutive draws in Tic-Tac-Toe** | *"The Unbreakable Stalemate Duo"* |

---

## 3. Privacy & Local Storage
- **100% Client-Side:** Memory cards are generated and stored **locally** on the device using Android's native canvas rendering (`Bitmap.createBitmap` / `Canvas`).
- **No Cloud Image Storage:** The image is never uploaded to remote servers. If the couple chooses to share, it utilizes Android's native `Intent.ACTION_SEND` sheet.
- **Persistent Love Journal:** Couples can open the "Stats" tab in the bottom dock anytime to browse past date-night memory cards!
