package com.wong.customtextview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomTextView extends View {
    private final Rect mTextBounds = new Rect();
    private String mText;
    private TextPaint mPaint;
    private int mTextColor;
    private float mTextSize;

    public CustomTextView(Context context) {
        this(context, null);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        @SuppressLint("Recycle") TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView);
        mText = a.getString(R.styleable.CustomTextView_custom_text);
        mTextColor = a.getColor(R.styleable.CustomTextView_custom_textColor, Color.BLACK);
        mTextSize = a.getFloat(R.styleable.CustomTextView_custom_textSize, 15);
        a.recycle();

        float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, context.getResources().getDisplayMetrics());
        mPaint = new TextPaint();
        mPaint.setColor(mTextColor);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setTextSize(size);
        if (!TextUtils.isEmpty(mText)) {
            mPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int mWidth = 0;
        int mHeight = 0;

        String widthModeStr;
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                widthModeStr = "AT_MOST";
                mWidth = getPaddingLeft() + getPaddingRight() + mTextBounds.width();
                break;
            case MeasureSpec.EXACTLY:
                widthModeStr = "EXACTLY";
                mWidth = getPaddingLeft() + getPaddingRight() + widthSize;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + widthMode);
        }
        String heightModeStr;
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
                heightModeStr = "AT_MOST";
                mHeight = getPaddingTop() + getPaddingBottom() + mTextBounds.height();
                break;
            case MeasureSpec.EXACTLY:
                heightModeStr = "EXACTLY";
                mHeight = getPaddingTop() + getPaddingBottom() + heightSize;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + heightMode);
        }
        setMeasuredDimension(mWidth, mHeight);
        String str = "@widthMode#" + widthModeStr + ":" + widthSize + "@heightMode#" + heightModeStr + ":" + heightSize + "###" + getSuggestedMinimumHeight() + "##$" + getSuggestedMinimumWidth();
        Log.d("尺寸", str);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!TextUtils.isEmpty(mText)) {
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            canvas.drawText(mText, getPaddingLeft(), Math.abs(fontMetrics.top)+(Integer)(getPaddingTop()/2), mPaint);
        }
    }
}
