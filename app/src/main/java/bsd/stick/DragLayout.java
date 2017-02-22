package bsd.stick;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by ShiDa.Bian on 2017/2/22.
 * 垂直滚动，类似与淘宝查看详情
 */
public class DragLayout extends LinearLayout {
    private Scroller mScroller;
    private float mTouchSlop;
    //1，2，3
    private View viewOne, viewTwo, viewThree;
    //当前的view
    private View mCurrentTargetView;

    private float downY, downX;

    //标记子view是否可以滚动
    private boolean mChildHasScrolled;
    private VelocityTracker mVelocityTracker;
    private float mInitialInterceptY;

    private int mMaxFlingVelocity;
    private int mMiniFlingVelocity;
    private static final int DEFAULT_DURATION = 300;
    private int mDuration = DEFAULT_DURATION;


    //当前view标记
    private CurrentTargetIndex mCurrentViewIndex = CurrentTargetIndex.VIEWONE;

    public enum CurrentTargetIndex {
        VIEWONE,
        VIEWTWO,
        VIEWTHREE;

        public static CurrentTargetIndex valueOf(int index) {
            switch (index) {
                case 1:
                    return VIEWTWO;
                case 2:
                    return VIEWTHREE;
                default:
                    return VIEWONE;
            }
        }
    }


    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext(), new DecelerateInterpolator());
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mMaxFlingVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        mMiniFlingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        setOrientation(VERTICAL);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (2 >= getChildCount()) {
            throw new RuntimeException("SlideDetailsLayout only accept childs more than 2!!");
        }
        viewOne = getChildAt(0);
        viewTwo = getChildAt(1);
        viewThree = getChildAt(2);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //如果手指还在拖动
        if (!mScroller.isFinished()) {
            resetDownPosition(ev);
            return true;
        }
        Log.i("=============", "======我还在拖动::" + getScrollY());
        Log.i("=============", "======我还在拖动::" + mCurrentViewIndex);
        //禁止子view可以自己处理事件
        requestDisallowInterceptTouchEvent(false);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                resetDownPosition(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                adjustValidDownPoint(ev);
                return checkCanInterceptTouchEvent(ev);
            default:
                break;
        }
        return false;
    }

    private void adjustValidDownPoint(MotionEvent event) {
        if (mCurrentViewIndex == CurrentTargetIndex.VIEWONE && event.getY() > downY
                || mCurrentViewIndex == CurrentTargetIndex.VIEWTHREE && event.getY() < downY
                || mCurrentViewIndex == CurrentTargetIndex.VIEWTWO) {
            downX = event.getX();
            downY = event.getY();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                flingToFinishScroll();
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_MOVE:
                scroll(event);
                break;
            default:
                break;
        }
        return true;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }
    }

    /***
     * 判断MotionEvent是否处于View上面
     */
    protected boolean isTransformedTouchPointInView(MotionEvent ev, View view) {
        float x = ev.getRawX();
        float y = ev.getRawY();
        int[] rect = new int[2];
        view.getLocationInWindow(rect);
        float localX = x - rect[0];
        float localY = y - rect[1];
        return localX >= 0 && localX < (view.getRight() - view.getLeft())
                && localY >= 0 && localY < (view.getBottom() - view.getTop());
    }


    private boolean checkCanInterceptTouchEvent(MotionEvent ev) {
        final float xDiff = ev.getX() - downX;
        final float yDiff = ev.getY() - downY;
        //子view不可以在滚动指定距离了
        if (!canChildScrollVertically((int) yDiff, ev)) {
            mInitialInterceptY = (int) ev.getY();
            if (Math.abs(yDiff) > mTouchSlop && Math.abs(yDiff) >= Math.abs(xDiff)
                    && !(mCurrentViewIndex == CurrentTargetIndex.VIEWONE && yDiff > 0
                    || mCurrentViewIndex == CurrentTargetIndex.VIEWTHREE && yDiff < 0)) {
                Log.i("================", "==============yDiff=" + yDiff);
                return true;
            }
        }
        return false;
    }


    /***
     * 判断view是否可以滚动
     * first    can view self  ScrollVertically
     * seconde  if View is ViewPager only judge current page
     * third    if view is viewgroup check it`s children
     */
    private boolean canScrollVertically(View view, int offSet, MotionEvent ev) {

        if (!mChildHasScrolled && !isTransformedTouchPointInView(ev, view)) {
            return false;
        }
        if (ViewCompat.canScrollVertically(view, offSet)) {
            mChildHasScrolled = true;
            return true;
        }

        if (view instanceof ViewGroup) {
            ViewGroup vGroup = (ViewGroup) view;
            for (int i = 0; i < vGroup.getChildCount(); i++) {
                if (canScrollVertically(vGroup.getChildAt(i), offSet, ev)) {
                    mChildHasScrolled = true;
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 当前view的子view是否可以滚动
     *
     * @param offSet
     * @param ev
     * @return
     */
    protected boolean canChildScrollVertically(int offSet, MotionEvent ev) {
        mCurrentTargetView = getCurrentTargetView();
        return canScrollVertically(mCurrentTargetView, -offSet, ev);
    }


    /**
     * 当前view
     *
     * @return
     */
    private View getCurrentTargetView() {
        switch (mCurrentViewIndex) {
            case VIEWTWO:
                return viewTwo;
            case VIEWTHREE:
                return viewThree;
            default:
                return viewOne;
        }
    }

    /**
     * 重置速度追踪器
     *
     * @param ev
     */
    private void resetDownPosition(MotionEvent ev) {
        downY = ev.getX();
        downX = ev.getY();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.clear();
        mChildHasScrolled = false;
        mInitialInterceptY = (int) ev.getY();
    }

    /**
     * 拦截之后的拖动
     */
    private void scroll(MotionEvent event) {
        //第一个view
        if (mCurrentViewIndex == CurrentTargetIndex.VIEWONE) {
            if (getScrollY() <= 0 && event.getY() >= mInitialInterceptY) {
                mInitialInterceptY = event.getY();
            }
            int distace = mInitialInterceptY - event.getY() >= 0 ? (int) (mInitialInterceptY - event.getY()) : 0;
            scrollTo(0, distace);
        } else if (mCurrentViewIndex == CurrentTargetIndex.VIEWTHREE) {
            //第三个view
            //往下拉
            if (getScrollY() >= viewOne.getMeasuredHeight() + viewTwo.getMeasuredHeight() && event.getY() <= mInitialInterceptY) {
                mInitialInterceptY = (int) event.getY();
            }
            int distance = event.getY() <= mInitialInterceptY ? viewOne.getMeasuredHeight() + viewTwo.getMeasuredHeight()
                    : (int) (mInitialInterceptY - event.getY() + viewOne.getMeasuredHeight() + viewTwo.getMeasuredHeight());
            scrollTo(0, distance);
        } else {
            //第二个view
            int distance = (int) (viewOne.getMeasuredHeight() + mInitialInterceptY - event.getY());
            scrollTo(0, distance);
        }
        mVelocityTracker.addMovement(event);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }


    /***
     * 复用已经实现的View，省却了测量布局之类的麻烦
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    }

    /**
     * 清理VelocityTracker
     */

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


    /**
     * 这里会告诉最终会停留在哪个上面，没有告诉之前，还是当前的view
     */
    private void flingToFinishScroll() {

        final int oneViewHeight = viewOne.getMeasuredHeight();
        final int twoViewHeight = viewTwo.getMeasuredHeight();
        //推荐需要惯性的大小
        final int threshold = getResources().getDisplayMetrics().heightPixels / 3;
        //需要惯性滑动的距离
        float needFlingDistance = 0;
        if (mCurrentViewIndex == CurrentTargetIndex.VIEWONE) {
            //当前view是viewOne
            //整个view没有平移过
            if (getScrollY() <= 0) {
                needFlingDistance = 0;
            } else if (getScrollY() <= threshold) {
                //应该滑会原处
                if (needFlingToToggleView()) {
                    //这里是指从第一个到第二个的时候，回弹到第二个
                    needFlingDistance = oneViewHeight - getScrollY();
                    mCurrentViewIndex = CurrentTargetIndex.VIEWTWO;
                } else {
                    //回弹到第一个
                    needFlingDistance = -getScrollY();
                }
            } else {
                //要滑动到第二个，需要到下一个
                needFlingDistance = oneViewHeight - getScrollY();
                mCurrentViewIndex = CurrentTargetIndex.VIEWTWO;
            }
        } else if (mCurrentViewIndex == CurrentTargetIndex.VIEWTWO) {
            //当前view是viewTwo
            //回弹到viewTwo
            if (Math.abs(oneViewHeight - getScrollY()) <= threshold) {
                needFlingDistance = oneViewHeight - getScrollY();
                mCurrentViewIndex = CurrentTargetIndex.VIEWTWO;
            } else {
                //滑动到viewOne
                if (oneViewHeight - getScrollY() > threshold) {
                    needFlingDistance = -getScrollY();
                    mCurrentViewIndex = CurrentTargetIndex.VIEWONE;
                } else {
                    //滑动到viewThree
                    needFlingDistance = oneViewHeight + twoViewHeight - getScrollY();
                    mCurrentViewIndex = CurrentTargetIndex.VIEWTHREE;
                }
            }
        } else {
            //当前view是viewThree
            if (oneViewHeight + twoViewHeight <= getScrollY()) {
                needFlingDistance = 0;
            } else if (oneViewHeight + twoViewHeight - getScrollY() < threshold) {
                needFlingDistance = oneViewHeight + twoViewHeight - getScrollY();
                mCurrentViewIndex = CurrentTargetIndex.VIEWTHREE;
            } else {
                needFlingDistance = oneViewHeight - getScrollY();
                mCurrentViewIndex = CurrentTargetIndex.VIEWTWO;
            }
        }
        //开始滑动了
        mScroller.startScroll(0, getScrollY(), 0, (int) needFlingDistance, mDuration);
        postInvalidate();
    }


    private boolean needFlingToToggleView() {
        mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
        if (mCurrentViewIndex == CurrentTargetIndex.VIEWONE) {
            if (-mVelocityTracker.getYVelocity() > mMiniFlingVelocity) {
                return true;
            }
        } else if (mCurrentViewIndex == CurrentTargetIndex.VIEWTHREE) {
            if (mVelocityTracker.getYVelocity() > mMiniFlingVelocity) {
                return true;
            }
        } else if (mCurrentViewIndex == CurrentTargetIndex.VIEWTWO) {
            //中间这里不管怎么滑动，都是需要惯性滑动的
            if (Math.abs(mVelocityTracker.getYVelocity()) > mMiniFlingVelocity) {
                return true;
            }
        }
        return false;
    }


}
