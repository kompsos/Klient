package com.kompsos.system.lib;

public class MathUtil {
    public static double easeOutCubic(double time) {
        return 1 - Math.pow(1 - time, 3);
    }

    public static float lerp(float start, float end, float alpha) {
        return start + alpha * (end - start);
    }


    public static float easeOutCubicBetween(float start, float end, float t) {
        t = Math.clamp(t, start, end);

        float easedT = (float) easeOutCubic(t);
        return lerp(start, end, easedT);
    }
}
