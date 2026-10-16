/**
 * 💖 POOKIE SERVERLESS BACKEND
 * System: Pookie (Turn-Based Couple's Mini-Game Platform)
 * Lead Architect & Developer: Soham Dalwadi
 * 
 * Built with Google Apps Script + Google Sheets (Zero-Cost Serverless REST API).
 * Features: Dynamic Sheet Binding, Self-Bootstrapping Schema, Optimistic Concurrency Control,
 * Pure Game Reducers, and Automatic Inactivity Cleanup Cron.
 */

// ============================================================================
// 1. DYNAMIC SHEET BINDING & DATABASE BOOTSTRAPPING
// ============================================================================

/**
 * Resolves the Google Spreadsheet dynamically without hardcoded IDs.
 * Priority 1: Container-bound spreadsheet (Zero config).
 * Priority 2: Script Property 'SPREADSHEET_ID' (For standalone clasp projects).
 */
function getDatabaseSpreadsheet() {
  let ss = null;
  try {
    ss = SpreadsheetApp.getActiveSpreadsheet();
  } catch (e) {
    // In standalone or headless execution, getActiveSpreadsheet can throw NOT_FOUND
  }
  if (ss) return ss;

  const scriptProperties = PropertiesService.getScriptProperties();
  const configuredSheetId = scriptProperties.getProperty("SPREADSHEET_ID");
  if (configuredSheetId && configuredSheetId.trim() !== "") {
    return SpreadsheetApp.openById(configuredSheetId.trim());
  }

  // Fallback to parent container spreadsheet
  const defaultSheetId = "1WEDHaxXss-ehwbq6XBGbMp0FQPWUa3J7eZOcxqK1Igk";
  if (defaultSheetId) {
    try {
      return SpreadsheetApp.openById(defaultSheetId);
    } catch (err) {
      // Not yet authorized or invalid
    }
  }

  throw new Error(
    "Pookie Initialization Error: No Google Sheet bound to this script. " +
    "Attach this script directly to a Google Sheet (Extensions > Apps Script) " +
    "or set 'SPREADSHEET_ID' in Project Settings > Script Properties."
  );
}

/**
 * Custom UI Menu added to Google Sheets for easy manual administration.
 */
function onOpen() {
  SpreadsheetApp.getUi()
    .createMenu("💖 Pookie Admin")
    .addItem("Initialize / Reset Schema", "bootstrapDatabase")
    .addSeparator()
    .addItem("Run Cleanup (Purge Inactive Rooms)", "cleanupInactiveRooms")
    .addToUi();
}

/**
 * Self-bootstraps the database tabs ('Rooms' and 'QuizQuestions') with exact
 * columns, headers, font formatting, and default couples quiz questions.
 */
