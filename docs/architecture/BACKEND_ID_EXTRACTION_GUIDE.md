# 🔑 How to Find Your Deployment ID & Web App URL

This guide walks you step-by-step through finding your **Deployment ID** or **Web App URL** to connect the Pookie app.

---

## 📍 Where Does the ID Come From?

When you deploy your Google Apps Script backend, Google generates a unique alphanumeric string called the **Deployment ID**:
```
AKfycb0123456789_EXAMPLE_DEPLOYMENT_ID_POOKIE_xyz
```
And its corresponding **Web App Executable URL**:
```
https://script.google.com/macros/s/AKfycb0123456789_EXAMPLE_DEPLOYMENT_ID_POOKIE_xyz/exec
```

> **Note:** In the Pookie app, you can paste **EITHER** the short Deployment ID or the full Web App URL. The app automatically detects and formats it!

---

## Method 1: Using the Google Apps Script Web Editor (Browser)

1. Open your Google Sheet in your browser.
2. In the top menu bar, click **Extensions** > **Apps Script**.
3. In the top-right corner of the Apps Script editor, click the blue **Deploy** button.
4. Select **Manage deployments**.

```
┌────────────────────────────────────────────────────────┐
│  Manage deployments                                    │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Active deployments                                    │
│  ● Pookie REST API v1                                  │
│                                                        │
│  Deployment ID:                                        │
│  [ AKfycb0123456789_EXAMPLE_ID...      ] [Copy] ◄────── Copy this!
│                                                        │
│  Web app URL:                                          │
│  [ https://script.google.com/.../exec  ] [Copy] ◄────── Or copy this!
│                                                        │
│  Who has access: Anyone                                │
│                                                        │
│                                          [ Close ]     │
└────────────────────────────────────────────────────────┘
```

5. Click **Copy** next to either **Deployment ID** or **Web app URL**.
6. Paste it into the Pookie app on your phone!

---

## Method 2: Using the Clasp CLI (Terminal)

If you use the command-line workflow:

1. Run the deployments command in your terminal:
   ```bash
   clasp deployments
   ```
2. You will see output similar to this:
   ```text
   Found 2 deployments.
   - AKfycb_HEAD_STAGING_DEPLOYMENT_ID_HERE @HEAD 
   - AKfycb0123456789_EXAMPLE_DEPLOYMENT_ID_POOKIE_xyz @1 - Pookie REST API v1
   ```
3. Copy the string starting with `AKfycb...` next to your version `@1` (e.g. `AKfycb0123456789_EXAMPLE_DEPLOYMENT_ID_POOKIE_xyz`).
4. Enter this ID into the app!

---

## 📲 How to Connect on the App

```
┌──────────────────────────────────────────────┐
│           💖 Connect Your Server             │
├──────────────────────────────────────────────┤
│  Paste your Deployment ID or URL below:      │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │ AKfycb0123456789_EXAMPLE_ID...         │  │ ◄── Paste here
│  └────────────────────────────────────────┘  │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │         CONNECT & PLAY  [→]            │  │ ◄── Tap to verify
│  └────────────────────────────────────────┘  │
│                                              │
│                  - OR -                      │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │   [QR Vector] Scan Partner's QR Code   │  │ ◄── Instant Zero-Typing
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
```
1. Paste the ID or URL.
2. Tap **CONNECT & PLAY**.
3. Pookie tests the endpoint (`GET ?action=HEALTH_CHECK`).
4. Once verified with a green checkmark, the app unlocks the game lobby forever!
