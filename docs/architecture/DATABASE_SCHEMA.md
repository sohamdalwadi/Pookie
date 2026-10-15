# 🗄️ Database Schema & Google Sheets Architecture

**Author:** Soham Dalwadi  
**Role:** Principal Architect  
**Project:** Pookie  

---

## 📌 Architectural Rationale
In traditional systems, game sessions are maintained in Redis with a persistent PostgreSQL backing store. For Pookie, our design goal is zero operational cost, zero maintenance server overhead, and human inspectability. 

Using **Google Sheets** as a database backed by **Google Apps Script** requires strict architectural discipline to prevent performance bottlenecks.

### Core Constraints & Strategies
1. **Minimize Range Operations:** Read/write operations to spreadsheets have higher latency than native memory (~200ms–600ms). All reads for a room query must fetch the entire row in a single `getValues()` call.
2. **Column Consistency:** All columns are indexed with constant offsets.
3. **Atomic Single-Cell Serialization:** All dynamic game state matrices are serialized into single JSON string cells.

---

## 1. Sheet: `Rooms`

### Visual Structure
```
+----------+---------+-------------+----------------------+----------+----------------------+-------------+-------------+-----------------------------+-----------+--------------+-------------------+--------------------------+--------------------------+---------+
| RoomCode | Status  | GameType    | HostId               | HostName | PartnerId            | PartnerName | CurrentTurn | GameState                   | ScoreHost | ScorePartner | RematchState      | CreatedAt                | LastActivity             | Version |
+----------+---------+-------------+----------------------+----------+----------------------+-------------+-------------+-----------------------------+-----------+--------------+-------------------+--------------------------+--------------------------+---------+
| POOK99   | PLAYING | TIC_TAC_TOE | c3f912e6-...-32f281  | Soham    | 8e10d293-...-9c9f02  | Pookie      | HOST        | {"grid":[...],"moveCount":0}| 0         | 0            | {"hostReady":...} | 2026-10-15T13:30:00.000Z | 2026-10-15T13:35:12.450Z | 3       |
+----------+---------+-------------+----------------------+----------+----------------------+-------------+-------------+-----------------------------+-----------+--------------+-------------------+--------------------------+--------------------------+---------+
```

### Column Dictionary

| Col # | Name | Data Type | Nullable | Description |
|:---:|:---|:---|:---:|:---|
| `A` | `RoomCode` | String | NO | 6-character unique uppercase code (e.g. `POOK01`). |
| `B` | `Status` | Enum String | NO | `LOBBY`, `WAITING_FOR_PARTNER`, `PLAYING`, `FINISHED`, `ABANDONED`. |
| `C` | `GameType` | Enum String | NO | `NONE`, `TIC_TAC_TOE`, `DOTS_AND_BOXES`, `COUPLES_QUIZ`. |
| `D` | `HostId` | UUIDv4 String | NO | Secret identifier of the host player. |
| `E` | `HostName` | String | NO | Screen name chosen by host. |
| `F` | `PartnerId` | UUIDv4 String | YES | Secret identifier of partner; filled upon `JOIN_ROOM`. |
| `G` | `PartnerName` | String | YES | Screen name chosen by partner. |
| `H` | `CurrentTurn` | Enum String | NO | `HOST` or `PARTNER`. |
| `I` | `GameState` | JSON String | NO | Serialized JSON representing active board/round. |
| `J` | `ScoreHost` | Integer | NO | Match tally for Host. |
| `K` | `ScorePartner` | Integer | NO | Match tally for Partner. |
| `L` | `RematchState` | JSON String | NO | Rematch flags: `{"hostReady": false, "partnerReady": false}`. |
| `M` | `CreatedAt` | ISO-8601 | NO | Creation UTC timestamp. |
| `N` | `LastActivity` | ISO-8601 | NO | Timestamp of latest move or ping; drives cleanup cron. |
| `O` | `Version` | Integer | NO | Monotonically increasing revision counter (OCC lock). |

---

## 2. Sheet: `QuizQuestions`

Static bank of questions used for the Couples Icebreaker Quiz.

| Col # | Name | Data Type | Description |
|:---:|:---|:---|:---|
| `A` | `QuestionId` | String | Question identifier (e.g. `Q001`). |
| `B` | `Category` | Enum String | `ROMANTIC`, `FUNNY`, `DEEP`, `SPICY`. |
| `C` | `QuestionText` | String | Prompt displayed to players. |
| `D` | `OptionA` | String | Choice A text. |
| `E` | `OptionB` | String | Choice B text. |
| `F` | `OptionC` | String | Choice C text. |
| `G` | `OptionD` | String | Choice D text. |

---

## 3. Serialization Specifications

### 3.1 Tic-Tac-Toe
```json
{
  "grid": ["X", null, null, null, "O", null, null, null, null],
  "moveCount": 2,
  "winningLine": null,
  "winner": null
}
```

### 3.2 Dots and Boxes
```json
{
  "gridSize": 3,
  "hLines": [[0,0,0],[0,0,0],[0,0,0],[0,0,0]],
  "vLines": [[0,0,0,0],[0,0,0,0],[0,0,0,0]],
  "boxes": [[null,null,null],[null,null,null],[null,null,null]],
  "hostBoxesCount": 0,
  "partnerBoxesCount": 0,
  "lastMoveAwardedExtraTurn": false,
  "winner": null
}
```

### 3.3 Couples Icebreaker Quiz
```json
{
  "currentQuestionIndex": 0,
  "totalQuestions": 5,
  "questionId": "Q001",
  "category": "ROMANTIC",
  "questionText": "What was our best first impression moment?",
  "options": {
    "A": "The clumsy awkward laughter",
    "B": "The deep non-stop conversation",
    "C": "The stylish outfit",
    "D": "The immediate mutual chemistry"
  },
  "hostAnswer": null,
  "partnerAnswer": null,
  "answersRevealed": false,
  "roundMatches": 0,
  "completed": false
}
```