function bootstrapDatabase() {
  const ss = getDatabaseSpreadsheet();

  // 1. Bootstrap Rooms Sheet
  let roomsSheet = ss.getSheetByName("Rooms");
  if (!roomsSheet) {
    roomsSheet = ss.insertSheet("Rooms");
    const headers = [
      "RoomCode", "Status", "GameType", "HostId", "HostName",
      "PartnerId", "PartnerName", "CurrentTurn", "GameState",
      "ScoreHost", "ScorePartner", "RematchState", "CreatedAt",
      "LastActivity", "Version"
    ];
    roomsSheet.getRange(1, 1, 1, headers.length).setValues([headers]);
    roomsSheet.getRange(1, 1, 1, headers.length).setFontWeight("bold").setBackground("#131722").setFontColor("#FFFFFF");
    roomsSheet.setFrozenRows(1);
  }

  // 2. Bootstrap QuizQuestions Sheet
  let quizSheet = ss.getSheetByName("QuizQuestions");
  if (!quizSheet) {
    quizSheet = ss.insertSheet("QuizQuestions");
    const headers = [
      "QuestionId", "Category", "QuestionText", "OptionA", "OptionB", "OptionC", "OptionD"
    ];
    quizSheet.getRange(1, 1, 1, headers.length).setValues([headers]);
    quizSheet.getRange(1, 1, 1, headers.length).setFontWeight("bold").setBackground("#FF4E78").setFontColor("#FFFFFF");
    quizSheet.setFrozenRows(1);

    const defaultQuestions = [
      ["Q001", "ROMANTIC", "What is our ideal weekend getaway?", "Cozy mountain cabin", "Beach resort & sunsets", "Spontaneous road trip", "Staying in bed binging shows"],
      ["Q002", "FOODIES", "It's 11 PM on Sunday. Who convinces the other to order late-night food?", "Definitely me", "100% them", "We both suggested it simultaneously", "Neither, we raid the kitchen"],
      ["Q003", "CHAOTIC", "Who is more likely to lose their keys or sunglasses while wearing them?", "Me, absolutely", "Without doubt them", "Both of us daily", "Neither, we are super organized"],
      ["Q004", "ROMANTIC", "Who made the very first romantic move?", "Me, boldly", "Them, smoothly", "It was completely mutual", "Destiny took over"],
      ["Q005", "PETS", "Who uses the silliest baby voice when talking to pets or cute animals?", "Me without shame", "Them completely", "Both of us in harmony", "We pretend we don't, but we do"]
    ];
    quizSheet.getRange(2, 1, defaultQuestions.length, 7).setValues(defaultQuestions);
  }

  return { success: true, message: "Database schema successfully bootstrapped." };
}

// ============================================================================
// 2. HTTP ROUTING & ENVELOPES
// ============================================================================

function jsonSuccess(data) {
  return ContentService.createTextOutput(JSON.stringify({
    status: "SUCCESS",
    timestamp: new Date().toISOString(),
    data: data
  })).setMimeType(ContentService.MimeType.JSON);
}

function jsonError(code, message, httpStatus) {
  return ContentService.createTextOutput(JSON.stringify({
    status: "ERROR",
    timestamp: new Date().toISOString(),
    error: {
      code: code || "INTERNAL_ERROR",
      message: message || "An unexpected error occurred."
    }
  })).setMimeType(ContentService.MimeType.JSON);
}

/**
 * HTTP GET Entry Point (Sync & Read Operations)
 */
function doGet(e) {
  try {
    const params = e.parameter || {};
    const action = params.action;

    if (!action || action === "HEALTH_CHECK") {
      return jsonSuccess({ message: "Pookie API is online and healthy.", version: "1.0.0" });
    }

    if (action === "SETUP") {
      const result = bootstrapDatabase();
      return jsonSuccess(result);
    }

    if (action === "GET_STATE") {
      return handleGetState(params);
    }

    return jsonError("INVALID_ACTION", "Unrecognized GET action: " + action);
  } catch (err) {
    return jsonError("SERVER_EXCEPTION", err.toString());
  }
}

/**
 * HTTP POST Entry Point (State Mutation Operations)
 */
function doPost(e) {
  try {
    if (!e.postData || !e.postData.contents) {
      return jsonError("EMPTY_PAYLOAD", "Missing POST request payload.");
    }

    const request = JSON.parse(e.postData.contents);
    const action = request.action;
    const payload = request.payload || {};

    switch (action) {
      case "CREATE_ROOM":
        return handleCreateRoom(payload);
      case "JOIN_ROOM":
        return handleJoinRoom(payload);
      case "MAKE_MOVE":
        return handleMakeMove(payload);
      case "RESET_GAME":
        return handleResetGame(payload);
      case "START_QUIZ":
        return handleStartQuiz(payload);
      case "SEND_REACTION":
        return handleSendReaction(payload);
      default:
        return jsonError("INVALID_ACTION", "Unrecognized POST action: " + action);
    }
  } catch (err) {
    return jsonError("SERVER_EXCEPTION", err.toString());
  }
}

// ============================================================================
// 3. ROOM SESSION REPOSITORY HELPERS
// ============================================================================

