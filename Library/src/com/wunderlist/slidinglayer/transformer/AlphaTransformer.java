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

        final float alpha = Math.max(0, Math.min(1, layerProgress * mMultiplier));
        layerView.setAlpha(alpha);
    }
}
