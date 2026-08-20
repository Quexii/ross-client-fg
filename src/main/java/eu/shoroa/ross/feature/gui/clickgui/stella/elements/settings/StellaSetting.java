package eu.shoroa.ross.feature.gui.clickgui.stella.elements.settings;

import eu.shoroa.ross.feature.gui.GuiElement;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.setting.Setting;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import io.github.humbleui.skija.Paint;

public abstract class StellaSetting<T> extends GuiElement {
    public static final float ROW_HEIGHT = 56f;

    private final Setting<T> setting;

    public StellaSetting(float x, float y, float width, float height, Setting<T> setting) {
        super(x, y, width, height);
        this.setting = setting;
    }

    public Setting<T> getSetting() {
        return setting;
    }

    protected StellaTheme theme() {
        return StellaTheme.get();
    }

    protected void drawLabel(Paint p) {
        p.setColor(theme().foreground);
        UI.drawText(setting.getName(), getX() + 4f, getY() + getHeight() / 2f, Fonts.GoogleFlex.weight(550), 20f, Align.CENTER_LEFT, p);
    }
}
