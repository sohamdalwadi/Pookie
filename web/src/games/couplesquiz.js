/**
 * 💖 Couples Quiz Component
 */
import { sounds } from '../utils/sound.js';
import confetti from 'canvas-confetti';

export const DEFAULT_QUIZ_QUESTIONS = [
  {
    questionId: 'Q001',
    category: 'ROMANTIC',
    questionText: 'What is our ideal weekend getaway together?',
    optionA: 'Cozy mountain cabin & hot cocoa',
    optionB: 'Beach resort with sunset cocktails',
    optionC: 'Spontaneous road trip without GPS',
    optionD: 'Staying in bed binge-watching shows'
  },
  {
    questionId: 'Q002',
    category: 'FOODIES',
    questionText: "It's 11 PM on Sunday. Who convinces the other to order late-night food?",
    optionA: 'Definitely me',
    optionB: '100% them',
    optionC: 'We suggest it at the exact same second',
    optionD: 'Neither, we raid the kitchen fridge'
  },
  {
    questionId: 'Q003',
    category: 'CHAOTIC',
    questionText: 'Who is more likely to lose their keys or phone while looking directly at them?',
    optionA: 'Me, without a doubt',
    optionB: 'Them, every single week',
    optionC: 'Both of us equally',
    optionD: 'Neither, we are super organized'
  },
  {
    questionId: 'Q004',
    category: 'DEEP',
    questionText: 'What is the absolute best part of our relationship?',
    optionA: 'How hard we laugh at silly things',
    optionB: 'The quiet comfort when doing nothing together',
    optionC: 'How supportive we are of each other’s dreams',
    optionD: 'Our delicious food adventures'
  },
  {
    questionId: 'Q005',
    category: 'ROMANTIC',
    questionText: 'If we could teleport anywhere right now for dinner, where would we go?',
    optionA: 'Rooftop cafe in Paris',
    optionB: 'Street-food night market in Tokyo',
    optionC: 'Seaside cliff in Santorini',
    optionD: 'Our favorite local comfort diner'
  }
];

