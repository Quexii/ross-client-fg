package eu.shoroa.ross.util;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import static eu.shoroa.ross.Client.mc;

public class ChatUtil {

    private static final EnumChatFormatting ACCENT  = EnumChatFormatting.LIGHT_PURPLE;
    private static final EnumChatFormatting PRIMARY = EnumChatFormatting.WHITE;
    private static final EnumChatFormatting MUTED   = EnumChatFormatting.GRAY;
    private static final EnumChatFormatting SUCCESS  = EnumChatFormatting.GREEN;
    private static final EnumChatFormatting ERROR    = EnumChatFormatting.RED;
    private static final EnumChatFormatting BOLD     = EnumChatFormatting.BOLD;
    private static final EnumChatFormatting RESET    = EnumChatFormatting.RESET;

    private static final String PREFIX =
            ACCENT + "" + BOLD + "ross " +
            EnumChatFormatting.DARK_GRAY + "" + BOLD + "\u00BB " +
            RESET;

    private static void send(String message) {
        mc.thePlayer.addChatMessage(new ChatComponentText(PREFIX + message));
    }

    public static void info(String message) {
        send(MUTED + message);
    }

    public static void info(String label, String value) {
        send(MUTED + label + " " + EnumChatFormatting.DARK_GRAY + "\u2192 " + PRIMARY + value);
    }

    public static void success(String message) {
        send(SUCCESS + message);
    }

    public static void error(String message) {
        send(ERROR + message);
    }

    public static void header(String title) {
        send(ACCENT + "" + BOLD + title);
    }

    public static void entry(String text) {
        mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.DARK_GRAY + "  \u00B7 " + MUTED + text
        ));
    }

    public static void entry(String key, String value) {
        mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.DARK_GRAY + "  \u00B7 " +
                PRIMARY + key +
                EnumChatFormatting.DARK_GRAY + " \u2014 " +
                MUTED + value
        ));
    }

    public static void toggled(String moduleName, boolean enabled) {
        send(MUTED + "Toggled " + PRIMARY + moduleName +
             EnumChatFormatting.DARK_GRAY + " \u2014 " +
             (enabled ? SUCCESS + "enabled" : ERROR + "disabled"));
    }

    public static void settingSet(String moduleName, String settingName, String value) {
        send(MUTED + "Set " +
             PRIMARY + moduleName +
             EnumChatFormatting.DARK_GRAY + " \u203A " +
             MUTED + settingName +
             EnumChatFormatting.DARK_GRAY + " \u2192 " +
             PRIMARY + value);
    }

    public static void invalidUsage(String[] usageLines) {
        send(ERROR + "Invalid usage:");
        for (String line : usageLines) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.DARK_GRAY + "  \u00B7 " + MUTED + line
            ));
        }
    }
}