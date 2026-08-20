package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.*;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.ModeEnum;
import eu.shoroa.ross.feature.setting.ModeSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.Size;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleArrayList extends HUDModule {
    private static final Log log = LogFactory.getLog(ModuleArrayList.class);
    private final SettingCategory settings = addCategory("Settings", ".", "settings");
    private final ModeSetting<Style> style = register(new ModeSetting<>("Style", "style", Style.CLASSIC), settings);
    private final ModeSetting<ColorMode> color = register(new ModeSetting<>("Color", "color", ColorMode.THEME), settings);

    private float calcW = 0f, calcH = 0f;
    private List<Module> sortedList = new ArrayList<>();

    public ModuleArrayList() {
        super("Array List", "Shows the enabled modules", MaterialIcons.SORT);
        addElement(new Element());
    }
    private class Element extends HUDElement {

        protected Element() {
            super("main");
            setPlacement(HUDAnchor.RIGHT_TOP, -10, 66);
        }

        @Override
        public void render(Hud.Layer layer) {
            if (!layer.is(Hud.Layer.NAME_SKIA_BOTTOM)) return;

            sortedList.clear();
            sortedList.addAll(ModuleManager.getEnabledModules());

            switch (style.getCurrent()) {
                case CLASSIC:
                    renderClassic();
                    break;
                case OUTLINE:
                    renderOutline();
                    break;
                case TEXT:
                    renderText();
                    break;
            }
        }

        private void renderClassic() {
            VariableFont.DerivedFont font = Fonts.GoogleFlex.weight(600);
            sortedList.sort(Comparator.comparingDouble(module -> UI.getTextBounds(((Module) module).name, font, 16f).width).reversed());

            final float ROW_HEIGHT = 20f;

            calcH = sortedList.size() * ROW_HEIGHT;
            calcW = sortedList.get(sortedList.size() - 1) != null ? UI.getTextBounds(sortedList.get(0).name, font, 16f).width + 10f : 0f;

            StellaTheme t = StellaTheme.get();

            Align align = rightAligned() ? Align.TOP_RIGHT : Align.TOP_LEFT;

            PathBuilder pb = new PathBuilder();

            float my = 0;
            int i = 0;
            for (Module module : sortedList) {
                float w = UI.getTextBounds(module.name, font, 16f).width + 10f;
                float rectX = rightAligned() ? getBounds().x + getBounds().width - w : getBounds().x;

                float rTL, rTR, rBL, rBR;
                rTL = i == 0 ? 8f : 0f;
                rTR = i == 0 ? 8f : 0f;
                if (rightAligned()) {
                    rBL = i == sortedList.size() - 1 ? 8f : Math.min(8f, w - UI.getTextBounds(sortedList.get(i + 1).name, font, 16f).width - 10);
                    rBR = i == sortedList.size() - 1 ? 8f : 0f;
                } else {
                    rBL = i == sortedList.size() - 1 ? 8f : 0f;
                    rBR = i == sortedList.size() - 1 ? 8f : Math.min(8f, w - UI.getTextBounds(sortedList.get(i + 1).name, font, 16f).width - 10);
                }

                pb.addRRect(RRect.makeXYWH(rectX, getBounds().y + my, w, ROW_HEIGHT, rTL, rTR, rBR, rBL));
                my += ROW_HEIGHT;
                i++;
            }
            Path path = pb.build();
            try (Paint p = new Paint()) {
                p.setColor(t.shadow);
                p.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 5f));
                UI.drawPath(path, p);
                p.setMaskFilter(null);
                p.setColor(t.surface);
                UI.drawPath(path, p);
            }

            my = 0;
            i = 0;
            for (Module module : sortedList) {
                float textX = rightAligned() ? getBounds().x + getBounds().width - 5 : getBounds().x + 5;

                try (Paint p = new Paint()) {
                    p.setColor(t.foreground);
                    UI.drawText(module.name, textX, getBounds().y + my, font, 16f, align, p);
                }
                my += ROW_HEIGHT;
                i++;
            }
        }

        private void renderOutline() {
            VariableFont.DerivedFont font = Fonts.GoogleFlex.weight(600);
            sortedList.sort(Comparator.comparingDouble(module -> UI.getTextBounds(((Module) module).name, font, 16f).width).reversed());

            final float ROW_HEIGHT = 20f;

            calcH = sortedList.size() * ROW_HEIGHT;
            calcW = sortedList.get(sortedList.size() - 1) != null ? UI.getTextBounds(sortedList.get(0).name, font, 16f).width + 10f : 0f;

            StellaTheme t = StellaTheme.get();

            Align align = rightAligned() ? Align.TOP_RIGHT : Align.TOP_LEFT;

            PathBuilder pb = new PathBuilder();

            int[] colors = new int[sortedList.size()];
            float[] positions = new float[sortedList.size()];

            float my = 0;
            int i = 0;
            for (Module module : sortedList) {
                colors[i] = Color.getHSBColor(((System.currentTimeMillis() + i * 300) % 10000L) / 10000f, 0.5f, 0.9f).getRGB();
                positions[i] = (float) i / (sortedList.size() - 1);
                float w = UI.getTextBounds(module.name, font, 16f).width + 10f;
                float rectX = rightAligned() ? getBounds().x + getBounds().width - w : getBounds().x;

                float rTL, rTR, rBL, rBR;
                rTL = i == 0 ? 8f : 0f;
                rTR = i == 0 ? 8f : 0f;
                if (rightAligned()) {
                    rBL = i == sortedList.size() - 1 ? 8f : Math.min(8f, w - UI.getTextBounds(sortedList.get(i + 1).name, font, 16f).width - 10);
                    rBR = i == sortedList.size() - 1 ? 8f : 0f;
                } else {
                    rBL = i == sortedList.size() - 1 ? 8f : 0f;
                    rBR = i == sortedList.size() - 1 ? 8f : Math.min(8f, w - UI.getTextBounds(sortedList.get(i + 1).name, font, 16f).width - 10);
                }

                pb.addRRect(RRect.makeXYWH(rectX, getBounds().y + my, w, ROW_HEIGHT, rTL, rTR, rBR, rBL));
                my += ROW_HEIGHT;
                i++;
            }
            Path path = pb.build();
            try (Paint p = new Paint()) {
                p.setColor(t.shadow);
                p.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 5f));
                UI.drawPath(path, p);
                p.setMaskFilter(null);

                p.setColor(-1);

                p.setStroke(true);
                p.setStrokeWidth(6f);

                if (color.get().equals(ColorMode.RAINBOW)) {
                    p.setShader(Shader.makeLinearGradient(
                            getBounds().x, getBounds().y, getBounds().x, getBounds().y + calcH,
                            colors,
                            positions
                    ));
                } else {
                    p.setColor(t.accent);
                }
                UI.drawPath(path, p);
                p.setShader(null);

                p.setStroke(false);
                p.setColor(t.surface);
                UI.drawPath(path, p);
            }

            my = 0;
            i = 0;
            for (Module module : sortedList) {
                float textX = rightAligned() ? getBounds().x + getBounds().width - 5 : getBounds().x + 5;

                try (Paint p = new Paint()) {
                    p.setColor(t.foreground);
                    UI.drawText(module.name, textX, getBounds().y + my, font, 16f, align, p);
                }
                my += ROW_HEIGHT;
                i++;
            }
        }

        private void renderText() {
            VariableFont.DerivedFont font = Fonts.GoogleFlex.weight(600).roundness(50);
            sortedList.sort(Comparator.comparingDouble(module -> UI.getTextBounds(((Module) module).name, font, 16f).width).reversed());

            final float ROW_HEIGHT = 20f;

            calcH = sortedList.size() * ROW_HEIGHT;
            calcW = sortedList.get(sortedList.size() - 1) != null ? UI.getTextBounds(sortedList.get(0).name, font, 16f).width + 10f : 0f;

            StellaTheme t = StellaTheme.get();

            Align align = rightAligned() ? Align.TOP_RIGHT : Align.TOP_LEFT;

            int colorOutline = t.surface;
            int colorText = t.foreground;

            int i = 0;
            float my = 0;
            for (Module module : sortedList) {
                float textX = rightAligned() ? getBounds().x + getBounds().width - 5 : getBounds().x + 5;

                if (color.get().equals(ColorMode.RAINBOW)) {
                    float hue = ((System.currentTimeMillis() + i * 300) % 10000L) / 10000f;
                    colorOutline = Color.getHSBColor(hue, 0.5f, 0.9f).getRGB();
                    colorText = Color.getHSBColor(hue, 0.5f, 0.1f).getRGB();
                }

                try (Paint p = new Paint()) {
                    p.setStroke(true);
                    p.setStrokeWidth(3f);
                    p.setColor(colorOutline);
                    UI.drawText(module.name, textX, getBounds().y + my, font, 16f, align, p);

                    p.setStroke(false);
                    p.setColor(colorText);
                    UI.drawText(module.name, textX, getBounds().y + my, font, 16f, align, p);
                }
                my += ROW_HEIGHT;
                i++;
            }
        }

        private boolean rightAligned() {
            return getAnchor().fx >= 0.5;
        }

        @Override
        public void dummy(Hud.Layer layer) {
            render(layer);
        }

        @Override
        public Size getSize() {
            return new Size(calcW, calcH);
        }
    }

    public enum Style implements ModeEnum {
        CLASSIC("Classic"), OUTLINE("Outline"), TEXT("Text");

        private final String label;

        Style(String label) {
            this.label = label;
        }

        @Override
        public String displayName() {
            return label;
        }
    }

    public enum ColorMode implements ModeEnum {
        THEME("Theme"), RAINBOW("Rainbow");

        private final String label;

        ColorMode(String label) {
            this.label = label;
        }

        @Override
        public String displayName() {
            return label;
        }
    }
}
