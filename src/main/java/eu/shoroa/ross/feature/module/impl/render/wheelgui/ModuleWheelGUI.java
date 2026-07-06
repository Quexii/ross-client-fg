package eu.shoroa.ross.feature.module.impl.render.wheelgui;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.*;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.animate.Easing;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.util.ChatComponentText;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.List;

import static eu.shoroa.ross.Client.mc;

public class ModuleWheelGUI extends Module {
    private final Animate animate = new Animate(140L, Easing.CIRC_OUT);
    private boolean open = false;

    private final List<@Nullable WheelEntry> entries = new ArrayList<>();
    private final List<Animate> selectionAnimations = new ArrayList<>();
    int selectedIndex = -1;

    private float mouseDX = 0f;
    private float mouseDY = 0f;

    enum Mode implements ModeEnum {
        ONE("ONE"), TWO("TWO"), THREE("THREE");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String displayName() {
            return name;
        }
    }

    public ModuleWheelGUI() {
        super("WheelGUI", ".", Category.RENDER, "\uf39c");
        for (int i = 0; i < 6; i++) {
            entries.add(null);
            selectionAnimations.add(new Animate(140L, Easing.CIRC_OUT));
        }

        SettingCategory cat = addCategory("category", "c", "category");

        register(new NumberSetting("slider", "slider", 4, 0, 10, 0.1f), cat);
        register(new BooleanSetting("boolean", "boolean", false), cat);
        register(new ColorSetting("color", "color", new java.awt.Color(0xFF00FF00)), cat);
        register(new ModeSetting<>("mode", "mode", Mode.ONE), cat);

        entries.set(0, new WheelEntry("Test 1", MaterialIcons.HANDSHAKE, () -> mc.thePlayer.addChatMessage(new ChatComponentText("Test 1"))));
    }

    @Subscribe
    @ApiStatus.Internal
    public void onInput(EventInput event) {
        if (event.type == EventInput.Type.KEYBOARD && event.action == EventInput.Action.PRESS && event.value == Keyboard.KEY_TAB) {
            open = true;
//            MINECRAFT.mouseHelper.ungrabMouseCursor();
        } else if (event.type == EventInput.Type.KEYBOARD && event.action == EventInput.Action.RELEASE && event.value == Keyboard.KEY_TAB) {
            open = false;

            mouseDX = 0f;
            mouseDY = 0f;
//            MINECRAFT.mouseHelper.grabMouseCursor();

            System.out.println(selectedIndex);
            if (selectedIndex != -1) {
                WheelEntry entry = entries.get(selectedIndex);
                if (entry != null && entry.action != null) {
                    entry.action.run();
                }
            }
        }
    }

