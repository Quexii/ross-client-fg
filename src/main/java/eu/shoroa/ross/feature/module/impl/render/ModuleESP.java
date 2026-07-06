package eu.shoroa.ross.feature.module.impl.render;

import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.ColorSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.Rect;
import eu.shoroa.ross.utils.player.TeamHelper;
import eu.shoroa.ross.utils.proj.EntityProjection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;
import java.util.List;

import io.github.humbleui.skija.Paint;

import static eu.shoroa.ross.Client.mc;

public class ModuleESP extends Module {
    private final SettingCategory categorySettings = addCategory("Settings", ".", "settings");
    private final BooleanSetting mode2d = register(new BooleanSetting("2D Mode", "2d_mode", true), categorySettings);
    private final BooleanSetting showSelf = register(new BooleanSetting("Show Self", "show_self", true), categorySettings);

    private final SettingCategory categoryTargets = addCategory("Targets", ".", "targets");
    private final BooleanSetting targetPlayers = register(new BooleanSetting("Target Players", "target_players", true), categoryTargets);
    private final BooleanSetting targetMobs = register(new BooleanSetting("Target Mobs", "target_mobs", false), categoryTargets);
    private final BooleanSetting targetAnimals = register(new BooleanSetting("Target Animals", "target_animals", false), categoryTargets);

    private final SettingCategory categoryColors = addCategory("Colors", ".", "colors");
    private final BooleanSetting useTeamColors = register(new BooleanSetting("Use team colors", "team_colors", true), categoryColors);
    private final ColorSetting playerColor = register(new ColorSetting("Player Color", "player_color", new Color(0x00E1FF)), categoryColors);
    private final ColorSetting mobColor = register(new ColorSetting("Mob Color", "mob_color", new Color(0xFF003B)), categoryColors);
    private final ColorSetting animalColor = register(new ColorSetting("Animal Color", "animal_color", new Color(0xFF1AFF00, true)), categoryColors);

    public ModuleESP() {
        super("ESP", "Enables ESP rendering", Category.RENDER, "\uf31d");
    }

    @Subscribe
    @ApiStatus.Internal
    public void onHud(Hud.Layer event) {
        if (!mode2d.get()) return;

        if (event.is(Hud.Layer.NAME_SKIA_BOTTOM)) {
            List<Entity> entities = mc.theWorld.loadedEntityList;
            for (Entity entity : entities) {
                boolean isPlayer = entity instanceof EntityPlayer;
                boolean isMob = entity instanceof EntityMob;
                boolean isAnimal = entity instanceof EntityAnimal;

                if (!isPlayer && !isMob && !isAnimal) {
                    continue;
                }

                if (isPlayer && !targetPlayers.get()) continue;
                if (isMob && !targetMobs.get()) continue;
                if (isAnimal && !targetAnimals.get()) continue;

                if (entity instanceof EntityPlayer && !targetPlayers.get()) continue;
                if (entity instanceof EntityMob && !targetMobs.get()) continue;
                if (entity instanceof EntityAnimal && !targetAnimals.get()) continue;
                if (entity == mc.thePlayer) {
                    if (!showSelf.get()) continue;
                    if (mc.gameSettings.thirdPersonView == 0) continue;
                }

                int color;
                if (isPlayer) {
                    color = useTeamColors.get()
                            ? TeamHelper.getTeamColor((EntityPlayer) entity)
                            : playerColor.get().getRGB();
                } else if (isMob) {
                    color = mobColor.get().getRGB();
                } else {
                    color = animalColor.get().getRGB();
                }

                drawESP2D(entity, color);
            }
        }
    }

    private void drawESP2D(Entity entity, int color) {
        Rect rect = EntityProjection.getInstance().getScreenPosition(entity);
        if (rect != null) {
            if (rect.width < 0) {
                float newX = rect.x + rect.width;
                float newWidth = -rect.width;

                rect = new Rect(newX, rect.y, newWidth, rect.height);
            }

            if (rect.height < 0) {
                float newY = rect.y + rect.height;
                float newHeight = -rect.height;

                rect = new Rect(rect.x, newY, rect.width, newHeight);
            }

            try (Paint p = new Paint()) {
                p.setAntiAlias(false);
                p.setColor(0xFF000000);
                p.setStroke(true);
                p.setStrokeWidth(3f);
                UI.drawRect(rect.x, rect.y, rect.width, rect.height, p);
                p.setStrokeWidth(1f);
                p.setColor(color);
                UI.drawRect(rect.x, rect.y, rect.width, rect.height, p);

                p.setStroke(false);
                p.setColor(0xFF000000);
                UI.drawRect(rect.x + rect.width + 3f, rect.y - 2f, 4f, rect.height + 4f, p);

                p.setColor(0xFFFF0000);
                UI.drawRect(rect.x + rect.width + 4f, rect.y - 1f, 2f, rect.height + 2f, p);

                if (entity instanceof EntityLiving) {
                    EntityLiving e = (EntityLiving) entity;
                    float healthValue = e.getHealth() / e.getMaxHealth();
                    float healthHeight = (rect.height + 2f) * healthValue;
                    p.setColor(0xFF00FF00);
                    UI.drawRect(rect.x + rect.width + 4f, rect.y - 1f + (rect.height + 2f) - healthHeight, 2f, healthHeight, p);
                }

                VariableFont.DerivedFont font = Fonts.GoogleFlex.weight(400).opticSize(14);

                p.setStroke(true);
                p.setStrokeWidth(2f);
                p.setColor(0xFF000000);
                UI.drawText(entity.getName(), rect.x + rect.width / 2f, rect.y - 10f, font, 14, Align.CENTER, p);

                p.setStroke(false);
                p.setColor(-1);
                UI.drawText(entity.getName(), rect.x + rect.width / 2f, rect.y - 10f, font, 14, Align.CENTER, p);
            }
        }
    }
}
