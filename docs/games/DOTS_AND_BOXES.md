# 🟦 Dots and Boxes Game Specification

**Game Type:** `DOTS_AND_BOXES`  
**Max Players:** 2 (Host & Partner)  
**Board Type:** 3x3 Boxes (4x4 Dots Matrix)  

---

## 1. Grid & Line Geometry

```
(0,0) --- H[0][0] --- (0,1) --- H[0][1] --- (0,2) --- H[0][2] --- (0,3)
  |                     |                     |                     |
V[0][0]     B[0][0]   V[0][1]     B[0][1]   V[0][2]     B[0][2]   V[0][3]
  |                     |                     |                     |
(1,0) --- H[1][0] --- (1,1) --- H[1][1] --- (1,2) --- H[1][2] --- (1,3)
  |                     |                     |                     |
V[1][0]     B[1][0]   V[1][1]     B[1][1]   V[1][2]     B[1][2]   V[1][3]
  |                     |                     |                     |
(2,0) --- H[2][0] --- (2,1) --- H[2][1] --- (2,2) --- H[2][2] --- (2,3)
  |                     |                     |                     |
V[2][0]     B[2][0]   V[2][1]     B[2][1]   V[2][2]     B[2][2]   V[2][3]
  |                     |                     |                     |
(3,0) --- H[3][0] --- (3,1) --- H[3][1] --- (3,2) --- H[3][2] --- (3,3)
```

- **Horizontal Edges (`hLines`):** 4 rows × 3 columns (12 edges total).
- **Vertical Edges (`vLines`):** 3 rows × 4 columns (12 edges total).
- **Boxes (`boxes`):** 3 rows × 3 columns (9 boxes total).

---

## 2. JSON State Representation
```json
{
  "gridSize": 3,
  "hLines": [
    [0, 0, 0],
    [0, 0, 0],
    [0, 0, 0],
    [0, 0, 0]
  ],
  "vLines": [
    [0, 0, 0, 0],
    [0, 0, 0, 0],
    [0, 0, 0, 0]
  ],
  "boxes": [
    [null, null, null],
    [null, null, null],
    [null, null, null]
  ],
  "hostBoxesCount": 0,
  "partnerBoxesCount": 0,
  "lastMoveAwardedExtraTurn": false,
  "winner": null
}
```

---

## 3. Move Submission & Double Turn Mechanics
- **Move Payload:**
```json
{
  "lineType": "H",
  "row": 0,
  "col": 1
}
```
- **Rules:**
  1. A move claims an unselected edge (`0` -> `1`).
  2. The server verifies if claiming this edge completes any 1x1 box:
     - Box `(r, c)` is surrounded by:
       - Top: `hLines[r][c]`
       - Bottom: `hLines[r+1][c]`
       - Left: `vLines[r][c]`
       - Right: `vLines[r][c+1]`
  3. **Extra Turn Rule:** If 1 or 2 boxes are completed by this move:
     - Mark box ownership with `"HOST"` or `"PARTNER"`.
     - Increment the player's box count.
     - **The player retains the turn (`CurrentTurn` is NOT flipped).**
  4. If no box is completed, `CurrentTurn` switches to the opponent.
  5. When all 9 boxes are claimed, the player with the most boxes wins.
