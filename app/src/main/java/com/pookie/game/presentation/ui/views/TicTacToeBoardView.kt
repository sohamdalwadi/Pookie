package com.pookie.game.presentation.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import kotlin.math.min

class TicTacToeBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2D3748")
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
    }

    private val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF4E78")
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }

    private val oPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00D2A0")
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }

    private val winLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFB800")
        strokeWidth = 14f
        strokeCap = Paint.Cap.ROUND
    }

    private var board: List<String?> = List(9) { null }
    private var winningLine: List<Int>? = null
    private var onCellClickListener: ((Int) -> Unit)? = null
    private var isInteractive: Boolean = true

    private var cellSize = 0f
    private var padding = 24f

    fun setBoard(newBoard: List<String?>, winLine: List<Int>? = null, interactive: Boolean = true) {
        board = newBoard
        winningLine = winLine
        isInteractive = interactive
        invalidate()
    }

    fun setOnCellClickListener(listener: (Int) -> Unit) {
        onCellClickListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = (w - padding * 2) / 3f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw 3x3 Grid Lines
        val startX = padding
        val startY = padding
        val endX = width - padding
        val endY = height - padding

        // Vertical lines
        canvas.drawLine(startX + cellSize, startY, startX + cellSize, endY, gridPaint)
        canvas.drawLine(startX + cellSize * 2, startY, startX + cellSize * 2, endY, gridPaint)

        // Horizontal lines
        canvas.drawLine(startX, startY + cellSize, endX, startY + cellSize, gridPaint)
        canvas.drawLine(startX, startY + cellSize * 2, endX, startY + cellSize * 2, gridPaint)

        // Draw Marks
        for (i in 0 until 9) {
            val mark = board[i] ?: continue
            val row = i / 3
            val col = i % 3

            val cellLeft = startX + col * cellSize
            val cellTop = startY + row * cellSize
            val cellRight = cellLeft + cellSize
            val cellBottom = cellTop + cellSize
            val inset = cellSize * 0.22f

            if (mark.equals("X", ignoreCase = true)) {
                // Draw X
                canvas.drawLine(cellLeft + inset, cellTop + inset, cellRight - inset, cellBottom - inset, xPaint)
                canvas.drawLine(cellRight - inset, cellTop + inset, cellLeft + inset, cellBottom - inset, xPaint)
            } else {
                // Draw O
                val centerX = (cellLeft + cellRight) / 2f
                val centerY = (cellTop + cellBottom) / 2f
                val radius = (cellSize / 2f) - inset
                canvas.drawCircle(centerX, centerY, radius, oPaint)
            }
        }

        // Draw Winning Line if any
        winningLine?.let { line ->
            if (line.size == 3) {
                val startRow = line[0] / 3
                val startCol = line[0] % 3
                val endRow = line[2] / 3
                val endCol = line[2] % 3

                val x1 = startX + startCol * cellSize + cellSize / 2f
                val y1 = startY + startRow * cellSize + cellSize / 2f
                val x2 = startX + endCol * cellSize + cellSize / 2f
                val y2 = startY + endRow * cellSize + cellSize / 2f

                canvas.drawLine(x1, y1, x2, y2, winLinePaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isInteractive || onCellClickListener == null) return false

        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x - padding
            val y = event.y - padding

            if (x >= 0 && y >= 0 && x <= cellSize * 3 && y <= cellSize * 3) {
                val col = (x / cellSize).toInt().coerceIn(0, 2)
                val row = (y / cellSize).toInt().coerceIn(0, 2)
                val index = row * 3 + col

                if (board[index] == null) {
                    performClick()
                    onCellClickListener?.invoke(index)
                    return true
                }
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
