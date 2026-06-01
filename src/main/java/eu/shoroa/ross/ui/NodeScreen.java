package eu.shoroa.ross.ui;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.gui.RossScreen;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.ui.api.LayoutDirection;
import eu.shoroa.ross.ui.api.Node;
import org.lwjgl.opengl.Display;
import org.lwjgl.input.Mouse;

public abstract class NodeScreen extends RossScreen {
    protected abstract Node build();

    protected final Node root = build();

    protected void updateLayout() {}

    @Override
    protected void init() {
        root.markDirty();
    }

    private void updateRootLayout() {
        float windowWidth = Display.getWidth();
        float windowHeight = Display.getHeight();

        root.width(windowWidth);
        root.height(windowHeight);

        updateLayout();

        root.calcLayout(windowWidth, windowHeight, LayoutDirection.LTR);
        root.resolveAbsolutePositions(0f, 0f);
    }

    @Override
    protected void render(float mouseX, float mouseY, float partialTicks) {
        if (root.consumeDirty()) {
            updateRootLayout();
        }

        Client.INSTANCE.skia.beginFrame();
        Renderer.use(Client.INSTANCE.skia);
        root.render(mouseX, mouseY, partialTicks);

        Client.INSTANCE.skia.endFrame();
    }

    @Override
    protected void input(float mouseX, float mouseY, EventInput event) {
        root.input(mouseX, mouseY, event);
    }

    @Override
    protected void scroll(float value, float partialTicks) {
        root.scroll(Mouse.getX(), Display.getHeight() - Mouse.getY(), value, partialTicks);
    }
}
