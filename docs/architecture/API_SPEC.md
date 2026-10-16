# 🌐 Pookie REST API Specification

**Author:** Soham Dalwadi  
**Role:** Principal Architect  
**Backend:** Google Apps Script (Web App Engine)  

---

## 🚀 Entry Points Overview
Google Apps Script exposes two standard HTTP entry points:
- `doGet(e)`: Query and read operations.
- `doPost(e)`: State transitions and data mutation operations.

---

## 1. Action: `CREATE_ROOM`
Creates a new room session, generates a unique 6-character room code, and registers the host.

- **Method:** `POST`
- **Request Body:**
```json
{
  "action": "CREATE_ROOM",
  "payload": {
    "hostName": "Soham",
    "hostId": "c3f912e6-8c43-4b61-9c12-32f281e0129a",
    "gameType": "DOTS_AND_BOXES",
    "gridSize": 3,
    "gender": "MALE"
  }
}
```
> Note: `gridSize` is optional and applies to `DOTS_AND_BOXES`: `3` (Small - 3x3), `4` (Medium - 4x4), or `5` (High - 5x5). Defaults to `3`. `gender` can be `MALE` or `FEMALE`.
- **Response Body:**
```json
{
  "status": "SUCCESS",
  "timestamp": "2026-10-15T13:30:00.000Z",
  "data": {
    "roomCode": "POOK99",
    "status": "WAITING_FOR_PARTNER",
    "gameType": "TIC_TAC_TOE",
    "role": "HOST",
    "host": {
      "id": "c3f912e6-8c43-4b61-9c12-32f281e0129a",
      "name": "Soham",
      "score": 0,
      "gender": "MALE"
    },
    "partner": null,
    "currentTurn": "HOST",
    "gameState": {
      "grid": [null, null, null, null, null, null, null, null, null],
      "moveCount": 0,
      "winningLine": null,
      "winner": null,
      "playerGenders": {
        "c3f912e6-8c43-4b61-9c12-32f281e0129a": "MALE"
      }
    },
    "version": 1
  }
}
```

---

## 2. Action: `JOIN_ROOM`
Enables the partner to join an existing game room using the 6-character room code.

- **Method:** `POST`
- **Request Body:**
```json
{
  "action": "JOIN_ROOM",
  "payload": {
    "roomCode": "POOK99",
    "partnerName": "Pookie",
    "partnerId": "8e10d293-1b91-45f8-bb32-9c9f023412ab",
    "gender": "FEMALE"
  }
}
```
- **Response Body:**
```json
{
  "status": "SUCCESS",
  "timestamp": "2026-10-15T13:31:05.120Z",
  "data": {
    "roomCode": "POOK99",
    "status": "PLAYING",
    "gameType": "TIC_TAC_TOE",
    "role": "PARTNER",
    "host": {
      "name": "Soham",
      "score": 0
    },
    "partner": {
      "id": "8e10d293-1b91-45f8-bb32-9c9f023412ab",
      "name": "Pookie",
      "score": 0
    },
    "currentTurn": "HOST",
    "gameState": {
      "grid": [null, null, null, null, null, null, null, null, null],
      "moveCount": 0,
      "winningLine": null,
      "winner": null
    },
    "version": 2
  }
}
```

---

## 3. Action: `GET_STATE`
Sync pulse and heartbeat. Enables client to check if the opponent has moved.

- **Method:** `GET`
- **URL Parameters:** `?action=GET_STATE&roomCode=POOK99&playerId=8e10d293-1b91-45f8-bb32-9c9f023412ab&v=2`

- **Response Body (304 Unchanged):**
```json
{
  "status": "SUCCESS",
  "timestamp": "2026-10-15T13:32:00.000Z",
  "data": {
    "roomCode": "POOK99",
    "isUpdated": false,
    "version": 2
  }
}
```

- **Response Body (Updated State):**
```json
{
  "status": "SUCCESS",
  "timestamp": "2026-10-15T13:32:04.200Z",
  "data": {
    "roomCode": "POOK99",
    "isUpdated": true,
    "status": "PLAYING",
    "gameType": "TIC_TAC_TOE",
    "currentTurn": "PARTNER",
    "scores": {
      "host": 0,
      "partner": 0
    },
    "gameState": {
      "grid": [null, null, null, null, "X", null, null, null, null],
      "moveCount": 1,
      "winningLine": null,
      "winner": null
    },
    "rematchState": {
      "hostReady": false,
      "partnerReady": false
    },
    "version": 3
  }
}
```

---

## 4. Action: `MAKE_MOVE`
Submits a turn from the active player.

- **Method:** `POST`
- **Request Body (Tic-Tac-Toe):**
```json
{
  "action": "MAKE_MOVE",
  "payload": {
    "roomCode": "POOK99",
    "playerId": "c3f912e6-8c43-4b61-9c12-32f281e0129a",
    "expectedVersion": 2,
    "move": {
      "cellIndex": 4
    }
  }
}
```
- **Response Body:**
```json
{
  "status": "SUCCESS",
  "timestamp": "2026-10-15T13:32:04.180Z",
  "data": {
    "roomCode": "POOK99",
    "status": "PLAYING",
    "currentTurn": "PARTNER",
    "gameState": {
      "grid": [null, null, null, null, "X", null, null, null, null],
      "moveCount": 1,
      "winningLine": null,
      "winner": null
    },
    "version": 3
  }
}
```

---

## 5. Action: `RESET_GAME`
Submits a rematch request or confirms an existing rematch request.

- **Method:** `POST`
- **Request Body:**
```json
{
  "action": "RESET_GAME",
  "payload": {
    "roomCode": "POOK99",
    "playerId": "c3f912e6-8c43-4b61-9c12-32f281e0129a",
    "gameType": "TIC_TAC_TOE"
  }
}
```

---

## 6. Action: `SEND_REACTION`
Sends a quick reaction or nudge buzzer to the partner during an active game session.

- **Method:** `POST`
- **Request Body:**
```json
{
  "action": "SEND_REACTION",
  "payload": {
    "roomCode": "POOK99",
    "playerId": "c3f912e6-8c43-4b61-9c12-32f281e0129a",
    "reaction": "NUDGE"
  }
}
```
> Supported `reaction` values: `HEART`, `THUMBS_UP`, `THUMBS_DOWN`, `LIGHTNING`, `NUDGE`.
> `NUDGE` triggers strong tactile haptic feedback on the opponent's device.

---

## 7. Error Codes Catalog

| Error Code | HTTP Status Equivalent | Description |
|:---|:---:|:---|
| `ROOM_NOT_FOUND` | 404 | Room code does not exist or has expired. |
| `ROOM_FULL` | 409 | Room already has two active players. |
| `NOT_YOUR_TURN` | 403 | Move submitted by the waiting player. |
| `INVALID_MOVE` | 400 | Coordinate/cell is already occupied or out of bounds. |
| `CONCURRENCY_CONFLICT` | 409 | `expectedVersion` does not match current server version. |
| `UNAUTHORIZED` | 401 | `playerId` does not belong to this room session. |
| `RATE_LIMITED` | 429 | Request frequency exceeds allowed safety thresholds. |
