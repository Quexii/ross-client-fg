package eu.shoroa.ross.feature.module.impl.render;

import eu.shoroa.ross.event.EventRenderLiving;
import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.event.api.EventPreOverlay;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.ColorSetting;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.mixins.interfaces.IMinecraft;
import eu.shoroa.ross.render.opengl.Shader;
import eu.shoroa.ross.render.opengl.uniform.Uniform;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.Rect;
import eu.shoroa.ross.utils.player.TeamHelper;
import eu.shoroa.ross.utils.proj.EntityProjection;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import static eu.shoroa.ross.Client.mc;

public class ModuleESP extends Module {
    private static final Log log = LogFactory.getLog(ModuleESP.class);
    private final SettingCategory categorySettings = addCategory("Settings", ".", "settings");
    private final BooleanSetting mode2d = register(new BooleanSetting("2D Mode", "mode_2d", false), categorySettings);
    private final BooleanSetting modeShader = register(new BooleanSetting("Shader Mode", "mode_shader", false), categorySettings);
    private final BooleanSetting showSelf = register(new BooleanSetting("Show Self", "show_self", false), categorySettings);

    private final SettingCategory categoryTargets = addCategory("Targets", ".", "targets");
    private final BooleanSetting targetPlayers = register(new BooleanSetting("Target Players", "target_players", true), categoryTargets);
    private final BooleanSetting targetMobs = register(new BooleanSetting("Target Mobs", "target_mobs", false), categoryTargets);
    private final BooleanSetting targetAnimals = register(new BooleanSetting("Target Animals", "target_animals", false), categoryTargets);

    private final SettingCategory categoryColors = addCategory("Colors", ".", "colors");
    private final BooleanSetting useTeamColors = register(new BooleanSetting("Use team colors", "team_colors", true), categoryColors);
    private final ColorSetting playerColor = register(new ColorSetting("Player Color", "player_color", new Color(0x00E1FF)), categoryColors);
    private final ColorSetting mobColor = register(new ColorSetting("Mob Color", "mob_color", new Color(0xFF003B)), categoryColors);
    private final ColorSetting animalColor = register(new ColorSetting("Animal Color", "animal_color", new Color(0xFF1AFF00, true)), categoryColors);

    private final SettingCategory categoryShader = addCategory("Shader Settings", ".", "shader");

    private final NumberSetting shaderOutlineThickness = register(new NumberSetting("Shader Outline", "shader_outline_thickness", 1f, 0f, 10f, 0.1f), categoryShader);
    private final BooleanSetting shaderDoGlow = register(new BooleanSetting("Outline Glow", "outline_glow", false), categoryShader);
    private final BooleanSetting shaderClipGlow = (BooleanSetting) register(new BooleanSetting("Glow Clip", "outline_glow_clip", true), categoryShader).visibleWhen(shaderDoGlow::get);
    private final NumberSetting shaderBlurRadius = register(new NumberSetting("Shader Blur Radius", "shader_blur_radius", 1f, 0f, 10f, 1f), categoryShader);
    private final NumberSetting shaderGlowFalloffStart = register(new NumberSetting("Glow Falloff Start", "shader_glow_falloff_start", 0.05f, 0f, 1f, 0.01f), categoryShader);
    private final NumberSetting shaderGlowFalloffEnd = register(new NumberSetting("Glow Falloff End", "shader_glow_falloff_end", 0.4f, 0f, 1f, 0.01f), categoryShader);

    private Framebuffer colorBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private Framebuffer horizontalOutlinePass = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private Framebuffer verticalOutlinePass = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private Framebuffer fullOutlinePass = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private Framebuffer glowOutlineVerticalPass = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private Framebuffer glowOutlineHorizontalPass = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private Framebuffer glowOutlineMixedPass = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private boolean renderingColorPass;
    private static boolean shaderPass;

    private final Shader outlineDirShader = new Shader("shaders/vertex.vert", "shaders/outline_dir.frag");
    private final Shader outlineSeparateShader = new Shader("shaders/vertex.vert", "shaders/outline_separate.frag");
    private final Shader outlineGlowShader = new Shader("shaders/vertex.vert", "shaders/outline_glow.frag");
    private final Shader blitShader = new Shader("shaders/vertex.vert", "shaders/blit.frag");
    private boolean shaderInit = false;

    public ModuleESP() {
        super("ESP", "Enables ESP rendering", Category.RENDER, "\uf31d");
    }

    public static boolean isShaderPass() {
        return shaderPass;
    }

