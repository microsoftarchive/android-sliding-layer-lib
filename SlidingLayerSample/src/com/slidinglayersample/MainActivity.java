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

package com.slidinglayersample;

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
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.slidinglayer.SlidingLayer;

public class MainActivity extends Activity {

    private SlidingLayer mSlidingLayer;
    private TextView swipeText;

    private String mStickContainerToRightLeftOrMiddle;
    private boolean mShowShadow;
    private boolean mShowOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPrefs();
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
     * Get current value for preferences
     */
    private void getPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mStickContainerToRightLeftOrMiddle = prefs.getString("layer_location", "right");
        mShowShadow = prefs.getBoolean("layer_has_shadow", false);
        mShowOffset = prefs.getBoolean("layer_has_offset", false);
    }

    /**
     * Initializes the origin state of the layer
     */
    private void initState() {

        // Sticks container to right or left
        LayoutParams rlp = (LayoutParams) mSlidingLayer.getLayoutParams();
        int textResource;
        Drawable d;

        if (mStickContainerToRightLeftOrMiddle.equals("right")) {
            textResource = R.string.swipe_right_label;
            d = getResources().getDrawable(R.drawable.container_rocket_right);

            rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else if (mStickContainerToRightLeftOrMiddle.equals("left")) {
            textResource = R.string.swipe_left_label;
            d = getResources().getDrawable(R.drawable.container_rocket_left);

            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (mStickContainerToRightLeftOrMiddle.equals("top")) {
            textResource = R.string.swipe_up_label;
            d = getResources().getDrawable(R.drawable.container_rocket);

            mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_TOP);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            rlp.width = LayoutParams.MATCH_PARENT;
            rlp.height = getResources().getDimensionPixelSize(R.dimen.layer_width);
        } else if (mStickContainerToRightLeftOrMiddle.equals("bottom")) {
            textResource = R.string.swipe_down_label;
            d = getResources().getDrawable(R.drawable.container_rocket);

            mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_BOTTOM);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            rlp.width = LayoutParams.MATCH_PARENT;
            rlp.height = getResources().getDimensionPixelSize(R.dimen.layer_width);
        } else {
            textResource = R.string.swipe_label;
            d = getResources().getDrawable(R.drawable.container_rocket);

            rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
            rlp.width = LayoutParams.MATCH_PARENT;
        }

        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        swipeText.setCompoundDrawables(null, d, null, null);
        swipeText.setText(getResources().getString(textResource));
        mSlidingLayer.setLayoutParams(rlp);

        // Sets the shadow of the container
        if (mShowShadow) {
            mSlidingLayer.setShadowWidthRes(R.dimen.shadow_width);
            mSlidingLayer.setShadowDrawable(R.drawable.sidebar_shadow);
        } else {
            mSlidingLayer.setShadowWidth(0);
            mSlidingLayer.setShadowDrawable(null);
        }
        if(mShowOffset) {
            mSlidingLayer.setOffsetWidth(getResources().getDimensionPixelOffset(R.dimen.offset_width));
        } else {
            mSlidingLayer.setOffsetWidth(0);
        }
    }

    public void buttonClicked(View v) {
        switch (v.getId()) {
        case R.id.buttonOpen:
            if (!mSlidingLayer.isOpened()) {
                mSlidingLayer.openLayer(true);
            }
            break;
        case R.id.buttonClose:
            if (mSlidingLayer.isOpened()) {
                mSlidingLayer.closeLayer(true);
            }
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
