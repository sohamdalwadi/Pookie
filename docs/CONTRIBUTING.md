# 🤝 Contributing to Pookie with Antigravity

Welcome! We are thrilled you want to help make **Pookie** the best couple's game platform in the world.

## 🚀 No Android Experience? No Problem!
**Pookie is built with Antigravity AI-first design.**

You do **NOT** need to be an Android developer or a 3D game engine expert to contribute. By using Antigravity with our custom skills and instructions, the AI agent can scaffold, test, and integrate full mini-games, game logic engines, and visual polish automatically.

---

## 🤖 Using Antigravity to Contribute

This repository includes a built-in Antigravity skill: `.agents/skills/pookie-game-dev/SKILL.md`.

### Example Prompts You Can Give to Antigravity:
1. **Add a new mini-game:**
   > *"Using the pookie-game-dev skill, please design and implement 'Connect Four' as a new mini-game in Pookie. Follow our Google Sheets serialization format and add the game spec to docs/games/CONNECT_FOUR.md."*
2. **Add more Icebreaker questions:**
   > *"Please generate 20 new romantic and funny couple questions formatted for our QuizQuestions sheet with 4 choices each."*
3. **Enhance UI animations:**
   > *"Add a heart-burst particle animation when players complete a box in Dots and Boxes."*

---

## 🛠️ Contribution Workflow

1. **Fork the Repository:**
   Fork `https://github.com/sohamdalwadi/Pookie` to your personal GitHub account.
2. **Clone and Open in Antigravity:**
   ```bash
   git clone https://github.com/<your-username>/Pookie.git
   ```
3. **Create a Feature Branch:**
   ```bash
   git checkout -b feature/awesome-new-mini-game
   ```
4. **Build & Verify:**
   - Ensure your game logic follows the deterministic state reducer contract.
   - Run compilation and tests.
5. **Submit a Pull Request:**
   - Link any related issue or design doc.
   - Describe the game rules and serialization structure.

---

## 📜 Code of Conduct & Principles
- Keep game mechanics lighthearted, romantic, and fun for couples.
- Prioritize clean, deterministic code with clear comments.
- Always preserve documentation integrity.
