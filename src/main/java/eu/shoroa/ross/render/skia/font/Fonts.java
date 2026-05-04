package eu.shoroa.ross.render.skia.font;

import eu.shoroa.ross.util.IO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Fonts {
    private static Map<String, SimpleFont> simpleFonts = new HashMap<>();
    private static Map<String, VariableFont> variableFonts = new HashMap<>();

    /**
     * <strong>Material Rounded</strong>
     * <ul>
     *  <li>FILL: 0 to 1 (default: 0)</li>
     *  <li>GRAD: -50 to 200 (default: 0)</li>
     *  <li>opsz: 20 to 48 (default: 24)</li>
     *  <li>wght: 100 to 700 (default: 400)</li>
     * </ul>
     */
    public static final VariableFont MaterialIcons = Fonts.createVariableFont("/assets/rossclient/fonts/MaterialRounded.ttf");

    /**
     * <strong>Google Sans Flex</strong>
     * <ul>
     *  <li>GRAD: 0 to 100 (default: 0)</li>
     *  <li>ROND: 0 to 100 (default: 0)</li>
     *  <li>opsz: 6 to 144 (default: 18)</li>
     *  <li>slnt: -10 to 0 (default: 0)</li>
     *  <li>wdth: 25 to 151 (default: 100)</li>
     *  <li>wght: 1 to 1000 (default: 400)</li>
     * </ul>
     */
    public static final VariableFont GoogleFlex = Fonts.createVariableFont("/assets/rossclient/fonts/GoogleSansFlex.ttf");

    public static final SimpleFont CaviarDreams = Fonts.createSimpleFont("/assets/rossclient/fonts/CaviarDreams.ttf");

    public static final SimpleFont CaviarDreamsBold = Fonts.createSimpleFont("/assets/rossclient/fonts/CaviarDreams_Bold.ttf");

    public static final SimpleFont MarketDeco = Fonts.createSimpleFont("/assets/rossclient/fonts/MarketDeco.ttf");

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
