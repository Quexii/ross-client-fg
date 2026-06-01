package eu.shoroa.ross.gui.mainmenu.node;

import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.ui.api.Node;
import eu.shoroa.ross.ui.api.UINode;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;

import static eu.shoroa.ross.Client.mc;

public class MenuButton extends UINode {
    private boolean pressed = false;
    private int pressedButton = -1;

    private final Runnable onClick;

    private final Animate hoverAnim = new Animate(220L, Easing.LINEAR);
    private final Animate pressAnim = new Animate(140L, Easing.LINEAR);

    public MenuButton(Runnable onClick) {
        this.onClick = onClick;
    }

    @Override
    public void nodeOnRender(Node node, float mouseX, float mouseY, float partialTicks) {
        if (contains(mouseX, mouseY)) hoverAnim.forceFinish();

        hoverAnim.doEase(contains(mouseX, mouseY));
        pressAnim.doEase(pressed);

        float hover = (float) hoverAnim.getValue();
        float press = (float) pressAnim.getValue();
        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();
        float radius = 12f;

        Renderer.drawFilter(Filter.kawase(), mc.getFramebuffer().framebufferTexture, x, y, w, h, radius);

        int base = Color.makeLerp(0x1F0F0F10, 0x2EFFFFFF, hover);
        base = Color.makeLerp(base, 0x4A000000, press * 0.2f);
        int border = Color.makeLerp(0x18FFFFFF, 0x2AFFFFFF, hover);

        try (Paint p = new Paint()) {
            p.setColor(base);
            Renderer.drawRRect(x, y, w, h, radius, p);

            p.setColor(border);
            p.setStroke(true);
            p.setStrokeWidth(1f);
            Renderer.drawRRect(x + 0.5f, y + 0.5f, w - 1f, h - 1f, radius - 0.5f, p);
        }
    }

    @Override
    public boolean nodeOnInput(Node node, float mouseX, float mouseY, EventInput event) {
        if (event.type == EventInput.Type.MOUSE) {
            if (contains(mouseX, mouseY)) {
                if (event.action == EventInput.Action.PRESS) {
                    pressed = true;
                    pressedButton = event.value;
                    return true;
                }
                if (event.action == EventInput.Action.RELEASE) {
                    if (event.value == pressedButton) {
                        pressed = false;
                        if (onClick != null) {
                            onClick.run();
                        }
                        return true;
                    }
                }
            } else {
                if (event.action == EventInput.Action.RELEASE && pressed && event.value == pressedButton) {
                    pressed = false;
                    return false;
                }
            }
        }
        return false;
    }
}
