package com.chuan_sir.customviewpractice.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.chuan_sir.customviewpractice.R;

public class SlideLockView extends View {

    private static final String TAG="SlideLockView";
    private Bitmap mLockBitmap;
    private int mLockDrawableId;
    private Paint mPaint;
    private int mRadius;
    private String mText;
    private int mTextSize;
    private int mTextColor;
    private Rect mTextRect = new Rect();
    private float mLeftX=0;
    private boolean mIsDragable = false;
    private OnLockListener mOnLockListener;


    public SlideLockView(Context context) {
        this(context, null);
    }

    public SlideLockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideLockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideLockView);
        mLockDrawableId = typedArray.getResourceId(R.styleable.SlideLockView_lock_drawable, -1);
        mRadius = typedArray.getDimensionPixelSize(R.styleable.SlideLockView_lock_radius, 1);
        mText = typedArray.getString(R.styleable.SlideLockView_lock_text);
        mTextColor = typedArray.getColor(R.styleable.SlideLockView_lock_text_color, Color.BLACK);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.SlideLockView_lock_text_size, 12);
        typedArray.recycle();

        if (mLockDrawableId == -1) {
            throw new RuntimeException("未设置滑动图片");
        }
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mTextColor);

        mLockBitmap = BitmapFactory.decodeResource(context.getResources(), mLockDrawableId);
        int oldSize = mLockBitmap.getHeight();
        int newSize = mRadius * 2;
        float scale = newSize * 1.0f / oldSize;
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        mLockBitmap = Bitmap.createBitmap(mLockBitmap, 0, 0, oldSize, oldSize, matrix, true);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(600, 100);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(600, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, 100);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画超出这个边界会被裁剪掉
        canvas.getClipBounds(mTextRect);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.getTextBounds(mText, 0, mText.length(), mTextRect);
        float x = width / 2f - mTextRect.width() / 2f;
        float y = height / 2f + mTextRect.height() / 2f;
        canvas.drawText(mText, x, y, mPaint);

        int rightMax =getWidth() - mRadius * 2;
        if (mLeftX <= 0) {
            canvas.drawBitmap(mLockBitmap, 0, 0, mPaint);
        } else if (mLeftX > rightMax) {
            canvas.drawBitmap(mLockBitmap, rightMax, 0, mPaint);
        } else {
            canvas.drawBitmap(mLockBitmap, mLeftX, 0, mPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX(), y = event.getY();
                if (isTouchLock(x, y)) {
                    mLeftX = x - mRadius;
                    mIsDragable = true;
                    invalidate();
                } else {
                    mIsDragable = false;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!mIsDragable) {
                    return true;
                }

                int rightMax = getWidth() - mRadius * 2;
                resetLeftX(event.getX(), rightMax);
                invalidate();

                if (mLeftX >=rightMax) {
                    mIsDragable = false;
                    mLeftX = 0;
                    invalidate();
                    if (mOnLockListener != null) {
                        mOnLockListener.onOpenLockSuccess();
                    }
                    Log.e(TAG,"解锁成功！");
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!mIsDragable) {
                    return true;
                }
                restLock();
                break;
        }
        return super.onTouchEvent(event);
    }


    private void restLock() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mLeftX, 0);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLeftX = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.start();
    }

    private void resetLeftX(float curX, float rightMax) {
        mLeftX = curX - mRadius;
        if (mLeftX < 0) {
            mLeftX = 0;
        } else if (mLeftX > rightMax) {
            mLeftX = rightMax;
        }
    }

    private boolean isTouchLock(float x, float y) {
        float centX = mLeftX + mRadius, centY = mRadius;
        float diffX = x - centX;
        float diffY = y - centY;
        return diffX * diffX + diffY * diffY <= mRadius * mRadius;
    }

    public void setmOnLockListener(OnLockListener mOnLockListener) {
        this.mOnLockListener = mOnLockListener;
    }

    public interface OnLockListener {
        void onOpenLockSuccess();
    }
}
