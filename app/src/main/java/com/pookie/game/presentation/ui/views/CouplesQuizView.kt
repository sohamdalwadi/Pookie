package com.pookie.game.presentation.ui.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.pookie.game.R
import com.pookie.game.domain.engine.CouplesQuizState
import com.pookie.game.domain.engine.QuizQuestion

class CouplesQuizView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val textCategory: TextView
    private val textQuestion: TextView
    private val textStatus: TextView
    private val optionViews: List<TextView>

    private var onOptionSelectedListener: ((Int) -> Unit)? = null
    private var isInteractive: Boolean = true

    init {
        orientation = VERTICAL
        setPadding(32, 24, 32, 24)

        // Category Tag
        textCategory = TextView(context).apply {
            textSize = 13f
            setTextColor(Color.parseColor("#FF4E78"))
            setPadding(0, 0, 0, 16)
        }
        addView(textCategory)

        // Question Text
        textQuestion = TextView(context).apply {
            textSize = 18f
            setTextColor(Color.parseColor("#F5F7FB"))
            setLineSpacing(8f, 1.1f)
            setPadding(0, 0, 0, 32)
        }
        addView(textQuestion)

        // 4 Options
        val optionsList = mutableListOf<TextView>()
        for (i in 0 until 4) {
            val optionBtn = TextView(context).apply {
                textSize = 15f
                setTextColor(Color.parseColor("#F5F7FB"))
                setBackgroundResource(R.drawable.bg_rounded_card)
                setPadding(32, 28, 32, 28)
                val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, 20)
                }
                layoutParams = params

                setOnClickListener {
                    if (isInteractive) {
                        onOptionSelectedListener?.invoke(i)
                    }
                }
            }
            optionsList.add(optionBtn)
            addView(optionBtn)
        }
        optionViews = optionsList

        // Waiting / Reveal status
        textStatus = TextView(context).apply {
            textSize = 14f
            setTextColor(Color.parseColor("#8E9CAE"))
            textAlignment = TEXT_ALIGNMENT_CENTER
            setPadding(0, 16, 0, 16)
        }
        addView(textStatus)
    }

    fun setOnOptionSelectedListener(listener: (Int) -> Unit) {
        onOptionSelectedListener = listener
    }

    fun render(
        quizState: CouplesQuizState,
        myPlayerId: String,
        partnerId: String?,
        interactive: Boolean
    ) {
        isInteractive = interactive
        val question = quizState.currentQuestion ?: return

        textCategory.text = "QUESTION ${quizState.currentQuestionIndex + 1} OF ${quizState.questions.size} • ${question.category}"
        textQuestion.text = question.questionText

        val currentAnswers = quizState.submittedAnswers[quizState.currentQuestionIndex] ?: emptyMap()
        val myAnswer = currentAnswers[myPlayerId]
        val partnerAnswer = partnerId?.let { currentAnswers[it] }

        val isBothAnswered = quizState.isCurrentQuestionRevealed

        for (i in 0 until 4) {
            val optionText = question.options.getOrNull(i) ?: ""
            val btn = optionViews[i]

            when {
                isBothAnswered -> {
                    // Both answered - reveal results
                    val isMyChoice = myAnswer == i
                    val isPartnerChoice = partnerAnswer == i

                    if (isMyChoice && isPartnerChoice) {
                        btn.setBackgroundColor(Color.parseColor("#3300D2A0")) // Matching Mint!
                        btn.text = "$optionText  (Matched!)"
                    } else if (isMyChoice) {
                        btn.setBackgroundColor(Color.parseColor("#33FF4E78")) // Pink
                        btn.text = "$optionText  (You)"
                    } else if (isPartnerChoice) {
                        btn.setBackgroundColor(Color.parseColor("#33FFB800")) // Amber
                        btn.text = "$optionText  (Partner)"
                    } else {
                        btn.setBackgroundResource(R.drawable.bg_rounded_card)
                        btn.text = optionText
                    }
                }
                myAnswer == i -> {
                    // Player has selected this option
                    btn.setBackgroundColor(Color.parseColor("#33FF4E78"))
                    btn.text = "$optionText  (Selected)"
                }
                else -> {
                    btn.setBackgroundResource(R.drawable.bg_rounded_card)
                    btn.text = optionText
                }
            }
        }

        // Status banner
        textStatus.text = when {
            isBothAnswered -> {
                if (myAnswer == partnerAnswer) "Both of you are in sync!" else "A fun contrast in styles!"
            }
            myAnswer != null -> "Waiting for your partner to answer..."
            else -> "Select your honest answer"
        }
    }
}
