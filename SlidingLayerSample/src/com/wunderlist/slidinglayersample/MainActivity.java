/*
 * MainActivity.java
 * 
 * Copyright (C) 2013 6 Wunderkinder GmbH.
 * 
 * @author      Jose L Ugia - @Jl_Ugia
 * @author      Antonio Consuegra - @aconsuegra
 * @author      Cesar Valiente - @CesarValiente
 * @author      Benedikt Lehnert - @blehnert
 * @author      Timothy Achumba - @iam_timm
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

package com.wunderlist.slidinglayersample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.wunderlist.slidinglayer.SlidingLayer;

public class MainActivity extends Activity {

    private SlidingLayer mSlidingLayer;
    private TextView swipeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        initState();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * View binding
     */
    private void bindViews() {
        mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
        swipeText = (TextView) findViewById(R.id.swipeText);
    }

    /**
     * Initializes the origin state of the layer
     */
    private void initState() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setupSlidingLayerPosition(prefs.getString("layer_location", "right"));

        setupShadow(prefs.getBoolean("layer_has_shadow", false));
        setupLayerOffset(prefs.getBoolean("layer_has_offset", false));
        setupPreviewMode(prefs.getBoolean("preview_mode_enabled", false));
    }

    private void setupSlidingLayerPosition(String layerPosition) {

        LayoutParams rlp = (LayoutParams) mSlidingLayer.getLayoutParams();
        int textResource;
        Drawable d;

        if (layerPosition.equals("right")) {
            textResource = R.string.swipe_right_label;
            d = getResources().getDrawable(R.drawable.container_rocket_right);

            mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_RIGHT);
        } else if (layerPosition.equals("left")) {
            textResource = R.string.swipe_left_label;
            d = getResources().getDrawable(R.drawable.container_rocket_left);

            mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);
        } else if (layerPosition.equals("top")) {
            textResource = R.string.swipe_up_label;
            d = getResources().getDrawable(R.drawable.container_rocket);

            mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_TOP);
            rlp.width = LayoutParams.MATCH_PARENT;
            rlp.height = getResources().getDimensionPixelSize(R.dimen.layer_size);
        } else {
            textResource = R.string.swipe_down_label;
            d = getResources().getDrawable(R.drawable.container_rocket);

            mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_BOTTOM);
            rlp.width = LayoutParams.MATCH_PARENT;
            rlp.height = getResources().getDimensionPixelSize(R.dimen.layer_size);
        }

        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        swipeText.setCompoundDrawables(null, d, null, null);
        swipeText.setText(getResources().getString(textResource));
        mSlidingLayer.setLayoutParams(rlp);
    }

    private void setupShadow(boolean enabled) {
        if (enabled) {
            mSlidingLayer.setShadowSizeRes(R.dimen.shadow_size);
            mSlidingLayer.setShadowDrawable(R.drawable.sidebar_shadow);
        } else {
            mSlidingLayer.setShadowSize(0);
            mSlidingLayer.setShadowDrawable(null);
        }
    }

    private void setupLayerOffset(boolean enabled) {
        int offsetDistance = enabled ? getResources().getDimensionPixelOffset(R.dimen.offset_distance) : 0;
        mSlidingLayer.setOffsetDistance(offsetDistance);
    }

    private void setupPreviewMode(boolean enabled) {
        int previewOffset = enabled ? getResources().getDimensionPixelOffset(R.dimen.preview_offset_distance) : -1;
        mSlidingLayer.setPreviewOffsetDistance(previewOffset);
    }

    public void buttonClicked(View v) {
        switch (v.getId()) {
        case R.id.buttonOpen:
            mSlidingLayer.openLayer(true);
            break;
        case R.id.buttonClose:
            mSlidingLayer.closeLayer(true);
            break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            if (mSlidingLayer.isOpened()) {
                mSlidingLayer.closeLayer(true);
                return true;
            }

        default:
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
