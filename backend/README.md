# ⚙️ Pookie Backend (Google Apps Script + Google Sheets)

This directory contains backend scripts and deployment configurations for the serverless REST API for Pookie.

## 🚀 Key Architectural Strategies
1. **Dynamic Sheet Binding:** Zero hardcoded Google Spreadsheet IDs. Automatically binds to the container spreadsheet via `SpreadsheetApp.getActiveSpreadsheet()` or falls back to `PropertiesService` for standalone deployments.
2. **Self-Bootstrapping Database:** Running `bootstrapDatabase()` automatically initializes the `Rooms` and `QuizQuestions` sheets with exact schemas and default couples icebreaker questions.
3. **Clasp Support:** Use `.clasp.json.example` to develop and deploy locally using Google's `@google/clasp` CLI.

## 📚 Related Documentation
- [Setup & Deployment Guide](../docs/SETUP_GUIDE.md)
- [Dynamic Binding & Security Architecture](../docs/architecture/DYNAMIC_BINDING_AND_SECURITY.md)
- [REST API Specification](../docs/architecture/API_SPEC.md)
- [Auto-Cleanup Cron Logic](../docs/architecture/CLEANUP_CRON.md)
