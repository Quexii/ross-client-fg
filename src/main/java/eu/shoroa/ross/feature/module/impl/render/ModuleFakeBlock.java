package eu.shoroa.ross.feature.module.impl.render;

import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.scoreboard.Team;

import static eu.shoroa.ross.Client.mc;

public class ModuleFakeBlock extends Module {
    private final SettingCategory categorySettings = addCategory("Settings", ".", "settings");
    private final NumberSetting range = register(new NumberSetting("Range", "range", 6f, 1f, 20f, 0.5f), categorySettings);
    private final BooleanSetting onlyPlayers = register(new BooleanSetting("Only Players", "only_players", false), categorySettings);
    private final BooleanSetting onlyEnemyTeam = register(new BooleanSetting("Enemy Team Only", "only_enemy_team", true), categorySettings);

    public ModuleFakeBlock() {
        super("Fake Block", "Client-side autoblock.", Category.RENDER, "\uf686");
    }

    public boolean shouldBlock() {
        if (!isEnabled()) return false;
        if (mc.thePlayer == null || mc.theWorld == null) return false;
        if (mc.thePlayer.inventory.getCurrentItem() == null) return false;
        if (!(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword)) return false;
        return hasNearbyThreat();
    }

    private boolean hasNearbyThreat() {
        double rangeSq = range.get() * range.get();
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer) continue;
            if (!(entity instanceof EntityLivingBase)) continue;
            if (((EntityLivingBase) entity).getHealth() <= 0) continue;
            if (!isThreat(entity)) continue;
            if (mc.thePlayer.getDistanceSqToEntity(entity) <= rangeSq) return true;
        }
        return false;
    }

    private boolean isThreat(Entity entity) {
        if (entity instanceof EntityPlayer) return isEnemy(entity);
        return !onlyPlayers.get() && entity instanceof IMob;
    }

    private boolean isEnemy(Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return true;
        if (!onlyEnemyTeam.get()) return true;

        Team myTeam = mc.thePlayer.getTeam();
        Team theirTeam = ((EntityLivingBase) entity).getTeam();

        if (myTeam == null) return true;
        return myTeam != theirTeam;
    }
}
