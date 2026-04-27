package eu.shoroa.ross.util;

public class MathHelper {
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
