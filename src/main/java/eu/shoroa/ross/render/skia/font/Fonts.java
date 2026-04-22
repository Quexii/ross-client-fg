package eu.shoroa.ross.render.skia.font;

import eu.shoroa.ross.util.IO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Fonts {
    private static Map<String, SimpleFont> simpleFonts = new HashMap<>();
    private static Map<String, VariableFont> variableFonts = new HashMap<>();

    public static final SimpleFont MapleMono = Fonts.createSimpleFont("/assets/rossclient/fonts/MapleMono.ttf");
    public static final VariableFont MirandaSans = Fonts.createVariableFont("/assets/rossclient/fonts/MirandaSans_wght.ttf");
    public static final VariableFont MirandaSansItalic = Fonts.createVariableFont("/assets/rossclient/fonts/MirandaSans-Italic_wght.ttf");
    public static final SimpleFont RajdhaniBold = Fonts.createSimpleFont("/assets/rossclient/fonts/rajdhani/bold.ttf");
    public static final SimpleFont RajdhaniLight = Fonts.createSimpleFont("/assets/rossclient/fonts/rajdhani/light.ttf");
    public static final SimpleFont RajdhaniMedium = Fonts.createSimpleFont("/assets/rossclient/fonts/rajdhani/medium.ttf");
    public static final SimpleFont RajdhaniRegular = Fonts.createSimpleFont("/assets/rossclient/fonts/rajdhani/regular.ttf");
    public static final SimpleFont RajdhaniSemiBold = Fonts.createSimpleFont("/assets/rossclient/fonts/rajdhani/semibold.ttf");

    public static SimpleFont createSimpleFont(String path) {
        simpleFonts.put(path, new SimpleFont());
        return simpleFonts.get(path);
    }

    public static VariableFont createVariableFont(String path) {
        variableFonts.put(path, new VariableFont());
        return variableFonts.get(path);
    }

    public static void load() {
        simpleFonts.forEach((path, font) -> {
            try {
                font.init(IO.resourceToBuffer(path, 1024));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        variableFonts.forEach((path, variableFont) -> {
            try {
                variableFont.init(IO.resourceToBuffer(path, 1024));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
