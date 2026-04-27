package eu.shoroa.ross.gui.clickgui.elements;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.gui.GuiElement;
import eu.shoroa.ross.gui.clickgui.elements.settings.*;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.settings.*;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;

import java.util.Arrays;
import java.util.List;

import static eu.shoroa.ross.Client.mc;

public class EleModule extends GuiElement {
    private final Module module;

    private final Animate hoverAnimate = new Animate(150, Easing.CUBIC_IN_OUT);
    private final Animate toggleAnimate = new Animate(150, Easing.CUBIC_IN_OUT);
    private final Animate expandAnimate = new Animate(150, Easing.CUBIC_IN_OUT);

    private boolean expanded = false;

    private final EleSetting[] settings;

    public EleModule(Module module) {
        super(0f, 0f, 220f, 40f);
        this.module = module;

        List<Setting<?>> moduleSettings = module.getSettings();

        settings = new EleSetting[moduleSettings.size()];
        for (int i = 0; i < moduleSettings.size(); i++) {
            Setting<?> setting = moduleSettings.get(i);
            switch (setting.getType()) {
                case BOOLEAN:
                    settings[i] = new EleSettingBoolean((BooleanSetting) setting);
                    break;
                case NUMBER:
                    settings[i] = new EleSettingNumber((NumberSetting) setting);
                    break;
                case MODE:
                    settings[i] = new EleSettingMode((ModeSetting) setting);
                    break;
                case COLOR:
                    settings[i] = new EleSettingColor((ColorSetting) setting);
                    break;
            }
        }
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        hoverAnimate.doEase(getBounds().contains(mouseX, mouseY));
        toggleAnimate.doEase(module.isEnabled());
        expandAnimate.doEase(expanded);

        if (getBounds().contains(mouseX, mouseY)) hoverAnimate.forceFinish();

        float extendHeight = getExtendHeight();

        try (Paint p = new Paint()) {
            p.setStroke(true);
            p.setStrokeWidth(2f);
            p.setColor(Color.makeLerp(0xFF303030, 0xFF334f7f, (float) toggleAnimate.getLinearValue()));
            Renderer.drawRRect(getX() + 4, getY() + 4, getWidth() - 8, extendHeight - 8, 6f, p);
        }

//0xFF2F4E84
        try (Paint p = new Paint()) {
            p.setColor(Color.makeLerp(0xFF282828, 0xFF294068, (float) toggleAnimate.getLinearValue()));
            Renderer.drawRRect(getX() + 4, getY() + 4, getWidth() - 8, extendHeight - 8, 6f, p);
        }

        try (Paint p = new Paint()) {
            p.setColor(Color.makeLerp(0x00FFFFFF, 0x19FFFFFF, (float) hoverAnimate.getLinearValue()));
            Renderer.drawRRect(getX() + 4, getY() + 4, getWidth() - 8, extendHeight - 8, 6f, p);
        }

        try (Paint p = new Paint()) {
            p.setColor(Color.makeLerp(0xffa9a9a9, 0xffe2e2e2, (float) toggleAnimate.getLinearValue()));
            VariableFont.DerivedFont font = Fonts.GoogleFlex
                    .weight(450)
                    .opticSize(12);
            Renderer.drawText(module.name, getX() + 12, getY() + getHeight() / 2f, font, 14f, Font.Align.CENTER_LEFT, p);
        }

        Client.INSTANCE.skia.getCanvas().save();
        Client.INSTANCE.skia.getCanvas().clipRRect(RRect.makeXYWH(getX(), getY() + getHeight(), getWidth(), extendHeight - getHeight(), 6f), true);

        try (Paint p = new Paint()) {
            p.setColor(0x20FFFFFF);
            Renderer.drawRect(getX() + 10, getY() + getHeight(), getWidth() - 20, 1f, p);
        }

        if (expandAnimate.getValue() > 0.01) {
            float my = getHeight();
            for (EleSetting setting : settings) {
                setting.setX(getX() + 4f);
                setting.setY(getY() + my);
                setting.setWidth(getWidth() - 8f);
                setting.render(mouseX, mouseY, partialTicks);
                my += setting.getExtendHeight();
            }
        }
        Client.INSTANCE.skia.getCanvas().restore();
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (getBounds().contains(mouseX, mouseY) && event.action == EventInput.Action.PRESS && event.type == EventInput.Type.MOUSE) {
            if (event.value == 0) module.toggle();
            else if (event.value == 1) expanded = !expanded;
            return true;
        }
        if (expanded) {
            for (EleSetting setting : settings) {
                if (setting.input(mouseX, mouseY, event)) {
                    return true;
                }
            }
        }
        return false;
    }

    public float getExtendHeight() {
        return (float) (getHeight() + Arrays.stream(settings).filter(eleSetting -> eleSetting.getSetting().isVisible())
                .map(EleSetting::getExtendHeight)
                .reduce(0f, Float::sum) * expandAnimate.getValue());
    }
}
