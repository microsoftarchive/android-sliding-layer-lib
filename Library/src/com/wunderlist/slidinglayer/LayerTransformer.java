package com.wunderlist.slidinglayer;

import android.view.View;

/**
 * Created by joseluisugia on 16/03/15.
 */
public abstract class LayerTransformer {

    /**
     * Internal method to expose necessary properties for internal transformers to operate. Note that custom
     * transformers will modify views based on their circumstances. For example,
     * a default transformer if the view is stuck to the top or bottom, or if the preview is enabled,
     * whereas a custom implementation has that information already.
     *
     * @param layerView             A reference to the layer itself.
     * @param layerSize             Total size of the layer
     * @param absoluteScroll        Current absolute scroll position
     * @param previewOffsetDistance Size of preview mode, or 0
     * @param screenSide            Side of the screen where the layer is stuck to
     */
    protected void internalTransform(View layerView, int layerSize, int absoluteScroll,
                                     int previewOffsetDistance, int screenSide) {

        float layerProgress = (float) absoluteScroll / layerSize;
        float previewProgress = previewOffsetDistance > 0 ? absoluteScroll / previewOffsetDistance : 0;

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
