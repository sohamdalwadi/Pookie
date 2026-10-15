# ❌⭕ Tic-Tac-Toe Game Specification

**Game Type:** `TIC_TAC_TOE`  
**Max Players:** 2 (Host = X, Partner = O)  
**Board Type:** 3x3 Grid (9 Cells)  

---

## 1. Board Layout & Indices

The 3x3 board is represented as a single 1D array of 9 elements:

```
 0 | 1 | 2 
---+---+---
 3 | 4 | 5 
---+---+---
 6 | 7 | 8 
```

---

## 2. JSON State Representation
```json
{
  "grid": ["X", null, null, null, "O", null, null, null, null],
  "moveCount": 2,
  "winningLine": null,
  "winner": null
}
```

- `grid`: Array of 9 items (`"X"`, `"O"`, or `null`).
- `winningLine`: Array of 3 indices (e.g., `[0, 1, 2]`) or `null` if no winner yet.
- `winner`: `"HOST"`, `"PARTNER"`, `"DRAW"`, or `null`.

---

## 3. Move Validation & Transition
- **Move Payload:**
```json
{ "cellIndex": 4 }
```
- **Validation Rules:**
  1. `cellIndex` must be an integer between 0 and 8.
  2. `grid[cellIndex]` must be strictly `null`.
  3. `winner` must be `null` (game not already terminated).
- **Win Condition Check:**
  Check the 8 possible winning combinations:
  - Rows: `[0,1,2]`, `[3,4,5]`, `[6,7,8]`
  - Columns: `[0,3,6]`, `[1,4,7]`, `[2,5,8]`
  - Diagonals: `[0,4,8]`, `[2,4,6]`
- **Draw Condition Check:**
  If `moveCount === 9` and no winning combo is matched, `winner = "DRAW"`.
