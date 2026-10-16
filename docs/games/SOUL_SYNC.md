# 🔮 SoulSync - Blind-Matching Bonding Quiz Specification

**Game Type:** `SOUL_SYNC`  
**Max Players:** 2 (Host & Partner)  
**Format:** Simultaneous Blind Input → Side-by-Side Reveal → Couple Mutual Judging  
**Scoring:** Cooperative / Synchronized Couple Score  

---

## 1. Game Overview & Design Philosophy

**SoulSync** is an intimate, open-ended bonding quiz for couples. Unlike traditional multiple-choice quizzes, SoulSync prompts couples with freeform, open-ended prompts about their shared memories, quirks, and desires (e.g., *"What is my go-to comfort food when stressed?"*, *"Where was our most memorable evening together?"*).

### Why Manual Verdict Judging?
Automated string comparison algorithms (Levenshtein distance, regex, or exact match) notoriously fail in couples games due to:
- Nicknames and shorthand (*"mcd"* vs *"McDonald's"*)
- Typos and autocorrect artifacts (*"cheesecak"* vs *"Cheesecake"*)
- Language variations and synonyms (*"cinema"* vs *"movie theater"*)

To eliminate false negatives and spark playful debates, **SoulSync uses a Player-Judged Handshake**:
Both partners read each other's revealed freeform answers side-by-side and independently declare whether their answers align!

---

## 2. SoulSync Round Lifecycle & State Machine

```
   ┌─────────────────────────────────────────────────────────────┐
   │                       1. ANSWERING                          │
   │  Both partners type secret freeform text into hidden input. │
   │  Backend masks opponent's text until both submit.           │
   └──────────────────────────────┬──────────────────────────────┘
                                  │
                                  ▼
   ┌─────────────────────────────────────────────────────────────┐
   │                        2. JUDGING                           │
   │  Input fields disappear. Secret answers display side-by-side│
   │  Both see: "We Match (+2 Pts)" & "We Missed (-1 Pt)".       │
   └──────────────────────────────┬──────────────────────────────┘
                                  │
                                  ▼
   ┌─────────────────────────────────────────────────────────────┐
   │                   3. VERDICT RESOLUTION                     │
   │  Both submit verdict via SUBMIT_VERDICT.                    │
   │  - Both 'MATCH' ──► +2 Points                               │
   │  - Either 'MISS' ──► -1 Point                               │
   └──────────────────────────────┬──────────────────────────────┘
                                  │
                                  ▼
   ┌─────────────────────────────────────────────────────────────┐
   │                      4. NEXT ROUND                          │
   │  Temporary text answers purged. Board resets for next prompt│
   └─────────────────────────────────────────────────────────────┘
```

---

## 3. JSON State Representation

```json
{
  "currentRound": 1,
  "totalRounds": 5,
  "promptId": "SYNC_001",
  "promptText": "What was the exact moment you knew you had feelings for me?",
  "category": "INTIMACY",
  "phase": "JUDGING",
  "hostAnswer": "When we got soaked in the rain eating ice cream at 2 AM",
  "partnerAnswer": "The midnight walk in July when it poured rain",
  "answersRevealed": true,
  "verdicts": {
    "host": "MATCH",
    "partner": null
  },
  "playerReady": {
    "host": true,
    "partner": false
  },
  "coupleScore": 4,
  "roundHistory": [
    {
      "round": 1,
      "prompt": "Where was our first unofficial date?",
      "outcome": "MATCH",
      "pointsAwarded": 2
    }
  ],
  "completed": false
}
```

---

## 4. Privacy & Masking Handshake

### Answering Phase (`phase = "ANSWERING"`)
1. **Blind Input:**
   - Host types secret text: `"When we got soaked in the rain"`.
   - Partner types secret text: `"The midnight walk in July"`.
