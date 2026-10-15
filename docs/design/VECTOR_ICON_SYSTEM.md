# 📐 Pookie Vector Icon System (Simple, Modern & Emoji-Free)

**Author:** Soham Dalwadi  
**Role:** Principal Architect & Lead UI Designer  
**Standard:** 24x24 ViewBox • 2.0px Stroke Width • Round LineCaps & LineJoins • Zero-Emoji Policy  

---

## 📌 Design Philosophy

Every icon in Pookie adheres to a strict **Minimalist Vector Design Language** inspired by modern Telegram Mini Apps (EdChess). 
- **Simplicity:** Clean geometric primitives (circles, rounded rectangles, single-stroke arcs).
- **Zero Cheesy Emojis:** The app strictly uses clean monochrome/duotone SVG vector icons everywhere.
- **Consistency:** All icons share identical metrics (`viewBox="0 0 24 24"`, `stroke="currentColor"`, `stroke-width="2"`, `stroke-linecap="round"`, `stroke-linejoin="round"`).
- **Android Ready:** Compatible directly with Android `VectorDrawable` XML and Web SVG.

---

## 🎨 Master Icon Catalog

### 1. 🎲 Nickname Dice Randomizer (`ic_dice.svg`)
*Used in the Onboarding screen next to the Nickname field to roll random cute couple names.*

```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <rect x="3" y="3" width="18" height="18" rx="4" ry="4"/>
  <circle cx="8.5" cy="8.5" r="1.25" fill="currentColor"/>
  <circle cx="15.5" cy="8.5" r="1.25" fill="currentColor"/>
  <circle cx="12" cy="12" r="1.25" fill="currentColor"/>
  <circle cx="8.5" cy="15.5" r="1.25" fill="currentColor"/>
  <circle cx="15.5" cy="15.5" r="1.25" fill="currentColor"/>
</svg>
```

---

### 2. ❌⭕ Tic-Tac-Toe Game Icon (`ic_game_tictactoe.svg`)
*Used in the Game Hub selector card for Tic-Tac-Toe.*

```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <line x1="8.5" y1="3" x2="8.5" y2="21"/>
  <line x1="15.5" y1="3" x2="15.5" y2="21"/>
  <line x1="3" y1="8.5" x2="21" y2="8.5"/>
  <line x1="3" y1="15.5" x2="21" y2="15.5"/>
  <!-- Subtle minimal X mark in center -->
  <line x1="10.5" y1="10.5" x2="13.5" y2="13.5"/>
  <line x1="13.5" y1="10.5" x2="10.5" y2="13.5"/>
</svg>
```

---

### 3. 🟦 Dots & Boxes Game Icon (`ic_game_dots_boxes.svg`)
*Used in the Game Hub selector card for Dots & Boxes.*

```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <circle cx="6" cy="6" r="2" fill="currentColor"/>
  <circle cx="18" cy="6" r="2" fill="currentColor"/>
  <circle cx="6" cy="18" r="2" fill="currentColor"/>
  <circle cx="18" cy="18" r="2" fill="currentColor"/>
  <!-- Connecting line showing game mechanic -->
  <line x1="8" y1="6" x2="16" y2="6"/>
  <line x1="6" y1="8" x2="6" y2="16"/>
  <line x1="18" y1="8" x2="18" y2="16"/>
</svg>
```

---

### 4. 💖 Couples Quiz Icon (`ic_game_quiz.svg`)
*Dual speech bubbles intersecting to form a subtle heart outline.*

```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
  <!-- Interior heart spark -->
  <path d="M12 9.5c-.8-1-2.2-.8-2.6.2-.4 1 .8 2 2.6 3.3 1.8-1.3 3-2.3 2.6-3.3-.4-1-1.8-1.2-2.6-.2z" fill="currentColor"/>
</svg>
```

---

### 5. ➕ Create Room Icon (`ic_create_room.svg`)
*Clean plus action button inside a rounded circle.*

```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <circle cx="12" cy="12" r="9"/>
  <line x1="12" y1="8" x2="12" y2="16"/>
  <line x1="8" y1="12" x2="16" y2="12"/>
</svg>
```

---

### 6. 🚪 Join Room Icon (`ic_join_room.svg`)
*Entrance door with an arrow indicating join entry.*

```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
  <polyline points="10 17 15 12 10 7"/>
  <line x1="15" y1="12" x2="3" y2="12"/>
</svg>
```

---

### 7. 📋 Copy Code Icon (`ic_copy.svg`)
*Clean double-layer card for copying the 6-character room code.*

```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
</svg>
```

---

### 8. 📷 QR Code Share & Scan Icon (`ic_qr_code.svg`)
*Minimalist QR viewport for instant server / room pairing.*

```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <rect x="3" y="3" width="7" height="7" rx="1"/>
  <rect x="14" y="3" width="7" height="7" rx="1"/>
  <rect x="3" y="14" width="7" height="7" rx="1"/>
  <rect x="14" y="14" width="3" height="3" fill="currentColor"/>
  <line x1="21" y1="14" x2="21" y2="14.01"/>
  <line x1="21" y1="21" x2="17" y2="21"/>
</svg>
```

---

### 9. 🔄 Rematch & Refresh Icon (`ic_rematch.svg`)
*Smooth dual circular reload arrows for rematch requests.*

```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <polyline points="23 4 23 10 17 10"/>
  <polyline points="1 20 1 14 7 14"/>
  <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
</svg>
```

