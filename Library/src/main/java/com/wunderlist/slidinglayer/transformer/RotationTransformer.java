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

package com.wunderlist.slidinglayer.transformer;

import android.view.View;

import com.wunderlist.slidinglayer.LayerTransformer;
import com.wunderlist.slidinglayer.SlidingLayer;

/**
 * Created by joseluisugia on 16/03/15.
 */
public final class RotationTransformer extends LayerTransformer {

    private static final int DEFAULT_ANGLE = 10;

    private final float mMaxAngle;
    private float mAngle;

    public RotationTransformer() {
        this(DEFAULT_ANGLE);
    }

    public RotationTransformer(float maxAngle) {
        mMaxAngle = maxAngle;
    }

    @Override
    protected void onMeasure(View layerView, int screenSide) {

        final int[] pivotPosition = pivotPositionForScreenSide(layerView, screenSide);
        layerView.setPivotX(pivotPosition[0]);
        layerView.setPivotY(pivotPosition[1]);

        mAngle = mMaxAngle *
                (screenSide == SlidingLayer.STICK_TO_LEFT || screenSide == SlidingLayer.STICK_TO_TOP ? -1 : 1);
    }

    @Override
    public void transform(View layerView, float previewProgress, float layerProgress) {
    }

    @Override
    protected void internalTransform(View layerView, float previewProgress, float layerProgress, int screenSide) {
        
        final float progressRatioToAnimate = Math.max(previewProgress, layerProgress);
        layerView.setRotation(mAngle * (1 - progressRatioToAnimate));
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
