package com.wunderlist.slidinglayer.transformer;

import android.view.View;

import com.wunderlist.slidinglayer.LayerTransformer;
import com.wunderlist.slidinglayer.SlidingLayer;
import com.wunderlist.slidinglayer.utils.Transitions;

/**
 * Created by joseluisugia on 16/03/15.
 */
public final class SlideJoyTransformer extends LayerTransformer {

    @Override
    public void transform(View layerView, float previewProgress, float layerProgress) {
    }

    @Override
    protected void internalTransform(View layerView, float previewProgress, float layerProgress, int screenSide) {

        float progressRatioToAnimate = Math.max(previewProgress, layerProgress);

        // Scale

        float scaleValue = Transitions.intermediateValueForRange(progressRatioToAnimate, 0.9f, 1,
                new float[] { 0.9f, 1 });
        layerView.setScaleX(scaleValue);
        layerView.setScaleY(scaleValue);

        // Rotation

        float[] rotationValues = rotationValueForScreenSide(-9.5f, screenSide);

        float rotationX, rotationY;
        float[] floor, ceil, range;
        if (layerProgress < 0.9f) {
            floor = new float[] { 0, 0 };
            ceil = rotationValues;
            range = new float[] { 0.7f, 0.8f };
        } else {
            floor = rotationValues;
            ceil = new float[] { 0, 0 };
            range = new float[] { 0.9f, 1 };
        }

        rotationX = Transitions.intermediateValueForRange(progressRatioToAnimate, floor[0], ceil[0], range);
        layerView.setRotationX(rotationX);

        rotationY = Transitions.intermediateValueForRange(progressRatioToAnimate, floor[1], ceil[1], range);
        layerView.setRotationY(rotationY);

        // Pivot

        int[] pivotPosition = pivotPositionForScreenSide(layerView, screenSide);
        layerView.setPivotX(pivotPosition[0]);
        layerView.setPivotY(pivotPosition[1]);
    }

    private float[] rotationValueForScreenSide(float value, int screenSide) {

        switch (screenSide) {

        case SlidingLayer.STICK_TO_LEFT:
            return new float[] { 0, value };

        case SlidingLayer.STICK_TO_TOP:
            return new float[] { -value, 0 };

        case SlidingLayer.STICK_TO_RIGHT:
            return new float[] { 0, -value };

        case SlidingLayer.STICK_TO_BOTTOM:
            return new float[] { value, 0 };

        default:
            return new float[] { 0, 0 };
        }
    }

    private int[] pivotPositionForScreenSide(View layerView, int screenSide) {

        switch (screenSide) {

        case SlidingLayer.STICK_TO_LEFT:
            return new int[] { 0, layerView.getMeasuredHeight() / 2 };

        case SlidingLayer.STICK_TO_TOP:
            return new int[] { layerView.getMeasuredWidth() / 2, 0 };

        case SlidingLayer.STICK_TO_RIGHT:
            return new int[] { layerView.getMeasuredWidth(), layerView.getMeasuredHeight() / 2 };

        case SlidingLayer.STICK_TO_BOTTOM:
            return new int[] { layerView.getMeasuredWidth() / 2, layerView.getMeasuredHeight() };

        default:
            return new int[] { 0, 0 };
        }
    }
}
