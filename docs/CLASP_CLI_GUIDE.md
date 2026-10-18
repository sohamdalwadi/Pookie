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

### 4. Deploy or Redeploy Backend Code

#### ⚠️ CRITICAL: Permanent Deployment Rule
To keep the **same deployment ID**, **same description**, and **same settings** (such as **"Anyone"** access) in Google Apps Script using clasp, you **must redeploy or update the existing deployment** instead of creating a new one.

Creating a new deployment (`clasp deploy`) will generate a new Deployment ID, breaking client installations and requiring every user to update their app configuration.

#### Step 4a: Push Code
```bash
clasp push
```

#### Step 4b: List Existing Deployments
```bash
clasp deployments
```

#### Step 4c: Redeploy to the Existing Deployment ID
Use `clasp redeploy <deploymentId> -V <versionNumber> -d "Pookie Backend API"`:

> **Important:** The description is strictly constant across all redeployments: `"Pookie Backend API"`.

```bash
# Strict redeploy command format:
clasp redeploy <deploymentId> -V <versionNumber> -d "Pookie Backend API"
```

**Canonical Redeployment Command:**
```bash
# Redeploy in-place with strict constant description:
clasp redeploy <deploymentId> -V <versionNumber> -d "Pookie Backend API"

# Or automatically using PowerShell reading from local.properties:
$deployId = (Get-Content local.properties | Select-String "POOKIE_DEMO_DEPLOYMENT_ID").ToString().Split("=")[1].Trim()
clasp redeploy $deployId -V <versionNumber> -d "Pookie Backend API"
```
*(Always specify `-V <versionNumber>` and `-d "Pookie Backend API"` to maintain the constant description and keep the deployment ID permanent).*

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
