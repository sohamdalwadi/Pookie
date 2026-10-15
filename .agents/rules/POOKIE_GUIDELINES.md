# Pookie Guidelines for Antigravity AI Agents & Developers

Welcome to the **Pookie** project! Pookie is an open-source, turn-based couple's mini-game platform designed to bring couples closer through delightful interactive games.

## 🎯 Architecture Pillars
1. **Lightweight Serverless Backend**: Powered by Google Sheets + Google Apps Script acting as a deterministic REST API. No expensive database instances or persistent servers required.
2. **Modular Game Engine Architecture**: Every mini-game is isolated into its own game contract:
   - Board state model
   - Validation & move transition function: `(currentState, moveAction) => nextState`
   - Win/draw condition evaluation
   - Serialization to/from Google Sheets JSON cell
3. **Anti Gravity Friendly**: Anyone should be able to contribute new games or UI improvements using the Antigravity AI Agent even without Android or deep game development experience.

---

## 🛠️ Tech Stack & Constraints
- **Frontend / Client**: Anti Gravity Game Engine / Android Kotlin client.
- **Backend**: Google Apps Script (GAS) deployed as a Web App (`doGet`, `doPost`).
- **Database**: Google Spreadsheet with strict schema contracts.
- **Protocols**: JSON over HTTPS REST, optimized with versioning (`v` parameter) to minimize GAS quota consumption.

---

## 📐 Coding & Contribution Principles
- **Stateless Game Logic**: Game engines MUST be pure functions whenever possible.
- **Single-Source-of-Truth**: The Google Sheet row version (`Version`) is authoritative. Optimistic concurrency control prevents race conditions.
- **Defensive Error Handling**: Always return structured errors `{ status: "ERROR", error: { code, message } }`.
- **Preserve Documentation**: All changes must maintain markdown docs in `docs/` and adhere to semantic commit messages.