function generateUniqueRoomCode(sheet) {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Removed ambiguous chars like O, 0, 1, I
  for (let attempt = 0; attempt < 10; attempt++) {
    let code = "PK";
    for (let i = 0; i < 4; i++) {
      code += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    if (findRoomRowIndex(sheet, code) === -1) {
      return code;
    }
  }
  return "PK" + Math.floor(1000 + Math.random() * 9000);
}

function findRoomRowIndex(sheet, roomCode) {
  const cleanCode = (roomCode || "").toString().replace(/[\s-]/g, '').toUpperCase();
  const lastRow = sheet.getLastRow();
  if (lastRow <= 1) return -1;
  const codes = sheet.getRange(2, 1, lastRow - 1, 1).getValues();
  for (let i = 0; i < codes.length; i++) {
    if (codes[i][0] && codes[i][0].toString().replace(/[\s-]/g, '').toUpperCase() === cleanCode) {
      return i + 2; // 1-indexed row number in Google Sheet
    }
  }
  return -1;
}

function rowToRoomObject(row) {
  const gameState = row[8] ? JSON.parse(row[8]) : null;
  const playerGenders = (gameState && gameState.playerGenders) ? gameState.playerGenders : {};
  return {
    roomCode: row[0],
    status: row[1],
    gameType: row[2],
    host: {
      id: row[3],
      name: row[4],
      score: parseInt(row[9], 10) || 0,
      gender: playerGenders[row[3]] || "MALE"
    },
    partner: row[5] ? {
      id: row[5],
      name: row[6],
      score: parseInt(row[10], 10) || 0,
      gender: playerGenders[row[5]] || "FEMALE"
    } : null,
    currentTurn: row[7],
    gameState: gameState,
    rematchState: row[11] ? JSON.parse(row[11]) : { hostReady: false, partnerReady: false },
    createdAt: row[12],
    lastActivity: row[13],
    version: parseInt(row[14], 10) || 1
  };
}

function roomObjectToRow(room) {
  return [
    room.roomCode,
    room.status,
    room.gameType,
    room.host.id,
    room.host.name,
    room.partner ? room.partner.id : "",
    room.partner ? room.partner.name : "",
    room.currentTurn,
    JSON.stringify(room.gameState),
    room.host.score,
    room.partner ? room.partner.score : 0,
    JSON.stringify(room.rematchState),
    room.createdAt,
    room.lastActivity,
    room.version
  ];
}

// ============================================================================
// 4. ACTION HANDLERS
// ============================================================================

function handleGetState(params) {
  const roomCode = params.roomCode;
  const playerId = params.playerId;
  const clientVersion = parseInt(params.v, 10);

  if (!roomCode) return jsonError("MISSING_PARAM", "Parameter 'roomCode' is required.");

  const ss = getDatabaseSpreadsheet();
  const sheet = ss.getSheetByName("Rooms");
  const rowIndex = findRoomRowIndex(sheet, roomCode);

  if (rowIndex === -1) {
    return jsonError("ROOM_NOT_FOUND", "Room '" + roomCode + "' does not exist or has expired.");
  }

  const row = sheet.getRange(rowIndex, 1, 1, 15).getValues()[0];
  const room = rowToRoomObject(row);

  // ETag-style Version Check (Returns lightweight 58-byte response if unchanged)
  if (!isNaN(clientVersion) && clientVersion === room.version) {
    return jsonSuccess({
      roomCode: room.roomCode,
      isUpdated: false,
      version: room.version
    });
  }

  // Couples Quiz Blind Answer Masking
  if (room.gameType === "COUPLES_QUIZ" && room.gameState && !room.gameState.answersRevealed) {
    const isHost = playerId === room.host.id;
    if (isHost) {
      room.gameState.hasPartnerAnswered = (room.gameState.partnerAnswer !== null && room.gameState.partnerAnswer !== undefined);
      delete room.gameState.partnerAnswer; // Mask partner's answer from Host
    } else {
      room.gameState.hasHostAnswered = (room.gameState.hostAnswer !== null && room.gameState.hostAnswer !== undefined);
      delete room.gameState.hostAnswer; // Mask host's answer from Partner
    }
  }

  return jsonSuccess({
    isUpdated: true,
    ...room
  });
}

function handleCreateRoom(payload) {
  const hostName = (payload.hostName || "Host").trim();
  const hostId = payload.hostId || Utilities.getUuid();
  const gameType = payload.gameType || "TIC_TAC_TOE";

  const ss = getDatabaseSpreadsheet();
  const sheet = ss.getSheetByName("Rooms");
  const roomCode = generateUniqueRoomCode(sheet);
  const now = new Date().toISOString();

  let initialGameState = {};
  if (gameType === "TIC_TAC_TOE") {
    initialGameState = { grid: [null, null, null, null, null, null, null, null, null], moveCount: 0, winningLine: null, winner: null };
  } else if (gameType === "DOTS_AND_BOXES") {
    const gridSize = parseInt(payload.gridSize, 10) || 3;
    initialGameState = {
      gridSize: gridSize,
      claimedLines: {},
      claimedBoxes: {},
      hostBoxesCount: 0,
      partnerBoxesCount: 0,
      lastMoveAwardedExtraTurn: false,
      winner: null
    };
  } else if (gameType === "COUPLES_QUIZ") {
    initialGameState = { currentQuestionIndex: 0, totalQuestions: 5, questions: [], hostAnswer: null, partnerAnswer: null, answersRevealed: false, roundMatches: 0, completed: false };
  }

  const hostGender = payload.gender || "MALE";
  if (!initialGameState.playerGenders) {
    initialGameState.playerGenders = {};
  }
  initialGameState.playerGenders[hostId] = hostGender;

  const room = {
    roomCode: roomCode,
    status: "WAITING_FOR_PARTNER",
    gameType: gameType,
    host: { id: hostId, name: hostName, score: 0, gender: hostGender },
    partner: null,
    currentTurn: "HOST",
    gameState: initialGameState,
    rematchState: { hostReady: false, partnerReady: false },
    createdAt: now,
    lastActivity: now,
    version: 1
  };

  const rowValues = roomObjectToRow(room);
  sheet.appendRow(rowValues);

  return jsonSuccess(room);
}

function handleJoinRoom(payload) {
  const roomCode = (payload.roomCode || "").toUpperCase().trim();
  const partnerName = (payload.partnerName || "Partner").trim();
  const partnerId = payload.partnerId || Utilities.getUuid();

  const lock = LockService.getScriptLock();
  if (!lock.tryLock(5000)) {
    return jsonError("CONCURRENCY_TIMEOUT", "Server busy, please retry in a moment.");
  }

  try {
    const ss = getDatabaseSpreadsheet();
    const sheet = ss.getSheetByName("Rooms");
    const rowIndex = findRoomRowIndex(sheet, roomCode);

    if (rowIndex === -1) {
      return jsonError("ROOM_NOT_FOUND", "Room '" + roomCode + "' does not exist.");
    }

    const row = sheet.getRange(rowIndex, 1, 1, 15).getValues()[0];
    const room = rowToRoomObject(row);

    if (room.partner && room.partner.id !== partnerId) {
      return jsonError("ROOM_FULL", "This room already has two active players.");
    }

    const partnerGender = payload.gender || "FEMALE";
    if (!room.gameState) room.gameState = {};
    if (!room.gameState.playerGenders) room.gameState.playerGenders = {};
    room.gameState.playerGenders[partnerId] = partnerGender;

    room.partner = { id: partnerId, name: partnerName, score: 0, gender: partnerGender };
    room.status = "PLAYING";
    room.lastActivity = new Date().toISOString();
    room.version += 1;

    sheet.getRange(rowIndex, 1, 1, 15).setValues([roomObjectToRow(room)]);

    return jsonSuccess(room);
  } finally {
    lock.releaseLock();
  }
}

function handleMakeMove(payload) {
  const roomCode = (payload.roomCode || "").toUpperCase().trim();
  const playerId = payload.playerId;
  const expectedVersion = parseInt(payload.expectedVersion, 10);
  const move = payload.move || {};

  const lock = LockService.getScriptLock();
  if (!lock.tryLock(5000)) {
    return jsonError("CONCURRENCY_TIMEOUT", "Server busy processing a move.");
  }

  try {
    const ss = getDatabaseSpreadsheet();
    const sheet = ss.getSheetByName("Rooms");
    const rowIndex = findRoomRowIndex(sheet, roomCode);

    if (rowIndex === -1) {
      return jsonError("ROOM_NOT_FOUND", "Room '" + roomCode + "' does not exist.");
    }

    const row = sheet.getRange(rowIndex, 1, 1, 15).getValues()[0];
    const room = rowToRoomObject(row);

    // Optimistic Concurrency Control Check
    if (!isNaN(expectedVersion) && room.version !== expectedVersion) {
      return jsonError("CONCURRENCY_CONFLICT", "Local game version out of sync. Please pull latest state.");
    }

    // Role verification
    const isHost = (playerId === room.host.id);
    const isPartner = (room.partner && playerId === room.partner.id);
    if (!isHost && !isPartner) {
      return jsonError("UNAUTHORIZED", "Player not recognized in this room session.");
    }

    const playerRole = isHost ? "HOST" : "PARTNER";

    // Turn verification (except simultaneous Quiz moves)
    if (room.gameType !== "COUPLES_QUIZ" && room.currentTurn !== playerRole) {
      return jsonError("NOT_YOUR_TURN", "It is not your turn to move.");
    }

    // Pure Game Reducer Execution
    if (room.gameType === "TIC_TAC_TOE") {
      const result = reduceTicTacToe(room.gameState, move, playerRole);
      if (result.error) return jsonError("INVALID_MOVE", result.error);
      room.gameState = result.nextState;
      room.currentTurn = result.nextTurn;
      if (result.winner === "HOST") {
        room.host.score += 1;
        room.status = "FINISHED";
      } else if (result.winner === "PARTNER") {
        room.partner.score += 1;
        room.status = "FINISHED";
      } else if (result.winner === "DRAW") {
        room.status = "FINISHED";
      }
    } else if (room.gameType === "DOTS_AND_BOXES") {
      const result = reduceDotsAndBoxes(room.gameState, move, playerRole);
      if (result.error) return jsonError("INVALID_MOVE", result.error);
      room.gameState = result.nextState;
      room.currentTurn = result.nextTurn;
      if (result.winner === "HOST") {
        room.host.score += 1;
        room.status = "FINISHED";
      } else if (result.winner === "PARTNER") {
        room.partner.score += 1;
        room.status = "FINISHED";
      } else if (result.winner === "DRAW") {
        room.status = "FINISHED";
      }
    } else if (room.gameType === "COUPLES_QUIZ") {
      const result = reduceCouplesQuiz(room.gameState, move, playerRole);
      if (result.error) return jsonError("INVALID_MOVE", result.error);
      room.gameState = result.nextState;
    }

    if (payload.reaction) {
      if (!room.gameState) room.gameState = {};
      room.gameState.lastReaction = {
        type: payload.reaction,
        sender: playerRole,
        timestamp: new Date().toISOString()
      };
    }

    room.lastActivity = new Date().toISOString();
    room.version += 1;

    sheet.getRange(rowIndex, 1, 1, 15).setValues([roomObjectToRow(room)]);

    return jsonSuccess(room);
  } finally {
    lock.releaseLock();
  }
}

function handleResetGame(payload) {
  const roomCode = (payload.roomCode || "").toUpperCase().trim();
  const playerId = payload.playerId;

  const lock = LockService.getScriptLock();
  if (!lock.tryLock(5000)) return jsonError("CONCURRENCY_TIMEOUT", "Server busy.");

  try {
    const ss = getDatabaseSpreadsheet();
    const sheet = ss.getSheetByName("Rooms");
    const rowIndex = findRoomRowIndex(sheet, roomCode);
    if (rowIndex === -1) return jsonError("ROOM_NOT_FOUND", "Room not found.");

    const row = sheet.getRange(rowIndex, 1, 1, 15).getValues()[0];
    const room = rowToRoomObject(row);

    const isHost = (playerId === room.host.id);
    if (isHost) {
      room.rematchState.hostReady = true;
    } else {
      room.rematchState.partnerReady = true;
    }

    // Both accepted rematch: Reset board!
    if (room.rematchState.hostReady && room.rematchState.partnerReady) {
      room.status = "PLAYING";
      room.rematchState = { hostReady: false, partnerReady: false };
      room.currentTurn = (room.currentTurn === "HOST") ? "PARTNER" : "HOST"; // Alternate starting player

      if (room.gameType === "TIC_TAC_TOE") {
        room.gameState = { grid: [null, null, null, null, null, null, null, null, null], moveCount: 0, winningLine: null, winner: null };
      } else if (room.gameType === "DOTS_AND_BOXES") {
        const currentGridSize = (room.gameState && room.gameState.gridSize) ? room.gameState.gridSize : 3;
        room.gameState = {
          gridSize: currentGridSize,
          claimedLines: {},
          claimedBoxes: {},
          hostBoxesCount: 0,
          partnerBoxesCount: 0,
          lastMoveAwardedExtraTurn: false,
          winner: null
        };
      }
    }

    room.lastActivity = new Date().toISOString();
    room.version += 1;

    sheet.getRange(rowIndex, 1, 1, 15).setValues([roomObjectToRow(room)]);
    return jsonSuccess(room);
  } finally {
    lock.releaseLock();
  }
}

function handleStartQuiz(payload) {
  const roomCode = (payload.roomCode || "").toUpperCase().trim();
  const questions = payload.questions || [];

  const lock = LockService.getScriptLock();
  if (!lock.tryLock(5000)) return jsonError("CONCURRENCY_TIMEOUT", "Server busy.");

  try {
    const ss = getDatabaseSpreadsheet();
    const sheet = ss.getSheetByName("Rooms");
    const rowIndex = findRoomRowIndex(sheet, roomCode);
    if (rowIndex === -1) return jsonError("ROOM_NOT_FOUND", "Room not found.");

    const row = sheet.getRange(rowIndex, 1, 1, 15).getValues()[0];
    const room = rowToRoomObject(row);

    room.gameType = "COUPLES_QUIZ";
    room.status = "PLAYING";
    room.gameState = {
      currentQuestionIndex: 0,
      totalQuestions: questions.length,
      questions: questions,
      hostAnswer: null,
      partnerAnswer: null,
      answersRevealed: false,
      roundMatches: 0,
      completed: false
    };

    room.lastActivity = new Date().toISOString();
    room.version += 1;

    sheet.getRange(rowIndex, 1, 1, 15).setValues([roomObjectToRow(room)]);
    return jsonSuccess(room);
  } finally {
    lock.releaseLock();
  }
}

function handleSendReaction(payload) {
  const roomCode = (payload.roomCode || "").toString().replace(/[\s-]/g, '').toUpperCase();
  const playerId = payload.playerId;
  const reactionType = payload.reaction || "HEART";

  const lock = LockService.getScriptLock();
  if (!lock.tryLock(3000)) return jsonError("CONCURRENCY_TIMEOUT", "Server busy.");

  try {
    const ss = getDatabaseSpreadsheet();
    const sheet = ss.getSheetByName("Rooms");
    const rowIndex = findRoomRowIndex(sheet, roomCode);
    if (rowIndex === -1) return jsonError("ROOM_NOT_FOUND", "Room not found.");

    const row = sheet.getRange(rowIndex, 1, 1, 15).getValues()[0];
    const room = rowToRoomObject(row);

    const isHost = (playerId === room.host.id);
    const isPartner = (room.partner && playerId === room.partner.id);
    if (!isHost && !isPartner) return jsonError("UNAUTHORIZED", "Player not in room.");

    const playerRole = isHost ? "HOST" : "PARTNER";

    if (!room.gameState) room.gameState = {};
    room.gameState.lastReaction = {
      type: reactionType,
      sender: playerRole,
      timestamp: new Date().toISOString()
    };

    room.lastActivity = new Date().toISOString();
    room.version += 1;

    sheet.getRange(rowIndex, 1, 1, 15).setValues([roomObjectToRow(room)]);
    return jsonSuccess({ success: true, lastReaction: room.gameState.lastReaction, version: room.version });
  } finally {
    lock.releaseLock();
  }
}

// ============================================================================
// 5. PURE GAME REDUCERS (STATE MUTATION ENGINES)
// ============================================================================

/**
 * Tic-Tac-Toe Move Reducer
 */
function reduceTicTacToe(state, move, player) {
  const cell = move.cellIndex;
  if (cell < 0 || cell > 8) return { error: "Cell index must be 0 to 8." };
  if (state.grid[cell] !== null) return { error: "Cell is already occupied." };
  if (state.winner !== null) return { error: "Game is already completed." };

  const symbol = (player === "HOST") ? "X" : "O";
  const nextGrid = [...state.grid];
  nextGrid[cell] = symbol;
  const moveCount = state.moveCount + 1;

  const lines = [
    [0,1,2], [3,4,5], [6,7,8], // Rows
    [0,3,6], [1,4,7], [2,5,8], // Columns
    [0,4,8], [2,4,6]           // Diagonals
  ];

  let winningLine = null;
  let winner = null;

  for (let i = 0; i < lines.length; i++) {
    const [a, b, c] = lines[i];
    if (nextGrid[a] && nextGrid[a] === nextGrid[b] && nextGrid[a] === nextGrid[c]) {
      winningLine = lines[i];
      winner = player;
      break;
    }
  }

  if (!winner && moveCount >= 9) {
    winner = "DRAW";
  }

  return {
    nextState: {
      grid: nextGrid,
      moveCount: moveCount,
      winningLine: winningLine,
      winner: winner
    },
    nextTurn: (player === "HOST") ? "PARTNER" : "HOST",
    winner: winner
  };
}

/**
 * Dots and Boxes Move Reducer (Supports Dynamic Grid Size 3x3, 4x4, 5x5)
 */
function reduceDotsAndBoxes(state, move, player) {
  const gridSize = state.gridSize || 3;
  let lineKey = "";
  if (typeof move === "string") {
    lineKey = move;
  } else if (move && move.lineKey) {
    lineKey = move.lineKey;
  } else if (move && move.lineType) {
    lineKey = `${move.lineType}_${move.row}_${move.col}`;
  }

  const parts = lineKey.split("_");
  if (parts.length !== 3) {
    return { error: "Invalid line key format. Expected H_r_c or V_r_c." };
  }
  const lineType = parts[0];
  const r = parseInt(parts[1], 10);
  const c = parseInt(parts[2], 10);

  if (lineType === "H") {
    if (r < 0 || r > gridSize || c < 0 || c >= gridSize) {
      return { error: "Horizontal line coordinates out of bounds." };
    }
  } else if (lineType === "V") {
    if (r < 0 || r >= gridSize || c < 0 || c > gridSize) {
      return { error: "Vertical line coordinates out of bounds." };
    }
  } else {
    return { error: "Invalid line type. Must be H or V." };
  }

  const claimedLines = Object.assign({}, state.claimedLines || {});
  const claimedBoxes = Object.assign({}, state.claimedBoxes || {});

  if (claimedLines[lineKey]) {
    return { error: "Line already claimed." };
  }
  claimedLines[lineKey] = player;

  // Check if any box was completed by this line move
  let newlyCapturedBoxes = 0;
  for (let row = 0; row < gridSize; row++) {
    for (let col = 0; col < gridSize; col++) {
      const boxKey = `box_${row}_${col}`;
      if (!claimedBoxes[boxKey]) {
        const top = `H_${row}_${col}`;
        const bottom = `H_${row + 1}_${col}`;
        const left = `V_${row}_${col}`;
        const right = `V_${row}_${col + 1}`;

        if (claimedLines[top] && claimedLines[bottom] && claimedLines[left] && claimedLines[right]) {
          claimedBoxes[boxKey] = player;
          newlyCapturedBoxes++;
        }
      }
    }
  }

  let hostBoxes = 0;
  let partnerBoxes = 0;
  Object.keys(claimedBoxes).forEach(function(k) {
    if (claimedBoxes[k] === "HOST") {
      hostBoxes++;
    } else {
      partnerBoxes++;
    }
  });

  const totalBoxes = gridSize * gridSize;
  const isGameOver = Object.keys(claimedBoxes).length >= totalBoxes;
  const awardedExtraTurn = newlyCapturedBoxes > 0 && !isGameOver;
  const nextTurn = awardedExtraTurn ? player : (player === "HOST" ? "PARTNER" : "HOST");

  let winner = null;
  if (isGameOver) {
    if (hostBoxes > partnerBoxes) {
      winner = "HOST";
    } else if (partnerBoxes > hostBoxes) {
      winner = "PARTNER";
    } else {
      winner = "DRAW";
    }
  }

  return {
    nextState: {
      gridSize: gridSize,
      claimedLines: claimedLines,
      claimedBoxes: claimedBoxes,
      hostBoxesCount: hostBoxes,
      partnerBoxesCount: partnerBoxes,
      lastMoveAwardedExtraTurn: awardedExtraTurn,
      winner: winner
    },
    nextTurn: nextTurn,
    winner: winner
  };
}

/**
 * Couples Quiz Move Reducer
 */
function reduceCouplesQuiz(state, move, player) {
  const answer = move.answer; // e.g. "A", "B", "C", "D"
  const nextState = { ...state };

  if (player === "HOST") {
    nextState.hostAnswer = answer;
  } else {
    nextState.partnerAnswer = answer;
  }

  // If both partners have submitted their choices, reveal simultaneously!
  if (nextState.hostAnswer !== null && nextState.partnerAnswer !== null) {
    nextState.answersRevealed = true;
    if (nextState.hostAnswer === nextState.partnerAnswer) {
      nextState.roundMatches = (nextState.roundMatches || 0) + 1;
    }
  }

  return { nextState: nextState };
}

// ============================================================================
// 6. AUTO-CLEANUP CRON TRIGGER
// ============================================================================

/**
 * Cron trigger executed every 10-15 minutes to delete abandoned/idle rooms.
 */
function cleanupInactiveRooms() {
  const lock = LockService.getScriptLock();
  if (!lock.tryLock(10000)) {
    Logger.log("Cleanup deferred: Script lock busy.");
    return;
  }

  try {
    const ss = getDatabaseSpreadsheet();
    const sheet = ss.getSheetByName("Rooms");
    const lastRow = sheet.getLastRow();
    if (lastRow <= 1) return;

    const dataRange = sheet.getRange(2, 1, lastRow - 1, 15);
    const rows = dataRange.getValues();
    const now = new Date().getTime();
    const INACTIVITY_THRESHOLD_MS = 30 * 60 * 1000;   // 30 min expiration
    const ABANDONED_GRACE_PERIOD_MS = 10 * 60 * 1000; // 10 min for abandoned

    const rowsToKeep = [];
    let purgedCount = 0;

    for (let i = 0; i < rows.length; i++) {
      const row = rows[i];
      const status = row[1];
      const lastActivityTime = new Date(row[13]).getTime();
      const idleDuration = now - lastActivityTime;

      let shouldDelete = false;
      if (isNaN(lastActivityTime) || idleDuration > INACTIVITY_THRESHOLD_MS) {
        shouldDelete = true;
      } else if (status === "ABANDONED" && idleDuration > ABANDONED_GRACE_PERIOD_MS) {
        shouldDelete = true;
      }

      if (shouldDelete) {
        purgedCount++;
      } else {
        rowsToKeep.push(row);
      }
    }

    if (purgedCount > 0) {
      sheet.getRange(2, 1, lastRow - 1, 15).clearContent();
      if (rowsToKeep.length > 0) {
        sheet.getRange(2, 1, rowsToKeep.length, 15).setValues(rowsToKeep);
      }
      Logger.log("Cleanup complete. Purged " + purgedCount + " inactive rooms.");
    }
  } catch (err) {
    Logger.log("Error in cleanupInactiveRooms: " + err.toString());
  } finally {
    lock.releaseLock();
  }
}
