# ⚙️ Pookie Backend (Google Apps Script + Google Sheets)

This directory contains backend scripts and setup guides for deploying the serverless REST API for Pookie.

## 📁 Architecture
- **Host:** Google Apps Script Web App (`doGet`, `doPost`)
- **Database:** Google Sheets (`Rooms`, `QuizQuestions`)
- **Triggers:** Time-driven cron `cleanupInactiveRooms` running every 10 minutes.

Refer to [`docs/architecture/TECHNICAL_SPEC.md`](../docs/architecture/TECHNICAL_SPEC.md) for the complete specification.
