# Pookie Guidelines for Antigravity AI Agents & Developers

Welcome to the **Pookie** project! Pookie is an open-source, turn-based couple's mini-game platform designed to bring couples closer through delightful interactive games.

---

## 🎯 Architecture Pillars
1. **Lightweight Serverless Backend**: Powered by Google Sheets + Google Apps Script acting as a deterministic REST API.
2. **Modular Game Engine Architecture**: Every mini-game is isolated into pure reducer contracts (`(state, move, player) => nextState`).
3. **Anti Gravity AI-First**: Anyone can contribute using Antigravity even without prior Android or GameDev experience.
4. **Zero-PII Privacy**: No personal data (real names, locations, secrets) ever leaks to LLMs or remote databases.

---

## 🛠️ Build, Environment & Execution Rules

### 1. Java / JDK Configuration (Android Client)
On Windows, the authoritative Android Studio Java Development Kit (JBR) must always be used:
- **Authoritative `JAVA_HOME` Path:** `C:\Program Files\Android\Android Studio\jbr`
- **Kotlin Compile Check:**
  ```powershell
  $env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; .\gradlew compileDebugKotlin
  ```
- **Assemble Debug APK:**
  ```powershell
  $env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; .\gradlew assembleDebug
  ```

### 2. Common Android Compilation Errors & Fixes
- **`Unresolved reference: Context`**: Missing context import in activities/services.
  *Fix:* Add `import android.content.Context` to the top of the file.
- **Missing SharedPreferences / Package Resolution**: Ensure proper package declaration matching the manifest.

### 3. Backend Sync Rules (Google Apps Script / Clasp)
Whenever changes are made to `backend/src/Code.js` or `backend/src/appsscript.json`:
- **Verify status:** `clasp status`
- **Push to Google Apps Script:** `clasp push -f`
- **Deploy new release:** `clasp deploy --description "Descriptive message"`

---

## 🔒 Security & Git Checklist
- **Never commit credentials:** `.clasp.json`, `.clasprc.json`, `config.json`, and `.env` must remain strictly ignored.
- **Always verify git status:** Ensure working tree is clean and only intentional files are staged.
- **Maintain documentation:** Update corresponding docs in `docs/` whenever adding features or altering contracts.