export function renderCouplesQuiz({ room, playerId, onMove, onNextQuestion }) {
  const container = document.createElement('div');
  container.className = 'anim-entrance quiz-container';

  const isHost = playerId === room.host?.id;
  const playerRole = isHost ? 'HOST' : 'PARTNER';

  const gameState = room.gameState || {};
  const questions = (gameState.questions && gameState.questions.length > 0)
    ? gameState.questions
    : DEFAULT_QUIZ_QUESTIONS;

  const currentIdx = gameState.currentQuestionIndex || 0;
  const currentQ = questions[currentIdx] || DEFAULT_QUIZ_QUESTIONS[0];

  const myAnswer = isHost ? gameState.hostAnswer : gameState.partnerAnswer;
  const partnerAnswered = isHost ? gameState.hasPartnerAnswered : gameState.hasHostAnswered;
  const answersRevealed = Boolean(gameState.answersRevealed);

  // Card header
  const card = document.createElement('div');
  card.className = 'quiz-card';

  const catTag = document.createElement('div');
  catTag.className = 'quiz-category-tag';
  catTag.textContent = `${currentQ.category || 'COUPLE'} • QUESTION ${currentIdx + 1} OF ${questions.length}`;
  card.appendChild(catTag);

  const promptTitle = document.createElement('h3');
  promptTitle.className = 'quiz-prompt-text';
  promptTitle.textContent = currentQ.questionText;
  card.appendChild(promptTitle);

  container.appendChild(card);

  // Status / Reveal banner
  if (answersRevealed) {
    const isMatch = gameState.hostAnswer === gameState.partnerAnswer;
    const revealBox = document.createElement('div');
    revealBox.className = 'anim-pop';
    revealBox.style.marginBottom = '1.5rem';
    revealBox.style.padding = '1.25rem';
    revealBox.style.borderRadius = 'var(--radius-md)';
    revealBox.style.textAlign = 'center';

    if (isMatch) {
      revealBox.style.background = 'rgba(16, 185, 129, 0.2)';
      revealBox.style.border = '1px solid var(--accent-emerald)';
      revealBox.innerHTML = `
        <div style="font-size: 1.8rem; margin-bottom: 0.35rem;">💖 SOUL MATCH! 💖</div>
        <div style="color: #6ee7b7; font-weight: 700;">You both chose Option ${gameState.hostAnswer}!</div>
      `;
      try {
        confetti({ particleCount: 50, spread: 60, origin: { y: 0.7 } });
      } catch (e) {}
    } else {
      revealBox.style.background = 'rgba(244, 63, 94, 0.15)';
      revealBox.style.border = '1px solid var(--primary)';
      revealBox.innerHTML = `
        <div style="font-size: 1.5rem; margin-bottom: 0.35rem;">Opposites Attract! 🥰</div>
        <div style="color: var(--text-muted); font-size: 0.95rem;">
          ${room.host.name}: <b>Option ${gameState.hostAnswer}</b> vs ${room.partner?.name || 'Partner'}: <b>Option ${gameState.partnerAnswer}</b>
        </div>
      `;
    }
    container.appendChild(revealBox);
  } else if (myAnswer) {
    const waitingBox = document.createElement('div');
    waitingBox.className = 'turn-pill partner-turn';
    waitingBox.style.display = 'block';
    waitingBox.style.textAlign = 'center';
    waitingBox.textContent = partnerAnswered
      ? 'Syncing partner answer...'
      : 'Waiting for your partner to pick their secret answer... 🤫';
    container.appendChild(waitingBox);
  } else {
    const pickBox = document.createElement('div');
    pickBox.className = 'turn-pill my-turn anim-turn';
    pickBox.style.display = 'block';
    pickBox.style.textAlign = 'center';
    pickBox.textContent = 'Pick your honest answer below! 💖';
    container.appendChild(pickBox);
  }

  // Options List
  const optionsList = document.createElement('div');
  optionsList.className = 'quiz-options-list';

  const options = [
    { key: 'A', text: currentQ.optionA },
    { key: 'B', text: currentQ.optionB },
    { key: 'C', text: currentQ.optionC },
    { key: 'D', text: currentQ.optionD }
  ];

  options.forEach((opt) => {
    const btn = document.createElement('button');
    btn.className = 'quiz-option-btn';

    if (myAnswer === opt.key) {
      btn.classList.add('selected');
    }

    if (answersRevealed) {
      btn.disabled = true;
    } else if (myAnswer) {
      btn.disabled = true;
    } else {
      btn.addEventListener('click', () => {
        sounds.playTap();

        // ⚡ Optimistic UI Update (0ms instant selection response)
        optionsList.querySelectorAll('.quiz-option-btn').forEach((b) => {
          b.disabled = true;
          b.classList.remove('selected');
        });
        btn.classList.add('selected');

        const banner = container.querySelector('.turn-pill');
        if (banner) {
          banner.className = 'turn-pill partner-turn';
          banner.textContent = 'Waiting for your partner to pick their secret answer... 🤫';
        }

        onMove({ answer: opt.key });
      });
    }


    btn.innerHTML = `
      <div class="quiz-option-badge">${opt.key}</div>
      <div style="flex: 1; font-weight: 600;">${opt.text}</div>
    `;

    optionsList.appendChild(btn);
  });

  container.appendChild(optionsList);

  // Next Question Button (if revealed and questions remain)
  if (answersRevealed && currentIdx < questions.length - 1) {
    const nextRow = document.createElement('div');
    nextRow.style.textAlign = 'center';
    nextRow.style.marginTop = '1.5rem';

    const nextBtn = document.createElement('button');
    nextBtn.className = 'btn-primary';
    nextBtn.textContent = 'Next Question ➔';
    nextBtn.addEventListener('click', () => {
      sounds.playTap();
      if (onNextQuestion) {
        onNextQuestion(currentIdx + 1);
      }
    });

    nextRow.appendChild(nextBtn);
    container.appendChild(nextRow);
  }

  return container;
}
