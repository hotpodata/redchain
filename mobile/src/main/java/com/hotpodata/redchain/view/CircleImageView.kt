package com.hotpodata.redchain.view

import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

import com.hotpodata.redchain.R


/**
 * Created by jdrotos on 7/30/14.
 */
class CircleImageView : ImageView {
    private var mCircleBgColor = Color.TRANSPARENT
    private var mCircleBorderColor = Color.TRANSPARENT
    private var mCircleBorderWidth = 2

    private val mBgPaint = Paint()
    private val mBorderPaint = Paint()

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs,
                    R.styleable.CircleImageView)
            if (a != null) {
                if (a.hasValue(R.styleable.CircleImageView_circleBackgroundColor)) {
                    mCircleBgColor = a.getColor(R.styleable.CircleImageView_circleBackgroundColor, mCircleBgColor)
                }
                if (a.hasValue(R.styleable.CircleImageView_circleBorderColor)) {
                    mCircleBorderColor = a.getColor(R.styleable.CircleImageView_circleBorderColor, mCircleBorderColor)
                }
                if (a.hasValue(R.styleable.CircleImageView_circleBorderWidth)) {
                    mCircleBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_circleBorderWidth, mCircleBorderWidth)
                }
                a.recycle()
            }
        }

        updatePaints()

        //clipPath does not work if we are working in the hardware layer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setLayerType(View.LAYER_TYPE_NONE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    private fun updatePaints() {
        //the circle background
        mBgPaint.isAntiAlias = true
        mBgPaint.color = mCircleBgColor
        mBgPaint.style = Paint.Style.FILL

        //circle border
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = mCircleBorderColor
        mBorderPaint.strokeWidth = mCircleBorderWidth.toFloat()
        mBorderPaint.style = Paint.Style.STROKE
    }

    fun setCircleBgColor(color: Int) {
        mCircleBgColor = color
        updatePaints()
    }

    fun setCircleBorderColor(color: Int) {
        mCircleBorderColor = color
        updatePaints()
    }

    fun setCircleBorderWidth(width: Int) {
        mCircleBorderWidth = width
        updatePaints()
    }


    override fun onDraw(canvas: Canvas) {
        val w = width
        val h = height

        val circle = Path()
        circle.addCircle(w / 2f, h / 2f, Math.min(w, h) / 2f, Path.Direction.CW)
        if (mCircleBgColor != Color.TRANSPARENT) {
            canvas.drawPath(circle, mBgPaint)
        }

        if (mCircleBorderColor != Color.TRANSPARENT && mCircleBorderWidth > 0) {
            val borderCirclePath = Path()
            val hsw = mCircleBorderWidth / 2f
            borderCirclePath.addCircle(w / 2f, h / 2f, (Math.min(w, h) / 2f) - hsw, Path.Direction.CW)
            canvas.drawPath(borderCirclePath, mBorderPaint)
        }

        canvas.clipPath(circle)
        super.onDraw(canvas)
    }
}
