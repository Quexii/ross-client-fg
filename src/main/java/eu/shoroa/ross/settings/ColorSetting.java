package eu.shoroa.ross.settings;


import java.awt.*;

public class ColorSetting extends Setting<Color> {

    public ColorSetting(String name, String id, Color defaultColor) {
        super(name, id, defaultColor, Type.COLOR);
    }

    public int getRGB() {
        return get().getRGB();
    }

    @Override
    public boolean setFromString(String value) {
        // formats: #aarrggbb, argb(int,int,int,int), argb(float,float,float,float), rgba(int,int,int,int), rgba(float,float,float,float), rgb(int, int, int) = argbs with alpha = 255, rgb(float, float, float) = argbs with alpha = 1.0
        try {
            if (value.startsWith("#")) {
                set(new Color((int) Long.parseLong(value.substring(1), 16), true));
                return true;
            } else if (value.startsWith("argb(") && value.endsWith(")")) {
                String[] parts = value.substring(5, value.length() - 1).split(",");
                if (parts.length == 4) {
                    set(new Color(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()), Integer.parseInt(parts[3].trim())));
                    return true;
                }
            } else if (value.startsWith("argb(") && value.endsWith(")")) {
                String[] parts = value.substring(5, value.length() - 1).split(",");
                if (parts.length == 4) {
                    set(new Color(Float.parseFloat(parts[0].trim()), Float.parseFloat(parts[1].trim()), Float.parseFloat(parts[2].trim()), Float.parseFloat(parts[3].trim())));
                    return true;
                }
            } else if (value.startsWith("rgba(") && value.endsWith(")")) {
                String[] parts = value.substring(5, value.length() - 1).split(",");
                if (parts.length == 4) {
                    set(new Color(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()), Integer.parseInt(parts[3].trim())));
                    return true;
                }
            } else if (value.startsWith("rgba(") && value.endsWith(")")) {
                String[] parts = value.substring(5, value.length() - 1).split(",");
                if (parts.length == 4) {
                    set(new Color(Float.parseFloat(parts[0].trim()), Float.parseFloat(parts[1].trim()), Float.parseFloat(parts[2].trim()), Float.parseFloat(parts[3].trim())));
                    return true;
                }
            } else if (value.startsWith("rgb(") && value.endsWith(")")) {
                String[] parts = value.substring(4, value.length() - 1).split(",");
                if (parts.length == 3) {
                    set(new Color(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim())));
                    return true;
                }
            } else if (value.startsWith("rgb(") && value.endsWith(")")) {
                String[] parts = value.substring(4, value.length() - 1).split(",");
                if (parts.length == 3) {
                    set(new Color(Float.parseFloat(parts[0].trim()), Float.parseFloat(parts[1].trim()), Float.parseFloat(parts[2].trim())));
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return false;
    }
}