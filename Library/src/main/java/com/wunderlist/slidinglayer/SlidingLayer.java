/*
 * SlidingLayer.java
 * 
 * Copyright (C) 2015 6 Wunderkinder GmbH.
 * 
 * @author      Jose L Ugia - @Jl_Ugia
 * @author      Antonio Consuegra - @aconsuegra
 * @author      Cesar Valiente - @CesarValiente
 * @version     1.2.0
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

package com.wunderlist.slidinglayer;

import java.util.Random;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;


public class SlidingLayer extends FrameLayout {

    private static final String STATE_KEY = "state";

    /**
     * Special value for the position of the layer. STICK_TO_RIGHT means that
     * the view shall be attached to the right side of the screen, and come from
     * there into the viewable area.
     */
    public static final int STICK_TO_RIGHT = -1;

    /**
     * Special value for the position of the layer. STICK_TO_LEFT means that the
     * view shall be attached to the left side of the screen, and come from
     * there into the viewable area.
     */
    public static final int STICK_TO_LEFT = -2;

    /**
     * Special value for the position of the layer. STICK_TO_TOP means that the view will stay attached to the top
     * part of the screen, and come from there into the viewable area.
     */
    public static final int STICK_TO_TOP = -3;

    /**
     * Special value for the position of the layer. STICK_TO_BOTTOM means that the view will stay attached to the
     * bottom part of the screen, and come from there into the viewable area.
     */
    public static final int STICK_TO_BOTTOM = -4;

    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;

    private static final int HIGH_VELOCITY = 9000;
    private static final int MAX_SCROLLING_DURATION = 600; // in ms
    private static final int MIN_DISTANCE_FOR_FLING = 10; // in dip
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
    private static final int INVALID_VALUE = -1;
    protected int mActivePointerId = INVALID_VALUE;
    protected VelocityTracker mVelocityTracker;
    protected int mMaximumVelocity;

    private Random mRandom;
    protected Bundle mState;

    private Scroller mScroller;

    private int mShadowSize;
    private Drawable mShadowDrawable;
    private boolean mForceLayout;

    /**
     * The size of the panel that sticks out when closed
     */
    private int mOffsetDistance;

    private boolean mDrawingCacheEnabled;
    private int mScreenSide;

    /**
     * If the user taps the layer then we will switch state it if enabled.
     */
    private boolean changeStateOnTap = true;

    /**
     * The size of the panel in preview mode
     */
    private int mPreviewOffsetDistance = INVALID_VALUE;

    private boolean mEnabled = true;
    private boolean mSlidingFromShadowEnabled = true;
    private boolean mIsDragging;
    private boolean mIsUnableToDrag;
    private int mTouchSlop;

    private float mLastX = INVALID_VALUE;
    private float mLastY = INVALID_VALUE;

    private float mInitialX = INVALID_VALUE;
    private float mInitialY = INVALID_VALUE;
    private float mInitialRawX = INVALID_VALUE;
    private float mInitialRawY = INVALID_VALUE;

    /**
     * Flags to determine the state of the layer
     */
    private static final int STATE_CLOSED = 0;
    private static final int STATE_PREVIEW = 1;
    private static final int STATE_OPENED = 2;

    private int mCurrentState;

    private boolean mScrolling;

    private OnInteractListener mOnInteractListener;

    /**
     * Optional callback to notify client when scroll position has changed
     */
    private OnScrollListener mOnScrollListener;

    private int mMinimumVelocity;
    private int mFlingDistance;

    private LayerTransformer mLayerTransformer;

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
     * <li>{@link #setStickTo(int)} with param {@link #STICK_TO_RIGHT}</li>
     * <li>Use no shadow drawable. (i.e. with size of 0)</li>
     * <li>Close when the panel is tapped</li>
     * <li>Open when the offset is tapped, but will have an offset of 0</li>
     * </ol>
     *
     * @param context  a reference to an existing context
     * @param attrs    attribute set constructed from attributes set in android .xml file
     * @param defStyle style res id
     */
    public SlidingLayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Style
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingLayer);

        // Set the side of the screen
        setStickTo(ta.getInt(R.styleable.SlidingLayer_stickTo, STICK_TO_RIGHT));

        // Sets the shadow drawable
        int shadowRes = ta.getResourceId(R.styleable.SlidingLayer_shadowDrawable, INVALID_VALUE);
        if (shadowRes != INVALID_VALUE) {
            setShadowDrawable(shadowRes);
        }

        // Sets the shadow size
        mShadowSize = (int) ta.getDimension(R.styleable.SlidingLayer_shadowSize, 0);

        // Sets the ability to open or close the layer by tapping in any empty space
        changeStateOnTap = ta.getBoolean(R.styleable.SlidingLayer_changeStateOnTap, true);

        // How much of the view sticks out when closed
        mOffsetDistance = ta.getDimensionPixelOffset(R.styleable.SlidingLayer_offsetDistance, 0);

        // Sets the size of the preview summary, if any
        mPreviewOffsetDistance = ta.getDimensionPixelOffset(R.styleable.SlidingLayer_previewOffsetDistance,
                INVALID_VALUE);

        // If showing offset is greater than preview mode offset dimension, exception is thrown
        checkPreviewModeConsistency();

        ta.recycle();

        init();
    }

    private void init() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }

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
     * Method exposing the state of the panel
     *
     * @return returns the state of the panel (@link STATE_OPENED, STATE_CLOSED or STATE_PREVIEW). Please note
     * that if the panel was opened with smooth animation this method is not guaranteed to return
     * its final value until the panel has reached its final position.
     */
    private int getCurrentState() {
        return mCurrentState;
    }

    public boolean isOpened() {
        return mCurrentState == STATE_OPENED;
    }

    public boolean isInPreviewMode() {
        return mCurrentState == STATE_PREVIEW;
    }

    public boolean isClosed() {
        return mCurrentState == STATE_CLOSED;
    }

    public void openLayer(final boolean smoothAnimation) {
        setLayerState(STATE_OPENED, smoothAnimation);
    }

    public void openPreview(final boolean smoothAnimation) {
        if (mPreviewOffsetDistance == INVALID_VALUE) {
            throw new IllegalStateException("A value offset for the preview has to be specified in order to open " +
                    "the layer in preview mode. Use setPreviewOffsetDistance or set its associated XML property ");
        }
        setLayerState(STATE_PREVIEW, smoothAnimation);
    }

    public void closeLayer(final boolean smoothAnimation) {
        setLayerState(STATE_CLOSED, smoothAnimation);
    }

    private void setLayerState(final int state, final boolean smoothAnimation) {
        setLayerState(state, smoothAnimation, false);
    }

    private void setLayerState(final int state, final boolean smoothAnimation, boolean force) {
        setLayerState(state, smoothAnimation, force, 0, 0);
    }

    private void setLayerState(final int state, final boolean smoothAnimation, final boolean force,
                               final int velocityX, final int velocityY) {

        if (!force && mCurrentState == state) {
            setDrawingCacheEnabled(false);
            return;
        }

        if (mOnInteractListener != null) {
            notifyActionStartedForState(state);
        }

        final int pos[] = getDestScrollPosForState(state);

        if (smoothAnimation) {
            int velocity = allowedDirection() == HORIZONTAL ? velocityX : velocityY;
            smoothScrollTo(pos[0], pos[1], velocity);
        } else {
            completeScroll();
            scrollToAndNotify(pos[0], pos[1]);
        }

        mCurrentState = state;
    }

    /**
     * Sets the listener to be invoked after a switch change
     * {@link OnInteractListener}.
     *
     * @param listener Listener to set
     */
    public void setOnInteractListener(OnInteractListener listener) {
        mOnInteractListener = listener;
    }

    /**
     * Sets the listener to be invoked when the layer is being scrolled
     * {@link OnScrollListener}.
     *
     * @param listener Listener to set
     */
    public void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    /**
     * Sets the transformer to use when the layer is being scrolled
     * {@link LayerTransformer}.
     *
     * @param layerTransformer Transformer to adopt
     */
    public void setLayerTransformer(LayerTransformer layerTransformer) {
        mLayerTransformer = layerTransformer;
    }

    /**
     * Sets the shadow of the size which will be included within the view by
     * using padding since it's on the left of the view in this case
     *
     * @param shadowSize Desired size of the shadow
     * @see #getShadowSize()
     * @see #setShadowDrawable(Drawable)
     * @see #setShadowDrawable(int)
     */
    public void setShadowSize(final int shadowSize) {
        mShadowSize = shadowSize;
        invalidate(getLeft(), getTop(), getRight(), getBottom());
    }

    /**
     * Sets the shadow size by the value of a resource.
     *
     * @param resId The dimension resource id to be set as the shadow size.
     */
    public void setShadowSizeRes(int resId) {
        setShadowSize((int) getResources().getDimension(resId));
    }

    /**
     * Return the current size of the shadow.
     *
     * @return The size of the shadow in pixels
     */
    public int getShadowSize() {
        return mShadowSize;
    }

    /**
     * Sets a drawable that will be used to create the shadow for the layer.
     *
     * @param d Drawable append as a shadow
     */
    public void setShadowDrawable(final Drawable d) {
        mShadowDrawable = d;
        refreshDrawableState();
        setWillNotDraw(false);
        invalidate(getLeft(), getTop(), getRight(), getBottom());
    }

    /**
     * Sets a drawable resource that will be used to create the shadow for the
     * layer.
     *
     * @param resId Resource ID of a drawable
     */
    public void setShadowDrawable(int resId) {
        setShadowDrawable(getContext().getResources().getDrawable(resId));
    }

    /**
     * Sets the offset distance of the panel by using a dimension resource.
     *
     * @param resId The dimension resource id to be set as the offset.
     */
    public void setOffsetDistanceRes(int resId) {
        setOffsetDistance((int) getResources().getDimension(resId));
    }

    /**
     * Sets the offset distance of the panel. How much sticks out when off screen.
     *
     * @param offsetDistance Size of the offset in pixels
     * @see #getOffsetDistance()
     */
    public void setOffsetDistance(int offsetDistance) {
        mOffsetDistance = offsetDistance;
        checkPreviewModeConsistency();
        invalidate(getLeft(), getTop(), getRight(), getBottom());
    }

    /**
     * @return returns the number of pixels that are visible when the panel is closed
     */
    public int getOffsetDistance() {
        return mOffsetDistance;
    }

    /**
     * Sets the offset distance of the preview panel by using a dimension resource.
     *
     * @param resId The dimension resource id to be set as the size of the preview mode.
     */
    public void setPreviewOffsetDistanceRes(int resId) {
        setPreviewOffsetDistance((int) getResources().getDimension(resId));
    }

    /**
     * Sets the size of the panel when in preview mode.
     *
     * @param previewOffsetDistance Size of the offset in pixels
     * @see #getOffsetDistance()
     */
    public void setPreviewOffsetDistance(int previewOffsetDistance) {
        mPreviewOffsetDistance = previewOffsetDistance;

        checkPreviewModeConsistency();
        invalidate(getLeft(), getTop(), getRight(), getBottom());

        if (mCurrentState == STATE_PREVIEW) {
            smoothScrollToCurrentPosition();
        }
    }

    private void checkPreviewModeConsistency() {
        if (isPreviewModeEnabled() && mOffsetDistance > mPreviewOffsetDistance) {
            throw new IllegalStateException("The showing offset of the layer can never be greater than the " +
                    "offset dimension of the preview mode");
        }
    }

    /**
     * @return true if the preview mode is enabled
     */
    private boolean isPreviewModeEnabled() {
        return mPreviewOffsetDistance != INVALID_VALUE;
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
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);
        if (mState == null) {
            mState = new Bundle();
        }
        mState.putInt(STATE_KEY, mCurrentState);
        state.mState = mState;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        restoreState(savedState.mState);
    }

    public void restoreState(Parcelable in) {
        mState = (Bundle) in;
        int state = mState.getInt(STATE_KEY);
        setLayerState(state, true);
    }

    private float getViewX(MotionEvent event) {
        return event.getRawX();
    }

    private float getViewY(MotionEvent event) {
        return event.getRawY();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!mEnabled) {
            return false;
        }

        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mIsDragging = false;
            mIsUnableToDrag = false;
            mActivePointerId = INVALID_VALUE;
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
            if (activePointerId == INVALID_VALUE) {
                break;
            }
            final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
            final float x = getViewX(ev);
            final float dx = x - mLastX;
            final float xDiff = Math.abs(dx);
            final float y = getViewY(ev);
            final float dy = y - mLastY;
            final float yDiff = Math.abs(dy);

            if ((dx != 0 || dy != 0) &&
                    canScroll(this, false, (int) dx, (int) dy, (int) x, (int) y)) {
                mLastX = mInitialRawX = x;
                mLastY = mInitialRawY = y;
                mInitialX = ev.getX(pointerIndex);
                mInitialY = ev.getY(pointerIndex);
                return false;
            }

            final boolean validHorizontalDrag = xDiff > mTouchSlop && xDiff > yDiff;
            final boolean validVerticalDrag = yDiff > mTouchSlop && yDiff > xDiff;

            if (validHorizontalDrag) {
                mLastX = x;
            } else if (validVerticalDrag) {
                mLastY = y;
            }

            if (validHorizontalDrag || validVerticalDrag) {
                mIsDragging = true;
                setDrawingCacheEnabled(true);
            }
            break;

        case MotionEvent.ACTION_DOWN:
            mLastX = mInitialRawX = getViewX(ev);
            mLastY = mInitialRawY = getViewY(ev);
            mInitialX = ev.getX(0);
            mInitialY = ev.getY(0);
            mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

            if (touchPointIsWithinBounds(ev.getX(), ev.getY())) {
                mIsDragging = false;
                mIsUnableToDrag = false;
                // We don't want to do anything, send the event up
                return super.onInterceptTouchEvent(ev);
            } else {
                completeScroll();
                mIsDragging = false;
                mIsUnableToDrag = true;
            }
            break;
        case MotionEvent.ACTION_POINTER_UP:
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

        if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
            return false;
        }

        if (!mEnabled || !mIsDragging && !touchPointIsWithinBounds(mInitialX, mInitialY)) {
            return false;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            completeScroll();

            // Remember where the motion event started
            mLastX = mInitialRawX = getViewX(ev);
            mLastY = mInitialRawY = getViewY(ev);
            mInitialX = ev.getX();
            mInitialY = ev.getY();
            mActivePointerId = ev.getPointerId(0);
            break;
        }

        case MotionEvent.ACTION_MOVE: {

            final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);

            if (!touchPointIsWithinBounds(ev.getX(), ev.getY(), false)) return false;

            final float x = getViewX(ev);
            final float y = getViewY(ev);

            final float deltaX = mLastX - x;
            final float deltaY = mLastY - y;

            mLastX = x;
            mLastY = y;

            if (!mIsDragging) {

                final float xDiff = Math.abs(x - mInitialRawX);
                final float yDiff = Math.abs(y - mInitialRawY);

                final boolean validHorizontalDrag = xDiff > mTouchSlop && xDiff > yDiff;
                final boolean validVerticalDrag = yDiff > mTouchSlop && yDiff > xDiff;

                if (validHorizontalDrag || validVerticalDrag) {
                    mIsDragging = true;
                    setDrawingCacheEnabled(true);
                }
            }

            if (mIsDragging) {

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
                case STICK_TO_RIGHT:
                    rightBound = -getWidth();
                    topBound = bottomBound = leftBound = 0;
                    break;
                case STICK_TO_TOP:
                    topBound = getHeight();
                    bottomBound = rightBound = leftBound = 0;
                    break;
                case STICK_TO_BOTTOM:
                    bottomBound = -getHeight();
                    topBound = rightBound = leftBound = 0;
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

                scrollToAndNotify((int) scrollX, (int) scrollY);
            }
            break;
        }

        case MotionEvent.ACTION_UP: {

            if (mIsDragging) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                final int initialVelocityX = (int) VelocityTrackerCompat.getXVelocity(velocityTracker,
                        mActivePointerId);
                final int initialVelocityY = (int) VelocityTrackerCompat.getYVelocity(velocityTracker,
                        mActivePointerId);
                final int scrollX = getScrollX();
                final int scrollY = getScrollY();

                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float x = getViewX(ev);
                final float y = getViewY(ev);

                int nextState = determineNextStateForDrag(scrollX, scrollY, initialVelocityX, initialVelocityY,
                        (int) mInitialRawX, (int) mInitialRawY, (int) x, (int) y);
                setLayerState(nextState, true, true, initialVelocityX, initialVelocityY);

                mActivePointerId = INVALID_VALUE;
                endDrag();

            } else if (changeStateOnTap) {
                int nextState = determineNextStateAfterTap();
                setLayerState(nextState, true, true);
            }
            break;
        }

        case MotionEvent.ACTION_CANCEL:
            if (mIsDragging) {
                setLayerState(mCurrentState, true, true);
                mActivePointerId = INVALID_VALUE;
                endDrag();
            }
            break;

        case MotionEvent.ACTION_POINTER_DOWN: {
            final int pointerIndex = MotionEventCompat.getActionIndex(ev);
            mActivePointerId = ev.getPointerId(pointerIndex);
            mLastX = getViewX(ev);
            mLastY = getViewY(ev);
            break;

        }
        case MotionEvent.ACTION_POINTER_UP: {
            onSecondaryPointerUp(ev);
            final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
            mLastX = getViewX(ev);
            mLastY = getViewY(ev);
            break;
        }
        }

        return true;
    }

    /**
     * Checks if it's allowed to slide from the given position.
     *
     * @param touchX where the touch event started
     * @param touchY where the touch event started.
     * @return true if you can drag this view, false otherwise
     */
    private boolean touchPointIsWithinBounds(final float touchX, final float touchY) {
        return touchPointIsWithinBounds(touchX, touchY, true);
    }

    private boolean touchPointIsWithinBounds(final float touchX, final float touchY, boolean withinLayer) {

        int scroll = 0;
        float touch;

        if (allowedDirection() == HORIZONTAL) {
            if (withinLayer) scroll = getScrollX();
            touch = touchX;
        } else {
            if (withinLayer) scroll = getScrollY();
            touch = touchY;
        }

        switch (mScreenSide) {
        case STICK_TO_RIGHT:
        case STICK_TO_BOTTOM:
            return touch >= -scroll;
        case STICK_TO_LEFT:
            return touch <= getWidth() - scroll;
        case STICK_TO_TOP:
            return touch <= getHeight() - scroll;
        default:
            throw new IllegalStateException("The layer has to be stuck to one of the sides of the screen. " +
                    "Current value is: " + mScreenSide);
        }
    }

    protected boolean canScroll(View v, boolean checkV, int dx, int dy, int x, int y) {

        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();

            final int count = group.getChildCount();
            // Count backwards - let topmost views consume scroll distance first.
            for (int i = count - 1; i >= 0; i--) {
                // TODO: Add versioned support here for transformed views.
                // This will not work for transformed views in Honeycomb+
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() &&
                        y + scrollY >= child.getTop() && y + scrollY < child.getBottom() &&
                        canScroll(child, true, dx, dy, x + scrollX - child.getLeft(),
                                y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }

        return checkV && (
                (allowedDirection() == HORIZONTAL && ViewCompat.canScrollHorizontally(v, -dx) ||
                        allowedDirection() == VERTICAL && ViewCompat.canScrollVertically(v, -dy)));
    }

    /**
     * Based on the position and velocity of the layer we calculate what the next state should be.
     *
     * @param velocityX
     * @param velocityY
     * @param initialX
     * @param initialY
     * @param currentX
     * @param currentY
     * @return the state of the panel (@link STATE_OPENED, STATE_CLOSED or STATE_PREVIEW).
     */
    private int determineNextStateForDrag(final int scrollX, final int scrollY, final int velocityX,
                                          final int velocityY, final int initialX, final int initialY,
                                          final int currentX, final int currentY) {

        int panelOffset;
        int panelSize;
        int relativeVelocity;
        int absoluteDelta;

        if (allowedDirection() == HORIZONTAL) {
            panelSize = getWidth();
            panelOffset = Math.abs(panelSize - Math.abs(scrollX));
            absoluteDelta = Math.abs(currentX - initialX);
            relativeVelocity = velocityX * (mScreenSide == STICK_TO_LEFT ? 1 : -1);
        } else {
            panelSize = getHeight();
            panelOffset = Math.abs(panelSize - Math.abs(scrollY));
            absoluteDelta = Math.abs(currentY - initialY);
            relativeVelocity = velocityY * (mScreenSide == STICK_TO_TOP ? 1 : -1);
        }

        final int absoluteVelocity = Math.abs(relativeVelocity);
        final boolean isOverThreshold = absoluteDelta > mFlingDistance && absoluteVelocity > mMinimumVelocity;

        if (isOverThreshold) {

            if (relativeVelocity > 0) {
                return STATE_OPENED;
            } else {

                boolean goesToPreview = isPreviewModeEnabled()
                        && panelOffset > mPreviewOffsetDistance
                        && absoluteVelocity < HIGH_VELOCITY;

                if (goesToPreview) {
                    return STATE_PREVIEW;
                } else {
                    return STATE_CLOSED;
                }
            }

        } else {

            int openedThreshold = (panelSize + (isPreviewModeEnabled() ? mPreviewOffsetDistance : 0)) / 2;

            if (panelOffset > openedThreshold) {
                return STATE_OPENED;
            } else if (isPreviewModeEnabled() && panelOffset > mPreviewOffsetDistance / 2) {
                return STATE_PREVIEW;
            } else {
                return STATE_CLOSED;
            }
        }
    }

    /**
     * Based on the current state of the panel, this method returns the next state after tapping.
     *
     * @return the state of the panel (@link STATE_OPENED, STATE_CLOSED or STATE_PREVIEW).
     */
    private int determineNextStateAfterTap() {

        switch (mCurrentState) {
        case STATE_CLOSED:
            return isPreviewModeEnabled() ? STATE_PREVIEW : STATE_OPENED;
        case STATE_PREVIEW:
            return STATE_OPENED;
        case STATE_OPENED:
            return isPreviewModeEnabled() ? STATE_PREVIEW : STATE_CLOSED;
        }

        return STATE_CLOSED;
    }

    /**
     * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
     *
     * @param x the number of pixels to scroll by on the X axis
     * @param y the number of pixels to scroll by on the Y axis
     */
    void smoothScrollTo(int x, int y) {
        smoothScrollTo(x, y, 0);
    }

    /**
     * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
     *
     * @param x        the number of pixels to scroll by on the X axis
     * @param y        the number of pixels to scroll by on the Y axis
     * @param velocity the velocity associated with a fling, if applicable. (0
     *                 otherwise)
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
            if (mOnInteractListener != null) {
                notifyActionFinished();
            }
            return;
        }

        setDrawingCacheEnabled(true);
        mScrolling = true;

        final int width = getWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width);
        final float distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceRatio);

        int duration;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            duration = MAX_SCROLLING_DURATION;
        }
        duration = Math.min(duration, MAX_SCROLLING_DURATION);

        mScroller.startScroll(sx, sy, dx, dy, duration);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void smoothScrollToCurrentPosition() {
        int[] pos = getDestScrollPosForState(mCurrentState);
        smoothScrollTo(pos[0], pos[1]);
    }

    // We want the duration of the page snap animation to be influenced by the
    // distance that
    // the screen has to travel, however, we don't want this duration to be
    // effected in a
    // purely linear fashion. Instead, we use this method to moderate the effect
    // that the distance
    // of travel has on the overall snap duration.
    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    private void endDrag() {
        mIsDragging = false;
        mIsUnableToDrag = false;

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
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
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
                scrollToAndNotify(x, y);
            }
            if (mOnInteractListener != null) {
                notifyActionFinished();
            }
        }
        mScrolling = false;
    }

    private void scrollToAndNotify(int x, int y) {

        scrollTo(x, y);

        if (mOnScrollListener == null && mLayerTransformer == null) {
            return;
        }

        int scroll;
        if (allowedDirection() == VERTICAL) {
            scroll = getHeight() - Math.abs(y);
        } else {
            scroll = getWidth() - Math.abs(x);
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(Math.abs(scroll));
        }

        if (mLayerTransformer != null) {

            int absoluteScroll = Math.abs(scroll);
            int layerSize = allowedDirection() == HORIZONTAL ? getMeasuredWidth() : getMeasuredHeight();

            float layerProgress = (float) absoluteScroll / layerSize;
            float previewProgress = mPreviewOffsetDistance > 0 ?
                    Math.min(1, (float) absoluteScroll / mPreviewOffsetDistance) :
                    0;

            mLayerTransformer.internalTransform(this, previewProgress, layerProgress, mScreenSide);
        }
    }

    /**
     * Sets the default location where the SlidingLayer will appear
     *
     * @param screenSide The location where the Sliding layer will appear. Possible values are
     *                   {@link #STICK_TO_BOTTOM}, {@link #STICK_TO_LEFT}
     *                   {@link #STICK_TO_RIGHT}, {@link #STICK_TO_TOP}
     */
    public void setStickTo(int screenSide) {
        mForceLayout = true;
        mScreenSide = screenSide;
        setLayerState(STATE_CLOSED, false, true);
    }

    private int allowedDirection() {

        if (mScreenSide == STICK_TO_TOP || mScreenSide == STICK_TO_BOTTOM) {
            return VERTICAL;
        } else if (mScreenSide == STICK_TO_LEFT || mScreenSide == STICK_TO_RIGHT) {
            return HORIZONTAL;
        }

        throw new IllegalStateException("The screen side of the layer is illegal");
    }

    /**
     * Sets the behavior when tapping the sliding layer
     *
     * @param changeStateOnTap
     */
    public void setChangeStateOnTap(boolean changeStateOnTap) {
        this.changeStateOnTap = changeStateOnTap;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mLayerTransformer != null) {
            mLayerTransformer.onMeasure(this, mScreenSide);
        }

        super.onMeasure(getChildMeasureSpec(widthMeasureSpec, 0, width),
                getChildMeasureSpec(heightMeasureSpec, 0, height));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        boolean scrollMustChange = false;
        if (allowedDirection() == VERTICAL) {
            if (h != oldh) {
                scrollMustChange = true;
            }
        } else if (w != oldw) {
            scrollMustChange = true;
        }

        if (scrollMustChange) {
            completeScroll();
            int[] pos = getDestScrollPosForState(mCurrentState);
            scrollTo(pos[0], pos[1]);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if (mForceLayout) {
            mForceLayout = false;
            adjustLayoutParams();

            if (mScreenSide == STICK_TO_RIGHT) {
                setPadding(getPaddingLeft() + mShadowSize, getPaddingTop(), getPaddingRight(), getPaddingBottom());
            } else if (mScreenSide == STICK_TO_BOTTOM) {
                setPadding(getPaddingLeft(), getPaddingTop() + mShadowSize, getPaddingRight(), getPaddingBottom());
            } else if (mScreenSide == STICK_TO_LEFT) {
                setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight() + mShadowSize, getPaddingBottom());
            } else if (mScreenSide == STICK_TO_TOP) {
                setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() + mShadowSize);
            }
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void adjustLayoutParams() {

        ViewGroup.LayoutParams baseParams = getLayoutParams();

        if (baseParams instanceof LayoutParams) {

            LayoutParams layoutParams = (LayoutParams) baseParams;

            switch (mScreenSide) {
            case STICK_TO_BOTTOM:
                layoutParams.gravity = Gravity.BOTTOM;
                break;
            case STICK_TO_LEFT:
                layoutParams.gravity = Gravity.LEFT;
                break;
            case STICK_TO_RIGHT:
                layoutParams.gravity = Gravity.RIGHT;
                break;
            case STICK_TO_TOP:
                layoutParams.gravity = Gravity.TOP;
                break;
            }
            setLayoutParams(baseParams);

        } else if (baseParams instanceof RelativeLayout.LayoutParams) {

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) baseParams;

            switch (mScreenSide) {
            case STICK_TO_BOTTOM:
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case STICK_TO_LEFT:
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            case STICK_TO_RIGHT:
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            case STICK_TO_TOP:
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            }
        }

    }

    /**
     * Get the destination position based on the velocity
     *
     * @return
     * @since 1.0
     */
    private int[] getDestScrollPosForState(int state) {

        int[] pos = new int[2];

        if (state == STATE_OPENED) {
            return pos;
        } else {

            int layerOffset = state == STATE_CLOSED ? mOffsetDistance : mPreviewOffsetDistance;

            switch (mScreenSide) {
            case STICK_TO_RIGHT:
                pos[0] = -getWidth() + layerOffset;
                break;
            case STICK_TO_LEFT:
                pos[0] = getWidth() - layerOffset;
                break;
            case STICK_TO_TOP:
                pos[1] = getHeight() - layerOffset;
                break;
            case STICK_TO_BOTTOM:
                pos[1] = -getHeight() + layerOffset;
                break;
            }

            return pos;
        }
    }

    public int getContentLeft() {
        return getLeft() + getPaddingLeft();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // Draw the margin drawable if needed.
        if (mShadowSize > 0 && mShadowDrawable != null) {
            if (mScreenSide == STICK_TO_RIGHT) {
                mShadowDrawable.setBounds(0, 0, mShadowSize, getHeight());
            }
            if (mScreenSide == STICK_TO_TOP) {
                mShadowDrawable.setBounds(0, getHeight() - mShadowSize, getWidth(), getHeight());
            }
            if (mScreenSide == STICK_TO_LEFT) {
                mShadowDrawable.setBounds(getWidth() - mShadowSize, 0, getWidth(), getHeight());
            }
            if (mScreenSide == STICK_TO_BOTTOM) {
                mShadowDrawable.setBounds(0, 0, getWidth(), mShadowSize);
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
                    scrollToAndNotify(x, y);
                }

                // Keep on drawing until the animation has finished.
                ViewCompat.postInvalidateOnAnimation(this);
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
        void onOpen();

        /**
         * This method is called when an attempt is made to show the preview mode in the current
         * <code>SlidingLayer</code>. Note that because of animation, the <code>SlidingLayer</code> may not be
         * visible yet.
         */
        void onShowPreview();

        /**
         * This method is called when an attempt is made to close the current <code>SlidingLayer</code>. Note
         * that because of animation, the <code>SlidingLayer</code> may still be visible.
         */
        void onClose();

        /**
         * this method is executed after <code>onOpen()</code>, when the animation has finished.
         */
        void onOpened();

        /**
         * this method is executed after <code>onShowPreview()</code>, when the animation has finished.
         */
        void onPreviewShowed();

        /**
         * this method is executed after <code>onClose()</code>, when the animation has finished and the
         * <code>SlidingLayer</code> is
         * therefore no longer visible.
         */
        void onClosed();
    }

    private void notifyActionStartedForState(int state) {

        switch (state) {
        case STATE_CLOSED:
            mOnInteractListener.onClose();
            break;

        case STATE_PREVIEW:
            mOnInteractListener.onShowPreview();
            break;

        case STATE_OPENED:
            mOnInteractListener.onOpen();
            break;
        }
    }

    private void notifyActionFinished() {

        switch (mCurrentState) {
        case STATE_CLOSED:
            mOnInteractListener.onClosed();
            break;

        case STATE_PREVIEW:
            mOnInteractListener.onPreviewShowed();
            break;

        case STATE_OPENED:
            mOnInteractListener.onOpened();
            break;
        }
    }

    /**
     * Interface definition for a callback to be invoked when the layer has been scrolled.
     */
    public interface OnScrollListener {

        /**
         * Callback method to be invoked when the layer has been scrolled. This will be
         * called after the scroll has completed
         *
         * @param absoluteScroll The absolute scrolling delta relative to the position of the container
         */
        void onScroll(int absoluteScroll);
    }

    static class SavedState extends BaseSavedState {

        Bundle mState;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel in) {
            super(in);
            mState = in.readBundle();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(mState);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}