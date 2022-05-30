package com.example.testing.ui.main

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import androidx.core.graphics.scale

class CanvasView constructor(
    context: Context?,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var screenHeight = 0
    var screenWidth = 0

    private var drawPath: Path? = null
    private var drawPaint: Paint? = null
    private var drawPaint2: Paint? = null
    var scaleGesture: ScaleGestureDetector? = null
    private val brushPaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
        strokeWidth = 20F
    }

    var myMatrix: Matrix? = null

    var move = false
    var scale = false

    var bitmapImage: Bitmap? = null
    var bitmapBackground: Bitmap? = null
    var mScaleFactor: Float = 1F

    var bgtouchX = 0F
    var bgtouchY = 0F

    var moveStart = false

    init {
        getEcranHeight()
        myMatrix = Matrix()
        scaleGesture = ScaleGestureDetector(context, ScaleListener())
        drawPaint = Paint()
        drawPaint!!.color = Color.TRANSPARENT
        drawPaint!!.isAntiAlias = true
        drawPaint!!.strokeWidth = 30F
        drawPaint2 = Paint()
        drawPaint2!!.color = Color.WHITE
        drawPaint2!!.isAntiAlias = true
        drawPaint2!!.strokeWidth = 30F
        drawPath = Path()
    }

    fun setbackground(bitmap2: Bitmap) {
        var scale = getScale(bitmap2.height, bitmap2.width)
        bitmapBackground = bitmap2.scale(bitmap2.width - scale, bitmap2.height - scale)
        bgtouchX = (screenWidth / 2 - (bitmap2!!.width - scale) / 2).toFloat()
        bgtouchY = (screenHeight / 2 - (bitmap2!!.height - scale) / 2).toFloat()
        invalidate()
    }

    fun getScale(bitmapH: Int, bitmapW: Int): Int {
        var differenceH = bitmapH - screenHeight
        var differenceW = bitmapW - screenWidth
        var scale = 0
        if (bitmapH < screenHeight && bitmapW < screenWidth) {
            if (differenceH > differenceW) {
                scale = differenceH
            } else if (differenceH < differenceW) {
                scale = differenceW
            }
        }
        return scale
    }
    fun setBitmap(bitmap: Bitmap) {
        var scale = getScale(bitmap.height, bitmap.width)
        this.bitmapImage = bitmap.scale(bitmap.width - scale, bitmap.height - scale)
        mask = Bitmap.createBitmap(bitmap.width - scale, bitmap.height - scale, Bitmap.Config.ALPHA_8)
        maskCanvas = Canvas(mask)
        bitmapBackground = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ALPHA_8)
    }

    lateinit var mask: Bitmap
    lateinit var maskCanvas: Canvas

    fun getEcranHeight() {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.concat(myMatrix)
        bitmapImage?.let {
            canvas.drawBitmap(bitmapBackground!!, bgtouchX, bgtouchY, Paint())
            canvas.saveLayer(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat(), drawPaint2)
            canvas.drawBitmap(bitmapImage!!, 0F, 0F, Paint())
            canvas.drawBitmap(mask, 0F, 0F, brushPaint)
            canvas.restore()
        }
    }

    private var maskPaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
        strokeWidth = 20F
    }

    fun changeMask() {
        maskCanvas.drawPath(drawPath!!, maskPaint)
    }

    fun convertPoint(x: Float, y: Float): PointF {
        val values = FloatArray(9)
        myMatrix?.getValues(values)
        var myX = (x - values[2]) / values[0]
        var myY = (y - values[5]) / values[4]
        return PointF(myX, myY)
    }

    fun getXandY(): ArrayList<Float> {
        val values = FloatArray(9)
        myMatrix?.getValues(values)
        return arrayListOf<Float>(values[2], values[5], values[0], values[4])
    }
    var refx = 0F
    var refy = 0F
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGesture?.onTouchEvent(event)
        if (move && !scale) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    refx = event.x
                    refy = event.y
                    moveStart =
                        (event.x >= getXandY()[0] && event.x <= bitmapImage!!.width * mScaleFactor + getXandY()[0]) &&
                        (event.y >= getXandY()[1] && event.y <= getXandY()[1] + bitmapImage!!.height * mScaleFactor)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (moveStart) {
                        var x = getXandY()[0]
                        var y = getXandY()[1]
                        x += (event.x - refx) / getXandY()[2]
                        y += (event.y - refy) / getXandY()[3]
                        myMatrix!!.postTranslate(x - getXandY()[0], y - getXandY()[1])
                        refx = event.x
                        refy = event.y
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    moveStart = false
                }
            }
        } else {
            if (!move && !scale) {
                val point = convertPoint(event.x, event.y)
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        drawPath?.moveTo(point.x, point.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        drawPath?.lineTo(point.x, point.y)
                        changeMask()
                        invalidate()
                    }
                    MotionEvent.ACTION_UP -> {
                        invalidate()
                    }
                    else -> return false
                }
            }
        }
        return true
    }

    fun getBitmap(): Bitmap {
        this.isDrawingCacheEnabled = true
        this.buildDrawingCache()
        val bmp = Bitmap.createBitmap(this.drawingCache)
        this.isDrawingCacheEnabled = false
        return bmp
    }
    inner class ScaleListener : OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactorNew = detector.scaleFactor
            if (detector.isInProgress && scale) {
                mScaleFactor *= scaleFactorNew
                myMatrix?.setScale(mScaleFactor, mScaleFactor, (detector.currentSpanX + detector.previousSpanX) / 2, (detector.currentSpanY + detector.previousSpanY) / 2)
                invalidate()
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
        }
    }
}
