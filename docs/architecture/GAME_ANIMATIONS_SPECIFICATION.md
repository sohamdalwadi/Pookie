# 🎨 Pookie Game Animations & Particle Physics Specification

**Author:** Soham Dalwadi  
**Role:** Principal Architect & Motion Designer  
**Target Performance:** 60fps / 120fps Native Hardware Acceleration  
**Design Philosophy:** Tactile Delight • Organic Physics • Zero Lag  

---

## 📌 Animation Framework Overview

All animations in Pookie utilize **Spring Physics** (Damped Harmonic Oscillators) rather than linear ease-in/ease-out curves. This gives moves, buttons, and boards a natural, tactile weight.

```
F = -k * x - c * v
(Stiffness k, Damping c, Mass m)
```

---

## 1. Mini-Game Specific Animation Choreography

### 1.1 Tic-Tac-Toe Animations

#### 1. Piece Placement (X and O Vector Draw):
- **Trigger:** Tapping an empty cell.
- **Motion:**
  - **For "X":** Dual diagonal SVG lines animate sequentially using `stroke-dashoffset` from 0% to 100% over 180ms. The mark pops with a subtle spring scale overshoot (`scale: 0.8 -> 1.15 -> 1.0`).
  - **For "O":** The circle draws in a clockwise sweep over 200ms with a soft radial bloom.

#### 2. Winning Strike Line:
- **Trigger:** Win detected (3 in a row).
- **Motion:**
  - An electric neon beam shoots across the 3 winning cells (duration: 320ms).
  - The 3 winning cells pulse with a soft glow while the losing cells gently dim (`opacity: 0.35`).

#### 3. Draw Game Shake:
- **Trigger:** Board fills with no winner.
- **Motion:** The entire 3x3 grid undergoes a soft horizontal vibration shake (5 oscillations over 300ms, damping ratio `0.75`).

---

### 1.2 Dots & Boxes Animations

#### 1. Magnetic Edge Proximity Snap:
- **Trigger:** Finger approaches within 28dp of an unselected horizontal or vertical edge.
- **Motion:**
  - A subtle dashed preview bar springs into view (`opacity: 0.0 -> 0.6`, `scaleY: 0.5 -> 1.0`).
  - Upon release, the edge snaps into a solid neon bar with a gentle click sound and tactile tick.

#### 2. Box Capture Ripple & Territory Claim:
- **Trigger:** Placing the 4th surrounding edge of a 1x1 box.
- **Motion:**
  - **Step 1 (Border Flash):** All 4 edges flash white for 80ms.
  - **Step 2 (Interior Fill Ripple):** An expanding circular wave springs from the center of the box to fill the tile with the player's color tint (Rose-Coral for Host, Electric Violet for Partner).
  - **Step 3 (Initial Stamp):** The capturing player's initial (`H` or `P`) drops in with an elastic bounce (`scale: 0.0 -> 1.2 -> 1.0`).

#### 3. Extra Turn Celebration Burst:
- **Trigger:** Box captured.
- **Motion:** A floating badge reads *"Extra Turn!"* with golden sparkle vectors radiating outward.

---

### 1.3 Couples Icebreaker Quiz Animations

#### 1. 3D Card Flip Perspective Reveal:
- **Trigger:** Both partners submit answers.
- **Motion:**
  - Cards rotate 180° along the Y-axis using 3D perspective (`cameraDistance = 8000dp`).
  - At 90° (halfway), the card face switches from the question text to the revealed answers.
  - Duration: 420ms with spring damping (`stiffness: 280`, `damping: 22`).

#### 2. Soulmate Match Heart Confetti:
- **Trigger:** Host and Partner chose the exact same option.
- **Motion:**
  - Both cards pulse green with a warm golden rim glow.
  - 30 micro-vector hearts explode outward in a radial burst, fluttering downward with gravity and wind drift for 2.5 seconds.
  - Haptic feedback plays a warm confirmation rumble.

#### 3. Banter Disagreement Wobble:
- **Trigger:** Partners chose different options.
- **Motion:**
  - Cards land with a gentle playful wobble (`rotationZ: -4° -> +4° -> 0°`).
  - An interactive discussion bubble pops: *"Time for some fun explaining!"*.

---

### 1.4 Interactive Dice Randomizer (Onboarding & Profile)

- **Trigger:** Tapping the isometric `[🎲]` vector next to the Nickname field.
- **Motion:**
  - The dice elevates (`translateY: -16dp`) and spins rapidly across 3 axes (`rotateX: 720°`, `rotateY: 540°`, `rotateZ: 360°`).
  - Damping slows the tumble over 650ms, landing with a satisfying mechanical click haptic and pop-in of the new cute nickname!

---

## 2. Animation Physics Token Table

| Animation Asset | Duration | Curve / Physics Parameters | Hardware Layer |
| :--- | :---: | :--- | :---: |
| **X / O Mark Draw** | 180ms | `Spring(stiffness: 350, damping: 18)` | GPU Vector Drawable |
| **Strike Win Line** | 320ms | `CubicBezier(0.16, 1, 0.3, 1)` | RenderNode / Canvas Path |
| **Edge Proximity Snap** | 120ms | `Spring(stiffness: 450, damping: 25)` | Android View Animator |
| **Box Capture Ripple** | 300ms | `Spring(stiffness: 220, damping: 14)` | Radial Shader Fill |
| **3D Card Flip** | 420ms | `Spring(stiffness: 280, damping: 22)` | `rotationY` 3D Matrix |
| **Floating Reaction** | 2200ms | `Sine(drift) + EaseOut(rise & fade)` | Particle System Canvas |
| **Dice 3D Tumble** | 650ms | `Spring(stiffness: 180, damping: 12)` | 3D Matrix Transform |
