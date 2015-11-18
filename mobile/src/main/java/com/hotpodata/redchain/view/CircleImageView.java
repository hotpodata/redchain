package com.hotpodata.redchain.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.hotpodata.redchain.R;


/**
 * Created by jdrotos on 7/30/14.
 */
public class CircleImageView extends ImageView {
    private int mCircleBgColor = Color.TRANSPARENT;
    private int mCircleBorderColor = Color.TRANSPARENT;
    private int mCircleBorderWidth = 2;

    private Paint mBgPaint = new Paint();
    private Paint mBorderPaint = new Paint();

    public CircleImageView(Context context) {
        super(context);
        init(context, null);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.CircleImageView);
            if (a != null) {
                if (a.hasValue(R.styleable.CircleImageView_circleBackgroundColor)) {
                    mCircleBgColor = a.getColor(R.styleable.CircleImageView_circleBackgroundColor, mCircleBgColor);
                }
                if (a.hasValue(R.styleable.CircleImageView_circleBorderColor)) {
                    mCircleBorderColor = a.getColor(R.styleable.CircleImageView_circleBorderColor, mCircleBorderColor);
                }
                if (a.hasValue(R.styleable.CircleImageView_circleBorderWidth)) {
                    mCircleBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_circleBorderWidth, mCircleBorderWidth);
                }
                a.recycle();
            }
        }

        updatePaints();

        //clipPath does not work if we are working in the hardware layer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setLayerType(LAYER_TYPE_NONE, null);
        } else {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void updatePaints() {
        //the circle background
        mBgPaint.setAntiAlias(true);
        mBgPaint.setColor(mCircleBgColor);
        mBgPaint.setStyle(Paint.Style.FILL);

        //circle border
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mCircleBorderColor);
        mBorderPaint.setStrokeWidth(mCircleBorderWidth);
        mBorderPaint.setStyle(Paint.Style.STROKE);
    }

    public void setCircleBgColor(int color) {
        mCircleBgColor = color;
        updatePaints();
    }

    public void setCircleBorderColor(int color) {
        mCircleBorderColor = color;
        updatePaints();
    }

    public void setCircleBorderWidth(int width) {
        mCircleBorderWidth = width;
        updatePaints();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();

        Path circle = new Path();
        circle.addCircle(w / 2f, h / 2f, Math.min(w, h) / 2f, Path.Direction.CW);
        if (mCircleBgColor != Color.TRANSPARENT) {
            canvas.drawPath(circle, mBgPaint);
        }

        if (mCircleBorderColor != Color.TRANSPARENT && mCircleBorderWidth > 0) {
            Path borderCirclePath = new Path();
            float hsw = mCircleBorderWidth / 2f;
            borderCirclePath.addCircle(w / 2f, h / 2f, (Math.min(w, h) / 2f) - hsw, Path.Direction.CW);
            canvas.drawPath(borderCirclePath, mBorderPaint);
        }

        canvas.clipPath(circle);
        super.onDraw(canvas);
    }
}