    @Subscribe
    @ApiStatus.Internal
    public void onHud(Hud.Layer e) {
        if (e.is(Hud.Layer.NAME_SKIA_TOP)) {
            float w = Display.getWidth();
            float h = Display.getHeight();

            float mx = Mouse.getX();
            float my = Mouse.getY();

            float cx = w / 2f;
            float cy = h / 2f;

            float r = 320;

            animate.doEase(open);

            if (open) {
                mouseDX -= mc.mouseHelper.deltaX;
                mouseDY += mc.mouseHelper.deltaY;
            }

            float alpha = (float) animate.getValue();
            float scale = (float) (0.8f + animate.getValue() * 0.2f);
            double angle = Math.atan2(mouseDY, mouseDX);

            float ringDist = r * 0.45f;
            float dist = (float) Math.sqrt(mouseDX * mouseDX + mouseDY * mouseDY);

            if (dist > ringDist) {
                mouseDX = (float) (Math.cos(angle) * ringDist);
                mouseDY = (float) (Math.sin(angle) * ringDist);
            }

            Canvas canvas = Client.INSTANCE.getSkia().getCanvas();

            UI.save();
            UI.scaleFrom(cx, cy, scale, scale);
            // base
            try (Paint p = new Paint()) {
                UI.save();
                canvas.clipRRect(RRect.makeXYWH(cx - ringDist / 2, cy - ringDist / 2, ringDist, ringDist, ringDist), ClipMode.DIFFERENCE, true);
                p.setShader(Shader.makeRadialGradient(cx, cy, r * 0.7f, new int[]{Color.withA(0, (int) (255 * alpha)), Color.withA(0, (int) (140 * alpha)), 0x00000000}, new float[]{0f, 0.7f, 1f}));
                canvas.drawCircle(cx, cy, r, p);
                p.setShader(null);
                UI.restore();

                p.setStroke(true);
                p.setColor(Color.withA(0xFFFFFFFF, (int) (255 * alpha)));
                canvas.drawCircle(cx, cy, ringDist / 2, p);
            }

            ImageFilter blur = ImageFilter.makeBlur(5, 5, FilterTileMode.DECAL);

            boolean wasSelected = false;

            // sections
            for (int i = 0; i < 6; i++) {
                try (Paint p = new Paint()) {
                    boolean selected = (Math.toDegrees(angle) + 180) >= 60f * i && (Math.toDegrees(angle) + 180) < 60f * (i + 1) && dist > ringDist / 2;
                    selectionAnimations.get(i).doEase(selected);
                    float pop = (float) selectionAnimations.get(i).getValue();

                    if (selected) {
                        selectedIndex = i;
                        wasSelected = true;
                    }

                    p.setColor(Color.withA(0xFFFFFFFF, (int) ((selected ? 255 : 100) * alpha)));
                    p.setStroke(true);
                    p.setStrokeWidth(6f);

                    float startAngle = 60f * i;
                    float x1 = cx + (float) Math.cos(Math.toRadians(startAngle)) * r * 2;
                    float y1 = cy + (float) Math.sin(Math.toRadians(startAngle)) * r * 2;
                    float x2 = cx + (float) Math.cos(Math.toRadians(startAngle + 60f)) * r * 2;
                    float y2 = cy + (float) Math.sin(Math.toRadians(startAngle + 60f)) * r * 2;

                    float tx = cx + (float) Math.cos(Math.toRadians(startAngle + 30f)) * r * 0.42f;
                    float ty = cy + (float) Math.sin(Math.toRadians(startAngle + 30f)) * r * 0.42f;
                    float gx = cx + (float) Math.cos(Math.toRadians(startAngle + 30f)) * r * 0.7f;
                    float gy = cy + (float) Math.sin(Math.toRadians(startAngle + 30f)) * r * 0.7f;

                    PathBuilder pb = new PathBuilder();
                    pb.arcTo(Rect.makeXYWH(cx - ringDist / 2, cy - ringDist / 2, ringDist, ringDist), 60f * i + 6, 60f - 12, false);
                    Path arc = pb.build();
                    canvas.drawPath(arc, p);
                    p.setImageFilter(blur);
                    canvas.drawPath(arc, p);
                    p.setImageFilter(null);

                    p.setStroke(false);
                    p.setColor(Color.withA(0xFFFFFFFF, (int) (255 * alpha)));

                    UI.save();
                    canvas.clipRRect(RRect.makeXYWH(cx - ringDist / 2, cy - ringDist / 2, ringDist, ringDist, ringDist), ClipMode.DIFFERENCE, true);
                    p.setShader(Shader.makeLinearGradient(cx, cy, gx, gy, new int[]{Color.withA(-1, (int) (150 * alpha * pop)), 0x00FFFFFF}));
                    canvas.drawTriangles(new Point[]{
                            new Point(cx, cy),
                            new Point(x1, y1),
                            new Point(x2, y2),
                    }, null, p);
                    p.setImageFilter(blur);
                    canvas.drawTriangles(new Point[]{
                            new Point(cx, cy),
                            new Point(x1, y1),
                            new Point(x2, y2),
                    }, null, p);
                    p.setImageFilter(null);
                    p.setShader(null);
                    UI.restore();


                    p.setStroke(true);
                    // icon rect
                    p.setStroke(false);
                    UI.drawText(entries.get(i) == null ? "None" : entries.get(i).title, tx, ty + 10, Fonts.GoogleFlex.weight(400), 12f, Align.TOP_CENTER, p, true);
                    UI.drawText(entries.get(i) == null ? MaterialIcons.CLOSE : entries.get(i).icon, tx, ty - 6, Fonts.MaterialIcons.weight(400), 40f, Align.CENTER, p, true);
                }

                if (!wasSelected) {
                    selectedIndex = -1;
                }
            }
            UI.restore();
        }
    }
}
