package eu.shoroa.ross.feature.gui.editor;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.feature.gui.RossScreen;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.HUDElement;
import eu.shoroa.ross.feature.module.HUDModule;
import eu.shoroa.ross.feature.module.ModuleManager;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.Skia;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.Rect;
import eu.shoroa.ross.utils.math.Mth;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PaintMode;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.List;

public class HUDEditor extends RossScreen {
    private static final float SNAP_THRESHOLD = 6f;
    private static final float OUTLINE_RADIUS = 4f;

    private HUDElement selected = null;

    private boolean dragging = false;
    private float dragOffsetX;
    private float dragOffsetY;

    private final List<Float> snapLinesX = new ArrayList<>();
    private final List<Float> snapLinesY = new ArrayList<>();

    @Override
    protected void init() {
    }

    @Override
    protected void render(float mouseX, float mouseY, float partialTicks) {
        if (dragging && selected != null) {
            drag(mouseX, mouseY);
        }

        Filter.kawase().capture(mc.getFramebuffer().framebufferTexture, 8, true, 2);

        Skia skia = Client.INSTANCE.getSkia();

        if (skia.isStale()) {
            return;
        }

        List<HUDModule> modules = ModuleManager.getEnabledHUDModules();

        skia.beginFrame();

        UI.use(skia);

        UI.drawFilter(Filter.kawase(), mc.getFramebuffer().framebufferTexture, 0f, 0f, Display.getWidth(), Display.getHeight());

        renderElements(modules, new Hud.Layer(Hud.Layer.NAME_SKIA_BOTTOM));

        skia.endFrame();

        renderElements(modules, new Hud.Layer(Hud.Layer.NAME_VANILLA_BOTTOM));

        skia.beginFrame();

        UI.use(skia);

        renderElements(modules, new Hud.Layer(Hud.Layer.NAME_SKIA_TOP));

        renderOverlay(modules, mouseX, mouseY);

        skia.endFrame();

        renderElements(modules, new Hud.Layer(Hud.Layer.NAME_VANILLA_TOP));
    }

    private void renderElements(List<HUDModule> modules, Hud.Layer layer) {
        for (HUDModule module : modules) {
            for (HUDElement element : module.getElements()) {
                element.dummy(layer);
            }
        }
    }

    private void renderOverlay(List<HUDModule> modules, float mouseX, float mouseY) {
        StellaTheme theme = StellaTheme.get();

        float dw = Display.getWidth();
        float dh = Display.getHeight();

        try (Paint p = new Paint()) {
            p.setAntiAlias(true);

            if (dragging) {
                p.setColor(theme.border);
                p.setMode(PaintMode.STROKE);
                p.setStrokeWidth(1f);

                for (int i = 1; i <= 2; i++) {
                    UI.drawLine(dw * i / 3f, 0f, dw * i / 3f, dh, p);

                    UI.drawLine(0f, dh * i / 3f, dw, dh * i / 3f, p);
                }

                p.setColor(theme.accent);
                p.setStrokeWidth(1.5f);

                for (float x : snapLinesX) {
                    UI.drawLine(x, 0f, x, dh, p);
                }

                for (float y : snapLinesY) {
                    UI.drawLine(0f, y, dw, y, p);
                }
            }

            HUDElement hovered = elementAt(modules, mouseX, mouseY);

            if (hovered != null && hovered != selected) {
                Rect b = hovered.getBounds();

                p.setColor(theme.accentSoft);
                p.setMode(PaintMode.STROKE);
                p.setStrokeWidth(1.5f);

                UI.drawRRect(b.x - 2f, b.y - 2f, b.width + 4f, b.height + 4f, OUTLINE_RADIUS, p);
            }

            if (selected != null) {
                Rect b = selected.getBounds();

                p.setColor(theme.shadowSoft);
                p.setMode(PaintMode.FILL);

                UI.drawRRect(b.x - 2f, b.y - 2f, b.width + 4f, b.height + 4f, OUTLINE_RADIUS, p);

                p.setColor(theme.accent);
                p.setMode(PaintMode.STROKE);
                p.setStrokeWidth(2f);

                UI.drawRRect(b.x - 2f, b.y - 2f, b.width + 4f, b.height + 4f, OUTLINE_RADIUS, p);

                float ax = (float) (selected.getAnchor().fx * dw);
                float ay = (float) (selected.getAnchor().fy * dh);

                ax = Mth.clamp(ax, 5f, dw - 5f);
                ay = Mth.clamp(ay, 5f, dh - 5f);

                p.setMode(PaintMode.FILL);
                p.setColor(theme.accentDeep);

                UI.drawCircle(ax, ay, 4f, p);

                p.setColor(theme.foregroundContrast);

                UI.drawCircle(ax, ay, 1.5f, p);
            }
        }
    }

