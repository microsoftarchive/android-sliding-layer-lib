/*
 * InitSelectionActivity.java
 * 
 * Copyright (C) 2013 6 Wunderkinder GmbH.
 * 
 * @author      Jose L Ugia - @Jl_Ugia
 * @author      Antonio Consuegra - @aconsuegra
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

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

public class InitSelectionActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initToolbar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        init();
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
    }

    /**
     * Initialization
     */
    @SuppressWarnings("deprecation")
    private void init() {

        addPreferencesFromResource(R.xml.pref_general_activity);

        final Preference goPreference = findPreference("pref_go");
        if (goPreference != null) {
            goPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                    return false;
                }
            });
        }

        final ListPreference posPreference = (ListPreference) findPreference("layer_location");
        if (posPreference != null) {

            setPreferenceSummary(posPreference,
                    PreferenceManager.getDefaultSharedPreferences(this).getString("layer_location", "right"));

            posPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    setPreferenceSummary(posPreference, (String) newValue);
                    return true;
                }
            });
        }

        final ListPreference transformPreference = (ListPreference) findPreference("layer_transform");
        if (transformPreference != null) {

            setTransformPreferenceSummary(transformPreference,
                    PreferenceManager.getDefaultSharedPreferences(this).getString("layer_transform", "none"));

            transformPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    setTransformPreferenceSummary(transformPreference, (String) newValue);
                    return true;
                }
            });
        }
    }

    private void setPreferenceSummary(Preference _preference, String _locationString) {

        switch (_locationString) {
        case "right":
            _preference.setSummary(getResources().getString(R.string.label_right));
            break;
        case "left":
            _preference.setSummary(getResources().getString(R.string.label_left));
            break;
        case "top":
            _preference.setSummary(getResources().getString(R.string.label_top));
            break;
        case "bottom":
            _preference.setSummary(getResources().getString(R.string.label_bottom));
            break;
        }
    }

    private void setTransformPreferenceSummary(Preference _preference, String _transformString) {

        switch (_transformString) {
        case "none":
            _preference.setSummary(getResources().getString(R.string.label_none));
            break;
        case "alpha":
            _preference.setSummary(getResources().getString(R.string.label_alpha));
            break;
        case "rotation":
            _preference.setSummary(getResources().getString(R.string.label_rotation));
            break;
        case "slide":
            _preference.setSummary(getResources().getString(R.string.label_slide_joy));
            break;
        }
    }
}
