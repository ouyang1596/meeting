package com.deshang365.meeting.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {
    private boolean isCanScroll = true;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    /*
     * @Override public void scrollTo(int x, int y) { if (isCanScroll ||
     * (getScrollX() - x > 50) || (getScrollX() - x) < -50 ) { super.scrollTo(x,
     * y); }
     * 
     * setCurrentItem(y); }
     */
    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        return isCanScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return false;
    }
}