    @Subscribe
    @ApiStatus.Internal
    public void onPreRenderLiving(EventRenderLiving.Pre event) {
        if (!modeShader.get()) return;
        if (renderingColorPass) return;
        if (shouldRender(event.entity)) return;

        renderingColorPass = true;
        shaderPass = true;
        try {
            colorBuffer.bindFramebuffer(false);

            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();

            int color = getEntityColor(event.entity);
            float r = (color >> 16 & 255) / 255.0F;
            float g = (color >> 8 & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            GlStateManager.color(r, g, b, 1.0F);

            mc.getRenderManager().setRenderOutlines(true);
            event.renderer.doRender(event.entity, event.x, event.y, event.z, event.entity.rotationYaw, ((IMinecraft) mc).getTimer().renderPartialTicks);
            mc.getRenderManager().setRenderOutlines(false);

            GlStateManager.resetColor();
        } finally {
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            mc.getFramebuffer().bindFramebuffer(true);
            shaderPass = false;
            renderingColorPass = false;
        }
    }

    private Framebuffer initFramebuffer(Framebuffer fbo) {
        if (fbo.framebufferWidth != mc.displayWidth || fbo.framebufferHeight != mc.displayHeight) {
            fbo.deleteFramebuffer();
            fbo = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        }
        return fbo;
    }

    private void doOutlineDir(Framebuffer src, Framebuffer fbo, ScaledResolution sr, boolean dir) {
        fbo.bindFramebuffer(false);
        GlStateManager.clearColor(0f, 0f, 0f, 0f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

        outlineDirShader.attach();
        src.bindFramebufferTexture();
        outlineDirShader.uniform(Uniform.makeInt("uTex", 0));
        outlineDirShader.uniform(Uniform.makeVec2("uTexelSize", 1f / mc.displayWidth, 1f / mc.displayHeight));
        outlineDirShader.uniform(Uniform.makeVec2("uDirection", dir ? 1f : 0f, dir ? 0f : 1f));
        outlineDirShader.uniform(Uniform.makeFloat("uThickness", shaderOutlineThickness.get()));
        outlineDirShader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());
        outlineDirShader.detach();
    }

    private void doOutlineSeparate(ScaledResolution sr, Framebuffer src, Framebuffer dst) {
        dst.bindFramebuffer(false);
        GlStateManager.clearColor(0f, 0f, 0f, 0f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

        outlineSeparateShader.attach();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        src.bindFramebufferTexture();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        colorBuffer.bindFramebufferTexture();
        outlineSeparateShader.uniform(Uniform.makeInt("uMask", 0));
        outlineSeparateShader.uniform(Uniform.makeInt("uOutlineTex", 1));
        outlineSeparateShader.uniform(Uniform.makeVec2("uTexelSize", 1f / mc.displayWidth, 1f / mc.displayHeight));
        outlineSeparateShader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());
        outlineSeparateShader.detach();
    }

    private void doOutlineGlow(Framebuffer src, Framebuffer dst, ScaledResolution sr, boolean dir, float radius, float falloffStart, float falloffEnd) {
        dst.bindFramebuffer(false);

        GlStateManager.clearColor(0f, 0f, 0f, 0f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

        outlineGlowShader.attach();
        src.bindFramebufferTexture();
        outlineGlowShader.uniform(Uniform.makeInt("uTex", 0));
        outlineGlowShader.uniform(Uniform.makeVec2("uDirection", dir ? 1f : 0f, dir ? 0f : 1f));
        outlineGlowShader.uniform(Uniform.makeVec2("uTexelSize", 1f / mc.displayWidth, 1f / mc.displayHeight));
        outlineGlowShader.uniform(Uniform.makeFloat("uRadius", radius * 2));
        outlineGlowShader.uniform(Uniform.makeFloat("uFalloffStart", falloffStart));
        outlineGlowShader.uniform(Uniform.makeFloat("uFalloffEnd", falloffEnd));
        outlineGlowShader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());

        outlineGlowShader.detach();
    }

    private void doBlit(Framebuffer src, ScaledResolution sr) {
        mc.getFramebuffer().bindFramebuffer(true);

        blitShader.attach();
        src.bindFramebufferTexture();
        blitShader.uniform(Uniform.makeInt("uTex", 0));
        blitShader.uniform(Uniform.makeVec2("uTexelSize", 1f / mc.displayWidth, 1f / mc.displayHeight));
        blitShader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());
        blitShader.detach();
    }

    @Subscribe
    @ApiStatus.Internal
    public void onPreOverlay(EventPreOverlay event) {
        ScaledResolution sr = new ScaledResolution(mc);

        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.clear(256);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0F, sr.getScaledWidth_double(), sr.getScaledHeight_double(), 0.0F, 1000.0F, 3000.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);

