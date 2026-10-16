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
- **CLI First Automation Rule**: Always execute clasp actions through the CLI automatically:
  1. `clasp status` to verify tracked files.
  2. `clasp push -f` to push latest code to Google Apps Script.
  3. **STRICT RULE: NEVER run `clasp deploy` without `-i`**: Extract `POOKIE_DEMO_DEPLOYMENT_ID` from `local.properties` and update the existing permanent deployment:
     ```powershell
     $permId = (Get-Content "local.properties" | Where-Object { $_ -match "^POOKIE_DEMO_DEPLOYMENT_ID=" } | ForEach-Object { $_.Split('=')[1] }).Trim()
     npx @google/clasp deploy -i $permId -d "<description>"
     ```
     Running `clasp deploy` without `-i` creates a new deployment ID and is STRICTLY FORBIDDEN.
- **Demo Key & Secret Management Rule**:
  1. Secrets and deployment keys must reside **exclusively in git-ignored `local.properties`** (`POOKIE_DEMO_DEPLOYMENT_ID=...`).
  2. `app/build.gradle` automatically encrypts this key at compile time using XOR masking (`0x5C`) and injects it into `BuildConfig.ENCRYPTED_DEMO_KEY_HEX`.
  3. Plaintext deployment keys must NEVER be hardcoded in `build.gradle`, source code, or tracked documentation files.
  4. Automatically recompile (`gradlew assembleDebug`) and reinstall to connected test devices via `adb install -r`.

---

## 🔒 Security & Git Checklist
- **CRITICAL: NEVER COMMIT KEYS & MANDATORY PRE-COMMIT AUDIT**:
  - Plaintext deployment keys, API keys, Google Apps Script deployment IDs (`AKfycb...`), and credentials must NEVER be committed to git.
  - **MANDATORY CHECK BEFORE EVERY COMMIT:** Before executing `git commit`, the agent/developer MUST inspect `git diff --cached` (or run a pattern scan `git diff --cached | Select-String "AKfycb"`) to verify zero keys or credentials are being committed.
  - All secret keys must live exclusively in git-ignored `local.properties`.
- **CRITICAL: CLI-First Automation**: Whenever any development action is possible through CLI (Gradle build, ADB install, clasp push/deploy, test execution), **ALWAYS execute it directly through CLI** instead of asking the user to perform manual steps.
- **CRITICAL: Git Push Control:** The AI Agent is permitted to create local git commits (`git commit`), but **MUST NEVER run `git push`**. All `git push` commands will be executed exclusively by the USER to control CI/CD execution and build triggers.
- **CRITICAL: Zero-Emoji Rule:** Absolutely NO emojis are allowed in UI elements, buttons, titles, dialogues, status messages, or code strings. Emojis are STRICTLY and EXCLUSIVELY permitted inside predefined couple vibe tags (e.g. `🍕 Foodies`, `🎬 Movie Buffs`).
- **Never commit credentials:** `.clasp.json`, `.clasprc.json`, `config.json`, `local.properties`, and `.env` must remain strictly ignored.
- **Always verify git status:** Ensure working tree is clean and only intentional files are staged.
- **Maintain documentation:** Update corresponding docs in `docs/` whenever adding features or altering contracts.
