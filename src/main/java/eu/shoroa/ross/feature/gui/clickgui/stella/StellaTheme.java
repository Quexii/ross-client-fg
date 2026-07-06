package eu.shoroa.ross.feature.gui.clickgui.stella;

public final class StellaTheme {
    public static final StellaTheme LIGHT = light();

    private static StellaTheme active = LIGHT;

    public static StellaTheme get() {
        return active;
    }

    public static void set(StellaTheme theme) {
        active = theme;
    }

    /** Primary teal — headers, sliders fill, toggles, scrollbar thumb. */
    public int accent;
    /** Darker teal — selected sidebar entry. */
    public int accentDeep;
    /** Lighter teal — category header notch. */
    public int accentSoft;
    /** Teal tint for the halftone dot pattern drawn over accent areas. */
    public int accentHalftone;

    /** Main background (cream). */
    public int surface;
    /** Slightly dimmed background — module grid area. */
    public int surfaceDim;
    /** Brightest surface — knobs, cycler boxes. */
    public int surfaceBright;
    /** Arrow wedge pointing out of the sidebar. */
    public int sidebarArrow;

    /** Primary text/icons on light surfaces (navy). */
    public int text;
    /** Muted text — placeholders like "No settings". */
    public int textMuted;
    /** Text/icons on accent-colored surfaces. */
    public int onAccent;

    /** Disabled/off state of accent-colored controls (toggle off, module off). */
    public int inactive;
    /** Empty slider track. */
    public int track;
    /** Unselected radio fill. */
    public int radio;
    /** Unselected radio outline. */
    public int radioBorder;
    /** Hairline border around cards, knobs and swatches. */
    public int border;

    /** Divider line on the panel. */
    public int divider;
    /** Divider line between setting rows. */
    public int dividerSoft;

    /** Soft navy drop shadow. */
    public int shadow;
    /** Fainter shadow — knob rings, scrollbar track. */
    public int shadowSoft;
    /** Dark outline stroke on accent-filled pills/buttons. */
    public int outline;
    /** Fullscreen dim behind popups. */
    public int dimmer;

    private StellaTheme() {
    }

    private static StellaTheme light() {
        StellaTheme t = new StellaTheme();

        t.accent = 0xFF0DBDC3;
        t.accentDeep = 0xFF00A7AB;
        t.accentSoft = 0xFF6EDEE1;
        t.accentHalftone = 0xFF09B7BB;

        t.surface = 0xFFF9F9F7;
        t.surfaceDim = 0xFFEFF3F3;
        t.surfaceBright = 0xFFFDFDFC;
        t.sidebarArrow = 0xFFEBF5F7;

        t.text = 0xFF264278;
        t.textMuted = 0x88264278;
        t.onAccent = 0xFFF9F9F7;

        t.inactive = 0xFF93A9BF;
        t.track = 0xFFC7CDD4;
        t.radio = 0xFFD5DBE1;
        t.radioBorder = 0x66889AB0;
        t.border = 0x447D8FB3;

        t.divider = 0xFFECECEB;
        t.dividerSoft = 0xFFE7EAE7;

        t.shadow = 0x33264278;
        t.shadowSoft = 0x22264278;
        t.outline = 0x33000000;
        t.dimmer = 0x66000000;

        return t;
    }
}
