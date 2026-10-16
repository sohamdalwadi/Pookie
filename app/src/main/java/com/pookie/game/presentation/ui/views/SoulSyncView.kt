package com.pookie.game.presentation.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.pookie.game.R
import com.pookie.game.domain.model.SoulSyncState

class SoulSyncView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val textSoulSyncRound: TextView
    private val textSoulSyncScore: TextView
    private val textSoulSyncPrompt: TextView

    // Phase 1: Answering
    private val containerAnsweringPhase: LinearLayout
    private val editSoulSyncAnswer: EditText
    private val btnSubmitSoulSyncAnswer: Button
    private val textPartnerAnswerStatus: TextView

    // Phase 2: Judging
    private val containerJudgingPhase: LinearLayout
    private val textHostAnswerLabel: TextView
    private val textHostAnswer: TextView
    private val textPartnerAnswerLabel: TextView
    private val textPartnerAnswer: TextView
    private val btnVerdictMatch: Button
    private val btnVerdictMiss: Button
    private val textVerdictStatus: TextView

    private var onSubmitAnswer: ((String) -> Unit)? = null
    private var onSubmitVerdict: ((String) -> Unit)? = null

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.view_soul_sync, this, true)

        textSoulSyncRound = root.findViewById(R.id.textSoulSyncRound)
        textSoulSyncScore = root.findViewById(R.id.textSoulSyncScore)
        textSoulSyncPrompt = root.findViewById(R.id.textSoulSyncPrompt)

        containerAnsweringPhase = root.findViewById(R.id.containerAnsweringPhase)
        editSoulSyncAnswer = root.findViewById(R.id.editSoulSyncAnswer)
        btnSubmitSoulSyncAnswer = root.findViewById(R.id.btnSubmitSoulSyncAnswer)
        textPartnerAnswerStatus = root.findViewById(R.id.textPartnerAnswerStatus)

        containerJudgingPhase = root.findViewById(R.id.containerJudgingPhase)
        textHostAnswerLabel = root.findViewById(R.id.textHostAnswerLabel)
        textHostAnswer = root.findViewById(R.id.textHostAnswer)
        textPartnerAnswerLabel = root.findViewById(R.id.textPartnerAnswerLabel)
        textPartnerAnswer = root.findViewById(R.id.textPartnerAnswer)
        btnVerdictMatch = root.findViewById(R.id.btnVerdictMatch)
        btnVerdictMiss = root.findViewById(R.id.btnVerdictMiss)
        textVerdictStatus = root.findViewById(R.id.textVerdictStatus)

        btnSubmitSoulSyncAnswer.setOnClickListener {
            val text = editSoulSyncAnswer.text.toString().trim()
            if (text.isNotEmpty()) {
                onSubmitAnswer?.invoke(text)
            }
        }

        btnVerdictMatch.setOnClickListener {
            onSubmitVerdict?.invoke("MATCH")
        }

        btnVerdictMiss.setOnClickListener {
            onSubmitVerdict?.invoke("MISS")
        }
    }

    fun setOnSubmitAnswerListener(listener: (String) -> Unit) {
        onSubmitAnswer = listener
    }

    fun setOnSubmitVerdictListener(listener: (String) -> Unit) {
        onSubmitVerdict = listener
    }

    fun render(
        state: SoulSyncState,
        isHost: Boolean,
        hostName: String,
        partnerName: String
    ) {
        textSoulSyncRound.text = "Round ${state.currentRound} / ${state.totalRounds}"
        textSoulSyncScore.text = "Couple Score: ${state.coupleScore}"
        textSoulSyncPrompt.text = state.promptText

        val isJudging = state.phase.equals("JUDGING", ignoreCase = true) || state.answersRevealed

        if (isJudging) {
            // PHASE 2: Side-by-Side Judging
            containerAnsweringPhase.visibility = View.GONE
            containerJudgingPhase.visibility = View.VISIBLE

            textHostAnswerLabel.text = "$hostName's Answer"
            textHostAnswer.text = state.hostAnswer ?: "(Empty answer)"

            textPartnerAnswerLabel.text = "$partnerName's Answer"
            textPartnerAnswer.text = state.partnerAnswer ?: "(Empty answer)"

            val myVerdict = state.getPlayerVerdict(isHost)
            val isMyVerdictSubmitted = state.isPlayerReadyToJudge(isHost)
            val isPartnerVerdictSubmitted = state.isPlayerReadyToJudge(!isHost)

            if (isMyVerdictSubmitted) {
                btnVerdictMatch.isEnabled = false
                btnVerdictMiss.isEnabled = false
                if (myVerdict == "MATCH") {
                    btnVerdictMatch.alpha = 1.0f
                    btnVerdictMiss.alpha = 0.35f
                } else if (myVerdict == "MISS") {
                    btnVerdictMatch.alpha = 0.35f
                    btnVerdictMiss.alpha = 1.0f
                }
                textVerdictStatus.text = if (isPartnerVerdictSubmitted) {
                    "Both ready! Evaluating score..."
                } else {
                    "Your verdict locked! Waiting for $partnerName..."
                }
            } else {
                btnVerdictMatch.isEnabled = true
                btnVerdictMiss.isEnabled = true
                btnVerdictMatch.alpha = 1.0f
                btnVerdictMiss.alpha = 1.0f
                textVerdictStatus.text = if (isPartnerVerdictSubmitted) {
                    "$partnerName marked their verdict! Your turn to judge."
                } else {
                    "Compare your answers and choose a verdict."
                }
            }
        } else {
            // PHASE 1: Answering
            containerAnsweringPhase.visibility = View.VISIBLE
            containerJudgingPhase.visibility = View.GONE

            val iHaveAnswered = state.hasPlayerAnswered(isHost)
            val partnerHasAnswered = state.hasPlayerAnswered(!isHost)

            if (iHaveAnswered) {
                editSoulSyncAnswer.isEnabled = false
                btnSubmitSoulSyncAnswer.isEnabled = false
                btnSubmitSoulSyncAnswer.alpha = 0.5f
                btnSubmitSoulSyncAnswer.text = "Secret Answer Locked In"
                textPartnerAnswerStatus.text = if (partnerHasAnswered) {
                    "Both answered! Revealing answers..."
                } else {
                    "Waiting for $partnerName to type their answer..."
                }
            } else {
                editSoulSyncAnswer.isEnabled = true
                btnSubmitSoulSyncAnswer.isEnabled = true
                btnSubmitSoulSyncAnswer.alpha = 1.0f
                btnSubmitSoulSyncAnswer.text = "Lock in Secret Answer"
                textPartnerAnswerStatus.text = if (partnerHasAnswered) {
                    "$partnerName locked in an answer! Your turn."
                } else {
                    "$partnerName is typing..."
                }
            }
        }
    }
}
