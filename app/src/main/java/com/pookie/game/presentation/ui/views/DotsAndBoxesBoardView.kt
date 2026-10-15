package com.pookie.game.presentation.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.min

class DotsAndBoxesBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F5F7FB")
        style = Paint.Style.FILL
    }

    private val unselectedLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2D3748")
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
    }

    private val hostLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF4E78")
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
    }

    private val partnerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00D2A0")
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
    }

    private val hostBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33FF4E78")
        style = Paint.Style.FILL
    }

    private val partnerBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3300D2A0")
        style = Paint.Style.FILL
    }

    private var hostPlayerId: String = ""
    private var claimedLines: Map<String, String> = emptyMap()
    private var claimedBoxes: Map<String, String> = emptyMap()
    private var isInteractive: Boolean = true
    private var onLineClickListener: ((String) -> Unit)? = null

    private val gridSize = 3 // 3x3 boxes, 4x4 dots
    private var step = 0f
    private var padding = 48f
    private val dotRadius = 10f

    fun setState(
        lines: Map<String, String>,
        boxes: Map<String, String>,
        hostId: String,
        interactive: Boolean = true
    ) {
        claimedLines = lines
        claimedBoxes = boxes
        hostPlayerId = hostId
        isInteractive = interactive
        invalidate()
    }

    fun setOnLineClickListener(listener: (String) -> Unit) {
        onLineClickListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        step = (w - padding * 2) / gridSize.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw Claimed Boxes
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val boxKey = "box_${r}_${c}"
                val owner = claimedBoxes[boxKey]
                if (owner != null) {
                    val left = padding + c * step
                    val top = padding + r * step
                    val right = left + step
                    val bottom = top + step
                    val rect = RectF(left + 6f, top + 6f, right - 6f, bottom - 6f)
                    val paint = if (owner == hostPlayerId) hostBoxPaint else partnerBoxPaint
                    canvas.drawRoundRect(rect, 12f, 12f, paint)
                }
            }
        }

        // 2. Draw Horizontal Lines
        for (r in 0..gridSize) {
            val y = padding + r * step
            for (c in 0 until gridSize) {
                val x1 = padding + c * step
                val x2 = x1 + step
                val lineKey = "H_${r}_${c}"
                val owner = claimedLines[lineKey]
                val paint = when {
                    owner == hostPlayerId -> hostLinePaint
                    owner != null -> partnerLinePaint
                    else -> unselectedLinePaint
                }
                canvas.drawLine(x1, y, x2, y, paint)
            }
        }

        // 3. Draw Vertical Lines
        for (r in 0 until gridSize) {
            val y1 = padding + r * step
            val y2 = y1 + step
            for (c in 0..gridSize) {
                val x = padding + c * step
                val lineKey = "V_${r}_${c}"
                val owner = claimedLines[lineKey]
                val paint = when {
                    owner == hostPlayerId -> hostLinePaint
                    owner != null -> partnerLinePaint
                    else -> unselectedLinePaint
                }
                canvas.drawLine(x, y1, x, y2, paint)
            }
        }

        // 4. Draw Dots (4x4)
        for (r in 0..gridSize) {
            val y = padding + r * step
            for (c in 0..gridSize) {
                val x = padding + c * step
                canvas.drawCircle(x, y, dotRadius, dotPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isInteractive || onLineClickListener == null) return false

        if (event.action == MotionEvent.ACTION_UP) {
            val touchX = event.x
            val touchY = event.y

            val nearestLine = findNearestUnclaimedLine(touchX, touchY)
            if (nearestLine != null) {
                performClick()
                onLineClickListener?.invoke(nearestLine)
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun findNearestUnclaimedLine(tx: Float, ty: Float): String? {
        val threshold = step * 0.45f
        var bestLine: String? = null
        var minDistance = Float.MAX_VALUE

        // Check horizontal lines
        for (r in 0..gridSize) {
            val y = padding + r * step
            for (c in 0 until gridSize) {
                val x1 = padding + c * step
                val x2 = x1 + step
                val lineKey = "H_${r}_${c}"

                if (!claimedLines.containsKey(lineKey)) {
                    if (tx in (x1 - dotRadius)..(x2 + dotRadius)) {
                        val d = abs(ty - y)
                        if (d < threshold && d < minDistance) {
                            minDistance = d
                            bestLine = lineKey
                        }
                    }
                }
            }
        }

        // Check vertical lines
        for (r in 0 until gridSize) {
            val y1 = padding + r * step
            val y2 = y1 + step
            for (c in 0..gridSize) {
                val x = padding + c * step
                val lineKey = "V_${r}_${c}"

                if (!claimedLines.containsKey(lineKey)) {
                    if (ty in (y1 - dotRadius)..(y2 + dotRadius)) {
                        val d = abs(tx - x)
                        if (d < threshold && d < minDistance) {
                            minDistance = d
                            bestLine = lineKey
                        }
                    }
                }
            }
        }

        return bestLine
    }
}
