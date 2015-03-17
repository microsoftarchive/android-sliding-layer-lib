package com.wunderlist.slidinglayer.utils;

/**
 * Created by joseluisugia on 17/03/15.
 */
public class Transitions {


    public static float intermediateValueForRange(float position, float floorValue, float ceilValue) {

        return intermediateValueForRange(position, floorValue, ceilValue, new float[] { 0, 1 });
    }

    public static float intermediateValueForRange(float position, float floorValue, float ceilValue,
                                                  float[] range) {

        if (position < range[0]) {
            return floorValue;
        } else if (position > range[1]) {
            return ceilValue;
        } else {
            float rangeRatio = (position - range[0]) / (range[1] - range[0]);
            return floorValue + ((ceilValue - floorValue) * rangeRatio);
        }
    }
}
