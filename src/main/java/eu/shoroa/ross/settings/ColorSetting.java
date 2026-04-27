package eu.shoroa.ross.settings;


import java.awt.*;

public class ColorSetting extends Setting<Color> {

    public ColorSetting(String name, String id, Color defaultColor) {
        super(name, id, defaultColor, Type.COLOR);
    }

    public int getRGB() {
        return get().getRGB();
    }
}