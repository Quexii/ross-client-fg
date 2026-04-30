package eu.shoroa.ross.gui.clickgui.elements.settings;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.settings.ModeEnum;
import eu.shoroa.ross.settings.ModeSetting;
import eu.shoroa.ross.types.Rect;
import eu.shoroa.ross.util.render.MaterialIcons;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;

public class EleSettingMode<T extends Enum<T> & ModeEnum> extends EleSetting<T> {
    private static final float COLLAPSED_HEIGHT = 48f;
    private static final float DROPDOWN_HEIGHT = 22f;
    private static final float OPTION_HEIGHT = 22f;

    private final Animate hoverAnimate = new Animate(150, Easing.CUBIC_IN_OUT);
    private final Animate expandAnimate = new Animate(150, Easing.CUBIC_IN_OUT);

    private boolean expanded = false;

    public EleSettingMode(ModeSetting<T> setting) {
        super(setting);
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        ModeSetting<T> setting = getModeSetting();
        Rect dropdown = getDropdownBounds();
        float expandedHeight = setting.getModes().size() * OPTION_HEIGHT * (float) expandAnimate.getValue();

        hoverAnimate.doEase(dropdown.contains(mouseX, mouseY));
        expandAnimate.doEase(expanded);
        if (dropdown.contains(mouseX, mouseY)) hoverAnimate.forceFinish();

        setHeight(COLLAPSED_HEIGHT + expandedHeight);

        float dropdownHeight = DROPDOWN_HEIGHT + expandedHeight;
        Canvas canvas = Client.INSTANCE.skia.getCanvas();

        try (Paint p = new Paint()) {
            VariableFont.DerivedFont titleFont = Fonts.GoogleFlex
                    .weight(430)
                    .opticSize(12);
            VariableFont.DerivedFont font = Fonts.GoogleFlex
                    .weight(400)
                    .opticSize(14);
            VariableFont.DerivedFont iconFont = Fonts.MaterialIcons.weight(200);

            p.setColor(0xffbfbfbf);
            Renderer.drawText(getSetting().getName(), getX() + 10f, getY() + 9f, titleFont, 12f, Font.Align.CENTER_LEFT, p);

            p.setColor(Color.makeLerp(0x16FFFFFF, 0x20FFFFFF, (float) hoverAnimate.getLinearValue()));
            Renderer.drawRRect(dropdown.x, dropdown.y, dropdown.width, dropdownHeight, 6f, p);

            p.setColor(Color.makeLerp(0x2DFFFFFF, 0x40FFFFFF, (float) hoverAnimate.getLinearValue()));
            p.setStroke(true);
            p.setStrokeWidth(1f);
            Renderer.drawRRect(dropdown.x, dropdown.y, dropdown.width, dropdownHeight, 6f, p);

            p.setStroke(false);
            p.setColor(0xffe2e2e2);
            Renderer.drawText(setting.getCurrentDisplayName(), dropdown.x + 10f, dropdown.y + DROPDOWN_HEIGHT / 2f, font, 13f, Font.Align.CENTER_LEFT, p);

            p.setColor(Color.makeLerp(0x70FFFFFF, 0xE0FFFFFF, (float) hoverAnimate.getLinearValue()));
            Renderer.drawText(expanded ? MaterialIcons.EXPAND_LESS : MaterialIcons.EXPAND_MORE, dropdown.x + dropdown.width - 10f, dropdown.y + DROPDOWN_HEIGHT / 2f, iconFont, 14f, Font.Align.CENTER, p);
        }

        if (expandedHeight > 0.01f) {
            canvas.save();
            canvas.clipRRect(RRect.makeXYWH(dropdown.x, dropdown.y, dropdown.width, dropdownHeight, 6f), true);

            try (Paint p = new Paint()) {
                p.setColor(0x20FFFFFF);
                Renderer.drawRect(dropdown.x + 1f, dropdown.y + DROPDOWN_HEIGHT, dropdown.width - 2f, 1f, p);

                VariableFont.DerivedFont font = Fonts.GoogleFlex
                        .weight(400)
                        .opticSize(14);
                VariableFont.DerivedFont iconFont = Fonts.MaterialIcons.weight(200);

                int currentIndex = setting.getCurrentIndex();
                for (int i = 0; i < setting.getModes().size(); i++) {
                    float rowY = dropdown.y + DROPDOWN_HEIGHT + i * OPTION_HEIGHT;
                    Rect row = new Rect(dropdown.x + 3f, rowY + 1f, dropdown.width - 6f, OPTION_HEIGHT - 2f);
                    boolean selected = i == currentIndex;
                    boolean hovered = row.contains(mouseX, mouseY);

                    p.setColor(selected ? 0x2AFFFFFF : Color.makeLerp(0x00FFFFFF, 0x16FFFFFF, hovered ? 1f : 0f));
                    Renderer.drawRRect(row.x, row.y, row.width, row.height, 4f, p);

                    p.setColor(selected ? 0xfff0f0f0 : 0xffb8b8b8);
                    Renderer.drawText(setting.getModes().get(i).displayName(), row.x + 8f, row.y + row.height / 2f, font, 13f, Font.Align.CENTER_LEFT, p);

                    if (selected) {
                        p.setColor(0xFF4caf50);
                        Renderer.drawText(MaterialIcons.CHECK, row.x + row.width - 8f, row.y + row.height / 2f, iconFont, 13f, Font.Align.CENTER_RIGHT, p);
                    }
                }
            }

            canvas.restore();
        }
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (event.action != EventInput.Action.PRESS || event.type != EventInput.Type.MOUSE) return false;

        ModeSetting<T> setting = getModeSetting();
        Rect dropdown = getDropdownBounds();

        if (dropdown.contains(mouseX, mouseY) && (event.value == 0 || event.value == 1)) {
            expanded = !expanded;
            return true;
        }

        if (expanded && event.value == 0) {
            for (int i = 0; i < setting.getModes().size(); i++) {
                Rect row = new Rect(dropdown.x + 3f, dropdown.y + DROPDOWN_HEIGHT + i * OPTION_HEIGHT + 1f, dropdown.width - 6f, OPTION_HEIGHT - 2f);
                if (row.contains(mouseX, mouseY)) {
                    setting.setIndex(i);
                    return true;
                }
            }
        }

        if (expanded && !getBounds().contains(mouseX, mouseY)) {
            expanded = false;
        }

        return false;
    }

    @Override
    public float getExtendHeight() {
        return getHeight();
    }

    @SuppressWarnings("unchecked")
    private ModeSetting<T> getModeSetting() {
        return (ModeSetting<T>) getSetting();
    }

    private Rect getDropdownBounds() {
        return new Rect(getX() + 10f, getY() + 18f, getWidth() - 20f, DROPDOWN_HEIGHT);
    }
}
