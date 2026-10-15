# 🛠️ Clasp CLI Developer Guide

This guide details how to develop, test, and deploy the Pookie serverless backend locally using **Google Clasp** (`@google/clasp`).

---

## 🔒 Security & Credential Isolation
- Your personal login credentials (`~/.clasprc.json`) and project binding (`.clasp.json`) contain secret script and Google Drive IDs.
- Both files are **strictly ignored by Git** in `.gitignore` to prevent any credential leaks to public GitHub repositories.

---

## 📋 Clasp Workflow Cheat Sheet

### 1. Authenticate with Google
Log in to your Google Account (opens browser authorization window):
```bash
clasp login
```
> **Note:** Ensure the **Google Apps Script API** is enabled in your Google Account settings:  
> 👉 [https://script.google.com/home/usersettings](https://script.google.com/home/usersettings)

---

### 2. Create or Clone a Pookie Project

#### Option A: Create a brand new Container-Bound Sheet:
```bash
clasp create --title "Pookie Database & API" --type sheets --rootDir ./src
```
This automatically creates a new Google Spreadsheet in your Google Drive and binds the Apps Script project to it.

#### Option B: Clone an existing Apps Script project:
```bash
clasp clone <SCRIPT_ID> --rootDir ./src
```

---

### 3. Push Local Code to Google Apps Script
After modifying files in `src/` (such as `src/Code.js` or `src/appsscript.json`):
```bash
clasp push
```

---

### 4. Deploy as a Public Web App
Deploy a new version of the Web App:
```bash
clasp deploy --description "Pookie REST API v1"
```

To list all existing deployments and find your Web App URL:
```bash
clasp deployments
```

---

### 5. Open in Browser
- Open the Google Apps Script IDE in your browser:
  ```bash
  clasp open
  ```
- Open the deployed Web App URL:
  ```bash
  clasp open --webapp
  ```
