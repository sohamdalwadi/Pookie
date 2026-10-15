package com.pookie.game.domain.engine

data class QuizQuestion(
    val id: String,
    val category: String,
    val questionText: String,
    val options: List<String>
)

data class CouplesQuizState(
    val questions: List<QuizQuestion> = DEFAULT_QUESTIONS,
    val currentQuestionIndex: Int = 0,
    // Map of questionIndex to Map of playerId to optionIndex (0..3)
    val submittedAnswers: Map<Int, Map<String, Int>> = emptyMap(),
    val matchScore: Int = 0
) {
    companion object {
        val DEFAULT_QUESTIONS = listOf(
            QuizQuestion(
                id = "Q1",
                category = "WEEKEND",
                questionText = "What is our ideal weekend getaway?",
                options = listOf(
                    "Cozy mountain cabin",
                    "Sunny beach resort",
                    "Spontaneous road trip",
                    "Staying in bed binging shows"
                )
            ),
            QuizQuestion(
                id = "Q2",
                category = "FOOD",
                questionText = "It is 11 PM on Sunday. Who initiates late-night food?",
                options = listOf(
                    "Definitely me",
                    "100% them",
                    "We both suggested it together",
                    "Neither, we raid the kitchen"
                )
            ),
            QuizQuestion(
                id = "Q3",
                category = "QUIRKS",
                questionText = "Who is more likely to lose their keys while holding them?",
                options = listOf(
                    "Me, absolutely",
                    "Without doubt them",
                    "Both of us daily",
                    "Neither, we are super organized"
                )
            ),
            QuizQuestion(
                id = "Q4",
                category = "ROMANCE",
                questionText = "Who made the very first romantic move?",
                options = listOf(
                    "Me, boldly",
                    "Them, smoothly",
                    "It was completely mutual",
                    "Destiny took over"
                )
            ),
            QuizQuestion(
                id = "Q5",
                category = "PETS",
                questionText = "Who uses the silliest baby voice with cute pets?",
                options = listOf(
                    "Me without shame",
                    "Them completely",
                    "Both of us in harmony",
                    "We pretend we do not, but we do"
                )
            )
        )
    }

    val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentQuestionIndex)
    fun isQuestionRevealed(questionIndex: Int): Boolean =
        (submittedAnswers[questionIndex]?.size ?: 0) >= 2

    val isCurrentQuestionRevealed: Boolean get() =
        isQuestionRevealed(currentQuestionIndex)

    fun hasPlayerAnswered(playerId: String): Boolean =
        submittedAnswers[currentQuestionIndex]?.containsKey(playerId) == true

    fun getHarmonyRating(): String = when (matchScore) {
        5 -> "Twin Flames (100% Harmony)"
        4 -> "Sweet Sync (80% Harmony)"
        3 -> "Dynamic Duo (60% Harmony)"
        2 -> "Opposites Attract (40% Harmony)"
        else -> "Unpredictable Chemistry (20% Harmony)"
    }
}

data class QuizAnswerMove(
    val questionIndex: Int,
    val selectedOptionIndex: Int
)

class CouplesQuizEngine : GameEngine<CouplesQuizState, QuizAnswerMove> {

    override fun createInitialState(hostId: String, partnerId: String?): CouplesQuizState {
        return CouplesQuizState()
    }

    override fun isValidMove(state: CouplesQuizState, move: QuizAnswerMove, playerId: String): Boolean {
        if (move.questionIndex != state.currentQuestionIndex) return false
        if (move.selectedOptionIndex !in 0..3) return false
        return !state.hasPlayerAnswered(playerId)
    }

    override fun applyMove(
        state: CouplesQuizState,
        move: QuizAnswerMove,
        playerId: String,
        opponentId: String
    ): MoveResult<CouplesQuizState> {
        val currentAnswers = (state.submittedAnswers[move.questionIndex] ?: emptyMap()).toMutableMap()
        currentAnswers[playerId] = move.selectedOptionIndex

        val newSubmittedAnswers = state.submittedAnswers.toMutableMap()
        newSubmittedAnswers[move.questionIndex] = currentAnswers

        val isBothAnswered = currentAnswers.size >= 2
        var newScore = state.matchScore

        if (isBothAnswered) {
            val player1Answer = currentAnswers[playerId]
            val opponentAnswer = currentAnswers[opponentId]
            if (player1Answer != null && player1Answer == opponentAnswer) {
                newScore++
            }
        }

        val isLastQuestion = state.currentQuestionIndex >= state.questions.size - 1
        val isGameOver = isLastQuestion && isBothAnswered

        val nextQuestionIdx = if (isBothAnswered && !isLastQuestion) {
            state.currentQuestionIndex + 1
        } else {
            state.currentQuestionIndex
        }

        val nextState = state.copy(
            currentQuestionIndex = nextQuestionIdx,
            submittedAnswers = newSubmittedAnswers,
            matchScore = newScore
        )

        return MoveResult(
            nextState = nextState,
            nextTurnPlayerId = opponentId,
            isGameOver = isGameOver,
            winnerId = if (isGameOver) (if (newScore >= 3) playerId else null) else null,
            isDraw = false
        )
    }
}
