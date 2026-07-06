package eu.shoroa.ross.utils.math;

import eu.shoroa.ross.render.animate.Easing;
import eu.shoroa.ross.type.DampFloat;

public class Mth {
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    public static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    public static void smoothDamp(DampFloat f, float target, float smoothTime, float dt) {
        float omega = 2f / smoothTime;
        float x = omega * dt;
        float exp = 1f / (1f + x + 0.48f * x * x + 0.235f * x * x * x);

        float change = f.value - target;
        float temp = (f.velocity + omega * change) * dt;

        f.velocity = (f.velocity - omega * temp) * exp;
        f.value = target + (change + temp) * exp;
    }

    public static double ease(double from, double to, double t, Easing easing) {
        return from + (to - from) * easing.ease(t);
    }

    public static float smoothStep(float edge0, float edge1, float x) {
        x = clamp((x - edge0) / (edge1 - edge0), 0, 1);
        return x * x * x * (x * (6.0f * x - 15.0f) + 10.0f);
    }
}
