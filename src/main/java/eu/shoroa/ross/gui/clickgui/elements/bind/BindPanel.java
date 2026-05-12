package eu.shoroa.ross.gui.clickgui.elements.bind;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.gui.GuiElement;
import eu.shoroa.ross.gui.clickgui.elements.bind.event.GuiEventSelectButton;
import eu.shoroa.ross.gui.elements.TextField;
import eu.shoroa.ross.module.Bind;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.util.MathHelper;
import eu.shoroa.ross.util.render.MaterialIcons;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.Rect;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BindPanel extends GuiElement {
    private Map<Btn, EleKey> btns = new HashMap<>();

    private final Btn[][] keyboard = {
            { // total width = 18.6
                    key(Keyboard.KEY_ESCAPE, 1f, "Esc"),
                    spacer(0.8f),
                    key(Keyboard.KEY_F1, 1f, "F1"),
                    key(Keyboard.KEY_F2, 1f, "F2"),
                    key(Keyboard.KEY_F3, 1f, "F3"),
                    key(Keyboard.KEY_F4, 1f, "F4"),
                    spacer(0.6f),
                    key(Keyboard.KEY_F5, 1f, "F5"),
                    key(Keyboard.KEY_F6, 1f, "F6"),
                    key(Keyboard.KEY_F7, 1f, "F7"),
                    key(Keyboard.KEY_F8, 1f, "F8"),
                    spacer(0.6f),
                    key(Keyboard.KEY_F9, 1f, "F9"),
                    key(Keyboard.KEY_F10, 1f, "F10"),
                    key(Keyboard.KEY_F11, 1f, "F11"),
                    key(Keyboard.KEY_F12, 1f, "F12"),
                    spacer(0.4f),
                    key(Keyboard.KEY_SCROLL, 1f, "ScrLK"),
                    key(Keyboard.KEY_SYSRQ, 1f, "PtrSc"),
                    key(Keyboard.KEY_PAUSE, 1f, "Pause")
            },
            { // total width = 18.6
                    key(Keyboard.KEY_GRAVE, 1f, "`"),
                    key(Keyboard.KEY_1, 1f, "1"),
                    key(Keyboard.KEY_2, 1f, "2"),
                    key(Keyboard.KEY_3, 1f, "3"),
                    key(Keyboard.KEY_4, 1f, "4"),
                    key(Keyboard.KEY_5, 1f, "5"),
                    key(Keyboard.KEY_6, 1f, "6"),
                    key(Keyboard.KEY_7, 1f, "7"),
                    key(Keyboard.KEY_8, 1f, "8"),
                    key(Keyboard.KEY_9, 1f, "9"),
                    key(Keyboard.KEY_0, 1f, "0"),
                    key(Keyboard.KEY_MINUS, 1f, "-"),
                    key(Keyboard.KEY_EQUALS, 1f, "="),
                    key(Keyboard.KEY_BACK, 2f, "i:" + MaterialIcons.KEYBOARD_BACKSPACE),
                    spacer(0.4f),
                    key(Keyboard.KEY_INSERT, 1f, "Ins"),
                    key(Keyboard.KEY_HOME, 1f, "Home"),
                    key(Keyboard.KEY_PRIOR, 1f, "PgUp")
            },
            { // total width = 18.6
                    key(Keyboard.KEY_TAB, 1.5f, "i:" + MaterialIcons.KEYBOARD_TAB),
                    key(Keyboard.KEY_Q, 1f, "Q"),
                    key(Keyboard.KEY_W, 1f, "W"),
                    key(Keyboard.KEY_E, 1f, "E"),
                    key(Keyboard.KEY_R, 1f, "R"),
                    key(Keyboard.KEY_T, 1f, "T"),
                    key(Keyboard.KEY_Y, 1f, "Y"),
                    key(Keyboard.KEY_U, 1f, "U"),
                    key(Keyboard.KEY_I, 1f, "I"),
                    key(Keyboard.KEY_O, 1f, "O"),
                    key(Keyboard.KEY_P, 1f, "P"),
                    key(Keyboard.KEY_LBRACKET, 1f, "["),
                    key(Keyboard.KEY_RBRACKET, 1f, "]"),
                    key(Keyboard.KEY_BACKSLASH, 1.5f, "\\"),
                    spacer(0.4f),
                    key(Keyboard.KEY_DELETE, 1f, "Del"),
                    key(Keyboard.KEY_END, 1f, "End"),
                    key(Keyboard.KEY_NEXT, 1f, "PgDn")
            },
            { // total width = 18.6
                    key(Keyboard.KEY_CAPITAL, 1.75f, "i:" + MaterialIcons.KEYBOARD_CAPSLOCK),
                    key(Keyboard.KEY_A, 1f, "A"),
                    key(Keyboard.KEY_S, 1f, "S"),
                    key(Keyboard.KEY_D, 1f, "D"),
                    key(Keyboard.KEY_F, 1f, "F"),
                    key(Keyboard.KEY_G, 1f, "G"),
                    key(Keyboard.KEY_H, 1f, "H"),
                    key(Keyboard.KEY_J, 1f, "J"),
                    key(Keyboard.KEY_K, 1f, "K"),
                    key(Keyboard.KEY_L, 1f, "L"),
                    key(Keyboard.KEY_SEMICOLON, 1f, ";"),
                    key(Keyboard.KEY_APOSTROPHE, 1f, "'"),
                    key(Keyboard.KEY_RETURN, 2.25f, "i:" + MaterialIcons.KEYBOARD_RETURN),
                    spacer(0.4f),
                    spacer(1f),
                    spacer(1f),
                    spacer(1f)
            },
            { // total width = 18.6
                    key(Keyboard.KEY_LSHIFT, 2.25f, "Shift"),
                    key(Keyboard.KEY_Z, 1f, "Z"),
                    key(Keyboard.KEY_X, 1f, "X"),
                    key(Keyboard.KEY_C, 1f, "C"),
                    key(Keyboard.KEY_V, 1f, "V"),
                    key(Keyboard.KEY_B, 1f, "B"),
                    key(Keyboard.KEY_N, 1f, "N"),
                    key(Keyboard.KEY_M, 1f, "M"),
                    key(Keyboard.KEY_COMMA, 1f, ","),
                    key(Keyboard.KEY_PERIOD, 1f, "."),
                    key(Keyboard.KEY_SLASH, 1f, "/"),
                    key(Keyboard.KEY_RSHIFT, 2.75f, "Shift"),
                    spacer(0.4f),
                    spacer(1f),
                    key(Keyboard.KEY_UP, 1f, "i:" + MaterialIcons.KEYBOARD_ARROW_UP),
                    spacer(1f)
            },
            { // total width = 18.6
                    key(Keyboard.KEY_LCONTROL, 1.25f, "Ctrl"),
                    key(Keyboard.KEY_LMETA, 1.25f, "i:" + MaterialIcons.WINDOW),
                    key(Keyboard.KEY_LMENU, 1.25f, "Alt"),
                    key(Keyboard.KEY_SPACE, 6.25f, "i:" + MaterialIcons.SPACE_BAR),
                    key(Keyboard.KEY_RMENU, 1.25f, "Alt"),
                    key(Keyboard.KEY_RMETA, 1.25f, "i:" + MaterialIcons.WINDOW),
                    key(Keyboard.KEY_APPS, 1.25f, "Menu"),
                    key(Keyboard.KEY_RCONTROL, 1.25f, "Ctrl"),
                    spacer(0.4f),
                    key(Keyboard.KEY_LEFT, 1f, "i:" + MaterialIcons.KEYBOARD_ARROW_LEFT),
                    key(Keyboard.KEY_DOWN, 1f, "i:" + MaterialIcons.KEYBOARD_ARROW_DOWN),
                    key(Keyboard.KEY_RIGHT, 1f, "i:" + MaterialIcons.KEYBOARD_ARROW_RIGHT)
            }
    };

    private GuiEventSelectButton selectedButton;
    private boolean selectorOpen = false;
    private Animate selectedAnimate = new Animate(150L, Easing.CIRC_OUT);
    private TextField modulesSearch = new TextField(0f, 0f, 220f, 40f).placeholder("Search Modules...").icon(Fonts.MaterialIcons.weight(600), MaterialIcons.SEARCH);
    private Rect selectorRect = null;
    private float selectorScroll = 0f;
    private float selectorScrollSmooth = 0f;

    public BindPanel(float x, float y, float width, float height) {
        super(x, y, width, height);
        for (Btn[] row : keyboard) {
            for (Btn button : row) {
                if (button.code != null) {
                    EleKey eleKey = new EleKey(0, 0, 0, 0, button);
                    btns.put(button, eleKey);
                }
            }
        }
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        final float baseWidth = 48f;
        final float baseHeight = 44f;
        final float gapX = 3f;
        final float gapY = 6f;

        final float baseX = getX() + 10;
        final float baseY = getY() + 10;

        float passedMX = mouseX;
        float passedMY = mouseY;

        Canvas canvas = Client.INSTANCE.skia.getCanvas();

        selectedAnimate.doEase(selectorOpen);
        Keyboard.enableRepeatEvents(selectorOpen);

        float selectorWidth = 220f;
        float selectorBaseH = 32f;
        float selectorHeight = 56;

        if (selectedButton != null) {
            EleKey key = btns.get(selectedButton.button);

            List<Module> modules = new  ArrayList<>();

            for (Module module : ModuleManager.getSortedModules()) {
                if (!modulesSearch.getText().isEmpty() && !module.name.toLowerCase().contains(modulesSearch.getText().toLowerCase())) continue;
                modules.add(module);
                selectorHeight += selectorBaseH;
            }

            float maxVisibleHeight = 56 + selectorBaseH * 6;
            float contentHeight = selectorHeight;
            selectorHeight = Math.min(contentHeight, maxVisibleHeight);
            float maxScroll = Math.min(0f, selectorHeight - contentHeight);
            selectorScroll = Math.max(selectorScroll, maxScroll);

            selectorRect = Rect.makeXYWH(key.getX() + (key.getWidth() - selectorWidth) / 2f, key.getY() + key.getHeight() + 12f, selectorWidth, selectorHeight);
            if (selectorRect.contains(passedMX, passedMY)) {
                passedMX = -1;
                passedMY = -1;
            }
        }

        selectorScrollSmooth = MathHelper.lerp(selectorScrollSmooth, selectorScroll, 1f / Minecraft.getDebugFPS() * 8f);
        if (Float.isNaN(selectorScrollSmooth) || Float.isInfinite(selectorScrollSmooth)) selectorScrollSmooth = selectorScroll;

        // TODO: Add mouse panel

        try (Paint p = new Paint()) {
            Client.INSTANCE.skia.getCanvas().drawRectShadowNoclip(Rect.makeXYWH(getX(), getY(), getWidth(), getHeight() + 6), 0f, 0f, 14f, 0f, 0x88000000);

            p.setColor(0xFF212121);
            p.setImageFilter(ImageFilter.makeBlur(3f, 3f, FilterTileMode.DECAL));
            Renderer.drawText("Binding Manager", getX() + 10f, getY() - 14f, Fonts.GoogleFlex.weight(500), 24f, Font.Align.BOTTOM_LEFT, p);
            p.setImageFilter(null);
            p.setColor(0xFFEEEEEE);
            Renderer.drawText("Binding Manager", getX() + 10f, getY() - 14f, Fonts.GoogleFlex.weight(500), 24f, Font.Align.BOTTOM_LEFT, p);

            p.setColor(0xFF888888);
            Renderer.drawRRect(getX(), getY(), getWidth(), getHeight() + 6, 12f, p);
            p.setColor(0xFF999999);
            Renderer.drawRRect(getX(), getY(), getWidth(), getHeight(), 12f, p);
            p.setColor(0xFFD5D5D5);
            Renderer.drawRRect(getX() + 2f, getY() + 2f, getWidth() - 4f, getHeight() - 4f, 10f, p);

            Font brandFont = Fonts.GoogleFlex.weight(900);

            p.setColor(0xFFC4C4C4);
            Renderer.drawText("ROSS", getX() + getWidth() - (baseWidth + gapX) * 1.5f - 5, baseY + (baseHeight + gapY) * 3.58f, brandFont, 24f, Font.Align.CENTER, p);
            p.setColor(0xFFBDBDBD);
            Renderer.drawText("ROSS", getX() + getWidth() - (baseWidth + gapX) * 1.5f - 5, baseY + (baseHeight + gapY) * 3.58f + 2, brandFont, 24f, Font.Align.CENTER, p);
        }

        float y = baseY;
        for (Btn[] row : keyboard) {
            float x = baseX;
            for (Btn button : row) {
                float buttonWidth = button.modifier * baseWidth - gapX;
                if (button.code != null) {
                    EleKey key = btns.get(button);
                    key.setX(x);
                    key.setY(y);
                    key.setWidth(buttonWidth);
                    key.setHeight(baseHeight);
                    key.render(passedMX, passedMY, partialTicks);
                }
                x += buttonWidth + gapX;
            }
            if (y == baseY) y += 6f;
            y += baseHeight + gapY;
        }

        if (selectedButton != null) {
            EleKey key = btns.get(selectedButton.button);

            List<Module> modules = new  ArrayList<>();
            for (Module module : ModuleManager.getSortedModules()) {
                if (!modulesSearch.getText().isEmpty() && !module.name.toLowerCase().contains(modulesSearch.getText().toLowerCase())) continue;
                modules.add(module);
            }

            PathBuilder pathBuilder = new PathBuilder();
            pathBuilder.moveTo(key.getX() + key.getWidth() / 2f - 10f, key.getY() + key.getHeight() + 13f);
            pathBuilder.lineTo(key.getX() + key.getWidth() / 2f + 10f, key.getY() + key.getHeight() + 13f);
            pathBuilder.lineTo(key.getX() + key.getWidth() / 2f, key.getY() + key.getHeight() + 4f);
            pathBuilder.closePath();
            Path path = pathBuilder.build();

            int selectorColor = 0xFF202020;
            int selectorText = 0xFFAAAAAA;
            int selectorBoundText = 0xFFEEEEEE;

            Renderer.saveAlpha((float) selectedAnimate.getLinearValue());
            Renderer.translate(0f, (float) (10 - 10 * selectedAnimate.getValue()));
            try (Paint p = new Paint()) {
                canvas.drawRectShadowNoclip(selectorRect, 0f, 0f, 14f, 0f, 0x88000000);
                p.setPathEffect(PathEffect.makeCorner(3f));
                p.setColor(0xFF000000);
                p.setImageFilter(ImageFilter.makeBlur(4f, 4f, FilterTileMode.DECAL));
                canvas.drawPath(path, p);
                p.setColor(selectorColor);
                p.setImageFilter(null);
                canvas.drawPath(path, p);

                p.setPathEffect(null);
                Renderer.drawRRect(selectorRect.getLeft(), selectorRect.getTop(), selectorRect.getWidth(), selectorRect.getHeight(), 12f, p);

                Renderer.save();
                canvas.clipRect(Rect.makeXYWH(selectorRect.getLeft(), selectorRect.getTop() + 50, selectorRect.getWidth(), selectorRect.getHeight() - 50));
                float sy = 0;
                for (Module module : modules) {
                    boolean isBound = module.bind != null && module.bind.type == selectedButton.button.type && module.bind.key == selectedButton.button.code;
                    p.setColor(isBound ? selectorBoundText : selectorText);
                    Renderer.drawText(module.name, selectorRect.getLeft() + 12f, selectorRect.getTop() + 54 + sy + selectorScrollSmooth, Fonts.GoogleFlex.weight(isBound ? 500 : 400), 18f, Font.Align.TOP_LEFT, p);
                    if (isBound) {
                        String ic = module.bind.action == EventInput.Action.PRESS ? MaterialIcons.TOUCH_APP : MaterialIcons.HOURGLASS_EMPTY;
                        Renderer.drawText(ic, selectorRect.getRight() - 10f, selectorRect.getTop() + 54 + sy + selectorScrollSmooth, Fonts.MaterialIcons.weight(500).opticSize(16).fill(true), 18f, Font.Align.TOP_RIGHT, p);
                    }
                    sy += 32;
                }
                Renderer.restore();
            }
            modulesSearch.setX(selectorRect.getLeft() + 6f);
            modulesSearch.setY(selectorRect.getTop() + 6f);
            modulesSearch.setWidth(selectorWidth - 12f);
            modulesSearch.setHeight(50 - 12f);
            modulesSearch.render(mouseX, mouseY, partialTicks);
            Renderer.restore();
        }
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (selectorOpen && event.type == EventInput.Type.KEYBOARD && event.value == Keyboard.KEY_ESCAPE && event.action == EventInput.Action.PRESS) {
            selectorOpen = false;
            return true;
        }

        if (selectorOpen) {
            if (modulesSearch.input(mouseX, mouseY, event)) {
                return true;
            }

            if (event.type == EventInput.Type.MOUSE) {
                if (!selectorRect.contains(mouseX, mouseY)) {
                    selectorOpen = false;
                    return true;
                } else if(event.action == EventInput.Action.PRESS) {
                    float y = 0;
                    for (Module module : ModuleManager.getSortedModules()) {
                        if (!modulesSearch.getText().isEmpty() && !module.name.toLowerCase().contains(modulesSearch.getText().toLowerCase())) continue;
                        Rect modRect = Rect.makeXYWH(selectorRect.getLeft(), selectorRect.getTop() + 54 + y + selectorScrollSmooth, selectorRect.getWidth(), 32);
                        if (modRect.contains(mouseX, mouseY)) {
                            if (module.bind != null && module.bind.type == selectedButton.button.type && module.bind.key == selectedButton.button.code) {
                                if (event.value == 0) {
                                    module.bind = null;
                                } else if  (event.value == 1) {
                                    EventInput.Action action = module.bind.action == EventInput.Action.PRESS ? EventInput.Action.HOLD : EventInput.Action.PRESS;
                                    module.bind = new Bind(selectedButton.button.code, selectedButton.button.type, action);
                                }
                            } else {
                                module.bind = new Bind(selectedButton.button.code, selectedButton.button.type, EventInput.Action.PRESS);
                            }
                        }
                        y += 32f;
                    }
                }
            }
        }

        for (EleKey key : btns.values()) {
            if (key.input(mouseX, mouseY, event)) {
                return true;
            }
        }
        return false;
    }

    public void scroll(float value, float partialTicks) {
        if (selectorOpen && selectorRect.contains(Mouse.getX(), Display.getHeight() - Mouse.getY())) {
            selectorScroll += value * 32f;
            selectorScroll = Math.min(0, selectorScroll);
        }
    }

    public boolean cancelEscape() {
        return selectorOpen;
    }

    @Subscribe
    public void oe$SelectedButton(GuiEventSelectButton event) {
        selectorOpen = false;
        selectedAnimate.doEase(false);
        selectedAnimate.forceFinish();
        selectedButton = event;
        selectorOpen = true;

        modulesSearch.clear();
        selectorScroll = 0f;
        selectorScrollSmooth = 0f;
    }

    private static Btn key(int code, float mod, String display) {
        return new Btn(code, EventInput.Type.KEYBOARD, mod, display);
    }

    private static Btn mouse(int code, float mod, String display) {
        return new Btn(code, EventInput.Type.MOUSE, mod, display);
    }

    private static Btn spacer(float mod) {
        return new Btn(null, null, mod, null);
    }

    public static class Btn {
        @Nullable
        public final Integer code;
        @Nullable
        public final EventInput.Type type;
        public final float modifier;
        @Nullable
        public final String display;

        public Btn(@Nullable Integer code, @Nullable EventInput.Type type, float modifier, String display) {
            this.code = code;
            this.type = type;
            this.modifier = modifier;
            this.display = display;
        }
    }
}
