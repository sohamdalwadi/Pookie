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
    private val avatar1Ring: View
    private val reaction1Icon: ImageView

    private val textPlayer2Name: TextView
    private val textPlayer2Score: TextView
    private val avatar2Ring: View
    private val reaction2Icon: ImageView

    private val textTurnStatus: TextView

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.view_dual_pvp_hud, this, true)

        textPlayer1Name = root.findViewById(R.id.textPlayer1Name)
        textPlayer1Score = root.findViewById(R.id.textPlayer1Score)
        avatar1Ring = root.findViewById(R.id.avatar1Ring)
        reaction1Icon = root.findViewById(R.id.reaction1Icon)

        textPlayer2Name = root.findViewById(R.id.textPlayer2Name)
        textPlayer2Score = root.findViewById(R.id.textPlayer2Score)
        avatar2Ring = root.findViewById(R.id.avatar2Ring)
        reaction2Icon = root.findViewById(R.id.reaction2Icon)

        textTurnStatus = root.findViewById(R.id.textTurnStatus)
    }

    fun updatePlayers(
        player1: Player,
        player2: Player?,
        currentTurnPlayerId: String,
        myPlayerId: String
    ) {
        textPlayer1Name.text = player1.name
        textPlayer1Score.text = player1.score.toString()

        if (player2 != null) {
            textPlayer2Name.text = player2.name
            textPlayer2Score.text = player2.score.toString()
        } else {
            textPlayer2Name.text = "Waiting..."
            textPlayer2Score.text = "-"
        }

        // Active turn indicator
        val isPlayer1Turn = currentTurnPlayerId == player1.id
        avatar1Ring.visibility = if (isPlayer1Turn) View.VISIBLE else View.INVISIBLE

        val isPlayer2Turn = player2 != null && currentTurnPlayerId == player2.id
        avatar2Ring.visibility = if (isPlayer2Turn) View.VISIBLE else View.INVISIBLE

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
        val targetIcon = if (isPlayer1) reaction1Icon else reaction2Icon
        val iconRes = when (reactionType) {
            ReactionType.HEART -> R.drawable.ic_reaction_heart
            ReactionType.LIGHTNING -> R.drawable.ic_reaction_lightning
            ReactionType.CROWN -> R.drawable.ic_reaction_crown
        }

        targetIcon.setImageResource(iconRes)
        targetIcon.visibility = View.VISIBLE
        targetIcon.alpha = 1f
        targetIcon.scaleX = 0f
        targetIcon.scaleY = 0f

        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1.4f, 1.0f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1.4f, 1.0f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 1.0f, 0.0f)
        val translationY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -40f, -80f)

        ObjectAnimator.ofPropertyValuesHolder(targetIcon, scaleX, scaleY, alpha, translationY).apply {
            duration = 1400
            interpolator = OvershootInterpolator(1.5f)
            start()
        }
    }
}
