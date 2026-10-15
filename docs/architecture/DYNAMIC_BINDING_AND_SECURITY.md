# 🔐 Generic Architecture: Dynamic Sheet Binding & Environment Configuration

**Author:** Soham Dalwadi  
**Role:** Principal Architect  
**Project:** Pookie  

---

## 📌 The Open-Source Challenge
In many open-source Google Apps Script projects, developers hardcode:
```javascript
// ❌ ANTI-PATTERN: Hardcoded Sheet ID
const SPREADSHEET_ID = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
const ss = SpreadsheetApp.openById(SPREADSHEET_ID);
```
And in client code:
```javascript
// ❌ ANTI-PATTERN: Hardcoded API URL
const API_URL = "https://script.google.com/macros/s/AKfycbx.../exec";
```

### Why this breaks:
1. **Security & Data Leaks:** Forkers inadvertently send game data and test traffic to the original maintainer's personal Google Sheet.
2. **Setup Friction:** Contributors must manually dig through source code to find and replace string literals.
3. **Fragility:** If the maintainer deletes or revokes access to their personal sheet, all forks break.

---

## 1. Dynamic Sheet Binding Strategy

To make Pookie 100% generic, zero-configuration, and isolated, the backend uses **Dynamic Container Binding with Script Property Fallback**:

```mermaid
flowchart TD
    Call[getSpreadsheet Call] --> CheckContainer{SpreadsheetApp.getActiveSpreadsheet() exists?}
    CheckContainer -- Yes (Container-Bound) --> UseContainer[Use Active Spreadsheet Container]
    CheckContainer -- No (Standalone Clasp) --> CheckProps{PropertiesService has SPREADSHEET_ID?}
    CheckProps -- Yes --> OpenById[SpreadsheetApp.openById(sheetId)]
    CheckProps -- No --> FriendlyError[Throw Diagnostic Error with Guided Setup Instructions]
```

### Architectural Implementation:
```javascript
/**
 * Dynamically resolves the target database spreadsheet without hardcoded IDs.
 * Priority 1: Container-bound spreadsheet (Zero configuration for copy/fork).
 * Priority 2: Script Property 'SPREADSHEET_ID' (For standalone clasp deployments).
 */
function getDatabaseSpreadsheet() {
  // 1. Automatic container binding
  let ss = SpreadsheetApp.getActiveSpreadsheet();
  if (ss) {
    return ss;
  }

  // 2. Fallback to Script Properties for standalone scripts
  const scriptProperties = PropertiesService.getScriptProperties();
  const configuredSheetId = scriptProperties.getProperty("SPREADSHEET_ID");

  if (configuredSheetId && configuredSheetId.trim() !== "") {
    try {
      return SpreadsheetApp.openById(configuredSheetId.trim());
    } catch (e) {
      throw new Error(
        "Pookie Configuration Error: Failed to open sheet with SPREADSHEET_ID: " +
        configuredSheetId + ". Verify permissions."
      );
    }
  }

  throw new Error(
    "Pookie Initialization Error: No Google Sheet bound to this script. " +
    "Either attach this script directly to a Google Sheet (Extensions > Apps Script) " +
    "or set 'SPREADSHEET_ID' in Project Settings > Script Properties."
  );
}
```

---

## 2. Self-Bootstrapping Database (`bootstrapDatabase`)

To eliminate the manual labor of creating tabs and typing column names, Pookie self-provisions its schema upon first deployment:

```javascript
/**
 * Automatically provisions 'Rooms' and 'QuizQuestions' tabs with
 * headers, column sizing, and starter questions if they do not exist.
 */
function bootstrapDatabase() {
  const ss = getDatabaseSpreadsheet();

  // 1. Provision Rooms Sheet
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
    roomsSheet.getRange(1, 1, 1, headers.length).setFontWeight("bold");
    roomsSheet.setFrozenRows(1);
  }

  // 2. Provision QuizQuestions Sheet
  let quizSheet = ss.getSheetByName("QuizQuestions");
  if (!quizSheet) {
    quizSheet = ss.insertSheet("QuizQuestions");
    const headers = [
      "QuestionId", "Category", "QuestionText", "OptionA", "OptionB", "OptionC", "OptionD"
    ];
    quizSheet.getRange(1, 1, 1, headers.length).setValues([headers]);
    quizSheet.getRange(1, 1, 1, headers.length).setFontWeight("bold");
    quizSheet.setFrozenRows(1);

    // Populate default starter questions
    const defaultQuestions = [
      ["Q001", "ROMANTIC", "What is our ideal weekend getaway?", "Cozy mountain cabin", "Beach resort & sunsets", "Spontaneous road trip", "Staying in bed binging shows"],
      ["Q002", "FUNNY", "Who is more likely to burn toast?", "Definitely me", "100% them", "Both of us equally", "Neither, we order takeout"],
      ["Q003", "DEEP", "What brings out the biggest smile on my face?", "Morning cuddles", "Your silly jokes", "Surprise food deliveries", "Deep late night talks"]
    ];
    quizSheet.getRange(2, 1, defaultQuestions.length, 7).setValues(defaultQuestions);
  }
}
```

---

## 3. Client Environment Config Abstraction

The Anti Gravity client completely decouples backend endpoints from application logic.

### 3.1 Template Architecture
- Template committed to Git: [`app/config.template.json`](../../app/config.template.json)
- Ignored by Git: `config.json`, `.env`, `.env.local`

```json
{
  "POOKIE_API_URL": "YOUR_DEPLOYED_GOOGLE_WEB_APP_URL_HERE",
  "POLLING_INTERVAL_ACTIVE_MS": 2000,
  "POLLING_INTERVAL_IDLE_MS": 4000,
  "ENABLE_DEBUG_LOGS": true
}
```

### 3.2 Client Startup Diagnostic Guard
If a developer forgets to configure `POOKIE_API_URL`, the client does not crash with an obscure network error. Instead, it transitions to a guided **Config Diagnostics Screen**:

```
┌──────────────────────────────────────────────┐
│           💖 Pookie Setup Required           │
├──────────────────────────────────────────────┤
│ No backend URL detected!                     │
│                                              │
│ 1. Deploy your Google Apps Script Web App.   │
│ 2. Copy your unique exec URL.                │
│ 3. Paste it into app/config.json or below:   │
│                                              │
│ [ https://script.google.com/.../exec       ] │
│                                              │
│             [ Connect & Save ]               │
└──────────────────────────────────────────────┘
```
This guarantees an effortless onboarding experience for any contributor.
