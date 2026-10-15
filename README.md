# 💖 Pookie - Turn-Based Couple's Mini-Game App

<p align="center">
  <img src="https://img.shields.io/badge/Status-Planning%20%26%20Architecture-pink?style=for-the-badge" alt="Status" />
  <img src="https://img.shields.io/badge/Architecture-Anti%20Gravity-blueviolet?style=for-the-badge" alt="Anti Gravity" />
  <img src="https://img.shields.io/badge/Backend-Google%20Apps%20Script%20%2B%20Sheets-success?style=for-the-badge" alt="Google Sheets Backend" />
  <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="License" />
</p>

> **"Because everyday love is made of little turns, playful moves, and shared laughter."**  
> **Pookie** is a room-based, turn-based mini-game app crafted for couples to connect, play, and spark conversations anywhere, anytime.

---

## 👨‍💻 Architecture & Lead Developer
- **Architect & Principal Developer:** [Soham Dalwadi](https://github.com/sohamdalwadi)
- **Official Repository:** [https://github.com/sohamdalwadi/Pookie](https://github.com/sohamdalwadi/Pookie)
- **Frontend / Game Engine:** Anti Gravity
- **Backend & Database:** Google Sheets + Google Apps Script (Zero-Cost Serverless REST API)

---

## 🎮 Launch Games

| Game | Format | Description | Specification |
| :--- | :---: | :--- | :--- |
| **❌⭕ Tic-Tac-Toe** | Turn-Based | The classic 3x3 showdown with delightful couple touches and animations. | [Read Spec](docs/games/TIC_TAC_TOE.md) |
| **🟦 Dots & Boxes** | Turn-Based | Tactical edge-connecting game where capturing a box grants an extra turn. | [Read Spec](docs/games/DOTS_AND_BOXES.md) |
| **💖 Couples Quiz** | Simultaneous | Blind icebreaker questions with simultaneous reveals to spark deep conversations. | [Read Spec](docs/games/COUPLES_QUIZ.md) |

---

## 📚 Comprehensive Architecture & Engineering Documentation

All engineering blueprints have been mapped out prior to code implementation:

| Document | Description |
| :--- | :--- |
| 🚀 **[Quickstart & Setup Guide](docs/SETUP_GUIDE.md)** | Step-by-step 3-minute self-hosting guide for forkers and contributors. |
| 🔐 **[Dynamic Binding & Security](docs/architecture/DYNAMIC_BINDING_AND_SECURITY.md)** | Zero-hardcoding architecture, container binding, and client config abstraction. |
| 📄 **[Master Technical Specification](docs/architecture/TECHNICAL_SPEC.md)** | End-to-end architectural design, schemas, API contracts, lifecycles, and FSM. |
| 🗄️ **[Database Schema Blueprint](docs/architecture/DATABASE_SCHEMA.md)** | Exact Google Sheets visual layout, columns, types, and single-cell JSON serialization. |
| 🌐 **[REST API Specification](docs/architecture/API_SPEC.md)** | Complete request/response contracts for `CREATE_ROOM`, `JOIN_ROOM`, `GET_STATE`, `MAKE_MOVE`, and `RESET_GAME`. |
| 🔄 **[State Machines & Polling](docs/architecture/STATE_MACHINES.md)** | Client FSM, turn handshakes, and 4-tier adaptive heartbeat engine to preserve battery and Google API quota. |
| 🧹 **[Auto-Cleanup & Cron](docs/architecture/CLEANUP_CRON.md)** | Pseudocode, 30-minute inactivity thresholds, and batch memory row cleanup. |
| 🗺️ **[Development Roadmap](docs/ROADMAP.md)** | Phased engineering timeline from planning through launch. |
| 🤝 **[Contributing with Antigravity](docs/CONTRIBUTING.md)** | Guide to adding new games and features using Antigravity AI agents without Android knowledge! |

---

## ⚡ 3-Minute Quickstart (Self-Hosting for Forkers)

Pookie is built with a **100% generic, zero-hardcoding architecture**. When you fork Pookie, you deploy your own private backend on your Google Drive at zero cost:

1. **Create your Google Sheet:** Go to [sheets.new](https://sheets.new) and open **Extensions > Apps Script**.
2. **Deploy as Web App:** Paste the backend script, run `bootstrapDatabase` once (auto-creates tabs & questions), and deploy as Web App (`Execute as: Me`, `Access: Anyone`).
3. **Configure your Client:**
   ```bash
   cp app/config.template.json app/config.json
   ```
   Paste your generated Web App URL into `app/config.json`:
   ```json
   {
     "POOKIE_API_URL": "https://script.google.com/macros/s/YOUR_DEPLOYMENT_ID/exec"
   }
   ```
4. **Compile & Play:** Your app now talks exclusively to your private Google Sheet database. `config.json` is ignored by Git to keep your endpoint private.

---

## 🏗️ High-Level System Architecture

```
┌────────────────────────────────────────────────────────┐
│             POOKIE CLIENT (ANTI GRAVITY)               │
│                                                        │
│  ┌────────────────┐  Adaptive Polling  ┌────────────┐  │
│  │ State Machine  │◄──────────────────►│ UI / Views │  │
│  └───────┬────────┘    (Opponent Turn) └────────────┘  │
└──────────┼─────────────────────────────────────────────┘
           │ HTTPS (REST)
           │ GET / POST (JSON Payload + ETag Versioning)
           ▼
┌────────────────────────────────────────────────────────┐
│      SERVERLESS BACKEND (GOOGLE APPS SCRIPT)           │
│                                                        │
│  ┌──────────────────────┐      ┌────────────────────┐  │
│  │ doGet(e) / doPost(e) │◄────►│ LockService (OCC)  │  │
│  └──────────┬───────────┘      └────────────────────┘  │
│             │                                          │
│             ▼                                          │
│  ┌──────────────────────┐      ┌────────────────────┐  │
│  │ Pure Game Reducers   │      │ cleanupInactive    │  │
│  │ (TTT, Boxes, Quiz)   │      │ Rooms (Cron Trigger│  │
│  └──────────┬───────────┘      └─────────┬──────────┘  │
└─────────────┼────────────────────────────┼─────────────┘
              ▼                            ▼
┌────────────────────────────────────────────────────────┐
│           DATABASE (GOOGLE SPREADSHEET)                │
│                                                        │
│  ┌──────────────────────┐      ┌────────────────────┐  │
│  │   'Rooms' Sheet      │      │ 'QuizQuestions'    │  │
│  │ (Serialized Boards)  │      │   Question Bank    │  │
│  └──────────────────────┘      └────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

---

## 🌟 Why Antigravity for Contributions?
You don't need Android SDK or complex game engine experience to build games for Pookie!
Our repository contains an Antigravity custom skill in [`.agents/skills/pookie-game-dev/SKILL.md`](.agents/skills/pookie-game-dev/SKILL.md) and rules in [`.agents/rules/POOKIE_GUIDELINES.md`](.agents/rules/POOKIE_GUIDELINES.md).

Simply open this repository in Antigravity and prompt:
> *"Add a new couple's mini-game called 'Memory Flip Cards' following the pookie-game-dev skill."*

Antigravity will understand the board serialization, state machine, and API contracts to scaffold your game end-to-end.

---

## 📜 License
Distributed under the MIT License. See `LICENSE` for details.
