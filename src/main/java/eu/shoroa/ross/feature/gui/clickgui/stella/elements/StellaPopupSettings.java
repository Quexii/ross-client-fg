package eu.shoroa.ross.feature.gui.clickgui.stella.elements;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.gui.clickgui.stella.elements.settings.*;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.*;
import eu.shoroa.ross.utils.math.Mth;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.DampFloat;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PathBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StellaPopupSettings extends StellaPopup {
    private static final float CONTENT_TOP = 80f;
    private static final float PAD = 24f;
    private static final float HEADER_H = 34f;
    private static final float HEADER_GAP_TOP = 14f;
    private static final float HEADER_GAP_BOTTOM = 8f;
    private static final float SCROLLBAR_W = 6f;

    public final Module module;

    private final Map<SettingCategory, List<StellaSetting<?>>> categories = new LinkedHashMap<>();
    private final Map<Setting<?>, StellaSetting<?>> elements = new LinkedHashMap<>();

    private final DampFloat scrollEase = new DampFloat();
    private float scrollTarget = 0f;
    private float maxScroll = 0f;

    public StellaPopupSettings(float width, float height, Module module) {
        super(width, height, module.name);
        this.module = module;

        float rowW = width - PAD * 2 - SCROLLBAR_W - 10f;
        for (SettingCategory category : module.getSettings()) {
            List<StellaSetting<?>> rows = new ArrayList<>();
            for (Setting<?> setting : category.getSettings()) {
                StellaSetting<?> element = createElement(setting, rowW);
                if (element == null) continue;
                rows.add(element);
                elements.put(setting, element);
            }
            categories.put(category, rows);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private StellaSetting<?> createElement(Setting<?> setting, float width) {
        switch (setting.getType()) {
            case BOOLEAN:
                return new StellaBooleanSetting(0f, 0f, width, StellaSetting.ROW_HEIGHT, (BooleanSetting) setting);
            case NUMBER:
                return new StellaNumberSetting(0f, 0f, width, StellaSetting.ROW_HEIGHT, (NumberSetting) setting);
            case MODE:
                return new StellaModeSetting(0f, 0f, width, StellaSetting.ROW_HEIGHT, (ModeSetting) setting);
            case COLOR:
                return new StellaColorSetting(0f, 0f, width, StellaSetting.ROW_HEIGHT, (ColorSetting) setting);
            default:
                return null;
        }
    }

    private float viewX() {
        return getX() + PAD;
    }

    private float viewY() {
        return getY() + CONTENT_TOP;
    }

    private float viewW() {
        return getWidth() - PAD * 2;
    }

    private float viewH() {
        return getHeight() - CONTENT_TOP - 16f;
    }

    private float contentHeight() {
        float h = 0f;
        for (Map.Entry<SettingCategory, List<StellaSetting<?>>> entry : categories.entrySet()) {
            if (!hasVisibleSettings(entry.getValue())) continue;
            h += HEADER_H + HEADER_GAP_TOP + HEADER_GAP_BOTTOM;
            for (StellaSetting<?> element : entry.getValue()) {
                if (element.getSetting().isVisible()) h += StellaSetting.ROW_HEIGHT;
            }
        }
        return h;
    }

    private boolean hasVisibleSettings(List<StellaSetting<?>> rows) {
        for (StellaSetting<?> element : rows) {
            if (element.getSetting().isVisible()) return true;
        }
        return false;
    }

    @Override
    void content(float mouseX, float mouseY, float partialTicks) {
        maxScroll = Math.max(0f, contentHeight() - viewH());
        scrollTarget = Mth.clamp(scrollTarget, 0f, maxScroll);
        Mth.smoothDamp(scrollEase, scrollTarget, 1f / 15f, (float) Animate.getDelta());

        try (Paint p = new Paint()) {
            UI.save();
            UI.clipRect(viewX(), viewY(), viewW(), viewH());

            float y = viewY() - scrollEase.value;
            for (Map.Entry<SettingCategory, List<StellaSetting<?>>> entry : categories.entrySet()) {
                if (!hasVisibleSettings(entry.getValue())) continue;

                y += HEADER_GAP_TOP;
                drawCategoryHeader(entry.getKey(), viewX(), y, p);
                y += HEADER_H + HEADER_GAP_BOTTOM;

                boolean first = true;
                for (StellaSetting<?> element : entry.getValue()) {
                    if (!element.getSetting().isVisible()) continue;

                    if (!first) {
                        p.setColor(StellaTheme.get().dividerSoft);
                        UI.drawRect(viewX() + 4f, y, viewW() - SCROLLBAR_W - 14f, 1.5f, p);
                    }
                    first = false;

                    element.setX(viewX());
                    element.setY(y);
                    element.render(mouseX, mouseY, partialTicks);
                    y += StellaSetting.ROW_HEIGHT;
                }
            }

            UI.restore();

            if (maxScroll > 0f) {
                float barX = getX() + getWidth() - PAD / 2f - SCROLLBAR_W;
                float thumbH = Math.max(30f, viewH() * (viewH() / contentHeight()));
                float thumbY = viewY() + (scrollEase.value / maxScroll) * (viewH() - thumbH);

                p.setColor(StellaTheme.get().shadowSoft);
                UI.drawRRect(barX, viewY(), SCROLLBAR_W, viewH(), SCROLLBAR_W / 2f, p);
                p.setColor(StellaTheme.get().accent);
                UI.drawRRect(barX, thumbY, SCROLLBAR_W, thumbH, SCROLLBAR_W / 2f, p);
            }

            if (categories.isEmpty() || elements.isEmpty()) {
                p.setColor(StellaTheme.get().textMuted);
                UI.drawText("No settings", getX() + getWidth() / 2f, viewY() + viewH() / 2f, Fonts.GoogleFlex.weight(500), 22f, Align.CENTER, p);
            }
        }
    }

    private void drawCategoryHeader(SettingCategory category, float x, float y, Paint p) {
        float w = viewW() - SCROLLBAR_W - 10f;

        p.setColor(StellaTheme.get().accent);
        try (PathBuilder pb = new PathBuilder()) {
            pb.moveTo(x, y);
            pb.lineTo(x + w, y);
            pb.lineTo(x + w - 10f, y + HEADER_H);
            pb.lineTo(x, y + HEADER_H);
            pb.closePath();
            UI.drawPath(pb.build(), p);
        }

        p.setColor(StellaTheme.get().accentSoft);
        try (PathBuilder pb = new PathBuilder()) {
            pb.moveTo(x, y);
            pb.lineTo(x + 14f, y);
            pb.lineTo(x + 8f, y + HEADER_H);
            pb.lineTo(x, y + HEADER_H);
            pb.closePath();
            UI.drawPath(pb.build(), p);
        }

        p.setColor(StellaTheme.get().onAccent);
        UI.drawText(category.name, x + 24f, y + HEADER_H / 2f, Fonts.GoogleFlex.weight(600), 19f, Align.CENTER_LEFT, p);
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (super.input(mouseX, mouseY, event)) {
            return true;
        }

        boolean inViewport = mouseX >= viewX() && mouseX <= viewX() + viewW() && mouseY >= viewY() && mouseY <= viewY() + viewH();
        boolean isPress = event.type == EventInput.Type.MOUSE && event.action == EventInput.Action.PRESS;

        for (List<StellaSetting<?>> rows : categories.values()) {
            for (StellaSetting<?> element : rows) {
                if (!element.getSetting().isVisible()) continue;
                // presses only count inside the viewport; releases always go through so drags end
                if (isPress && !inViewport) continue;
                if (element.input(mouseX, mouseY, event)) {
                    return true;
                }
            }
        }

        return contains(mouseX, mouseY);
    }

    @Override
    public void scroll(float value, float partialTicks) {
        scrollTarget = Mth.clamp(scrollTarget - value * 60f, 0f, maxScroll);
    }
}
