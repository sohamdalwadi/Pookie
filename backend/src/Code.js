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
      case "SUBMIT_VERDICT":
        return handleSubmitVerdict(payload);
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
  const clientVersion = parseInt(params.v || params.version, 10);

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

  // SoulSync Blind Freeform Answer Masking
  if (room.gameType === "SOUL_SYNC" && room.gameState && !room.gameState.answersRevealed) {
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
      boxNumbers: {},
      hostBoxesCount: 0,
      partnerBoxesCount: 0,
      lastMoveAwardedExtraTurn: false,
      winner: null
    };
  } else if (gameType === "COUPLES_QUIZ") {
    initialGameState = { currentQuestionIndex: 0, totalQuestions: 5, questions: [], hostAnswer: null, partnerAnswer: null, answersRevealed: false, roundMatches: 0, completed: false };
  } else if (gameType === "SOUL_SYNC") {
    initialGameState = createSoulSyncInitialState(hostId, payload.gender || "MALE");
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
  const expectedVersion = parseInt(payload.expectedVersion || payload.clientVersion, 10);
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

    // Turn verification (except simultaneous Quiz & SoulSync moves)
    if (room.gameType !== "COUPLES_QUIZ" && room.gameType !== "SOUL_SYNC" && room.currentTurn !== playerRole) {
      return jsonError("NOT_YOUR_TURN", "It is not your turn to move.");
    }

    // Turn Timeout Pass: Player ran out of time, switch turn advantage to partner
    if (move.isTimeout === true) {
      room.currentTurn = (playerRole === "HOST" ? "PARTNER" : "HOST");
      room.lastActivity = new Date().toISOString();
      room.version += 1;
      sheet.getRange(rowIndex, 1, 1, 15).setValues([roomObjectToRow(room)]);
      return jsonSuccess(room);
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
    } else if (room.gameType === "SOUL_SYNC") {
      const result = reduceSoulSync(room.gameState, move, playerRole);
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
      } else if (room.gameType === "SOUL_SYNC") {
        room.gameState = createSoulSyncInitialState(room.host.id, room.host.gender || "MALE");
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

function handleSubmitVerdict(payload) {
  const roomCode = (payload.roomCode || "").toString().replace(/[\s-]/g, '').toUpperCase();
  const playerId = payload.playerId;
  const verdict = (payload.verdict || "").toUpperCase().trim();

  if (verdict !== "MATCH" && verdict !== "MISS") {
    return jsonError("INVALID_VERDICT", "Verdict must be 'MATCH' or 'MISS'.");
  }

  const lock = LockService.getScriptLock();
  if (!lock.tryLock(5000)) return jsonError("CONCURRENCY_TIMEOUT", "Server busy, please retry.");

  try {
    const ss = getDatabaseSpreadsheet();
    const sheet = ss.getSheetByName("Rooms");
    const rowIndex = findRoomRowIndex(sheet, roomCode);

    if (rowIndex === -1) {
      return jsonError("ROOM_NOT_FOUND", "Room '" + roomCode + "' does not exist.");
    }

    const row = sheet.getRange(rowIndex, 1, 1, 15).getValues()[0];
    const room = rowToRoomObject(row);

    const isHost = (playerId === room.host.id);
    const isPartner = (room.partner && playerId === room.partner.id);
    if (!isHost && !isPartner) {
      return jsonError("UNAUTHORIZED", "Player not authorized in this room session.");
    }

    const roleKey = isHost ? "host" : "partner";
    if (!room.gameState) room.gameState = {};
    if (!room.gameState.verdicts) room.gameState.verdicts = { host: null, partner: null };
    if (!room.gameState.playerReady) room.gameState.playerReady = { host: false, partner: false };

    room.gameState.verdicts[roleKey] = verdict;
    room.gameState.playerReady[roleKey] = true;

    // Check if BOTH players have submitted their verdict
    const hostVerdict = room.gameState.verdicts.host;
    const partnerVerdict = room.gameState.verdicts.partner;

    if (hostVerdict && partnerVerdict) {
      const isMutualMatch = (hostVerdict === "MATCH" && partnerVerdict === "MATCH");
      let currentScore = room.gameState.coupleScore || 0;

      if (isMutualMatch) {
        currentScore += 2;
        room.host.score += 2;
        if (room.partner) room.partner.score += 2;
      } else {
        currentScore = Math.max(0, currentScore - 1);
        room.host.score = Math.max(0, room.host.score - 1);
        if (room.partner) room.partner.score = Math.max(0, room.partner.score - 1);
      }

      room.gameState.coupleScore = currentScore;
      room.gameState.lastRoundOutcome = isMutualMatch ? "MATCH" : "MISS";

      const totalRounds = room.gameState.totalRounds || 10;
      const nextRound = (room.gameState.currentRound || 1) + 1;

      if (nextRound > totalRounds) {
        room.status = "FINISHED";
        room.gameState.completed = true;
      } else {
        const prompts = room.gameState.prompts || [];
        const nextPromptObj = prompts[nextRound - 1];
        room.gameState.currentRound = nextRound;
        room.gameState.promptText = (nextPromptObj && nextPromptObj.template) ? nextPromptObj.template : ("SoulSync Prompt #" + nextRound);
        room.gameState.targetRole = (nextPromptObj && nextPromptObj.targetRole) ? nextPromptObj.targetRole : "HOST";
        room.gameState.phase = "ANSWERING";
        // Clean up temporary secret text answers for next round
        room.gameState.hostAnswer = null;
        room.gameState.partnerAnswer = null;
        room.gameState.answersRevealed = false;
        room.gameState.verdicts = { host: null, partner: null };
        room.gameState.playerReady = { host: false, partner: false };
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

// ============================================================================
// 5. PURE GAME REDUCERS (STATE MUTATION ENGINES)
// ============================================================================

/**
 * Tic-Tac-Toe Move Reducer
 */
function reduceTicTacToe(state, move, player) {
  const cell = (typeof move === "number") ? move : 
               (typeof move === "object" && move !== null && move.cellIndex !== undefined) ? parseInt(move.cellIndex, 10) : 
               parseInt(move, 10);
  if (isNaN(cell) || cell < 0 || cell > 8) return { error: "Cell index must be 0 to 8." };
  
  if (!state.grid) {
    state.grid = Array.isArray(state.board) ? state.board : Array(9).fill(null);
  }
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
  const boxNumbers = Object.assign({}, state.boxNumbers || {});

  if (claimedLines[lineKey]) {
    return { error: "Line already claimed." };
  }
  claimedLines[lineKey] = player;

  // Count existing boxes captured by this player to number sequentially (1, 2, 3...)
  let currentPlayerBoxCount = 0;
  Object.keys(claimedBoxes).forEach(function(k) {
    if (claimedBoxes[k] === player) currentPlayerBoxCount++;
  });

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
          currentPlayerBoxCount++;
          boxNumbers[boxKey] = currentPlayerBoxCount;
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
      boxNumbers: boxNumbers,
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

/**
 * SoulSync Move Reducer (Secret Freeform Answer Submission)
 */
function reduceSoulSync(state, move, player) {
  const text = (move.text || "").trim();
  const nextState = { ...state };

  if (player === "HOST") {
    nextState.hostAnswer = text || "Answer submitted";
  } else {
    nextState.partnerAnswer = text || "Answer submitted";
  }

  // If both have submitted secret answers, advance to JUDGING phase!
  if (nextState.hostAnswer !== null && nextState.partnerAnswer !== null) {
    nextState.answersRevealed = true;
    nextState.phase = "JUDGING";
    nextState.verdicts = { host: null, partner: null };
    nextState.playerReady = { host: false, partner: false };
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

// ============================================================================
// 7. SOULSYNC 100-PROMPT CATALOG & SESSION GENERATOR
// ============================================================================

const SOUL_SYNC_CATALOG = [
  "What is {TARGET}'s absolute favorite comfort food when having a rough day?",
  "What was the very first impression {TARGET} had of you?",
  "If {TARGET} could immediately master any musical instrument, which one would it be?",
  "What is {TARGET}'s biggest guilty pleasure song or artist?",
  "If {TARGET} had 1 million dollars to spend in 24 hours, what would they buy first?",
  "What is {TARGET}'s go-to midnight snack when everyone is asleep?",
  "What is one dream destination {TARGET} wants to visit with you?",
  "What silly quirk or habit of {TARGET} always makes you smile?",
  "What was {TARGET}'s childhood dream career or aspiration?",
  "What is {TARGET}'s favorite way to unwind after a stressful day?",
  "Which fictional character reminds you most of {TARGET}?",
  "What is {TARGET}'s favorite physical feature of yours?",
  "What is something that never fails to make {TARGET} laugh out loud?",
  "What was {TARGET}'s favorite subject back in school?",
  "If {TARGET} could have any superpower, what would they choose?",
  "What is {TARGET}'s favorite season of the year and why?",
  "What is {TARGET}'s biggest pet peeve in daily life?",
  "If {TARGET} could only eat one meal for the rest of their life, what would it be?",
  "What is the most adventurous thing {TARGET} has ever done?",
  "What is {TARGET}'s favorite coffee, tea, or signature drink?",
  "What movie could {TARGET} rewatch a hundred times without getting bored?",
  "What is {TARGET}'s hidden or underrated talent?",
  "Who is {TARGET}'s ultimate celebrity crush?",
  "What was {TARGET}'s first ever job or gig?",
  "If {TARGET} were an animal, what animal would they be?",
  "What is {TARGET}'s favorite holiday or celebration?",
  "What is {TARGET}'s primary love language (Words, Touch, Gifts, Acts, Quality Time)?",
  "What scent or perfume immediately reminds you of {TARGET}?",
  "What is {TARGET}'s favorite thing about your relationship?",
  "If {TARGET} could time travel, would they visit the past or future?",
  "What is {TARGET}'s favorite board or video game of all time?",
  "What is one fear or phobia that {TARGET} secretly has?",
  "What song always gets {TARGET} singing in the car or shower?",
  "What is {TARGET}'s favorite dessert or sweet treat?",
  "If {TARGET} could teleport anywhere right now for 1 hour, where would they go?",
  "What is {TARGET}'s favorite clothing item or favorite outfit to wear?",
  "How does {TARGET} like their eggs cooked for breakfast?",
  "What is {TARGET}'s dream car or dream way of traveling?",
  "What is one item {TARGET} cannot leave the house without?",
  "What was {TARGET}'s favorite cartoon or TV show growing up?",
  "What is {TARGET}'s ideal temperature when sleeping (Freezing cold or Warm & toasty)?",
  "What is {TARGET}'s favorite pizza topping combination?",
  "If {TARGET} could adopt any wild exotic animal as a pet, what would it be?",
  "What is the sweetest thing {TARGET} has ever done for you?",
  "How does {TARGET} handle scary movies or haunted houses?",
  "What is {TARGET}'s most frequently used emoji in text messages?",
  "If {TARGET} were stranded on a deserted island, what 1 item would they bring?",
  "What is {TARGET}'s proudest personal accomplishment so far?",
  "What is {TARGET}'s favorite ice cream flavor?",
  "What would {TARGET} name a cute little puppy if you got one together today?",
  "What is {TARGET}'s favorite indoor hobby on a rainy Sunday afternoon?",
  "Does {TARGET} prefer sunrise mornings or late-night stargazing?",
  "What is {TARGET}'s go-to karaoke anthem when handed a microphone?",
  "What is the funniest nickname {TARGET} has ever had?",
  "If {TARGET} opened a restaurant or café, what theme or cuisine would it be?",
  "What was the very first gift {TARGET} ever gave you?",
  "What is {TARGET}'s favorite kind of flowers or plants?",
  "If {TARGET} could meet any historical figure for dinner, who would it be?",
  "What is {TARGET}'s favorite theme park ride (Coaster, Water ride, or Ferris wheel)?",
  "What is {TARGET}'s ideal date night vibe (Fancy dinner or Cozy couch takeout)?",
  "What is one language {TARGET} has always wanted to speak fluently?",
  "What is {TARGET}'s favorite memory of the two of you laughing together?",
  "What is {TARGET}'s biggest weakness while shopping at a supermarket?",
  "If {TARGET} wrote a book, what genre would it belong to?",
  "What is {TARGET}'s favorite sport to play or watch?",
  "What is the most thoughtful compliment {TARGET} ever received?",
  "How does {TARGET} take their morning beverage (Sweet, Black, Milk, Iced)?",
  "What is {TARGET}'s favorite spot in the house to relax?",
  "If {TARGET} could trade lives with anyone for 24 hours, who would it be?",
  "What is {TARGET}'s signature kitchen dish that they make best?",
  "What was the last thing that made {TARGET} shed happy tears?",
  "What is {TARGET}'s favorite party game to play with friends?",
  "What is one bucket list item {TARGET} talks about constantly?",
  "Does {TARGET} prefer the mountains, the beach, or a bustling city?",
  "What is {TARGET}'s favorite shoe or sneaker style?",
  "If {TARGET} could design their dream home, what must-have room would it include?",
  "What is {TARGET}'s favorite comedy movie of all time?",
  "What is {TARGET}'s phone battery percentage usually at (90%+ or living at 5%)?",
  "What is {TARGET}'s favorite thing you do when they are feeling down?",
  "If {TARGET} had to survive a zombie apocalypse, what would their weapon of choice be?",
  "What is {TARGET}'s favorite smell in the world (Fresh rain, Coffee, Baked cookies)?",
  "What is {TARGET}'s favorite breakfast food?",
  "If {TARGET} could star in a TV show, which existing show would they join?",
  "What is {TARGET}'s favorite way to receive affection (Hug, Kiss, Back rub, Holding hands)?",
  "What is {TARGET}'s honest opinion on pineapple on pizza?",
  "What is {TARGET}'s favorite game to play when traveling?",
  "What is the first concert or live show {TARGET} ever attended?",
  "If {TARGET} were given a free plane ticket right now, what city would they fly to?",
  "What is {TARGET}'s favorite quote, motto, or personal life philosophy?",
  "What is {TARGET}'s favorite childhood memory?",
  "What was {TARGET}'s favorite bedtime story or book growing up?",
  "What is {TARGET}'s favorite jacket, hoodie, or sweater to wear?",
  "What is {TARGET}'s favorite late-night TV show or YouTube channel to binge?",
  "What is {TARGET}'s favorite flavor of bubble tea, smoothie, or juice?",
  "What is {TARGET}'s favorite kind of massage or pampering?",
  "What was {TARGET}'s very first car or mode of transport?",
  "What is {TARGET}'s favorite thing about your smile?",
  "If {TARGET} could live anywhere in the world for 1 year, where would it be?",
  "What is {TARGET}'s absolute favorite romantic song about us?",
  "What is one promise you know {TARGET} will always keep?"
];

function createSoulSyncInitialState(hostId, hostGender) {
  // Shuffle copy of the 100 questions
  const shuffled = SOUL_SYNC_CATALOG.slice().sort(function() { return 0.5 - Math.random(); });
  const selected = shuffled.slice(0, 10);

  const sessionPrompts = selected.map(function(template, index) {
    const role = (Math.random() < 0.5) ? "HOST" : "PARTNER";
    return {
      id: "SS_" + (index + 1),
      targetRole: role,
      template: template
    };
  });

  const first = sessionPrompts[0];

  const state = {
    currentRound: 1,
    totalRounds: 10,
    prompts: sessionPrompts,
    promptText: first.template,
    targetRole: first.targetRole,
    phase: "ANSWERING",
    hostAnswer: null,
    partnerAnswer: null,
    answersRevealed: false,
    verdicts: { host: null, partner: null },
    playerReady: { host: false, partner: false },
    coupleScore: 0,
    completed: false
  };

  if (hostId) {
    state.playerGenders = {};
    state.playerGenders[hostId] = hostGender || "MALE";
  }

  return state;
}