            if (modeShader.get()) {
                if (!shaderInit) {
                    try {
                        outlineDirShader.init();
                        outlineSeparateShader.init();
                        outlineGlowShader.init();
                        blitShader.init();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    shaderInit = true;
                }
                colorBuffer = initFramebuffer(colorBuffer);
                fullOutlinePass = initFramebuffer(fullOutlinePass);
                horizontalOutlinePass = initFramebuffer(horizontalOutlinePass);
                verticalOutlinePass = initFramebuffer(verticalOutlinePass);
                glowOutlineVerticalPass = initFramebuffer(glowOutlineVerticalPass);
                glowOutlineHorizontalPass = initFramebuffer(glowOutlineHorizontalPass);
                glowOutlineMixedPass = initFramebuffer(glowOutlineMixedPass);

                mc.entityRenderer.setupOverlayRendering();

                GlStateManager.disableDepth();
                GlStateManager.disableAlpha();

                doOutlineDir(colorBuffer, horizontalOutlinePass, sr, true);
                doOutlineDir(horizontalOutlinePass, verticalOutlinePass, sr, false);
                doOutlineSeparate(sr, verticalOutlinePass, fullOutlinePass);
                if (shaderDoGlow.get()) {
                    doOutlineGlow(fullOutlinePass, glowOutlineHorizontalPass, sr, true, shaderBlurRadius.get(), 0, 1);
                    doOutlineGlow(glowOutlineHorizontalPass, glowOutlineVerticalPass, sr, false, shaderBlurRadius.get(), shaderGlowFalloffStart.get(), shaderGlowFalloffEnd.get());

                    Framebuffer output = shaderClipGlow.get() ? glowOutlineMixedPass : glowOutlineVerticalPass;

                    if (shaderClipGlow.get())
                        doOutlineSeparate(sr, glowOutlineVerticalPass, glowOutlineMixedPass);

                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                    doBlit(output, sr);
                } else {
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                    doBlit(fullOutlinePass, sr);
                }

                GlStateManager.enableDepth();
                GlStateManager.enableAlpha();
                GlStateManager.color(1f, 1f, 1f, 1f);

                GlStateManager.translate(0.0F, 0.0F, 2000.0F);

                colorBuffer.setFramebufferColor(0, 0, 0, 0);
                colorBuffer.framebufferClear();
                mc.getFramebuffer().bindFramebuffer(true);
            }
        } finally {
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5889);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }

    @Subscribe
    @ApiStatus.Internal
    public void onHud(Hud.Layer event) {
        if (!mode2d.get()) return;

        if (event.is(Hud.Layer.NAME_SKIA_BOTTOM)) {
            List<Entity> entities = mc.theWorld.loadedEntityList;
            for (Entity entity : entities) {
                if (shouldRender(entity)) continue;

                drawESP2D(entity, getEntityColor(entity));
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

                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase e = (EntityLivingBase) entity;
                    float healthValue = e.getHealth() / e.getMaxHealth();
                    float healthHeight = (rect.height + 2f) * healthValue;

                    p.setColor(0xFF000000);
                    UI.drawRect(rect.x + rect.width + 3f, rect.y - 2f, 4f, rect.height + 4f, p);

                    p.setColor(0xFFFF0000);
                    UI.drawRect(rect.x + rect.width + 4f, rect.y - 1f, 2f, rect.height + 2f, p);

                    p.setColor(0xFF00FF00);
                    UI.drawRect(rect.x + rect.width + 4f, rect.y - 1f + (rect.height + 2f) - healthHeight, 2f, healthHeight, p);
                }

                VariableFont.DerivedFont font = Fonts.GoogleFlex.weight(400).opticSize(14);

                String text = entity.getName();

                p.setStroke(true);
                p.setStrokeWidth(2f);
                p.setColor(0xFF000000);
                UI.drawText(text, rect.x + rect.width / 2f, rect.y - 10f, font, 14, Align.CENTER, p);

                p.setStroke(false);
                p.setColor(-1);
                UI.drawText(text, rect.x + rect.width / 2f, rect.y - 10f, font, 14, Align.CENTER, p);
            }
        }
    }

    private boolean shouldRender(Entity entity) {
        if (entity instanceof EntityPlayer && targetPlayers.get()) {
            if (entity == mc.thePlayer) {
                return !showSelf.get() || mc.gameSettings.thirdPersonView == 0;
            }
            return false;
        }
        if (entity instanceof EntityMob && targetMobs.get()) return false;
        return !(entity instanceof EntityAnimal) || !targetAnimals.get();
    }

    private int getEntityColor(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return useTeamColors.get() ? TeamHelper.getTeamColor((EntityPlayer) entity) : playerColor.get().getRGB();
        } else if (entity instanceof EntityMob) {
            return mobColor.get().getRGB();
        } else if (entity instanceof EntityAnimal) {
            return animalColor.get().getRGB();
        }
        return 0xFFFFFFFF;
    }
}