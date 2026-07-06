package eu.shoroa.ross.feature.module.impl.render.wheelgui;

public class WheelEntry {
    public final String title;
    public final String icon;
    public final Runnable action;

    public WheelEntry(String title, String icon, Runnable action) {
        this.title = title;
        this.icon = icon;
        this.action = action;
    }
}
