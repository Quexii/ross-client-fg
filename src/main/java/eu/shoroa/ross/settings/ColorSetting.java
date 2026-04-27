package eu.shoroa.ross.settings;


import java.awt.*;

public class ColorSetting extends Setting<Color> {

    public ColorSetting(String name, Color defaultColor) {
        super(name, defaultColor, Type.COLOR);
    }

    public int getRGB() {
        return get().getRGB();
    }
}