package eu.shoroa.ross.feature.gui.clickgui.stella;

import com.google.gson.*;
import eu.shoroa.nori.parse.Node;
import eu.shoroa.ross.Client;
import eu.shoroa.ross.utils.io.Res;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

public final class StellaTheme {
    private static final Logger LOGGER = LogManager.getLogger(StellaTheme.class);

    public static StellaTheme LIGHT;
    public static StellaTheme DARK;
    public static StellaTheme FOREST;
    public static StellaTheme SLATE;
    public static StellaTheme EMBER;
    public static StellaTheme ROSE;
    public static StellaTheme AURORA;
    public static StellaTheme ABYSS;

    public static void init() {
        try {
            LIGHT = fromRes("/assets/rossclient/themes/light.nori");
            DARK = fromRes("/assets/rossclient/themes/dark.nori");
            FOREST = fromRes("/assets/rossclient/themes/forest.nori");
            SLATE = fromRes("/assets/rossclient/themes/slate.nori");
            EMBER = fromRes("/assets/rossclient/themes/ember.nori");
            ROSE = fromRes("/assets/rossclient/themes/rose.nori");
            AURORA = fromRes("/assets/rossclient/themes/aurora.nori");
            ABYSS = fromRes("/assets/rossclient/themes/abyss.nori");

            active = LIGHT;
        } catch (IOException e) {
            LOGGER.error("Failed to load theme", e);
            throw new RuntimeException(e);
        }
    }

    private static StellaTheme active;

    public static StellaTheme get() {
        return active;
    }

    public static void set(StellaTheme theme) {
        active = theme;
    }

    // Accent
    public int accent;
    public int accentDeep;
    public int accentSoft;
    public int accentHalftone;

    // Surfaces
    public int surface;
    public int surfaceDim;
    public int surfaceBright;
    public int surfaceRaised;

    // Foregrounds
    public int foreground;
    public int foregroundMuted;
    public int foregroundContrast;

    // Effects
    public int inactive;
    public int track;
    public int radio;
    public int radioBorder;
    public int border;

    public int divider;
    public int dividerSoft;

    public int shadow;
    public int shadowSoft;
    public int outline;
    public int dimmer;

    private StellaTheme() {
    }

    public static StellaTheme fromRes(String path) throws IOException {
        ByteBuffer buf = Res.resourceToBuffer(path, 1024);
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        Node<?> root = Client.nori.parse(new String(bytes));
        StellaTheme theme = new StellaTheme();
        theme.accent = root.get("accent").getInt();
        theme.accentDeep = root.get("accentDeep").getInt();
        theme.accentSoft = root.get("accentSoft").getInt();
        theme.accentHalftone = root.get("accentHalftone").getInt();
        theme.surface = root.get("surface").getInt();
        theme.surfaceDim = root.get("surfaceDim").getInt();
        theme.surfaceBright = root.get("surfaceBright").getInt();
        theme.surfaceRaised = root.get("surfaceRaised").getInt();
        theme.foreground = root.get("foreground").getInt();
        theme.foregroundMuted = root.get("foregroundMuted").getInt();
        theme.foregroundContrast = root.get("foregroundContrast").getInt();
        theme.inactive = root.get("inactive").getInt();
        theme.track = root.get("track").getInt();
        theme.radio = root.get("radio").getInt();
        theme.radioBorder = root.get("radioBorder").getInt();
        theme.border = root.get("border").getInt();
        theme.divider = root.get("divider").getInt();
        theme.dividerSoft = root.get("dividerSoft").getInt();
        theme.shadow = root.get("shadow").getInt();
        theme.shadowSoft = root.get("shadowSoft").getInt();
        theme.outline = root.get("outline").getInt();
        theme.dimmer = root.get("dimmer").getInt();
        return theme;
    }
}
