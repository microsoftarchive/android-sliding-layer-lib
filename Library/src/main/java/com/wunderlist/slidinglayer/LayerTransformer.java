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

import android.view.View;

/**
 * Created by joseluisugia on 16/03/15.
 */
public abstract class LayerTransformer {

    /**
     * This method is executed when the layer finish measurement. Use this method to set member values inside of
     * the transformer so that they do not need to be calculated on every iteration of the animation;
     *
     * @param layerView       A reference to the layer itself.
     * @param screenSide      Side of the screen where the layer is stuck to
     */
    protected void onMeasure(View layerView, int screenSide) {
    }

    /**
     * Internal method to expose necessary properties for internal transformers to operate. Note that custom
     * transformers will modify views based on their circumstances. For example,
     * a default transformer if the view is stuck to the top or bottom, or if the preview is enabled,
     * whereas a custom implementation has that information already.
     *
     * @param layerView       A reference to the layer itself.
     * @param previewProgress The progress of the layer relative to the preview mode [0 - 1]. 0 fixed if no preview
     * @param layerProgress   The progress of the layer relative to its total size [0 - 1]
     * @param screenSide      Side of the screen where the layer is stuck to
     */
    protected void internalTransform(View layerView, float previewProgress, float layerProgress, int screenSide) {
        transform(layerView, previewProgress, layerProgress);
    }

    /**
     * Apply a property transformation to layer based on its scrolling state for the total size of the layer
     * and preview mode.
     *
     * @param layerView       A reference to the layer itself.
     * @param previewProgress The progress of the layer relative to the preview mode [0 - 1]. 0 fixed if no preview
     * @param layerProgress   The progress of the layer relative to its total size [0 - 1]
     */
    public abstract void transform(View layerView, float previewProgress, float layerProgress);
}
