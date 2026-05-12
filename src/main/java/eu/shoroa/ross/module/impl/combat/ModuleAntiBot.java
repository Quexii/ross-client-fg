package eu.shoroa.ross.module.impl.combat;

import eu.shoroa.ross.event.EventWorld;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.*;
import java.util.regex.Pattern;

public class ModuleAntiBot extends Module {
    private static final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    private static final Pattern NPC_NAME_PATTERN = Pattern.compile(
            "(?i)(\\bNPC\\b|\\bBOT\\b|Farmer|Vendor|Shopkeeper|Guard|Citizen|Sentry|Merchant|Auctioneer)"
    );

    private final Map<UUID, Long> firstSeen = new HashMap<>();
    private final Set<UUID>       botCache  = new HashSet<>();
    private final Set<UUID>       realCache = new HashSet<>();

    private static final long PING_GRACE_MS = 3000;

    public ModuleAntiBot() {
        super("AntiBot", "Filters NPCs/bots from nametags and targeting", Category.COMBAT);
    }

    public boolean isBot(EntityPlayer player) {
        if (player == null) return false;

        UUID uuid = player.getUniqueID();

        if (botCache.contains(uuid))  return true;
        if (realCache.contains(uuid)) return false;

        firstSeen.putIfAbsent(uuid, System.currentTimeMillis());

        boolean bot = runChecks(player);
        (bot ? botCache : realCache).add(uuid);
        return bot;
    }

    private boolean runChecks(EntityPlayer player) {
        EntityPlayerSP local = Minecraft.getMinecraft().thePlayer;
        if (player == local) return false;

        String name = player.getName();
        UUID   uuid = player.getUniqueID();

        if (!VALID_NAME.matcher(name).matches()) return true;
        if (uuid.version() == 3) return true;
        if (NPC_NAME_PATTERN.matcher(name).find()) return true;

        int signals = 0;
        long seenTime  = firstSeen.getOrDefault(uuid, System.currentTimeMillis());
        boolean pastGrace = (System.currentTimeMillis() - seenTime) > PING_GRACE_MS;

        int ping = getPing(player);
        if (pastGrace && ping == 0) signals++;
        if (pastGrace && getPlayerInfo(player) == null) signals++;

        if (hasNPCTeam(player)) signals++;
        return signals >= 2;
    }

    private NetworkPlayerInfo getPlayerInfo(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getNetHandler() == null) return null;
        return mc.getNetHandler().getPlayerInfo(player.getUniqueID());
    }

    private int getPing(EntityPlayer player) {
        NetworkPlayerInfo info = getPlayerInfo(player);
        return (info != null) ? info.getResponseTime() : -1;
    }

    private boolean hasNPCTeam(EntityPlayer player) {
        Scoreboard sb = player.worldObj.getScoreboard();
        ScorePlayerTeam team = sb.getPlayersTeam(player.getName());
        if (team == null) return false;
        String teamName = team.getRegisteredName().toLowerCase();
        return teamName.contains("npc") || teamName.contains("bot");
    }

    @Subscribe
    public void oe$WorldLoad(EventWorld.Load event) {
        botCache.clear();
        realCache.clear();
        firstSeen.clear();
    }
}