---

### 10. In-Game Live Reaction Vectors (Clean Floating HUD)

#### A. Heart Burst Vector (`ic_reaction_heart.svg`):
```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
</svg>
```

#### B. Thunderbolt Tease Vector (`ic_reaction_lightning.svg`):
```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
</svg>
```

#### C. Royal Crown Flex Vector (`ic_reaction_crown.svg`):
```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <path d="M2 4l3 12h14l3-12-6 7-4-7-4 7-6-7z"/>
  <circle cx="12" cy="4" r="1" fill="currentColor"/>
</svg>
```

#### D. GitHub OTA Update Cloud Vector (`ic_ota_update.svg`):
```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <path d="M20 16.2A4.5 4.5 0 0 0 17.5 8h-1.8A7 7 0 1 0 4 14.9"/>
  <polyline points="8 17 12 21 16 17"/>
  <line x1="12" y1="12" x2="12" y2="21"/>
</svg>
```

---

### 11. Essential In-Game System Icons

#### A. Bedtime Sound / Haptics Toggle (`ic_sound_on.svg` & `ic_sound_off.svg`):
*Essential when couples play together late at night in bed!*
```xml
<!-- Sound On -->
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"/>
  <path d="M15.54 8.46a5 5 0 0 1 0 7.07"/>
  <path d="M19.07 4.93a10 10 0 0 1 0 14.14"/>
</svg>

<!-- Sound Off -->
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"/>
  <line x1="23" y1="9" x2="17" y2="15"/>
  <line x1="17" y1="9" x2="23" y2="15"/>
</svg>
```

#### B. Trophy & Match Winner (`ic_trophy.svg`):
```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <path d="M6 9H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h2"/>
  <path d="M18 9h2a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2h-2"/>
  <path d="M4 4h16v5a8 8 0 0 1-16 0V4z"/>
  <line x1="12" y1="17" x2="12" y2="21"/>
  <line x1="8" y1="21" x2="16" y2="21"/>
</svg>
```

#### C. Leave / Exit Room (`ic_leave.svg`):
```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
  <polyline points="16 17 21 12 16 7"/>
  <line x1="21" y1="12" x2="9" y2="12"/>
</svg>
```

#### D. Offline & Reconnection Status (`ic_offline.svg`):
```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <line x1="1" y1="1" x2="23" y2="23"/>
  <path d="M16.72 11.06A10.94 10.94 0 0 1 19 12.55"/>
  <path d="M5 12.55a10.94 10.94 0 0 1 5.17-2.39"/>
  <path d="M10.71 5.05A16 16 0 0 1 22.58 9"/>
  <path d="M1.42 9a15.91 15.91 0 0 1 4.7-2.88"/>
  <path d="M8.53 16.11a6 6 0 0 1 6.95 0"/>
  <line x1="12" y1="20" x2="12.01" y2="20"/>
</svg>
```

---

## 🌟 Interactive States (Active / Pressed / Glow)

To achieve the tactile richness of Telegram Mini Apps:
1. **Default State:** Stroke with `opacity: 0.8`, unfilled background.
2. **Pressed / Touch-Down:** Micro-scale `scale: 0.94`, spring damping, stroke changes to `#FFFFFF`.
3. **Active / Selected State (Duotone Glow):**
   - Background fills with 12% opacity tint of brand color (`fill: rgba(255, 78, 120, 0.12)`).
   - Outer glow halo: `filter: drop-shadow(0 0 6px rgba(255, 78, 120, 0.4))`.

## 🛠️ Android VectorDrawable Compatibility

For Android development, every SVG above maps directly to an Android `VectorDrawable` XML:

```xml
<!-- Example: res/drawable/ic_dice.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
  <path
      android:strokeColor="#FFFF4E78"
      android:strokeWidth="2"
      android:strokeLineCap="round"
      android:strokeLineJoin="round"
      android:pathData="M7,3 L17,3 A4,4 0 0,1 21,7 L21,17 A4,4 0 0,1 17,21 L7,21 A4,4 0 0,1 3,17 L3,7 A4,4 0 0,1 7,3 Z"/>
  <path
      android:fillColor="#FFFF4E78"
      android:pathData="M8.5,8.5m-1.25,0a1.25,1.25 0,1 1 2.5,0a1.25,1.25 0,1 1 -2.5,0"/>
  <path
      android:fillColor="#FFFF4E78"
      android:pathData="M15.5,8.5m-1.25,0a1.25,1.25 0,1 1 2.5,0a1.25,1.25 0,1 1 -2.5,0"/>
  <path
      android:fillColor="#FFFF4E78"
      android:pathData="M12,12m-1.25,0a1.25,1.25 0,1 1 2.5,0a1.25,1.25 0,1 1 -2.5,0"/>
  <path
      android:fillColor="#FFFF4E78"
      android:pathData="M8.5,15.5m-1.25,0a1.25,1.25 0,1 1 2.5,0a1.25,1.25 0,1 1 -2.5,0"/>
  <path
      android:fillColor="#FFFF4E78"
      android:pathData="M15.5,15.5m-1.25,0a1.25,1.25 0,1 1 2.5,0a1.25,1.25 0,1 1 -2.5,0"/>
</vector>
```

This ensures crisp rendering on all DPI screens (MDPI to XXXHDPI) with zero pixelation or image asset bloat.
