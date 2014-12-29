package hu.dushu.developers.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by renfeng on 12/29/14.
 */
public class MyView extends View {

    /*
     * constructors for being created in code, resource, and inflation
     */

    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);

        int height = 100;
        int width = 100;

        if (hMode == MeasureSpec.EXACTLY) {
            height = hSize;
        } else if (hMode == MeasureSpec.AT_MOST) {
            /*
             * TODO Wrap Content
             */
        }

        if (wMode == MeasureSpec.EXACTLY) {
            width = hSize;
        } else if (hMode == MeasureSpec.AT_MOST) {
            /*
             * TODO Wrap Content
             */
        }

        setMeasuredDimension(width, height);

//        if (AccessibilityManager.getInstance(mContext).isEnabled()) {
//            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
//        }

        return;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return true;
    }
}
