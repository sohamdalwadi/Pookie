# 💖 Couples Icebreaker Quiz Specification

**Game Type:** `COUPLES_QUIZ`  
**Max Players:** 2 (Host & Partner)  
**Format:** Turn-based / Simultaneous Blind Reveal (5 to 10 Questions)  

---

## 1. Game Flow & Psychology
The Couples Icebreaker Quiz is designed to spark intimate conversation, laughter, and playful debate. Both partners answer identical multiple-choice questions secretly. The answers are revealed simultaneously only after both submit.

---

## 2. JSON State Representation
```json
{
  "currentQuestionIndex": 2,
  "totalQuestions": 5,
  "questionId": "Q003",
  "category": "ROMANTIC",
  "questionText": "What's our favorite shared ritual?",
  "options": {
    "A": "Morning coffee together in silence",
    "B": "Late night drive listening to music",
    "C": "Cooking meals together on weekends",
    "D": "Sending silly reels throughout the day"
  },
  "hostAnswer": "A",
  "partnerAnswer": null,
  "answersRevealed": false,
  "roundMatches": 1,
  "completed": false
}
```

---

## 3. Privacy Masking & Reveal Handshake

1. **Blind Submission:**
   - When Host submits their choice (`"A"`), the server sets `hostAnswer: "A"`.
   - In the `GET_STATE` response sent to the Partner, the field is masked:
     `"hasHostAnswered": true`, but `hostAnswer: null`.
2. **Synchronized Reveal:**
   - When the second partner submits, both answers exist on the server.
   - The server sets `answersRevealed: true`, unmasking both answers and calculating `roundMatches`.
   - The client triggers a celebratory confetti animation if the answers match.
3. **Next Question Transition:**
   - Either partner clicks *"Next Question"*, which increments `currentQuestionIndex` and pulls the next question from the question catalog.

---

## 4. 🤖 AI Dynamic Question Generation (Google Gemini Free Tier)
In addition to the static `QuizQuestions` sheet, Pookie supports **AI-Generated Personalized Questions** powered by Google Gemini (`gemini-1.5-flash` / `gemini-2.5-flash`):
- **Single-Side API Key:** Only **one partner** (the Host) needs to provide a free Google AI Studio API key.
- **Client-Side Generation:** The key is kept strictly on the Host's phone (never sent to Google Sheets or the partner).
- **Custom Couple Vibes:** Host can enter themes like *"Our trip to Japan and silly pet habits"*.
- **Synchronized Play:** The generated questions are written to the room's `GameState`, so both partners play the exact same custom quiz seamlessly.

👉 Read the full [AI Quiz Architecture Specification](AI_QUIZ_ARCHITECTURE.md).
