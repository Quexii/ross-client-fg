package eu.shoroa.ross.gui.clickgui.elements.settings;

import eu.shoroa.ross.gui.GuiElement;
import eu.shoroa.ross.settings.Setting;

public abstract class EleSetting<T> extends GuiElement {
    private final Setting<T> setting;

    public EleSetting(Setting<T> setting) {
        super(0f, 0f, 220f, 40f);
        this.setting = setting;
    }

    public abstract float getExtendHeight();

    public Setting<T> getSetting() {
        return setting;
    }
}
