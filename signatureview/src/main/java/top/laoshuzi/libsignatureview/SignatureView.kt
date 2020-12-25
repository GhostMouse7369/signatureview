package top.laoshuzi.libsignatureview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * Created by laoshuzi on 2020/12/23.
 */
open class SignatureView : View {

    private val TAG = SignatureView::class.java.simpleName

    private val MIN_PEN_SIZE = 1f
    private val MIN_INCREMENT = 0.01f
    private val INCREMENT_CONSTANT = 0.0005f
    private val DRAWING_CONSTANT = 0.0085f
    private val MAX_VELOCITY_BOUND = 15f
    private val MIN_VELOCITY_BOUND = 1.6f
    private val STROKE_DES_VELOCITY = 1.0f
    private val VELOCITY_FILTER_WEIGHT = 0.2f

    private var mContext: Context? = null

    private var canvasBmp: Canvas? = null
    private var bmp: Bitmap? = null
    private var previousPoint: Point? = null
    private var startPoint: Point? = null
    private var currentPoint: Point? = null
    private var drawViewRect: Rect? = null

    private var lastVelocity: Float = 0f
    private var lastWidth: Float = 0f
    private var layoutLeft = 0
    private var layoutTop: Int = 0
    private var layoutRight: Int = 0
    private var layoutBottom: Int = 0
    private var ignoreTouch = false

    private var paint: Paint
    private var paintBm: Paint