2. **Server Masking via `GET_STATE`:**
   - If Host requests state:
     - `hostAnswer`: `"When we got soaked in the rain"`
     - `partnerAnswer`: `null` (masked)
     - `hasPartnerAnswered`: `true`
   - If Partner requests state:
     - `partnerAnswer`: `"The midnight walk in July"`
     - `hostAnswer`: `null` (masked)
     - `hasHostAnswered`: `true`

### Reveal & Judging Transition
When the second answer is submitted via `MAKE_MOVE`:
- Server sets `answersRevealed = true` and `phase = "JUDGING"`.
- Subsequent `GET_STATE` calls deliver full text strings for both `hostAnswer` and `partnerAnswer`.
- Verdict map resets: `verdicts = { host: null, partner: null }`.
- Status map resets: `playerReady = { host: false, partner: false }`.

---

## 5. Judging UI & Verdict Scoring Rules

### UI Specifications
1. **Side-by-Side Comparison Container:**
   - Two balanced, glassmorphic cards display:
     - **Left Card:** Host's Answer (tinted primary coral/pink `#FF4E78`).
     - **Right Card:** Partner's Answer (tinted mint teal `#00D2A0`).
   - Text inputs and virtual keyboards are completely hidden.
2. **Action Buttons:**
   - **`We Match (+2 Pts)` Button:** Emerald gradient accent with celebratory icon.
   - **`We Missed (-1 Pt)` Button:** Subtle crimson outline button.
3. **Local Ready State:**
   - When a player taps either button:
     - Selected button stays highlighted; opposite button dims.
     - Player's status indicator switches to **"Ready (Waiting for Partner)"**.
     - Fires `SUBMIT_VERDICT` action to the backend.

### Scoring Truth Table

| Host Verdict | Partner Verdict | Outcome | Score Delta | System Action |
|:---:|:---:|:---:|:---:|:---|
| `MATCH` | `MATCH` | **Mutual Match** | **+2 Pts** | Confetti animation + harmony audio chime. |
| `MATCH` | `MISS` | **Mismatch** | **-1 Pt** | Playful tease toast + gentle buzz. |
| `MISS` | `MATCH` | **Mismatch** | **-1 Pt** | Playful tease toast + gentle buzz. |
| `MISS` | `MISS` | **Mutual Miss** | **-1 Pt** | Shared chuckle feedback + next round. |

> **Score Floor Rule:** Cumulative couple score never drops below `0`.

---

## 6. API Action: `SUBMIT_VERDICT`

### Request Payload
```json
{
  "action": "SUBMIT_VERDICT",
  "payload": {
    "roomCode": "POOK99",
    "playerId": "c3f912e6-8c43-4b61-9c12-32f281e0129a",
    "verdict": "MATCH"
  }
}
```

### Backend Resolution Algorithm
1. Verify `playerId` belongs to room and `room.status === "PLAYING"`.
2. Verify `gameState.phase === "JUDGING"`.
3. Store verdict under `gameState.verdicts[playerRole.toLowerCase()]`.
4. Set `gameState.playerReady[playerRole.toLowerCase()] = true`.
5. **Check Dual Completion:**
   - If both `verdicts.host` and `verdicts.partner` are non-null:
     - If `host === "MATCH"` and `partner === "MATCH"`:
       - `coupleScore += 2`
       - `roundOutcome = "MATCH"`
     - Else:
       - `coupleScore = Math.max(0, coupleScore - 1)`
       - `roundOutcome = "MISS"`
     - Append entry to `roundHistory`.
     - **Reset Round State:**
       - Purge `hostAnswer = null`, `partnerAnswer = null`.
       - Reset `verdicts = { host: null, partner: null }`.
       - Reset `playerReady = { host: false, partner: false }`.
       - If `currentRound >= totalRounds`:
         - Set `status = "FINISHED"`.
         - Set `gameState.completed = true`.
       - Else:
         - `currentRound += 1`.
         - Advance `promptText` to the next prompt.
         - Set `phase = "ANSWERING"`.
         - Set `answersRevealed = false`.
6. Increment room `version += 1`, update `lastActivity`, and write row to Google Sheets.
