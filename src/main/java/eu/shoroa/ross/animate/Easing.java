package eu.shoroa.ross.animate;

import static java.lang.Math.*;

public interface Easing {
    float ease(float t);

    Easing LINEAR = t -> t;
    Easing SINE_IN = t -> (float) (-1 * cos(t * (PI / 2)) + 1);
    Easing SINE_OUT = t -> (float) sin(t * (PI / 2));
    Easing SINE_IN_OUT = t -> (float) (-0.5 * (cos(PI * t) - 1));
    Easing QUAD_IN = t -> t * t;
    Easing QUAD_OUT = t -> t * (2 - t);
    Easing QUAD_IN_OUT = t -> t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    Easing CUBIC_IN = t -> t * t * t;
    Easing CUBIC_OUT = t -> 1 + (t - 1) * t * t;
    Easing CUBIC_IN_OUT = t -> t < 0.5f ? 4 * t * t * t : 1 + (2 * t - 2) * t * t;
    Easing QUART_IN = t -> t * t * t * t;
    Easing QUART_OUT = t -> 1 - (t - 1) * t * t * t;
    Easing QUART_IN_OUT = t -> t < 0.5f ? 8 * t * t * t * t : 1 - 8 * (t - 1) * t * t * t;
    Easing QUINT_IN = t -> t * t * t * t * t;
    Easing QUINT_OUT = t -> 1 + (t - 1) * t * t * t * t;
    Easing QUINT_IN_OUT = t -> t < 0.5f ? 16 * t * t * t * t * t : 1 + 16 * (t - 1) * t * t * t * t;
    Easing EXPO_IN = t -> (float) pow(2.0, 10.0 * (t - 1));
    Easing EXPO_OUT = t -> 1 - (float) pow(2.0, -10.0 * t);
    Easing EXPO_IN_OUT = t -> t < 0.5f ? (float) pow(2.0, 20.0 * t - 10) / 2 : (float) (2 - pow(2.0, -20.0 * t + 10)) / 2;
    Easing CIRC_IN = t -> (float) (1 - sqrt(1 - t * t));
    Easing CIRC_OUT = t -> (float) sqrt(1 - (t - 1) * (t - 1));
    Easing CIRC_IN_OUT = t -> t < 0.5f ? (float) ((1 - sqrt(1 - 4 * t * t)) / 2) : (float) ((sqrt(1 - (2 * t - 2) * (2 * t - 2)) + 1) / 2);
    Easing BACK_IN = t -> 2.70158f * t * t * t - 1.70158f * t * t;
    Easing BACK_OUT = t -> 1 + 2.70158f * (t - 1) * (t - 1) * (t - 1) - 1.70158f * (t - 1) * (t - 1);
    Easing BACK_IN_OUT = t -> t < 0.5f ? (2 * t * t * (2.5949095f * t - 1.5949095f)) / 2 : (2 * t - 2) * (2 * t - 2) * (2.5949095f * (t * 2 - 2) + 1.5949095f) + (float) 2 / 2;
    Easing ELASTIC_IN = t -> (float) (-pow(2f, 10 * t - 10) * sin((t * 10 - 10.75) * (PI * 2) / 3));
    Easing ELASTIC_OUT = t -> (float) (pow(2f, -10f * t) * sin((t * 10 - 0) * (PI * 2) / 3) + 1);
    Easing ELASTIC_IN_OUT = t -> t < 0.5f ? (float) (-(pow(2f, 20 * t - 10) * sin((20 * t - 11.125f) * (PI * 2) / 4.5f)) / 2) : (float) ((pow(2f, -20 * t + 10) * sin((20 * t - 11.125f) * (PI * 2) / 4.5f)) / 2);
}