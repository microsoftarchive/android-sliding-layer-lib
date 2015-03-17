package com.wunderlist.slidinglayer.transformer;

import android.view.View;

import com.wunderlist.slidinglayer.LayerTransformer;
import com.wunderlist.slidinglayer.SlidingLayer;

/**
 * Created by joseluisugia on 16/03/15.
 */
public final class SlideJoyTransformer extends LayerTransformer {

    private static final int DEFAULT_ANGLE = 10;

    private final float mMaxAngle;

    public SlideJoyTransformer() {
        this(DEFAULT_ANGLE);
    }

    public SlideJoyTransformer(float maxAngle) {
        mMaxAngle = maxAngle;
    }

    @Override
    public void transform(View layerView, float previewProgress, float layerProgress) {
    }

    @Override
    protected void internalTransform(View layerView, float previewProgress, float layerProgress, int screenSide) {

        int[] pivotPosition = pivotPositionForScreenSide(layerView, screenSide);
        layerView.setPivotX(pivotPosition[0]);
        layerView.setPivotY(pivotPosition[1]);

        float angle = mMaxAngle *
                (screenSide == SlidingLayer.STICK_TO_LEFT || screenSide == SlidingLayer.STICK_TO_TOP ? -1 : 1);

        layerView.setRotation(angle * (1 - layerProgress));
    }

    private int[] pivotPositionForScreenSide(View layerView, int screenSide) {

        switch (screenSide) {

        case SlidingLayer.STICK_TO_LEFT:
            return new int[] { 0, layerView.getMeasuredHeight() };

        case SlidingLayer.STICK_TO_TOP:
            return new int[] { 0, 0 };

        case SlidingLayer.STICK_TO_RIGHT:
            return new int[] { layerView.getMeasuredWidth(), layerView.getMeasuredHeight() };

        case SlidingLayer.STICK_TO_BOTTOM:
            return new int[] { 0, layerView.getMeasuredHeight() };

        default:
            return new int[] { 0, 0 };
        }
    }
}
