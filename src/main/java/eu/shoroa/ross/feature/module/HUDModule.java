package eu.shoroa.ross.feature.module;

import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.gui.editor.HUDEditor;
import eu.shoroa.ross.type.Rect;
import eu.shoroa.ross.type.Size;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.Display;

import static eu.shoroa.ross.Client.mc;

public abstract class HUDModule extends Module {
    private boolean inEditor = false;

    private HUDAnchor anchor = HUDAnchor.LEFT_TOP;
    private double offsetX = 0.0, offsetY = 0.0;

    public HUDModule(String name, String description, Bind bind, String icon) {
        super(name, description, Category.HUD, bind, icon);
    }

    public HUDModule(String name, String description, String icon) {
        super(name, description, Category.HUD, icon);
    }

    public abstract void render(Hud.Layer layer);

    public abstract void dummy(Hud.Layer layer);

    public abstract Size getSize();

    public final Rect getBounds() {
        Size size = getSize();
        float x = (float) (anchor.fx * (Display.getWidth() - size.width) + offsetX);
        float y = (float) (anchor.fy * (Display.getHeight() - size.height) + offsetY);
        return new Rect(x, y, size.width, size.height);
    }

    /** Moves the module so its top-left lands at (x, y), keeping the current anchor. */
    public final void setPosition(double x, double y) {
        Size size = getSize();
        offsetX = x - anchor.fx * (Display.getWidth() - size.width);
        offsetY = y - anchor.fy * (Display.getHeight() - size.height);
    }

    /** Re-anchors to the screen region the module currently sits in, preserving its absolute position. */
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

    public boolean isInEditor() {
        return inEditor;
    }

    @Subscribe
    @ApiStatus.Internal
    public void onTick(EventTick event) {
        inEditor = mc.currentScreen != null && mc.currentScreen instanceof HUDEditor;
    }

    @Subscribe
    @ApiStatus.Internal
    public void onHud(Hud.Layer layer) {
        if (!isInEditor()) render(layer);
    }
}
