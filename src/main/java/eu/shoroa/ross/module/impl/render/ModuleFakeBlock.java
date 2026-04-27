package eu.shoroa.ross.module.impl.render;

import eu.shoroa.ross.module.Bind;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.settings.BooleanSetting;
import eu.shoroa.ross.settings.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.scoreboard.Team;

import static eu.shoroa.ross.Client.mc;

public class ModuleFakeBlock extends Module {
    private final NumberSetting range = register(new NumberSetting("Range", "fakeblock.range", 6f, 1f, 20f, 0.5f));
    private final BooleanSetting onlyPlayers = register(new BooleanSetting("Only Players", "fakeblock.only_players", false));
    private final BooleanSetting onlyEnemyTeam = register(new BooleanSetting("Enemy Team Only", "fakeblock.only_enemy_team", true));

    public ModuleFakeBlock() {
        super("FakeBlock", "Client-side autoblock", Category.RENDER, null);
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

