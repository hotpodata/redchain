package com.hotpodata.redchain.view


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import org.joda.time.DateTimeConstants
import org.joda.time.Days
import org.joda.time.LocalDate
import timber.log.Timber

/**
 * Created by jdrotos on 7/30/14.
 */
public class WeekChainView : View {

    private val mColorCircleFuture = Color.LTGRAY
    private val mColorTextFuture = Color.WHITE
    private val mColorCircleNoTasks = Color.LTGRAY
    private val mColorTextNoTasks = Color.WHITE
    private val mColorCircleNoTasksComplete = Color.GRAY
    private val mColorTextNoTasksComplete = Color.WHITE
    private val mColorCircleSomeTasksComplete = Color.WHITE
    private val mColorTextSomeTasksComplete = Color.DKGRAY
    private var mColorBorderSomeTasksComplete = Color.RED
    private var mColorCircleAllTasksComplete = Color.RED
    private val mColorTextAllTasksComplete = Color.WHITE
    private var mColorBorderAllTasksComplete = Color.RED
    private val mColorCircleAllTasksCompleteInStreak = Color.TRANSPARENT
    private val mColorTextAllTasksCompleteInStreak = Color.WHITE
    private var mColorStreakBackgroundColor = Color.RED
    private val mColorBorderToday = Color.TRANSPARENT

    private var mPaintCircleFuture: Paint? = null
    private var mPaintTextFuture: Paint? = null
    private var mPaintCircleNoTasks: Paint? = null
    private var mPaintTextNoTasks: Paint? = null
    private var mPaintCircleNoTasksComplete: Paint? = null
    private var mPaintTextNoTasksComplete: Paint? = null
    private var mPaintCircleSomeTasksComplete: Paint? = null
    private var mPaintTextSomeTasksComplete: Paint? = null
    private var mPaintBorderSomeTasksComplete: Paint? = null
    private var mPaintCircleAllTasksComplete: Paint? = null
    private var mPaintTextAllTasksComplete: Paint? = null
    private var mPaintBorderAllTasksComplete: Paint? = null
    private var mPaintCircleAllTasksCompleteInStreak: Paint? = null
    private var mPaintTextAllTasksCompleteInStreak: Paint? = null
    private var mPaintStreakBackgroundColor: Paint? = null
    private var mPaintBorderToday: Paint? = null

    private var mActiveLocalDate: LocalDate? = null
    private var mStartLocalDate: LocalDate? = null
    private var mHighlightedStartDate: LocalDate? = null
    private var mHighlightedEndDate: LocalDate? = null
    private var mDateSpan = 7
    private var mDateMode = false


    private var mCircleDiameter: Int = 0
    private var mCircleRadius: Int = 0
    private var mTextSize: Int = 0
    private var mBorderThickness: Int = 0
    private var mSectionWidth: Int = 0
    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0
    private var mCenterY: Int = 0
    private var mStartX: Int = 0

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

    private fun init(context: Context, attrs: AttributeSet?) {
        //Set start date to the beginning of the week
        setDaySpan(LocalDate.now(), 0, 0)
        mActiveLocalDate = LocalDate.now()

        updatePaints()

        //clipPath does not work if we are working in the hardware layer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setLayerType(View.LAYER_TYPE_NONE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    private fun updatePaints() {
        mPaintCircleFuture = getCirclePaint(mColorCircleFuture)
        mPaintTextFuture = getTextPaint(mColorTextFuture)
        mPaintCircleNoTasks = getCirclePaint(mColorCircleNoTasks)
        mPaintTextNoTasks = getTextPaint(mColorTextNoTasks)
        mPaintCircleNoTasksComplete = getCirclePaint(mColorCircleNoTasksComplete)
        mPaintTextNoTasksComplete = getTextPaint(mColorTextNoTasksComplete)
        mPaintCircleSomeTasksComplete = getCirclePaint(mColorCircleSomeTasksComplete)
        mPaintTextSomeTasksComplete = getTextPaint(mColorTextSomeTasksComplete)
        mPaintBorderSomeTasksComplete = getBorderPaint(mColorBorderSomeTasksComplete)
        mPaintCircleAllTasksComplete = getCirclePaint(mColorCircleAllTasksComplete)
        mPaintTextAllTasksComplete = getTextPaint(mColorTextAllTasksComplete)
        mPaintBorderAllTasksComplete = getBorderPaint(mColorBorderAllTasksComplete)
        mPaintCircleAllTasksCompleteInStreak = getCirclePaint(mColorCircleAllTasksCompleteInStreak)
        mPaintTextAllTasksCompleteInStreak = getTextPaint(mColorTextAllTasksCompleteInStreak)
        mPaintStreakBackgroundColor = getStreakPaint(mColorStreakBackgroundColor)
        mPaintBorderToday = getBorderPaint(mColorBorderToday)

        postInvalidate()

    }

