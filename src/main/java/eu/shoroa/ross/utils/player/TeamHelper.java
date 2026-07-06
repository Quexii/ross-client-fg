package eu.shoroa.ross.utils.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.IChatComponent;

import java.util.HashMap;
import java.util.Map;

public class TeamHelper {
    private static final Map<Integer, Integer> teamColorCache = new HashMap<>();

    static {
        teamColorCache.put(0, 0xFF000000);
        teamColorCache.put(1, 0xFF0000AA);
        teamColorCache.put(2, 0xFF00AA00);
        teamColorCache.put(3, 0xFF00AAAA);
        teamColorCache.put(4, 0xFFAA0000);
        teamColorCache.put(5, 0xFFAA00AA);
        teamColorCache.put(6, 0xFFFFAA00);
        teamColorCache.put(7, 0xFFAAAAAA);
        teamColorCache.put(8, 0xFF555555);
        teamColorCache.put(9, 0xFF5555FF);
        teamColorCache.put(10, 0xFF55FF55);
        teamColorCache.put(11, 0xFF55FFFF);
        teamColorCache.put(12, 0xFFFF5555);
        teamColorCache.put(13, 0xFFFF55FF);
        teamColorCache.put(14, 0xFFFFFF55);
        teamColorCache.put(15, 0xFFFFFFFF);
    }

    public static int getTeamColor(EntityPlayer entity) {
        if (entity.getDisplayName() != null) {
            char code = getDominantColor(entity.getDisplayName());
            if (isColorCode(code)) {
                int idx = Character.digit(code, 16);
                if (idx != -1) {
                    return teamColorCache.getOrDefault(idx, 0);
                }
            }
        } else if (entity.getTeam() != null && entity.getTeam() instanceof ScorePlayerTeam) {
            ScorePlayerTeam team = (ScorePlayerTeam) entity.getTeam();
            if (team != null && team.getChatFormat() != null) {
                int idx = team.getChatFormat().getColorIndex();
                if (idx == -1) return 0;
                return teamColorCache.getOrDefault(idx, 0);
            }
        }
        return 0;
    }

    private static char getDominantColor(IChatComponent component) {
        String text = component.getFormattedText();
        Map<Character, Integer> colorCount = new HashMap<>();

        char currentColor = 'f';

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));

                if (isColorCode(code)) {
                    currentColor = code;
                } else if (code == 'r') {
                    currentColor = 'f';
                }

                i++;
                continue;
            }

            colorCount.put(currentColor, colorCount.getOrDefault(currentColor, 0) + 1);
        }

        char dominant = 'f';
        int max = 0;

        for (Map.Entry<Character, Integer> entry : colorCount.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                dominant = entry.getKey();
            }
        }

        return dominant;
    }

    private static boolean isColorCode(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}