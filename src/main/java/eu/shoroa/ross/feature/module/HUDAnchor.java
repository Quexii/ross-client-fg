package eu.shoroa.ross.feature.module;

public enum HUDAnchor {
    LEFT_TOP(0.0, 0.0),
    CENTER_TOP(0.5, 0.0),
    RIGHT_TOP(1.0, 0.0),
    LEFT_MIDDLE(0.0, 0.5),
    CENTER(0.5, 0.5),
    RIGHT_MIDDLE(1.0, 0.5),
    LEFT_BOTTOM(0.0, 1.0),
    CENTER_BOTTOM(0.5, 1.0),
    RIGHT_BOTTOM(1.0, 1.0);

    public final double fx, fy;

    HUDAnchor(double fx, double fy) {
        this.fx = fx;
        this.fy = fy;
    }

    public static HUDAnchor closestTo(double fx, double fy) {
        double ax = fx < 1.0 / 3.0 ? 0.0 : (fx < 2.0 / 3.0 ? 0.5 : 1.0);
        double ay = fy < 1.0 / 3.0 ? 0.0 : (fy < 2.0 / 3.0 ? 0.5 : 1.0);
        for (HUDAnchor anchor : values()) {
            if (anchor.fx == ax && anchor.fy == ay) return anchor;
        }
        return LEFT_TOP;
    }
}
