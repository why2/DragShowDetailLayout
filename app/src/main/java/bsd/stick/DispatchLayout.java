package bsd.stick;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by ShiDa.Bian on 2017/2/22.
 * 拦截内部view事件
 */
public class DispatchLayout extends LinearLayout {
    public DispatchLayout(Context context) {
        super(context);
    }

    public DispatchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DispatchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
