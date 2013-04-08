/*
 * CommonUtils.java
 * 
 * Copyright (C) 2013 6 Wunderkinder GmbH.
 * 
 * @author      Jose L Ugia - @Jl_Ugia
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

package com.slidinglayer.util;

import java.util.Random;

/**
 * Common Utils for Android.
 * 
 */
public class CommonUtils {

    private static Random mRandom;

    /**
     * Get a random boolean
     */
    public static boolean getNextRandomBoolean() {

        if (mRandom == null) {
            mRandom = new Random();
        }

        return mRandom.nextBoolean();
    }

}