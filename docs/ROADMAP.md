# 🗺️ Pookie Development Roadmap

**Project:** Pookie (Turn-based Couple's Mini-Game App)  
**Lead Architect:** Soham Dalwadi  

---

## 📍 Phase 1: Planning & Specification (Current)
- [x] Complete Master Technical Architecture Document (`TECHNICAL_SPEC.md`).
- [x] Define Google Sheets transactional database schema (`DATABASE_SCHEMA.md`).
- [x] Define REST API contracts and serialization formats (`API_SPEC.md`).
- [x] Define Finite State Machine and Adaptive Polling Engine (`STATE_MACHINES.md`).
- [x] Specify Launch Mini-Games (Tic-Tac-Toe, Dots & Boxes, Couples Quiz).
- [x] Configure Antigravity skills & self-learning instructions (`.agents/skills/`).
- [x] Initialize public GitHub repository and documentation index.

---

## 📍 Phase 2: Serverless Backend Implementation (Google Apps Script)
- [ ] Create Google Spreadsheet template with `Rooms` and `QuizQuestions` sheets.
- [ ] Implement `doGet` and `doPost` routing logic in Google Apps Script.
- [ ] Implement optimistic locking (`Version` validation).
- [ ] Implement pure-function move validation engines for Tic-Tac-Toe, Dots & Boxes, and Quiz.
- [ ] Implement and verify `cleanupInactiveRooms` background time-driven trigger.
- [ ] Deploy Web App and verify with Postman / cURL test suite.

---

## 📍 Phase 3: Client & Game Engine Core (Anti Gravity)
- [ ] Setup client networking layer with adaptive heartbeat polling & OS lifecycle hooks.
- [ ] Build Lobby & Room Join UI (6-character code generator & QR code sharing).
- [ ] Implement Game View 1: Tic-Tac-Toe with smooth mark animations.
- [ ] Implement Game View 2: Dots & Boxes with line-snapping and box territory indicators.
- [ ] Implement Game View 3: Couples Icebreaker Quiz with simultaneous card flips.
- [ ] Build Rematch negotiation handshake UI and score tracking.

---

## 📍 Phase 4: Polish, Delight & Sound Design
- [ ] Romantic pastel design system (aesthetic gradients, micro-interactions, floating hearts).
- [ ] Confetti celebration effects on win/match.
- [ ] Haptic feedback on move turns and box claims.
- [ ] Offline recovery and reconnect toast notifications.

---

## 📍 Phase 5: Open Source Growth & Community Mini-Games
- [ ] Host community hackathon for new mini-games using Antigravity AI agents.
- [ ] Potential mini-game expansions:
  - *Wordle for Two (Guess your partner's secret 5-letter word)*
  - *Connect Four (Love drops)*
  - *Would You Rather (Couples Edition)*
