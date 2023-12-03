package com.dev.cuckooxa.nndraw.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.utils.widget.MotionButton
import com.dev.cuckooxa.nndraw.DrawingField
import com.dev.cuckooxa.nndraw.OnFieldChangedListener
import com.dev.cuckooxa.nndraw.R
import com.dev.cuckooxa.nndraw.toPx
import java.lang.Float.min
import java.lang.Integer.max
import kotlin.properties.Delegates

typealias onPixelActionListener = (x: Int, y: Int, field: DrawingField) -> Unit

class Drawer28View(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    var drawingField: DrawingField? = null
        set(value) {
            field?.listeners?.remove(listener)
            field = value
            value?.listeners?.add(listener)
            updateViewSizes()
            requestLayout()
            invalidate()
        }

    var actionListener: onPixelActionListener? = null
    private val scaledPx = context.toPx(DESIRED_PIXEL_SCALE_DP)
    private val scaleMatrix = Matrix()
    private val fieldRect = RectF(0f, 0f, 0f, 0f)

    private var emptyPixelValue by Delegates.notNull<Double>()
    private var paintPixelValue by Delegates.notNull<Double>()

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): this(context, attributeSet, defStyleAttr, R.style.DefaultDrawerViewStyle)
    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, R.attr.drawerViewStyle)
    constructor(context: Context): this(context, null)

    init{
        if (attributeSet != null) initAttributes(attributeSet, defStyleAttr, defStyleRes)
        else initDefaultValues()

        /*bitmap = Bitmap.createBitmap(28 * scaledPx, 28 * scaledPx, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap).also { it.drawColor(Color.BLACK) }*/
//        bitmapCanvas = Canvas(drawingField?.bitmap!!).also { it.drawColor(Color.BLACK) }
        if (isInEditMode){

        }
    }

    private fun initAttributes(
        attributeSet: AttributeSet?,
        defStyleAttr: Int,
        desStyleRes: Int
    ){
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.Drawer28View, defStyleAttr, desStyleRes)

        emptyPixelValue = typedArray.getFloat(R.styleable.Drawer28View_emptyPixelValue, EMPTY_VALUE.toFloat()).toDouble()
        paintPixelValue = typedArray.getFloat(R.styleable.Drawer28View_paintPixelValue, PAINT_VALUE.toFloat()).toDouble()

        typedArray.recycle()
    }

    private fun initDefaultValues(){
        emptyPixelValue = EMPTY_VALUE
        paintPixelValue = PAINT_VALUE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        drawingField?.listeners?.add(listener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        drawingField?.listeners?.remove(listener)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewSizes()
        scaleMatrix.postScale(scaledPx.toFloat(), scaledPx.toFloat())
    }

    private fun updateViewSizes() {
        val safeWidth = width - paddingLeft - paddingRight
        val safeHeight = height - paddingTop - paddingBottom

        val pixelWidth = safeWidth / (drawingField?.height!!).toFloat()
        val pixelHeight = safeHeight / (drawingField?.width!!).toFloat()
        val pixelSize = min(pixelWidth, pixelHeight)

        val fieldWidth = pixelSize * drawingField?.height!!
        val fieldHeight = pixelSize * drawingField?.width!!

        fieldRect.left = paddingLeft + (safeWidth - fieldWidth) / 2
        fieldRect.top = paddingTop + (safeHeight - fieldHeight) / 2
        fieldRect.right = fieldRect.left + fieldWidth
        fieldRect.bottom = fieldRect.top + fieldHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val desiredScaledPixelSize = scaledPx
        val rows = drawingField?.height!!
        val columns = drawingField?.width!!

        val desiredWidth = max(minWidth, columns * desiredScaledPixelSize + paddingRight + paddingLeft)
        val desiredHeight = max(minHeight, rows * desiredScaledPixelSize + paddingTop + paddingBottom)

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (fieldRect.width() <= 0 || fieldRect.height() <= 0) return
        canvas.drawBitmap(drawingField?.bitmap!!, scaleMatrix, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val field = this.drawingField ?: return false
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                val x = (event.x / scaledPx).toInt()
                val y = (event.y / scaledPx).toInt()
                return if (x in 0 until drawingField?.width!! && y in 0 until drawingField?.height!!){
                    actionListener?.invoke(x ,y, field)
                    true
                }else{
                    false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val x = (event.x / scaledPx).toInt()
                val y = (event.y / scaledPx).toInt()
                return if (x in 0 until drawingField?.width!! && y in 0 until drawingField?.height!!){
                    actionListener?.invoke(x ,y, field)
                    true
                }else{
                    false
                }
            }
        }
        return false
    }

    private val listener: OnFieldChangedListener = {
        invalidate()
    }

    companion object {
        const val EMPTY_VALUE = 0.0
        const val PAINT_VALUE = 0.1
        const val DESIRED_PIXEL_SCALE_DP = 13
    }

}