    private fun getTextPaint(color: Int): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        paint.textSize = mTextSize.toFloat()
        paint.textAlign = Paint.Align.CENTER
        return paint
    }

    private fun getCirclePaint(color: Int): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        paint.style = Paint.Style.FILL
        return paint
    }

    private fun getBorderPaint(color: Int): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        paint.style = Paint.Style.FILL
        return paint
    }

    private fun getStreakPaint(color: Int): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.strokeWidth = (mCircleDiameter + 2 * mBorderThickness).toFloat()
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        return paint
    }


    public fun setAccentColor(color: Int) {
        mColorBorderSomeTasksComplete = color
        mColorCircleAllTasksComplete = color
        mColorStreakBackgroundColor = color
        mColorBorderAllTasksComplete = color
        updatePaints()
    }

    public fun setActiveDay(day: LocalDate?) {
        mActiveLocalDate = day
        postInvalidate()
    }

    public fun getStartDay(): LocalDate? {
        return mStartLocalDate
    }

    public fun setDateMode(dateMode: Boolean){
        mDateMode = dateMode;
        postInvalidate()
    }

    public fun setDaySpan(weekDay: LocalDate, daysFromStart: Int, daysFromEnd: Int) {
        var startDate = weekDay.withDayOfWeek(DateTimeConstants.SUNDAY)
        if (LocalDate.now().dayOfWeek().get() != DateTimeConstants.SUNDAY) {
            startDate = startDate!!.minusWeeks(1)
        }
        mStartLocalDate = startDate
        mHighlightedStartDate = startDate.plusDays(daysFromStart)
        mHighlightedEndDate = startDate.plusDays(mDateSpan - 1).minusDays(daysFromEnd)
        //        mDaysFromStart = daysFromStart
        //        mDaysFromEnd = daysFromEnd
        postInvalidate()
    }

    //    public fun setActiveRange(start: LocalDate?, end: LocalDate?){
    //        mActiveStartDate = start
    //        mActiveEndDate = end
    //        postInvalidate()
    //    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mViewWidth = w
        mViewHeight = h

        //We assume longer than tall, otherwise we're boned anyway
        mSectionWidth = (mViewWidth / mDateSpan.toFloat()).toInt()
        mBorderThickness = Math.max(1f, Math.min(mViewHeight, mSectionWidth) / 10f).toInt()
        mCircleDiameter = Math.min(mViewHeight, mSectionWidth) - (2 * mBorderThickness)
        mCircleRadius = (mCircleDiameter / 2f).toInt()
        mTextSize = mCircleRadius
        mCenterY = (mViewHeight / 2f).toInt()
        mStartX = (mSectionWidth / 2f).toInt()

        updatePaints()
    }


    override fun onDraw(canvas: Canvas) {
        var daysFromStart = 0;
        if (mHighlightedStartDate?.isAfter(mStartLocalDate) == true) {
            daysFromStart = Days.daysBetween(mStartLocalDate, mHighlightedStartDate).days
        }
        var daysFromEnd = 0;
        if (mHighlightedEndDate?.isBefore(mStartLocalDate!!.plusDays(mDateSpan)) == true) {
            daysFromEnd = Days.daysBetween(mHighlightedEndDate, mStartLocalDate?.plusDays(mDateSpan)).days
        }
        val spanStart = mStartX + (daysFromStart * mSectionWidth)
        var spanLength = mDateSpan - daysFromStart - daysFromEnd;
        Timber.d("daysFromStart:$daysFromStart daysFromEnd:$daysFromEnd spanStart:$spanStart spanLength:$spanLength mHightlightStartDate:$mHighlightedStartDate mHightlightEndDate:$mHighlightedEndDate");
        if (mHighlightedStartDate == null || mHighlightedEndDate == null) {
            spanLength = 0
        }
        if (spanLength > 0) {
            canvas.drawLine(spanStart.toFloat(), mCenterY.toFloat(), (spanStart + (spanLength * mSectionWidth)).toFloat(), mCenterY.toFloat(), mPaintStreakBackgroundColor)
        }

        for (i in 0..mDateSpan - 1) {
            val day = LocalDate(mStartLocalDate).plusDays(i)
            val borderPaint: Paint?
            val circlePaint: Paint?
            val textPaint: Paint?

            if (mActiveLocalDate != null && mActiveLocalDate!!.equals(day)) {
                //active
                borderPaint = mPaintBorderToday
                circlePaint = mPaintCircleSomeTasksComplete
                textPaint = mPaintTextSomeTasksComplete
            } else if (mHighlightedStartDate != null && mHighlightedEndDate != null && i >= daysFromStart && i < mDateSpan - daysFromStart) {
                //highlighted
                circlePaint = mPaintCircleAllTasksCompleteInStreak;
                textPaint = mPaintTextAllTasksCompleteInStreak;
                borderPaint = null
            } else {
                //out of range
                circlePaint = mPaintCircleNoTasksComplete
                textPaint = mPaintTextNoTasksComplete
                borderPaint = null
            }

            //            if (day.isAfter(mActiveLocalDate)) {
            //                circlePaint = mPaintCircleFuture
            //                textPaint = mPaintTextFuture
            //            } else {
            //                if (day == mActiveLocalDate) {
            //                    boderPaint = mPaintBorderToday
            //                    circlePaint = mPaintCircleSomeTasksComplete
            //                    textPaint = mPaintTextSomeTasksComplete
            //                } else {
            //                    boderPaint = null//Steak color is border...
            //                    circlePaint = mPaintCircleSomeTasksComplete
            //                    textPaint = mPaintTextSomeTasksComplete
            //                }
            ////                circlePaint = mPaintCircleAllTasksCompleteInStreak
            ////                textPaint = mPaintTextAllTasksCompleteInStreak
            //            }


            if (borderPaint != null) {
                canvas.drawCircle((mStartX + (i * mSectionWidth)).toFloat(), mCenterY.toFloat(), (mCircleRadius + mBorderThickness).toFloat(), borderPaint)
            }
            canvas.drawCircle((mStartX + (i * mSectionWidth)).toFloat(), mCenterY.toFloat(), mCircleRadius.toFloat(), circlePaint)
            var dayText = day.dayOfWeek().asText.toUpperCase().substring(0, 1)
            if(mDateMode){
                dayText = "" + day.dayOfMonth
            }
            val textBounds = Rect()
            textPaint?.getTextBounds(dayText, 0, dayText.length, textBounds)
            canvas.drawText(dayText, (mStartX + (i * mSectionWidth)).toFloat(), mCenterY + (textBounds.height() / 2f), textPaint)
        }

        super.onDraw(canvas)
    }
}
