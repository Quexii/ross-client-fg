package eu.shoroa.ross.feature.setting;

import java.awt.Color;

public class ColorSetting extends Setting<Color> {

    public ColorSetting(String name, String id, Color defaultColor) {
        super(name, id, defaultColor, Type.COLOR);
    }

    public int getRGB() {
        return get().getRGB();
    }

    @Override
    public boolean setFromString(String value) {
        try {
            String v = value.trim();

            if (v.startsWith("#")) {
                String hex = v.substring(1);
                long parsed = Long.parseLong(hex, 16);
                if (hex.length() == 6) {
                    set(new Color((int) (parsed | 0xFF000000), true));
                    return true;
                } else if (hex.length() == 8) {
                    set(new Color((int) parsed, true));
                    return true;
                }
                return false;
            }

            if (v.startsWith("argb(") && v.endsWith(")")) {
                return parseComponents(v.substring(5, v.length() - 1), true, true);
            }
            if (v.startsWith("rgba(") && v.endsWith(")")) {
                return parseComponents(v.substring(5, v.length() - 1), false, true);
            }
            if (v.startsWith("rgb(") && v.endsWith(")")) {
                return parseComponents(v.substring(4, v.length() - 1), false, false);
            }

        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    private boolean parseComponents(String inner, boolean alphaFirst, boolean hasAlpha) {
        String[] parts = inner.split(",");
        int expected = hasAlpha ? 4 : 3;
        if (parts.length != expected) return false;

        boolean isFloat = false;
        for (String part : parts) {
            if (part.contains(".")) {
                isFloat = true;
                break;
            }
        }

        if (isFloat) {
            float[] f = new float[expected];
            for (int i = 0; i < expected; i++) {
                f[i] = Float.parseFloat(parts[i].trim());
                if (f[i] < 0 || f[i] > 1) return false;
            }
            if (alphaFirst && hasAlpha) {
                set(new Color(f[1], f[2], f[3], f[0]));
            } else if (hasAlpha) {
                set(new Color(f[0], f[1], f[2], f[3]));
            } else {
                set(new Color(f[0], f[1], f[2]));
            }
        } else {
            int[] c = new int[expected];
            for (int i = 0; i < expected; i++) {
                c[i] = Integer.parseInt(parts[i].trim());
                if (c[i] < 0 || c[i] > 255) return false;
            }
            if (alphaFirst && hasAlpha) {
                set(new Color(c[1], c[2], c[3], c[0]));
            } else if (hasAlpha) {
                set(new Color(c[0], c[1], c[2], c[3]));
            } else {
                set(new Color(c[0], c[1], c[2]));
            }
        }
        return true;
    }

    public String toHexString() {
        Color c = get();
        return String.format("#%02X%02X%02X%02X", c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
    }
}