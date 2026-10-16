package com.pookie.game.presentation.ui.views

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.pookie.game.R
import com.pookie.game.domain.model.Player
import com.pookie.game.domain.model.ReactionType

class DualPvpHudView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val textPlayer1Name: TextView
    private val textPlayer1Score: TextView
    private val avatar1TimerView: CircularTurnTimerView
    private val avatar1Icon: ImageView
    private val reaction1BelowIcon: ImageView

    private val textPlayer2Name: TextView
    private val textPlayer2Score: TextView
    private val avatar2TimerView: CircularTurnTimerView
    private val avatar2Icon: ImageView
    private val reaction2BelowIcon: ImageView

    private val textTurnStatus: TextView
    private var lastActiveTurnPlayerId: String? = null
    var onTurnTimeout: (() -> Unit)? = null

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.view_dual_pvp_hud, this, true)

        textPlayer1Name = root.findViewById(R.id.textPlayer1Name)
        textPlayer1Score = root.findViewById(R.id.textPlayer1Score)
        avatar1TimerView = root.findViewById(R.id.avatar1TimerView)
        avatar1Icon = root.findViewById(R.id.avatar1Icon)
        reaction1BelowIcon = root.findViewById(R.id.reaction1BelowIcon)

        textPlayer2Name = root.findViewById(R.id.textPlayer2Name)
        textPlayer2Score = root.findViewById(R.id.textPlayer2Score)
        avatar2TimerView = root.findViewById(R.id.avatar2TimerView)
        avatar2Icon = root.findViewById(R.id.avatar2Icon)
        reaction2BelowIcon = root.findViewById(R.id.reaction2BelowIcon)

        textTurnStatus = root.findViewById(R.id.textTurnStatus)

        avatar1TimerView.setTimerColor(Color.parseColor("#FF4E78"))
        avatar2TimerView.setTimerColor(Color.parseColor("#00D2A0"))
    }

    fun updatePlayers(
        player1: Player,
        player2: Player?,
        currentTurnPlayerId: String,
        myPlayerId: String
    ) {
        textPlayer1Name.text = player1.name
        textPlayer1Score.text = player1.score.toString()
        avatar1Icon.setImageResource(
            if (player1.gender == com.pookie.game.domain.model.Gender.FEMALE) R.drawable.ic_avatar_female else R.drawable.ic_avatar_male
        )

        if (player2 != null) {
            textPlayer2Name.text = player2.name
            textPlayer2Score.text = player2.score.toString()
            avatar2Icon.setImageResource(
                if (player2.gender == com.pookie.game.domain.model.Gender.FEMALE) R.drawable.ic_avatar_female else R.drawable.ic_avatar_male
            )
        } else {
            textPlayer2Name.text = "Waiting..."
            textPlayer2Score.text = "-"
            avatar2Icon.setImageResource(R.drawable.ic_avatar_female)
        }

        // Turn Timer Border & Countdown (10 seconds)
        val isPlayer1Turn = currentTurnPlayerId == player1.id
        val isPlayer2Turn = player2 != null && currentTurnPlayerId == player2.id

        if (currentTurnPlayerId != lastActiveTurnPlayerId) {
            lastActiveTurnPlayerId = currentTurnPlayerId
            if (isPlayer1Turn) {
                avatar1TimerView.startCountdown(10000L) {
                    onTurnTimeout?.invoke()
                }
                avatar2TimerView.setActive(false)
            } else if (isPlayer2Turn) {
                avatar2TimerView.startCountdown(10000L) {
                    onTurnTimeout?.invoke()
                }
                avatar1TimerView.setActive(false)
            } else {
                avatar1TimerView.setActive(false)
                avatar2TimerView.setActive(false)
            }
        }

        textTurnStatus.text = when {
            player2 == null -> "Waiting for partner..."
            currentTurnPlayerId == myPlayerId -> "Your Turn"
            else -> "Partner Thinking..."
        }
        textTurnStatus.setTextColor(
            if (currentTurnPlayerId == myPlayerId) Color.parseColor("#00D2A0") else Color.parseColor("#8E9CAE")
        )
    }

    fun triggerReaction(isPlayer1: Boolean, reactionType: ReactionType) {
        val targetBelowIcon = if (isPlayer1) reaction1BelowIcon else reaction2BelowIcon

        val iconRes = when (reactionType) {
            ReactionType.HEART -> R.drawable.ic_reaction_heart
            ReactionType.LIGHTNING -> R.drawable.ic_reaction_lightning
            ReactionType.CROWN -> R.drawable.ic_reaction_crown
            ReactionType.THUMBS_UP -> R.drawable.ic_thumbs_up
            ReactionType.THUMBS_DOWN -> R.drawable.ic_thumbs_down
            ReactionType.NUDGE -> R.drawable.ic_nudge
        }

        // Display reaction directly below profile avatar and float for 5 seconds
        targetBelowIcon.animate().cancel()
        targetBelowIcon.setImageResource(iconRes)
        targetBelowIcon.visibility = View.VISIBLE
        targetBelowIcon.alpha = 1.0f
        targetBelowIcon.scaleX = 0f
        targetBelowIcon.scaleY = 0f
        targetBelowIcon.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(250)
            .withEndAction {
                targetBelowIcon.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .withEndAction {
                        // Float / stay visible for 5000ms (5 seconds), then smoothly fade out
                        targetBelowIcon.animate()
                            .alpha(0f)
                            .setStartDelay(5000L)
                            .setDuration(400)
                            .withEndAction {
                                targetBelowIcon.visibility = View.INVISIBLE
                                targetBelowIcon.alpha = 1.0f
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }
}
