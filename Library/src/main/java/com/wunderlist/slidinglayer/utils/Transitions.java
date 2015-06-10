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
