package eu.shoroa.ross.feature.module;

import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.type.Rect;
import eu.shoroa.ross.type.Size;
import org.lwjgl.opengl.Display;

public abstract class HUDElement {
    private final String id;

    private boolean enabled = true;

    private HUDAnchor anchor = HUDAnchor.LEFT_TOP;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    protected HUDElement(String id) {
        this.id = id;
    }

    public final String getId() {
        return id;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public abstract void render(Hud.Layer layer);

    public void dummy(Hud.Layer layer) {
        render(layer);
    }

    public void onRemove() {}

    public abstract Size getSize();

    public final Rect getBounds() {
        Size size = getSize();

        float x = (float) (anchor.fx * (Display.getWidth() - size.width) + offsetX);
        float y = (float) (anchor.fy * (Display.getHeight() - size.height) + offsetY);

        return new Rect(x, y, size.width, size.height);
    }

    public final void setPosition(double x, double y) {
        Size size = getSize();

        offsetX = x - anchor.fx * (Display.getWidth() - size.width);
        offsetY = y - anchor.fy * (Display.getHeight() - size.height);
    }

    public final void updateAnchor() {
        Rect bounds = getBounds();

        double cx = (bounds.x + bounds.width / 2.0) / Display.getWidth();
        double cy = (bounds.y + bounds.height / 2.0) / Display.getHeight();

        anchor = HUDAnchor.closestTo(cx, cy);

        setPosition(bounds.x, bounds.y);
    }

    public final HUDAnchor getAnchor() {
        return anchor;
    }

    public final double getOffsetX() {
        return offsetX;
    }

    public final double getOffsetY() {
        return offsetY;
    }

    public final void setPlacement(HUDAnchor anchor, double offsetX, double offsetY) {
        this.anchor = anchor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
}
