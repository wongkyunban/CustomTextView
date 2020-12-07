package com.wong.customtextview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

public class CustomFlowLayout extends ViewGroup {

    private int mWidth = 0;
    private int mHeight = 0;
    private int realHeight;
    private boolean scrollable = false;
    private boolean isInterceptedTouch;
    /**
     * 判定为拖动的最小移动像素数
     */
    private int mTouchSlop;
    private int topBorder; // 上边界
    private int bottomBorder;// 下边界

    //记录每个View的位置
    private List<ChildPosition> mChildPos = new ArrayList<ChildPosition>();

    private static class ChildPosition {
        int left, top, right, bottom;

        public ChildPosition(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

    public CustomFlowLayout(Context context) {
        this(context, null);
    }

    public CustomFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        // 获取TouchSlop值,用于判断当前用户的操作是否是拖动
        mTouchSlop = configuration.getScaledPagingTouchSlop();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measuredWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int measuredHeightSize = MeasureSpec.getSize(heightMeasureSpec);

        int rowWidth = 0;// 临时记录行宽
        int rowHeight = 0;// 临时记录行高
        int maxWith = 0;
        int maxHeight = 0;
        measureChildren(widthMeasureSpec, heightMeasureSpec);// 测量children的大小
        int count = getChildCount();
        if (count != 0) {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                MarginLayoutParams mlp = (MarginLayoutParams) child.getLayoutParams();
                int childWidth = child.getMeasuredWidth() + mlp.rightMargin + mlp.rightMargin;
                int childHeight = child.getMeasuredHeight() + mlp.topMargin + mlp.bottomMargin;
                if (childWidth + rowWidth > measuredWidthSize - getPaddingLeft() - getPaddingRight()) {
                    // 换行
                    maxWith = Math.max(maxWith, rowWidth);
                    rowWidth = childWidth;
                    maxHeight += rowHeight;
                    rowHeight = childHeight;
                } else {
                    rowWidth += childWidth;
                    rowHeight = Math.max(childHeight, rowHeight);

                }
                //最后一个控件
                if (i == count - 1) {
                    maxWith = Math.max(maxWith, rowWidth);
                    maxHeight += rowHeight;

                }
            }
        }
        String widthModeStr = null;
        switch (measuredWidthMode) {
            case MeasureSpec.AT_MOST:
                widthModeStr = "AT_MOST";
                if (count == 0) {
                    mWidth = 0;
                } else {
                    mWidth = maxWith + getPaddingLeft() + getPaddingRight();
                }
                break;
            case MeasureSpec.EXACTLY:
                widthModeStr = "EXACTLY";
                mWidth = getPaddingLeft() + getPaddingRight() + measuredWidthSize;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + measuredWidthMode);
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        String heightModeStr = null;
        switch (measuredHeightMode) {
            case MeasureSpec.AT_MOST:
                heightModeStr = "AT_MOST";
                if (count == 0) {
                    mHeight = 0;
                } else {
                    mHeight = maxHeight + getPaddingTop() + getPaddingBottom();
                }
                break;
            case MeasureSpec.EXACTLY:
                heightModeStr = "EXACTLY";
                mHeight = getPaddingTop() + getPaddingBottom() + measuredHeightSize;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + measuredHeightMode);
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        //真实高度
        realHeight = maxHeight + getPaddingTop() + getPaddingBottom();
        //测量高度
        if (measuredHeightMode == MeasureSpec.EXACTLY) {
            scrollable = realHeight > mHeight;
        } else {
            scrollable = realHeight > measuredHeightSize;
        }
        if (scrollable) {
            // 初始化上下边界值
            MarginLayoutParams lp1 = (MarginLayoutParams) getChildAt(0).getLayoutParams();
            topBorder = getChildAt(0).getTop() - lp1.topMargin;
            if (measuredHeightMode == MeasureSpec.EXACTLY) {
                bottomBorder = realHeight - mHeight + getPaddingBottom();
            } else {
                bottomBorder = realHeight - measuredHeightSize + getPaddingBottom();
            }
        }
        setMeasuredDimension(mWidth, mHeight);
        String str = "@widthMode#" + widthModeStr + ":" + measuredWidthSize + "@heightMode#" + heightModeStr + ":" + measuredHeightSize;
        Log.d("Layout尺寸", str);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mChildPos.clear();
        int rowWidth = 0;// 临时记录行宽
        int rowHeight = 0;// 临时记录行高
        int maxWith = 0;
        int maxHeight = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            MarginLayoutParams mlp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + mlp.rightMargin + mlp.rightMargin;
            int childHeight = child.getMeasuredHeight() + mlp.topMargin + mlp.bottomMargin;
            if (childWidth + rowWidth > getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) {
                // 换行
                maxWith = Math.max(maxWith, rowWidth);
                rowWidth = childWidth;
                maxHeight += rowHeight;
                rowHeight = childHeight;
                mChildPos.add(new ChildPosition(
                        getPaddingLeft() + mlp.leftMargin,
                        getPaddingTop() + maxHeight + mlp.topMargin,
                        getPaddingLeft() + childWidth - mlp.rightMargin,
                        getPaddingTop() + maxHeight + childHeight - mlp.bottomMargin
                ));

            } else {
                // 不换行
                mChildPos.add(new ChildPosition(
                        getPaddingLeft() + rowWidth + mlp.leftMargin,
                        getPaddingTop() + maxHeight + mlp.topMargin,
                        getPaddingLeft() + rowWidth + childWidth - mlp.rightMargin,
                        getPaddingTop() + maxHeight + childHeight - mlp.bottomMargin
                ));
                rowWidth += childWidth;
                rowHeight = Math.max(childHeight, rowHeight);

            }
        }

        // 布局每一个child
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            Log.i("Child大小W", child.getMeasuredWidth() + "#H:" + child.getMeasuredHeight());
            ChildPosition pos = mChildPos.get(i);
            //设置View的左边、上边、右边底边位置
            child.layout(pos.left, pos.top, pos.right, pos.bottom);
        }
    }

    // 自定义ViewGroup必须要有以下这个方法，否则拿不到child的margin的信息
    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }


    private float mLastYMove;
    private float currentY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (scrollable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    this.mLastYMove = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    this.currentY = event.getRawY();
                    int scrolledY = getScrollY();
                    float diff = Math.abs(this.mLastYMove - this.currentY);
                    // 当手指拖动值大于TouchSlop值时，认为应该进行滚动，拦截子控件的事件
                    if (diff > mTouchSlop) {
                        int dy = (int) (this.mLastYMove - this.currentY);
                        if (scrolledY + dy < topBorder) {
                            dy = 0;
                            scrollTo(0, topBorder);
                            return true;
                            //最顶端，超过0时，不再下拉，要是不设置这个，getScrollY一直是负数
                        } else if (scrolledY + dy > bottomBorder) {
                            dy = 0;
                            scrollTo(0, bottomBorder);
                            return true;
                        }
                        scrollBy(0, dy);
                        this.mLastYMove = event.getRawY();
                    }
                    break;
            }
        }
        return true;
    }
}
