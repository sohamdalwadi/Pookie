package com.pookie.game.presentation.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.min

class CircularTurnTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val strokeWidthPx = 8f
    private val oval = RectF()

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1C2333")
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00D2A0")
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
    }

    private var progress: Float = 1.0f
    private var isActive: Boolean = false
    private var activeColor: Int = Color.parseColor("#00D2A0")
    private val warningColor: Int = Color.parseColor("#FF4757")

    private var countdownAnimator: ValueAnimator? = null

    fun setTimerColor(color: Int) {
        activeColor = color
        invalidate()
    }

    fun setActive(active: Boolean) {
        isActive = active
        if (!active) {
            cancelCountdown()
            progress = 1.0f
        }
        invalidate()
    }

    fun startCountdown(durationMs: Long = 10000L, onFinish: (() -> Unit)? = null) {
        cancelCountdown()
        isActive = true
        progress = 1.0f
        invalidate()

        countdownAnimator = ValueAnimator.ofFloat(1.0f, 0.0f).apply {
            duration = durationMs
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                progress = animator.animatedValue as Float
                invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (progress <= 0.01f) {
                        onFinish?.invoke()
                    }
                }
            })
        }
        countdownAnimator?.start()
    }

    fun cancelCountdown() {
        countdownAnimator?.cancel()
        countdownAnimator = null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = min(w, h).toFloat()
        val halfStroke = strokeWidthPx / 2f
        oval.set(halfStroke + 4f, halfStroke + 4f, size - halfStroke - 4f, size - halfStroke - 4f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isActive) return

        // Draw background circular track
        canvas.drawOval(oval, trackPaint)

        // Draw decreasing turn border
        if (progress > 0f) {
            progressPaint.color = if (progress < 0.25f) warningColor else activeColor
            val sweepAngle = 360f * progress
            canvas.drawArc(oval, -90f, sweepAngle, false, progressPaint)
        }
    }
}
