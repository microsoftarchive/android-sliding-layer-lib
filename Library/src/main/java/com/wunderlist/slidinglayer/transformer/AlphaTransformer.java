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

/**
 * Created by joseluisugia on 16/03/15.
 */
public final class AlphaTransformer extends LayerTransformer {

    private static final int DEFAULT_MULTIPLIER = 1;

    private final float mMultiplier;

    public AlphaTransformer() {
        this(DEFAULT_MULTIPLIER);
    }

    public AlphaTransformer(float multiplier) {
        mMultiplier = multiplier;
    }

    @Override
    public void transform(View layerView, float previewProgress, float layerProgress) {

        final float progressRatioToAnimate = Math.max(previewProgress, layerProgress);
        final float alpha = Math.max(0, Math.min(1, progressRatioToAnimate * mMultiplier));
        layerView.setAlpha(alpha);
    }
}
