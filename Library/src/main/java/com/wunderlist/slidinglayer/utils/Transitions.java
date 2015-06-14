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

package com.wunderlist.slidinglayer.utils;

/**
 * Created by joseluisugia on 17/03/15.
 */
public class Transitions {


    public static float intermediateValueForRange(float position, float[] values) {
        return intermediateValueForRange(position, new float[] { 0, 1 }, values);
    }

    public static float intermediateValueForCuePoints(float position, float[] range) {
        return intermediateValueForRange(position, range, range);
    }

    public static float intermediateValueForRange(float position, float[] cuePoints, float[] values) {

        if (cuePoints.length != values.length) {
            throw new IllegalArgumentException("Range and values arrays must be of the same size");
        }

        int length = cuePoints.length;

        if (position <= cuePoints[0]) {
            return values[0];
        } else {

            float cuePoint, previousCuePoint, value, previousValue;
            float rangeRatio;

            for (int i = 1; i < length; i++) {

                cuePoint = cuePoints[i];

                if (position <= cuePoint) {
                    previousCuePoint = cuePoints[i - 1];
                    value = values[i];
                    previousValue = values[i - 1];
                    rangeRatio = (position - previousCuePoint) / (cuePoint - previousCuePoint);
                    return previousValue + ((value - previousValue) * rangeRatio);
                }
            }

            return values[length - 1];
        }
    }
}