    var enableSignature = false
    var penColor: Int
    var backColor: Int
    var penSize: Float


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.mContext = context
        this.setWillNotDraw(false)
        this.isDrawingCacheEnabled = true

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.signature, 0, 0)
        try {
            enableSignature = typedArray.getBoolean(R.styleable.signature_enableSignature, true)
            backColor = typedArray.getColor(
                R.styleable.signature_backColor,
                context.resources.getColor(R.color.white)
            )
            penColor = typedArray.getColor(
                R.styleable.signature_penColor,
                context.resources.getColor(R.color.penRoyalBlue)
            )
            penSize = typedArray.getDimension(
                R.styleable.signature_penSize,
                context.resources.getDimension(R.dimen.pen_size)
            )
        } finally {
            typedArray.recycle()
        }

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = penColor
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = penSize

        paintBm = Paint(Paint.ANTI_ALIAS_FLAG)
        paintBm.isAntiAlias = true
        paintBm.style = Paint.Style.STROKE
        paintBm.strokeJoin = Paint.Join.ROUND
        paintBm.strokeCap = Paint.Cap.ROUND
        paintBm.color = Color.BLACK
    }

    override fun onDetachedFromWindow() {
        try {
            bmp?.recycle()
        } catch (th: Throwable) {
        }
        super.onDetachedFromWindow()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        layoutLeft = left
        layoutTop = top
        layoutRight = right
        layoutBottom = bottom
        if (bmp == null) {
            newBitmapCanvas(layoutLeft, layoutTop, layoutRight, layoutBottom)
        }
    }

    override fun onDraw(canvas: Canvas) {
        bmp?.also {
            canvas.drawBitmap(it, 0f, 0f, paintBm)
        } ?: Log.d(TAG, "bmp is null")
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enableSignature) {
            return false
        }
        if (event.pointerCount > 1) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                ignoreTouch = false
                drawViewRect = Rect(this.left, this.top, this.right, this.bottom)
                onTouchDownEvent(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE ->
                if (!drawViewRect!!.contains(left + event.x.toInt(), this.top + event.y.toInt())) {
                    //You are out of drawing area
                    if (!ignoreTouch) {
                        ignoreTouch = true
                        onTouchUpEvent(event.x, event.y)
                    }
                } else {
                    //You are in the drawing area
                    if (ignoreTouch) {
                        ignoreTouch = false
                        onTouchDownEvent(event.x, event.y)
                    } else {
                        onTouchMoveEvent(event.x, event.y)
                    }
                }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP ->
                onTouchUpEvent(event.x, event.y)
            else -> {
            }
        }
        return true // super.onTouchEvent(event)
    }


    /**
     * Get signature as bitmap
     *
     * @return Bitmap
     */
    fun getSignatureBitmap(): Bitmap? {
        return if (bmp != null) {
            Bitmap.createScaledBitmap(bmp!!, bmp!!.width, bmp!!.height, true)
        } else {
            null
        }
    }

    /**
     * Render bitmap in signature
     *
     * @param bitmap Bitmap
     */
    fun setBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            try {
                bmp?.recycle()
            } catch (th: Throwable) {
            }
            bmp = bitmap
            canvasBmp = Canvas(bmp!!)
            postInvalidate()
        }
    }

    /**
     * Check is signature bitmap empty
     *
     * @return boolean
     */
    fun isBitmapEmpty(): Boolean {
        if (bmp != null) {
            val emptyBitmap = Bitmap.createBitmap(
                bmp!!.width, bmp!!.height,
                bmp!!.config
            )
            val canvasBmp = Canvas(emptyBitmap)
            canvasBmp.drawColor(backColor)
            if (bmp!!.sameAs(emptyBitmap)) {
                return true
            }
        }
        return false
    }

    /**
     * Clear signature from canvas
     */
    fun clearCanvas() {
        previousPoint = null
        startPoint = null
        currentPoint = null
        lastVelocity = 0f
        lastWidth = 0f
        newBitmapCanvas(layoutLeft, layoutTop, layoutRight, layoutBottom)
        postInvalidate()
    }

    private fun newBitmapCanvas(left: Int, top: Int, right: Int, bottom: Int) {
        bmp = null
        canvasBmp = null
        if (right - left > 0 && bottom - top > 0) {
            bmp = Bitmap.createBitmap(right - left, bottom - top, Bitmap.Config.ARGB_8888)
            bmp?.also {
                canvasBmp = Canvas(it)
                canvasBmp?.also { c ->
                    c.drawColor(backColor)
                } ?: Log.d(TAG, "canvasBmp is null")
            } ?: Log.d(TAG, "bmp is null")
        }
    }

    private fun draw(
        p0: Point, p1: Point, p2: Point, lastWidth: Float,
        currentWidth: Float, velocity: Float
    ) {
        if (canvasBmp != null) {
            var xa: Float
            var xb: Float
            var ya: Float
            var yb: Float
            var x: Float
            var y: Float
            val increment: Float =
                if (velocity > MIN_VELOCITY_BOUND && velocity < MAX_VELOCITY_BOUND) {
                    DRAWING_CONSTANT - velocity * INCREMENT_CONSTANT
                } else {
                    MIN_INCREMENT
                }
            var i = 0f
            while (i < 1f) {
                xa = getPt(p0.x, p1.x, i)
                ya = getPt(p0.y, p1.y, i)
                xb = getPt(p1.x, p2.x, i)
                yb = getPt(p1.y, p2.y, i)
                x = getPt(xa, xb, i)
                y = getPt(ya, yb, i)
                val strokeVal = lastWidth + (currentWidth - lastWidth) * i
                paint.strokeWidth = if (strokeVal < MIN_PEN_SIZE) MIN_PEN_SIZE else strokeVal
                canvasBmp!!.drawPoint(x, y, paint)
                i += increment
            }
        }
    }

    private fun drawLine(lastWidth: Float, currentWidth: Float, velocity: Float) {
        val mid1: Point = midPoint(previousPoint!!, startPoint!!)
        val mid2: Point = midPoint(currentPoint!!, previousPoint!!)
        draw(mid1, previousPoint!!, mid2, lastWidth, currentWidth, velocity)
    }

    private fun getPt(n1: Float, n2: Float, perc: Float): Float {
        val diff = n2 - n1
        return n1 + diff * perc
    }

    private fun midPoint(p1: Point, p2: Point): Point {
        return Point((p1.x + p2.x) / 2.0f, (p1.y + p2.y) / 2, (p1.time + p2.time) / 2)
    }

    private fun getSignatureBitmap(bitmap: Bitmap): Bitmap? {
        return Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
    }

    private fun onTouchDownEvent(x: Float, y: Float) {
        previousPoint = null
        startPoint = null
        currentPoint = null
        lastVelocity = 0f
        lastWidth = penSize
        currentPoint = Point(x, y, System.currentTimeMillis())
        previousPoint = currentPoint
        startPoint = previousPoint
        postInvalidate()
    }

    private fun onTouchMoveEvent(x: Float, y: Float) {
        if (previousPoint == null) {
            return
        }
        startPoint = previousPoint
        previousPoint = currentPoint
        currentPoint = Point(x, y, System.currentTimeMillis())
        var velocity = currentPoint!!.velocityFrom(previousPoint!!)
        velocity = VELOCITY_FILTER_WEIGHT * velocity + (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity
        val strokeWidth: Float = getStrokeWidth(velocity)
        drawLine(lastWidth, strokeWidth, velocity)
        lastVelocity = velocity
        lastWidth = strokeWidth
        postInvalidate()
    }

    private fun onTouchUpEvent(x: Float, y: Float) {
        if (previousPoint == null) {
            return
        }
        startPoint = previousPoint
        previousPoint = currentPoint
        currentPoint = Point(x, y, System.currentTimeMillis())
        drawLine(lastWidth, 0f, lastVelocity)
        postInvalidate()
    }

    private fun getStrokeWidth(velocity: Float): Float {
        return penSize - velocity * STROKE_DES_VELOCITY
    }

}