    private void drag(float mouseX, float mouseY) {
        if (selected == null) {
            return;
        }

        float dw = Display.getWidth();
        float dh = Display.getHeight();

        Rect bounds = selected.getBounds();

        float x = Mth.clamp(mouseX - dragOffsetX, 0f, dw - bounds.width);

        float y = Mth.clamp(mouseY - dragOffsetY, 0f, dh - bounds.height);

        snapLinesX.clear();
        snapLinesY.clear();

        boolean snap = !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

        if (snap) {
            List<Float> candidatesX = new ArrayList<>();

            List<Float> candidatesY = new ArrayList<>();

            candidatesX.add(0f);
            candidatesX.add(dw / 2f);
            candidatesX.add(dw);

            candidatesY.add(0f);
            candidatesY.add(dh / 2f);
            candidatesY.add(dh);

            for (HUDModule module : ModuleManager.getEnabledHUDModules()) {

                for (HUDElement other : module.getElements()) {

                    if (other == selected) {
                        continue;
                    }

                    Rect o = other.getBounds();

                    candidatesX.add(o.x);
                    candidatesX.add(o.x + o.width / 2f);
                    candidatesX.add(o.x + o.width);

                    candidatesY.add(o.y);
                    candidatesY.add(o.y + o.height / 2f);
                    candidatesY.add(o.y + o.height);
                }
            }

            float[] snappedX = snapAxis(x, bounds.width, candidatesX);

            if (snappedX != null) {
                x = snappedX[0];
                snapLinesX.add(snappedX[1]);
            }

            float[] snappedY = snapAxis(y, bounds.height, candidatesY);

            if (snappedY != null) {
                y = snappedY[0];
                snapLinesY.add(snappedY[1]);
            }
        }

        selected.setPosition(x, y);
    }

    private float[] snapAxis(float pos, float extent, List<Float> candidates) {
        float bestDistance = SNAP_THRESHOLD;
        float[] best = null;

        float[] edgeOffsets = {0f, extent / 2f, extent};

        for (float candidate : candidates) {
            for (float edge : edgeOffsets) {
                float distance = Math.abs(pos + edge - candidate);

                if (distance < bestDistance) {
                    bestDistance = distance;

                    best = new float[]{candidate - edge, candidate};
                }
            }
        }

        return best;
    }

    @Override
    protected void input(float mouseX, float mouseY, EventInput event) {
        if (event.type == EventInput.Type.MOUSE && event.value == 0) {
            if (event.action == EventInput.Action.PRESS) {
                HUDElement hit = elementAt(ModuleManager.getEnabledHUDModules(), mouseX, mouseY);

                selected = hit;

                if (hit != null) {
                    Rect bounds = hit.getBounds();

                    dragging = true;

                    dragOffsetX = mouseX - bounds.x;

                    dragOffsetY = mouseY - bounds.y;
                }
            } else if (event.action == EventInput.Action.RELEASE && dragging) {
                dragging = false;

                snapLinesX.clear();
                snapLinesY.clear();

                if (selected != null) {
                    selected.updateAnchor();
                }
            }
        }

        if (event.type == EventInput.Type.KEYBOARD && event.action == EventInput.Action.PRESS && selected != null && !dragging) {
            float step = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? 10f : 1f;

            Rect b = selected.getBounds();

            switch (event.value) {
                case Keyboard.KEY_LEFT:
                    nudge(b.x - step, b.y);
                    break;

                case Keyboard.KEY_RIGHT:
                    nudge(b.x + step, b.y);
                    break;

                case Keyboard.KEY_UP:
                    nudge(b.x, b.y - step);
                    break;

                case Keyboard.KEY_DOWN:
                    nudge(b.x, b.y + step);
                    break;
            }
        }
    }

    private void nudge(float x, float y) {
        if (selected == null) {
            return;
        }

        Rect bounds = selected.getBounds();

        selected.setPosition(Mth.clamp(x, 0f, Display.getWidth() - bounds.width), Mth.clamp(y, 0f, Display.getHeight() - bounds.height));

        selected.updateAnchor();
    }

    private HUDElement elementAt(List<HUDModule> modules, float x, float y) {
        for (int i = modules.size() - 1; i >= 0; i--) {
            HUDModule module = modules.get(i);

            List<HUDElement> elements = module.getElements();

            for (int j = elements.size() - 1; j >= 0; j--) {
                HUDElement element = elements.get(j);

                Rect b = element.getBounds();

                if (x >= b.x && x <= b.x + b.width && y >= b.y && y <= b.y + b.height) {
                    return element;
                }
            }
        }

        return null;
    }

    @Override
    protected void scroll(float value, float partialTicks) {
    }
}
