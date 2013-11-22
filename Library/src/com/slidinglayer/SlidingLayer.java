/*
 * SlidingLayer.java
 * 
 * Copyright (C) 2013 6 Wunderkinder GmbH.
 * 
 * @author      Jose L Ugia - @Jl_Ugia
 * @author      Antonio Consuegra - @aconsuegra
 * @author      Cesar Valiente - @CesarValiente
 * @version     1.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.slidinglayer;

import java.lang.reflect.Method;
import java.util.Random;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;


public class SlidingLayer extends FrameLayout {

    /**
     * Default value for the position of the layer. STICK_TO_AUTO shall inspect the container and choose a stick
     * mode depending on the position of the layout (ie.: layout is positioned on the right = STICK_TO_RIGHT).
     */
    public static final int STICK_TO_AUTO = 0;

    /**
     * Special value for the position of the layer. STICK_TO_RIGHT means that the view shall be attached to the
     * right side of the screen, and come from there into the viewable area.
     */
    public static final int STICK_TO_RIGHT = -1;

    /**
     * Special value for the position of the layer. STICK_TO_LEFT means that the view shall be attached to the left
     * side of the screen, and come from there into the viewable area.
     */
    public static final int STICK_TO_LEFT = -2;

    /**
     * Special value for the position of the layer. STICK_TO_MIDDLE means that the view will stay attached trying to
     * be in the middle of the screen and allowing dismissing both to right and left side.
     */
    public static final int STICK_TO_MIDDLE = -3;

    /**
     * Special value for the position of the layer. STICK_TO_TOP means that the view will stay attached to the top
     * part of the screen, and come from there into the viewable area.
     */
    public static final int STICK_TO_TOP = -4;

    /**
     * Special value for the position of the layer. STICK_TO_BOTTOM means that the view will stay attached to the
     * bottom part of the screen, and come from there into the viewable area.
     */
    public static final int STICK_TO_BOTTOM = -5;

    private static final int MAX_SCROLLING_DURATION = 600; // in ms
    private static final int MIN_DISTANCE_FOR_FLING = 25; // in dip
    private static final Interpolator sMenuInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return (float) Math.pow(t, 5) + 1.0f;
        }
    };

    /**
     * Sentinel value for no current active pointer. Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;
    protected int mActivePointerId = INVALID_POINTER;
    protected VelocityTracker mVelocityTracker;
    protected int mMaximumVelocity;

    private Random mRandom;

    private Scroller mScroller;

    private int mShadowWidth;
    private Drawable mShadowDrawable;
    private boolean mForceLayout;

    /**
     * The with of the panel when closed
     */
    private int mOffsetWidth;

    private boolean mDrawingCacheEnabled;
    private int mScreenSide = STICK_TO_AUTO;

    /**
     * If the user taps the layer then we will close it if enabled.
     */
    private boolean closeOnTapEnabled = true;

    /**
     * If the user taps the offset then we will open it if enabled.
     */
    private boolean openOnTapEnabled = true;

    private boolean mEnabled = true;
    private boolean mSlidingFromShadowEnabled = true;
    private boolean mIsDragging;
    private boolean mIsUnableToDrag;
    private int mTouchSlop;

    private float mLastX = -1;
    private float mLastY = -1;

    private float mInitialX = -1;
    private float mInitialY = -1;

    private boolean mIsOpen;
    private boolean mScrolling;

    private OnInteractListener mOnInteractListener;

    private int mMinimumVelocity;
    private int mFlingDistance;
    private boolean mLastTouchAllowed = false;

    public SlidingLayer(Context context) {
        this(context, null);
    }

    public SlidingLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor for the sliding layer.<br>
     * By default this panel will
     * <ol>
     *     <li>{@link #setStickTo(int)} with param {@link #STICK_TO_AUTO}</li>
     * 	<li>Use no shadow drawable. (i.e. with width of 0)</li>
     * 	<li>Close when the panel is tapped</li>
     * 	<li>Open when the offset is tapped, but will have an offset of 0</li>
     * </ol>
     * @param context a reference to an existing context
     * @param attrs attribute set constructed from attributes set in android .xml file 
     * @param defStyle style res id
     */
    public SlidingLayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Style
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingLayer);

        // Set the side of the screen
        setStickTo(ta.getInt(R.styleable.SlidingLayer_stickTo, STICK_TO_AUTO));

        // Sets the shadow drawable
        int shadowRes = ta.getResourceId(R.styleable.SlidingLayer_shadowDrawable, -1);
        if (shadowRes != -1) {
            setShadowDrawable(shadowRes);
        }

        // Sets the shadow width
        setShadowWidth((int) ta.getDimension(R.styleable.SlidingLayer_shadowWidth, 0));

        // Sets the ability to close the layer by tapping in any empty space
        closeOnTapEnabled = ta.getBoolean(R.styleable.SlidingLayer_closeOnTapEnabled, true);
        // Sets the ability to open the layout by tapping on any of the exposed closed layer
        openOnTapEnabled = ta.getBoolean(R.styleable.SlidingLayer_openOnTapEnabled, true);

        // How much of the view sticks out when closed
        setOffsetWidth(ta.getDimensionPixelOffset(R.styleable.SlidingLayer_offsetWidth, 0));

        ta.recycle();

        init();
    }

    private void init() {
        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setFocusable(true);
        final Context context = getContext();
        mScroller = new Scroller(context, sMenuInterpolator);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        final float density = context.getResources().getDisplayMetrics().density;
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);

        mRandom = new Random();
    }

    /**
     * Returns whether the panel is open or not.
     * @return returns true if the panel is open, false otherwise. Please note that if 
     * the panel was opened with smooth animation this method is not guaranteed to return
     * true. This method will only return true after the panel has completely opened.
     */
    public boolean isOpened() {
        return mIsOpen;
    }

    public void openLayer(boolean smoothAnim) {
        openLayer(smoothAnim, false);
    }

    private void openLayer(boolean smoothAnim, boolean forceOpen) {
        switchLayer(true, smoothAnim, forceOpen, 0, 0);
    }

    public void closeLayer(boolean smoothAnim) {
        closeLayer(smoothAnim, false);
    }

    private void closeLayer(boolean smoothAnim, boolean forceClose) {
        switchLayer(false, smoothAnim, forceClose, 0, 0);
    }

    private void switchLayer(boolean open, boolean smoothAnim, boolean forceSwitch) {
        switchLayer(open, smoothAnim, forceSwitch, 0, 0);
    }

    private void switchLayer(final boolean open, final boolean smoothAnim, final boolean forceSwitch,
            final int velocityX, final int velocityY) {
        if (!forceSwitch && open == mIsOpen) {
            setDrawingCacheEnabled(false);
            return;
        }
        if (open) {
            if (mOnInteractListener != null) {
                mOnInteractListener.onOpen();
            }
        } else {
            if (mOnInteractListener != null) {
                mOnInteractListener.onClose();
            }
        }

        mIsOpen = open;

        // Get translation values
        float tx = mLastX - getWidth() / 2;
        float ty = mLastY - getHeight() / 2;

        // Get boolean for velocity check
        boolean noVelocityInStickToMidle = mScreenSide == STICK_TO_MIDDLE && Math.abs(velocityX) < mMinimumVelocity
                && Math.abs(velocityY) < mMinimumVelocity;

        // Follow velocity or translation depending on the case
        int dx = noVelocityInStickToMidle ? (int) tx : velocityX;
        int dy = noVelocityInStickToMidle ? (int) ty : velocityY;

        final int pos[] = getDestScrollPos(dx, dy);

        if (smoothAnim) {
            smoothScrollTo(pos[0], pos[1], Math.max(velocityX, velocityY));
        } else {
            completeScroll();
            scrollTo(pos[0], pos[1]);
        }
    }

    /**
     * Sets the listener to be invoked after a switch change {@link OnInteractListener}.
     * 
     * @param listener
     *            Listener to set
     */
    public void setOnInteractListener(OnInteractListener listener) {
        mOnInteractListener = listener;
    }

    /**
     * Sets the shadow width by the value of a resource.
     * 
     * @param resId
     *            The dimension resource id to be set as the shadow width.
     */
    public void setShadowWidthRes(int resId) {
        setShadowWidth((int) getResources().getDimension(resId));
    }

    /**
     * Return the current with of the shadow.
     * 
     * @return The size of the shadow in pixels
     */
    public int getShadowWidth() {
        return mShadowWidth;
    }

    /**
     * Sets the shadow of the width which will be included within the view by using padding since it's on the left
     * of the view in this case
     * 
     * @param shadowWidth
     *            Desired width of the shadow
     * @see #getShadowWidth()
     * @see #setShadowDrawable(Drawable)
     * @see #setShadowDrawable(int)
     */
    public void setShadowWidth(final int shadowWidth) {
        mShadowWidth = shadowWidth;
        invalidate(getLeft(), getTop(), getRight(), getBottom());
    }

    /**
     * Sets a drawable that will be used to create the shadow for the layer.
     * 
     * @param d
     *            Drawable append as a shadow
     */
    public void setShadowDrawable(final Drawable d) {
        mShadowDrawable = d;
        refreshDrawableState();
        setWillNotDraw(false);
        invalidate(getLeft(), getTop(), getRight(), getBottom());
    }

    /**
     * Sets a drawable resource that will be used to create the shadow for the layer.
     * 
     * @param resId
     *            Resource ID of a drawable
     */
    public void setShadowDrawable(int resId) {
        setShadowDrawable(getContext().getResources().getDrawable(resId));
    }

    /**
     * Sets the offset width of the panel. How much sticks out when off screen.
     * 
     * @param offsetWidth
     *            Width of the offset in pixels
     * @see #getOffsetWidth()
     */
    public void setOffsetWidth(int offsetWidth) {
        mOffsetWidth = offsetWidth;
        invalidate(getLeft(), getTop(), getRight(), getBottom());
    }

    /**
     * 
     * @return returns the number of pixels that are visible when the panel is closed
     */
    public int getOffsetWidth() {
        return mOffsetWidth;
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mShadowDrawable;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final Drawable d = mShadowDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }

    public boolean isSlidingEnabled() {
        return mEnabled;
    }

    public void setSlidingEnabled(boolean _enabled) {
        mEnabled = _enabled;
    }

    public boolean isSlidingFromShadowEnabled() {
        return mSlidingFromShadowEnabled;
    }

    public void setSlidingFromShadowEnabled(boolean _slidingShadow) {
        mSlidingFromShadowEnabled = _slidingShadow;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!mEnabled) {
            return false;
        }

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mIsDragging = false;
            mIsUnableToDrag = false;
            mActivePointerId = INVALID_POINTER;
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN) {
            if (mIsDragging) {
                return true;
            } else if (mIsUnableToDrag) {
                return false;
            }
        }

        switch (action) {
        case MotionEvent.ACTION_MOVE:
            final int activePointerId = mActivePointerId;
            if (activePointerId == INVALID_POINTER) {
                break;
            }

            final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
            if (pointerIndex == -1) {
                mActivePointerId = INVALID_POINTER;
                break;
            }

            final float x = MotionEventCompat.getX(ev, pointerIndex);
            final float dx = x - mLastX;
            final float xDiff = Math.abs(dx);
            final float y = MotionEventCompat.getY(ev, pointerIndex);
            final float dy = y - mLastY;
            final float yDiff = Math.abs(y - mLastY);

            if (xDiff > mTouchSlop && xDiff > yDiff && allowDragingX(dx, mInitialX)) {
                mIsDragging = true;
                mLastX = x;
                setDrawingCacheEnabled(true);
            } else if (yDiff > mTouchSlop && yDiff > xDiff && allowDragingY(dy, mInitialY)) {
                mIsDragging = true;
                mLastY = y;
                setDrawingCacheEnabled(true);
            }
            break;

        case MotionEvent.ACTION_DOWN:
            mActivePointerId = ev.getAction()
                    & (Build.VERSION.SDK_INT >= 8 ? MotionEvent.ACTION_POINTER_INDEX_MASK
                            : MotionEventCompat.ACTION_POINTER_INDEX_MASK);
            mLastX = mInitialX = MotionEventCompat.getX(ev, mActivePointerId);
            mLastY = mInitialY = MotionEventCompat.getY(ev, mActivePointerId);
            if (allowSlidingFromHereX(ev, mInitialX)) {
                mIsDragging = false;
                mIsUnableToDrag = false;
                // If nobody else got the focus we use it to close the layer
                return super.onInterceptTouchEvent(ev);
            } else if (allowSlidingFromHereY(ev, mInitialY)) {
                mIsDragging = false;
                mIsUnableToDrag = false;
                // If nobody else got the focus we use it to close the layer
                return super.onInterceptTouchEvent(ev);
            } else {
                mIsUnableToDrag = true;
            }
            break;
        case MotionEventCompat.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            break;
        }

        if (!mIsDragging) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);
        }

        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mEnabled || !mIsDragging && !mLastTouchAllowed && !allowSlidingFromHereX(ev, mInitialX)
                && !allowSlidingFromHereY(ev, mInitialY)) {
            return false;
        }

        final int action = ev.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_OUTSIDE) {
            mLastTouchAllowed = false;
        } else {
            mLastTouchAllowed = true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action & MotionEventCompat.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            completeScroll();

            // Remember where the motion event started
            mLastX = mInitialX = ev.getX();
            mLastY = mInitialY = ev.getY();
            mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
            break;
        case MotionEvent.ACTION_MOVE:
            if (!mIsDragging) {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex == -1) {
                    mActivePointerId = INVALID_POINTER;
                    break;
                }
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float xDiff = Math.abs(x - mLastX);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff = Math.abs(y - mLastY);
                if (xDiff > mTouchSlop && xDiff > yDiff) {
                    mIsDragging = true;
                    mLastX = x;
                    setDrawingCacheEnabled(true);
                } else if (yDiff > mTouchSlop && yDiff > xDiff) {
                    mIsDragging = true;
                    mLastY = y;
                    setDrawingCacheEnabled(true);
                }
            }
            if (mIsDragging) {
                // Scroll to follow the motion event
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (activePointerIndex == -1) {
                    mActivePointerId = INVALID_POINTER;
                    break;
                }
                final float x = MotionEventCompat.getX(ev, activePointerIndex);
                final float y = MotionEventCompat.getY(ev, activePointerIndex);
                final float deltaX = mLastX - x;
                final float deltaY = mLastY - y;
                mLastX = x;
                mLastY = y;
                final float oldScrollX = getScrollX();
                final float oldScrollY = getScrollY();
                float scrollX = oldScrollX + deltaX;
                float scrollY = oldScrollY + deltaY;

                // Log.d("Layer", String.format("Layer scrollX[%f],scrollY[%f]", scrollX, scrollY));
                final float leftBound, rightBound;
                final float bottomBound, topBound;
                switch (mScreenSide) {
                case STICK_TO_LEFT:
                    topBound = bottomBound = rightBound = 0;
                    leftBound = getWidth(); // How far left we can scroll
                    break;
                case STICK_TO_MIDDLE:
                    topBound = getHeight();
                    bottomBound = -getHeight();
                    leftBound = getWidth();
                    rightBound = -getWidth();
                    break;
                case STICK_TO_RIGHT:
                    rightBound = -getWidth();
                    topBound = bottomBound = leftBound = 0;
                    break;
                case STICK_TO_TOP:
                    topBound = getHeight();
                    bottomBound = rightBound = leftBound = 0;
                    break;
                case STICK_TO_BOTTOM:
                    topBound = rightBound = leftBound = 0;
                    bottomBound = -getHeight();
                    break;
                default:
                    topBound = bottomBound = rightBound = leftBound = 0;
                    break;
                }
                if (scrollX > leftBound) {
                    scrollX = leftBound;
                } else if (scrollX < rightBound) {
                    scrollX = rightBound;
                }
                if (scrollY > topBound) {
                    scrollY = topBound;
                } else if (scrollY < bottomBound) {
                    scrollY = bottomBound;
                }

                // Keep the precision
                mLastX += scrollX - (int) scrollX;
                mLastY += scrollY - (int) scrollY;
                scrollTo((int) scrollX, (int) scrollY);
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mIsDragging) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                final int initialVelocityX = (int) VelocityTrackerCompat.getXVelocity(velocityTracker,
                        mActivePointerId);
                final int initialVelocityY = (int) VelocityTrackerCompat.getYVelocity(velocityTracker,
                        mActivePointerId);
                final int scrollX = getScrollX();
                final int scrollY = getScrollY();
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float x = MotionEventCompat.getX(ev, activePointerIndex);
                final float y = MotionEventCompat.getY(ev, activePointerIndex);
                final int totalDeltaX = (int) (x - mInitialX);
                final int totalDeltaY = (int) (y - mInitialY);

                boolean nextStateOpened = determineNextStateOpened(mIsOpen, scrollX, scrollY, initialVelocityX,
                        initialVelocityY, totalDeltaX, totalDeltaY);
                switchLayer(nextStateOpened, true, true, initialVelocityX, initialVelocityY);

                mActivePointerId = INVALID_POINTER;
                endDrag();
            } else if (mIsOpen && closeOnTapEnabled) {
                closeLayer(true);
            } else if (!mIsOpen && openOnTapEnabled) {
                openLayer(true);
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            if (mIsDragging) {
                switchLayer(mIsOpen, true, true);
                mActivePointerId = INVALID_POINTER;
                endDrag();
            }
            break;
        case MotionEventCompat.ACTION_POINTER_DOWN: {
            final int index = MotionEventCompat.getActionIndex(ev);
            mLastX = MotionEventCompat.getX(ev, index);
            mLastY = MotionEventCompat.getY(ev, index);
            mActivePointerId = MotionEventCompat.getPointerId(ev, index);
            break;
        }
        case MotionEventCompat.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            mLastX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
            mLastY = MotionEventCompat.getY(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
            break;
        }
        if (mActivePointerId == INVALID_POINTER) {
            mLastTouchAllowed = false;
        }
        return true;
    }

    private boolean allowSlidingFromHereX(final MotionEvent ev, final float initialX) {
        switch (mScreenSide) {
        case STICK_TO_LEFT:
        case STICK_TO_RIGHT:
        case STICK_TO_MIDDLE:
            if (mIsOpen) {
                return true;
            }
            if (!mIsOpen && mOffsetWidth > 0) {
                switch (mScreenSide) {
                case STICK_TO_LEFT:
                    return initialX <= mOffsetWidth;
                case STICK_TO_RIGHT:
                    return initialX >= getWidth() - mOffsetWidth;
                }
            }
        default:
            return false;
        }
    }

    private boolean allowSlidingFromHereY(final MotionEvent ev, final float initialY) {
        switch (mScreenSide) {
        case STICK_TO_TOP:
        case STICK_TO_BOTTOM:
        case STICK_TO_MIDDLE:
            if (mIsOpen) {
                return true;
            }
            if (!mIsOpen && mOffsetWidth > 0) {
                switch (mScreenSide) {
                case STICK_TO_TOP:
                    return initialY <= mOffsetWidth;
                case STICK_TO_BOTTOM:
                    return initialY >= getHeight() - mOffsetWidth;
                }
            }
        default:
            return false;
        }
    }

    /**
     * Checks if the touch event is valid for dragging the view.
     * 
     * @param dx
     *            changed in delta from the initialX
     * @param initialX
     *            where the touch event started.
     * @return true if you can drag this view, false otherwise
     */
    private boolean allowDragingX(final float dx, final float initialX) {
        if (mIsOpen && getLeft() <= initialX || getRight() >= initialX) {
            switch (mScreenSide) {
            case STICK_TO_RIGHT:
                return dx > 0;
            case STICK_TO_LEFT:
                return dx < 0;
            case STICK_TO_MIDDLE:
                return dx != 0;
            }
        }
        if (!mIsOpen && mOffsetWidth > 0 && dx > 0) {
            switch (mScreenSide) {
            case STICK_TO_LEFT:
                return initialX <= mOffsetWidth && dx > 0;
            case STICK_TO_RIGHT:
                return initialX >= getWidth() - mOffsetWidth && dx < 0;
            case STICK_TO_MIDDLE:
                return dx != 0;
            }
        }
        return false;
    }

    private boolean allowDragingY(final float dy, final float initialY) {
        if (mIsOpen && getTop() <= initialY || getBottom() >= initialY) {
            switch (mScreenSide) {
            case STICK_TO_BOTTOM:
                return mIsOpen && dy > 0;
            case STICK_TO_TOP:
                return mIsOpen && dy < 0;
            case STICK_TO_MIDDLE:
                return mIsOpen && dy != 0;
            }
        }
        if (!mIsOpen && mOffsetWidth > 0 && dy > 0) {
            switch (mScreenSide) {
            case STICK_TO_TOP:
                return initialY <= mOffsetWidth && dy > 0;
            case STICK_TO_BOTTOM:
                return initialY >= getHeight() - mOffsetWidth && dy < 0;
            case STICK_TO_MIDDLE:
                return dy != 0;
            }
        }
        return false;
    }

    /**
     * Based on the current state, position and velocity of the layer we calculate what the next state should be.
     * 
     * @param currentState
     * @param swipeOffsetX
     * @param swipeOffsetY
     * @param velocityX
     * @param velocityY
     * @param deltaX
     * @param deltaY
     * @return true means we should open it, false close it.
     */
    private boolean determineNextStateOpened(final boolean currentState, final float swipeOffsetX,
            final float swipeOffsetY, final int velocityX, final int velocityY, final int deltaX, final int deltaY) {
        final boolean targetState;
        final boolean calcX;
        final boolean calcY;

        // Work out which velocity we should listen to.
        switch (mScreenSide) {
        case STICK_TO_TOP:
        case STICK_TO_BOTTOM:
            calcY = true;
            calcX = false;
            break;
        case STICK_TO_RIGHT:
        case STICK_TO_LEFT:
            calcX = true;
            calcY = false;
            break;
        case STICK_TO_MIDDLE:
            calcX = calcY = true;
            break;
        default:
            calcX = calcY = false;
            break;
        }

        if (calcX && Math.abs(deltaX) > mFlingDistance && Math.abs(velocityX) > mMinimumVelocity) {

            targetState = mScreenSide == STICK_TO_RIGHT && velocityX <= 0 || mScreenSide == STICK_TO_LEFT
                    && velocityX > 0;

        } else if (calcY && Math.abs(deltaY) > mFlingDistance && Math.abs(velocityY) > mMinimumVelocity) {

            targetState = mScreenSide == STICK_TO_BOTTOM && velocityY <= 0 || mScreenSide == STICK_TO_TOP
                    && velocityY > 0;

        } else {
            final int w = getWidth();
            final int h = getHeight();

            switch (mScreenSide) {
            case STICK_TO_RIGHT:
                targetState = swipeOffsetX > -w / 2;
                break;
            case STICK_TO_BOTTOM:
                targetState = swipeOffsetY > -h / 2;
                break;
            case STICK_TO_LEFT:
                targetState = swipeOffsetX < w / 2;
                break;
            case STICK_TO_TOP:
                targetState = swipeOffsetY < h / 2;
                break;
            case STICK_TO_MIDDLE:
                targetState = Math.abs(swipeOffsetX) < w / 2 && Math.abs(swipeOffsetY) < h / 2;
                break;
            default:
                targetState = true;
            }
        }

        return targetState;
    }

    /**
     * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
     * 
     * @param x
     *            the number of pixels to scroll by on the X axis
     * @param y
     *            the number of pixels to scroll by on the Y axis
     */
    void smoothScrollTo(int x, int y) {
        smoothScrollTo(x, y, 0);
    }

    /**
     * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
     * 
     * @param x
     *            the number of pixels to scroll by on the X axis
     * @param y
     *            the number of pixels to scroll by on the Y axis
     * @param velocity
     *            the velocity associated with a fling, if applicable. (0 otherwise)
     */
    void smoothScrollTo(int x, int y, int velocity) {
        if (getChildCount() == 0) {
            setDrawingCacheEnabled(false);
            return;
        }
        int sx = getScrollX();
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0) {
            completeScroll();
            if (mIsOpen) {
                if (mOnInteractListener != null) {
                    mOnInteractListener.onOpened();
                }
            } else {
                if (mOnInteractListener != null) {
                    mOnInteractListener.onClosed();
                }
            }
            return;
        }

        setDrawingCacheEnabled(true);
        mScrolling = true;

        final int width = getWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width);
        final float distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceRatio);

        int duration = 0;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            duration = MAX_SCROLLING_DURATION;
        }
        duration = Math.min(duration, MAX_SCROLLING_DURATION);

        mScroller.startScroll(sx, sy, dx, dy, duration);
        invalidate();
    }

    // We want the duration of the page snap animation to be influenced by the distance that
    // the screen has to travel, however, we don't want this duration to be effected in a
    // purely linear fashion. Instead, we use this method to moderate the effect that the distance
    // of travel has on the overall snap duration.
    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return FloatMath.sin(f);
    }

    private void endDrag() {
        mIsDragging = false;
        mIsUnableToDrag = false;
        mLastTouchAllowed = false;

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void setDrawingCacheEnabled(boolean enabled) {

        if (mDrawingCacheEnabled != enabled) {
            super.setDrawingCacheEnabled(enabled);
            mDrawingCacheEnabled = enabled;

            final int l = getChildCount();
            for (int i = 0; i < l; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != GONE) {
                    child.setDrawingCacheEnabled(enabled);
                }
            }
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastX = MotionEventCompat.getX(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void completeScroll() {
        boolean needPopulate = mScrolling;
        if (needPopulate) {
            // Done with scroll, no longer want to cache view drawing.
            setDrawingCacheEnabled(false);
            mScroller.abortAnimation();
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            }
            if (mIsOpen) {
                if (mOnInteractListener != null) {
                    mOnInteractListener.onOpened();
                }
            } else {
                if (mOnInteractListener != null) {
                    mOnInteractListener.onClosed();
                }
            }
        }
        mScrolling = false;
    }

    /**
     * Sets the default location where the SlidingLayer will appear
     * @param screenSide The location where the Sliding layer will appear. Possible values are
     * {@link #STICK_TO_AUTO}, {@link #STICK_TO_BOTTOM}, {@link #STICK_TO_LEFT}, {@link #STICK_TO_MIDDLE}
     * {@link #STICK_TO_RIGHT}, {@link #STICK_TO_TOP}
     */
    public void setStickTo(int screenSide) {

        if (screenSide != STICK_TO_AUTO) {
            mForceLayout = true;
        }

        mScreenSide = screenSide;
        closeLayer(false, true);
    }

    /**
     * If parameter is set to <code>true</code>, whenever the <code>SlidingLayer</code> is tapped and
     * the SlidingLayer is opened, it will attempt to close.
     * If parameter is set to <code>false</code>, then tapping the <code>SlidingLayer</code> will
     * do nothing
     * 
     * @param _closeOnTapEnabled  
     */
    public void setCloseOnTapEnabled(boolean _closeOnTapEnabled) {
        closeOnTapEnabled = _closeOnTapEnabled;
    }

    /**
     * Given that there is a visible offset and it is tapped, if the parameter is set 
     * to true it will attempt to open the <code>SlidingLayer</code>. If parameter is false, 
     * tapping a visible offset will yield no result.
     * @param _openOnTapEnabled
     */
    public void setOpenOnTapEnabled(boolean _openOnTapEnabled) {
        openOnTapEnabled = _openOnTapEnabled;
    }

    @SuppressWarnings("deprecation")
    private int getScreenSideAuto(int newLeft, int newRight) {

        int newScreenSide;

        if (mScreenSide == STICK_TO_AUTO) {
            int screenWidth;
            Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            try {
                Class<?> cls = Display.class;
                Class<?>[] parameterTypes = { Point.class };
                Point parameter = new Point();
                Method method = cls.getMethod("getSize", parameterTypes);
                method.invoke(display, parameter);
                screenWidth = parameter.x;
            } catch (Exception e) {
                screenWidth = display.getWidth();
            }

            boolean boundToLeftBorder = newLeft == 0;
            boolean boundToRightBorder = newRight == screenWidth;

            if (boundToLeftBorder == boundToRightBorder
                    && getLayoutParams().width == android.view.ViewGroup.LayoutParams.MATCH_PARENT) {
                newScreenSide = STICK_TO_MIDDLE;
            } else if (boundToLeftBorder) {
                newScreenSide = STICK_TO_LEFT;
            } else {
                newScreenSide = STICK_TO_RIGHT;
            }
        } else {
            newScreenSide = mScreenSide;
        }

        return newScreenSide;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);

        super.onMeasure(getChildMeasureSpec(widthMeasureSpec, 0, width),
                getChildMeasureSpec(heightMeasureSpec, 0, height));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Make sure scroll position is set correctly.
        if (w != oldw) {
            completeScroll();
            int[] pos = getDestScrollPos();
            scrollTo(pos[0], pos[1]);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int screenSide = mScreenSide;

        if (mScreenSide == STICK_TO_AUTO) {
            screenSide = getScreenSideAuto(left, right);
        }

        if (screenSide != mScreenSide || mForceLayout) {

            mForceLayout = false;

            mScreenSide = screenSide;
            closeLayer(false, true);

            if (mScreenSide == STICK_TO_RIGHT) {
                setPadding(getPaddingLeft() + mShadowWidth, getPaddingTop(), getPaddingRight(), getPaddingBottom());
            } else if (mScreenSide == STICK_TO_BOTTOM) {
                setPadding(getPaddingLeft(), getPaddingTop() + mShadowWidth, getPaddingRight(), getPaddingBottom());
            } else if (mScreenSide == STICK_TO_LEFT) {
                setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight() + mShadowWidth, getPaddingBottom());
            } else if (mScreenSide == STICK_TO_TOP) {
                setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() + mShadowWidth);
            } else if (mScreenSide == STICK_TO_MIDDLE) {
                setPadding(getPaddingLeft() + mShadowWidth, getPaddingTop(), getPaddingRight() + mShadowWidth,
                        getPaddingBottom());
            }
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private int[] getDestScrollPos() {
        return getDestScrollPos(0, 0);
    }

    /**
     * Get the x destination based on the velocity
     * 
     * @param xValue
     * @param yValue
     * @return
     * @since 1.0
     * 
     */
    private int[] getDestScrollPos(int xValue, int yValue) {

        int[] pos = new int[2];

        if (mIsOpen) {
            return pos;
        } else {

            switch (mScreenSide) {
            case STICK_TO_RIGHT:
                pos[0] = -getWidth() + mOffsetWidth;
                break;
            case STICK_TO_LEFT:
                pos[0] = getWidth() - mOffsetWidth;
                break;
            case STICK_TO_TOP:
                pos[1] = getHeight() - mOffsetWidth;
                break;
            case STICK_TO_BOTTOM:
                pos[1] = -getHeight() + mOffsetWidth;
                break;
            case STICK_TO_MIDDLE:

                // Calculate slope m to get direction of swiping and apply the same vector until the end of the
                // animation
                float m = 1;

                // If no veocity nor translation (difficult to get) the target is random
                if (xValue == 0 && yValue == 0) {
                    m = mRandom != null ? (float) Math.tan(mRandom.nextFloat() * Math.PI - Math.PI / 2) : 1;
                } else if (xValue == 0) {
                    // Avoid division by 0 (Get the max value of the tan which is equivalent)
                    m = (float) Math.tan(Math.PI / 2);
                } else {
                    // Get slope
                    m = yValue / (float) xValue;
                }

                if (Math.abs(m) >= 1) {
                    pos[0] = Math.round(getOperationSignForDiffMeasure(xValue) * getHeight() / Math.abs(m)
                            - (mLastX - getWidth() / 2));
                    pos[1] = Math.round(getOperationSignForDiffMeasure(yValue) * getHeight());
                } else {
                    pos[0] = Math.round(getOperationSignForDiffMeasure(xValue) * getWidth());
                    pos[1] = Math.round(getOperationSignForDiffMeasure(yValue) * getWidth() * Math.abs(m)
                            - (mLastY - getHeight() / 2));
                }
                break;

            }

            return pos;
        }
    }

    private int getOperationSignForDiffMeasure(float d) {
        if (mRandom == null) {
            return 1;
        } else {
            return Math.abs(d) < mMinimumVelocity ? mRandom.nextBoolean() ? 1 : -1 : d > 0 ? -1 : 1;
        }
    }

    public int getContentLeft() {
        return getLeft() + getPaddingLeft();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // Draw the margin drawable if needed.
        if (mShadowWidth > 0 && mShadowDrawable != null) {
            if (mScreenSide == STICK_TO_RIGHT) {
                mShadowDrawable.setBounds(0, 0, mShadowWidth, getHeight());
            }
            if (mScreenSide == STICK_TO_TOP) {
                mShadowDrawable.setBounds(0, getHeight() - mShadowWidth, getWidth(), getHeight());
            }
            if (mScreenSide == STICK_TO_LEFT) {
                mShadowDrawable.setBounds(getWidth() - mShadowWidth, 0, getWidth(), getHeight());
            }
            if (mScreenSide == STICK_TO_BOTTOM) {
                mShadowDrawable.setBounds(0, 0, getWidth(), mShadowWidth);
            }
            mShadowDrawable.draw(canvas);
        }
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                final int oldX = getScrollX();
                final int oldY = getScrollY();
                final int x = mScroller.getCurrX();
                final int y = mScroller.getCurrY();

                if (oldX != x || oldY != y) {
                    scrollTo(x, y);
                }

                // We invalidate a slightly larger area now, this was only optimised for right menu previously
                // Keep on drawing until the animation has finished. Just re-draw the necessary part
                invalidate(getLeft() + oldX, getTop() + oldY, getRight() - oldX, getBottom() - oldY);
                return;
            }
        }

        // Done with scroll, clean up state.
        completeScroll();
    }

    /**
     * Handler interface for obtaining updates on the <code>SlidingLayer</code>'s state.
     * <code>OnInteractListener</code> allows for external classes to be notified when the <code>SlidingLayer</code>
     * receives input to be opened or closed.
     */ 
    public interface OnInteractListener {

        /**
    	 * This method is called when an attempt is made to open the current <code>SlidingLayer</code>. Note
    	 * that because of animation, the <code>SlidingLayer</code> may not be visible yet. 
    	 */
        public void onOpen();
        
        /**
        * This method is called when an attempt is made to close the current <code>SlidingLayer</code>. Note
    	* that because of animation, the <code>SlidingLayer</code> may still be visible. 
    	*/
        public void onClose();

        /**
         * this method is executed after <code>onOpen()</code>, when the animation has finished. 
         */
        public void onOpened();

        /**
         * this method is executed after <code>onClose()</code>, when the animation has finished and the  <code>SlidingLayer</code> is
         * therefore no longer visible. 
         */
        public void onClosed();

    }

}
