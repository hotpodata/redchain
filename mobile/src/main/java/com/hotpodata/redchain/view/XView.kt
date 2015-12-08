package com.hotpodata.redchain.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.hotpodata.redchain.R
import timber.log.Timber

/**
 * Created by jdrotos on 10/5/15.
 */
public class XView : View {

    var xColor = Color.RED;
    var xColorSecondary = Color.BLUE;
    var xWidth = 16f
    var xPaint: Paint? = null
    var boxToXPercentage: Float = 0f
        get
        set(percentage: Float) {
            field = percentage
            Timber.d("setBoxToPErcentage:" + percentage)
            postInvalidate()
        }


    public constructor(context: Context) : super(context) {
        init(context, null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    @Suppress("DEPRECATION")
    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.XView)
            if (a != null) {
                if (a.hasValue(R.styleable.XView_boxToXPercentage)) {
                    boxToXPercentage = a.getFloat(R.styleable.XView_boxToXPercentage, boxToXPercentage)
                }
                a.recycle()
            }
        }

        xColor = context.resources.getColor(R.color.primary)
        xColorSecondary = context.resources.getColor(R.color.accent)
        xWidth = context.resources.getDimensionPixelSize(R.dimen.row_x_thickness).toFloat()
        xPaint = createFreshXPaint()
    }

    fun setColors(primColor: Int, secondaryColor: Int) {
        xColor = primColor;
        xColorSecondary = secondaryColor;
    }

    fun createFreshXPaint(): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = xColor
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = xWidth
        return paint
    }

    public fun setBox(boxMode: Boolean) {
        boxToXPercentage = if (boxMode) 0f else 1f
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0..1) {
            //This for loop is for the weird shadow effect
            if (xPaint != null && width > 0 && height > 0) {
                if (i == 0) {
                    xPaint?.color = xColorSecondary
                    canvas.save()
                    canvas.translate(xWidth / 2f, xWidth / 2f)
                } else {
                    xPaint?.color = xColor
                    canvas.restore()
                }


                var minLeft = xWidth + paddingLeft
                var maxRight = width.toFloat() - xWidth - paddingRight
                var minTop = xWidth + paddingTop
                var maxBottom = height.toFloat() - xWidth - paddingBottom

                if (boxToXPercentage == 0f) {
                    canvas.drawLine(minLeft, minTop, maxRight, minTop, xPaint)
                    canvas.drawLine(maxRight, minTop, maxRight, maxBottom, xPaint)
                    canvas.drawLine(maxRight, maxBottom, minLeft, maxBottom, xPaint)
                    canvas.drawLine(minLeft, maxBottom, minLeft, minTop, xPaint)
                } else if (boxToXPercentage == 1f) {
                    canvas.drawLine(minLeft, minTop, maxRight, maxBottom, xPaint)
                    canvas.drawLine(minLeft, maxBottom, maxRight, minTop, xPaint)
                } else {

                    if (boxToXPercentage < 0.5f) {
                        //In this case we are shrinking the box

                        var boxShowingPercentage = (0.5f - boxToXPercentage) / 0.5f;
                        Timber.d("boxShowingPercentage:" + boxShowingPercentage)

                        //First we draw the full portions of the box
                        if (boxShowingPercentage > 0.75f) {
                            canvas.drawLine(maxRight, minTop, maxRight, maxBottom, xPaint)
                        }
                        if (boxShowingPercentage > 0.5f) {
                            canvas.drawLine(maxRight, maxBottom, minLeft, maxBottom, xPaint)
                        }
                        if (boxShowingPercentage > 0.25f) {
                            canvas.drawLine(minLeft, maxBottom, minLeft, minTop, xPaint)
                        }


                        //Then we draw whatever partial portion of the box we need to
                        var portionPerc = 1f - ((Math.floor(boxShowingPercentage * 100.0) % 25) / 25f).toFloat() //(((Math.floor(boxShowingPercentage * 100.0) % 25)/100f)).toFloat()/0.25f;
                        Timber.d("portionPerc:" + portionPerc)
                        if (boxShowingPercentage > 0.75f) {
                            canvas.drawLine(minLeft + (portionPerc * (maxRight - minLeft)), minTop, maxRight, minTop, xPaint)
                        } else if (boxShowingPercentage > 0.5f) {
                            canvas.drawLine(maxRight, minTop + (portionPerc * (maxBottom - minTop)), maxRight, maxBottom, xPaint)
                        } else if (boxShowingPercentage > 0.25f) {
                            canvas.drawLine(maxRight - (portionPerc * (maxRight - minLeft)), maxBottom, minLeft, maxBottom, xPaint)
                        } else {
                            canvas.drawLine(minLeft, maxBottom - (portionPerc * (maxBottom - minTop)), minLeft, minTop, xPaint)
                        }

                    } else {
                        var xShowingPercentage = (boxToXPercentage - 0.5f) / 0.5f;
                        Timber.d("xShowingPercentage:" + xShowingPercentage)
                        canvas.drawLine(minLeft, minTop, minLeft + (xShowingPercentage * (maxRight - minLeft)), minTop + ( xShowingPercentage * (maxBottom - minTop)), xPaint)

                        //In this case we are growing the X
                        if (xShowingPercentage > 0.5f) {
                            var xHalfShowingPercentage = (xShowingPercentage - 0.5f) / 0.5f;
                            Timber.d("xHalfShowingPercentage:" + xHalfShowingPercentage)
                            var centerX = (maxRight - minLeft) / 2f + xWidth;
                            var centerY = (maxBottom - minTop) / 2f + xWidth;
                            canvas.drawLine(centerX, centerY, centerX + xHalfShowingPercentage * (maxRight - centerX), centerY - xHalfShowingPercentage * (centerY - minTop), xPaint)
                            canvas.drawLine(centerX, centerY, centerX - xHalfShowingPercentage * (centerX - minLeft), centerY + xHalfShowingPercentage * (maxBottom - centerY), xPaint)
                        }
                    }
                }
            }
        }
    }


}