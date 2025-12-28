package com.example.talesandtruths
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class TracingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val drawPaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 18f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    private val path = Path()
    private var totalLength = 0f
    private var lastX = 0f
    private var lastY = 0f

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(event.x, event.y)
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(event.x, event.y)
                totalLength += distance(lastX, lastY, event.x, event.y)
                lastX = event.x
                lastY = event.y
            }
        }
        invalidate()
        return true
    }

    fun clearCanvas() {
        path.reset()
        totalLength = 0f
        invalidate()
    }

    fun isTraceGood(): Boolean {
        return totalLength > 600f   // ✅ threshold (tune later)
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return kotlin.math.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1))
    }
}
