package eu.shoroa.ross.types;

import org.jetbrains.annotations.NotNull;

public class Rect {
    public final float x;
    public final float y;
    public final float width;
    public final float height;

    public Rect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(float x, float y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }

    public static Rect cover(@NotNull Size src, @NotNull Rect target) {
        float sw = src.width;
        float sh = src.height;
        if (sw <= 0f || sh <= 0f) {
            return new Rect(target.x, target.y, 0f, 0f);
        }

        float scaleX = target.width / sw;
        float scaleY = target.height / sh;
        float scale = Math.max(scaleX, scaleY);
        float rw = sw * scale;
        float rh = sh * scale;

        float rx = target.x + (target.width - rw) * 0.5f;
        float ry = target.y + (target.height - rh) * 0.5f;
        return new Rect(rx, ry, rw, rh);
    }
}
