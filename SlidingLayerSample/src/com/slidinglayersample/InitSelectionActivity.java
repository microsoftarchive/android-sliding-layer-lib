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
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class InitSelectionActivity extends PreferenceActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        init();
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

        final CheckBoxPreference shadowPreference = (CheckBoxPreference) findPreference("layer_has_shadow");
        final CheckBoxPreference offsetPreference = (CheckBoxPreference) findPreference("layer_has_offset");

        final ListPreference posPreference = (ListPreference) findPreference("layer_location");
        if (posPreference != null) {

            setPreferenceSummary(posPreference,
                    PreferenceManager.getDefaultSharedPreferences(this).getString("layer_location", "right"));

            posPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    setPreferenceSummary(posPreference, (String) newValue);

                    if (newValue.equals("middle")) {
                        shadowPreference.setEnabled(false);
                        shadowPreference.setChecked(false);
                        offsetPreference.setEnabled(false);
                        offsetPreference.setChecked(false);
                    } else if (!shadowPreference.isEnabled()) {
                        shadowPreference.setEnabled(true);
                        shadowPreference.setChecked(true);
                        offsetPreference.setEnabled(true);
                        offsetPreference.setChecked(true);
                    }
                    return true;
                }
            });
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getString("layer_location", "right")
                .equals("middle")) {
            shadowPreference.setEnabled(false);
            shadowPreference.setChecked(false);
            offsetPreference.setEnabled(false);
            offsetPreference.setChecked(false);
        }
    }

    private void setPreferenceSummary(Preference _preference, String _locationString) {
        if (_locationString.equals("right")) {
            _preference.setSummary(getResources().getString(R.string.label_right));
        } else if (_locationString.equals("left")) {
            _preference.setSummary(getResources().getString(R.string.label_left));
        } else if (_locationString.equals("top")) {
            _preference.setSummary(getResources().getString(R.string.label_top));
        } else if (_locationString.equals("bottom")) {
            _preference.setSummary(getResources().getString(R.string.label_bottom));
        } else {
            _preference.setSummary(getResources().getString(R.string.label_middle));
        }
    }
}
