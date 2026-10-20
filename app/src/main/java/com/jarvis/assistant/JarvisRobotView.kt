package com.jarvis.assistant

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class JarvisRobotView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG)
    private var phase = 0f

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        post(object : Runnable {
            override fun run() {
                phase += 0.05f
                invalidate()
                postDelayed(this, 33L)
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(width, height) * 0.31f
        val pulse = 1f + 0.035f * sin(phase * 1.6f)

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        glow.style = Paint.Style.STROKE
        glow.strokeWidth = 3f
        glow.color = Color.rgb(50, 210, 255)
        glow.alpha = 110
        glow.maskFilter = BlurMaskFilter(18f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawCircle(cx, cy, radius * 1.08f * pulse, glow)
        glow.maskFilter = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f
        paint.color = Color.rgb(61, 195, 255)
        paint.alpha = 210
        canvas.drawCircle(cx, cy, radius * 1.08f * pulse, paint)

        paint.alpha = 100
        canvas.drawCircle(cx, cy, radius * 1.28f, paint)
        canvas.drawCircle(cx, cy, radius * 1.45f, paint)

        val orbitR = radius * 1.28f
        for (i in 0 until 3) {
            val a = phase * (0.7f + i * 0.17f) + i * 2.1f
            canvas.drawCircle(cx + cos(a) * orbitR, cy + sin(a) * orbitR, 5f, glow)
        }

        val face = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(5, 25, 43)
        paint.alpha = 245
        canvas.drawOval(face, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.rgb(103, 232, 249)
        paint.alpha = 230
        canvas.drawOval(face, paint)

        val eyeY = cy - radius * 0.12f
        val eyeW = radius * 0.34f
        val eyeH = radius * 0.14f
        val blink = if ((phase % 6.0f) in 5.75f..5.92f) 0.08f else 1f
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(103, 232, 249)
        canvas.drawRoundRect(RectF(cx - radius * 0.58f, eyeY - eyeH * blink, cx - radius * 0.58f + eyeW, eyeY + eyeH * blink), eyeH, eyeH, paint)
        canvas.drawRoundRect(RectF(cx + radius * 0.24f, eyeY - eyeH * blink, cx + radius * 0.24f + eyeW, eyeY + eyeH * blink), eyeH, eyeH, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.rgb(75, 170, 255)
        val mouth = RectF(cx - radius * 0.36f, cy + radius * 0.22f, cx + radius * 0.36f, cy + radius * 0.48f)
        canvas.drawArc(mouth, 15f, 150f, false, paint)
    }
}
