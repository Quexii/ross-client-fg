package eu.shoroa.ross.module.impl.render;

import eu.shoroa.ross.event.*;
import eu.shoroa.ross.mixins.injection.client.MinecraftAccessor;
import eu.shoroa.ross.mixins.injection.client.renderer.entity.RenderManagerAccessor;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.gl.Shader;
import eu.shoroa.ross.render.gl.uniform.Uniform;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.settings.BooleanSetting;
import eu.shoroa.ross.settings.ModeEnum;
import eu.shoroa.ross.settings.ModeSetting;
import eu.shoroa.ross.settings.NumberSetting;
import eu.shoroa.ross.types.Rect;
import eu.shoroa.ross.util.entity.TeamHelper;
import eu.shoroa.ross.util.proj.EntityProjection;
import eu.shoroa.ross.util.render.Renderer3D;
import io.github.humbleui.skija.Paint;
import net.minecraft.block.BlockBed;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.IOException;

import static eu.shoroa.ross.Client.mc;

public class ModuleESP extends Module {
    private final ModeSetting<Mode> mode = register(new ModeSetting("Mode", "mode", Mode.MODE_2D));
    private final BooleanSetting self = register(new BooleanSetting("Self", "self", true));

    // box settings
    private final NumberSetting boxOutlineThickness = register(new NumberSetting("Box Outline", "box_outline_thickness", 1f, 0f, 5f, 0.1f));

    // shader settings
    private final NumberSetting shaderOutlineThickness = register(new NumberSetting("Shader Outline", "shader_outline_thickness", 1f, 0f, 15f, 0.1f));
    private final NumberSetting shaderFillAlpha = register(new NumberSetting("Fill Alpha", "shader_fill_alpha", 0.2f, 0f, 1f, 0.1f));
    private final NumberSetting shaderOutlineAlpha = register(new NumberSetting("Outline Alpha", "shader_outline_alpha", 1f, 0f, 1f, 0.1f));

    private Framebuffer colorBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private static boolean shaderPass;

    private final Shader outlineShader = new Shader("shaders/vertex.vert", "shaders/outline.frag");
    private boolean shaderInit = false;

    public static boolean isShaderPass() {
        return shaderPass;
    }

    public ModuleESP() {
        super("ESP", "Highlights players through walls.", Category.RENDER, null);
    }

    @Subscribe
    public void oe$PostRenderEntities(EventRenderEntities.Post event) {
        if (mode.get() != Mode.MODE_SHADER) return;

        if (colorBuffer.framebufferWidth != mc.displayWidth || colorBuffer.framebufferHeight != mc.displayHeight) {
            colorBuffer.deleteFramebuffer();
            colorBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

            colorBuffer.setFramebufferFilter(GL11.GL_LINEAR);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorBuffer.framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        }
        
        shaderPass = true;

        RenderManager rm = mc.getRenderManager();

        double renderX = event.renderViewEntity.prevPosX + (event.renderViewEntity.posX - event.renderViewEntity.prevPosX) * (double) event.partialTicks;
        double renderY = event.renderViewEntity.prevPosY + (event.renderViewEntity.posY - event.renderViewEntity.prevPosY) * (double) event.partialTicks;
        double renderZ = event.renderViewEntity.prevPosZ + (event.renderViewEntity.posZ - event.renderViewEntity.prevPosZ) * (double) event.partialTicks;

        colorBuffer.framebufferClear();
        colorBuffer.bindFramebuffer(true);

        GlStateManager.pushMatrix();
        GlStateManager.depthFunc(519);
        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);

        boolean prev = rm.isRenderShadow();
        rm.setRenderShadow(false);

        GlStateManager.enableTexture2D();

        RenderManagerAccessor rma = (RenderManagerAccessor) rm;

        for (Entity entity : event.entities) {
            boolean flag2 = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();
            boolean flag3 = entity.isInRangeToRender3d(renderX, renderY, renderZ) && (entity.ignoreFrustumCheck || event.camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox()) || entity.riddenByEntity == mc.thePlayer);

            if ((entity != mc.getRenderViewEntity() || mc.gameSettings.thirdPersonView != 0 || flag2) && flag3) {
                if (!shouldRender(entity)) continue;
                boolean prevInvis = entity.isInvisible();
                entity.setInvisible(false);

                GlStateManager.disableTexture2D();
                GlStateManager.disableLighting();

                int color = TeamHelper.getTeamColor((EntityPlayer) entity);
                float r = (color >> 16 & 255) / 255.0F;
                float g = (color >> 8 & 255) / 255.0F;
                float b = (color & 255) / 255.0F;
                if (entity.ticksExisted == 0) {
                    entity.lastTickPosX = entity.posX;
                    entity.lastTickPosY = entity.posY;
                    entity.lastTickPosZ = entity.posZ;
                }

                double ex = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) event.partialTicks;
                double ey = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) event.partialTicks;
                double ez = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) event.partialTicks;
                float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * event.partialTicks;

                GlStateManager.color(r, g, b, 1.0F);
                Render<Entity> render = rm.getEntityRenderObject(entity);
                boolean renderNametag = entity.getAlwaysRenderNameTagForRender();
                entity.setAlwaysRenderNameTag(false);
                render.doRender(entity, ex - rma.getRenderPosX(), ey - rma.getRenderPosY(), ez - rma.getRenderPosZ(), yaw, event.partialTicks);
                entity.setAlwaysRenderNameTag(renderNametag);
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();

                entity.setInvisible(prevInvis);
            }
        }

        rm.setRenderShadow(prev);

        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableFog();
        GlStateManager.depthFunc(515);
        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        mc.getFramebuffer().bindFramebuffer(false);

        shaderPass = false;
    }

    @Subscribe
    public void oe$EventHUD(EventHUD.BottomSkia event) {
        EntityPlayer[] entities = mc.theWorld.getLoadedEntityList().stream().filter(entity -> entity instanceof EntityPlayer).toArray(EntityPlayer[]::new);

        if (mode.get() == Mode.MODE_2D) {
            for (EntityPlayer entity : entities) {
                if (entity == mc.thePlayer && !self.get()) continue;
                if (mc.gameSettings.thirdPersonView == 0 && entity == mc.thePlayer && self.get()) continue;
                if (ModuleManager.antiBot.isEnabled() && ModuleManager.antiBot.isBot(entity)) continue;
                int teamColor = TeamHelper.getTeamColor(entity);
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
                        Renderer.drawRect(rect.x, rect.y, rect.width, rect.height, p);
                        p.setStrokeWidth(1f);
                        p.setColor(teamColor);
                        Renderer.drawRect(rect.x, rect.y, rect.width, rect.height, p);

                        float healthValue = entity.getHealth() / entity.getMaxHealth();
                        float healthHeight = (rect.height + 2f) * healthValue;

                        p.setStroke(false);
                        p.setColor(0xFF000000);
                        Renderer.drawRect(rect.x + rect.width + 3f, rect.y - 2f, 4f, rect.height + 4f, p);

                        p.setColor(0xFFFF0000);
                        Renderer.drawRect(rect.x + rect.width + 4f, rect.y - 1f, 2f, rect.height + 2f, p);

                        p.setColor(0xFF00FF00);
                        Renderer.drawRect(rect.x + rect.width + 4f, rect.y - 1f + (rect.height + 2f) - healthHeight, 2f, healthHeight, p);

                        VariableFont.DerivedFont font = Fonts.GoogleFlex.weight(400).opticSize(14);

                        p.setStroke(true);
                        p.setStrokeWidth(2f);
                        p.setColor(0xFF000000);
                        Renderer.drawText(entity.getName(), rect.x + rect.width / 2f, rect.y - 10f, font, 14, VariableFont.Align.CENTER, p);

                        p.setStroke(false);
                        p.setColor(-1);
                        Renderer.drawText(entity.getName(), rect.x + rect.width / 2f, rect.y - 10f, font, 14, VariableFont.Align.CENTER, p);
                    }
                }
            }
        }
    }

    @Subscribe
    public void oe$PreHUD(EventHUD.PreHud.Vanilla event) {
        if (mode.get() == Mode.MODE_SHADER) {
            if (!shaderInit) {
                try {
                    outlineShader.init();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                shaderInit = true;
            }

            ScaledResolution sr = new ScaledResolution(mc);

            GlStateManager.disableDepth();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            outlineShader.attach();
            colorBuffer.bindFramebufferTexture();
            outlineShader.uniform(Uniform.makeInt("uTex", 0));
            outlineShader.uniform(Uniform.makeVec2("uTexelSize", 1f / mc.displayWidth, 1f / mc.displayHeight));
            outlineShader.uniform(Uniform.makeFloat("uThickness", shaderOutlineThickness.get()));
            outlineShader.uniform(Uniform.makeFloat("uFillAlpha", shaderFillAlpha.get()));
            outlineShader.uniform(Uniform.makeFloat("uOutlineAlpha", shaderOutlineAlpha.get()));
            outlineShader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());
            outlineShader.detach();
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
            GlStateManager.color(1f, 1f, 1f, 1f);

            colorBuffer.bindFramebuffer(false);
            colorBuffer.setFramebufferColor(0, 0, 0, 0);
            colorBuffer.framebufferClear();
            mc.getFramebuffer().bindFramebuffer(true);
        }
    }

    @Subscribe
    public void oe$Event3D(EventRender3D event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (mode.get() == Mode.MODE_SHADER) {
        } else if (mode.get() == Mode.MODE_BOX) {
            EntityPlayer[] entities = mc.theWorld.getLoadedEntityList().stream().filter(entity -> entity instanceof EntityPlayer).toArray(EntityPlayer[]::new);
            float partialTicks = event.partialTicks;
            double renderX = ((RenderManagerAccessor) mc.getRenderManager()).getRenderPosX();
            double renderY = ((RenderManagerAccessor) mc.getRenderManager()).getRenderPosY();
            double renderZ = ((RenderManagerAccessor) mc.getRenderManager()).getRenderPosZ();

            Renderer3D.begin3D(boxOutlineThickness.get());
            try {
                for (EntityPlayer entity : entities) {
                    if (entity == mc.thePlayer && !self.get()) continue;
                    if (mc.gameSettings.thirdPersonView == 0 && entity == mc.thePlayer && self.get()) continue;
                    if (ModuleManager.antiBot.isEnabled() && ModuleManager.antiBot.isBot(entity)) continue;

                    int teamColor = TeamHelper.getTeamColor(entity);
                    double interpX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
                    double interpY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
                    double interpZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
                    float interpBodyYaw = interpolateAngle(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);

                    double halfWidth = entity.width / 2.0D;
                    double height = entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY;
                    AxisAlignedBB localBB = new AxisAlignedBB(-halfWidth, 0.0D, -halfWidth, halfWidth, height, halfWidth);

                    GlStateManager.pushMatrix();
                    try {
                        GlStateManager.translate(interpX - renderX, interpY - renderY, interpZ - renderZ);
                        GlStateManager.rotate(-interpBodyYaw, 0.0F, 1.0F, 0.0F);
                        Renderer3D.drawBoxWireframe(localBB, teamColor);
                        Renderer3D.drawBoxFilled(localBB, teamColor & 0x7FFFFFFF);
                    } finally {
                        GlStateManager.popMatrix();
                    }
                }
            } finally {
                Renderer3D.end3D();
            }
        }
    }

    private boolean shouldRender(Entity entity) {
        if (mode.get() != Mode.MODE_SHADER) return false;
        if (!(entity instanceof EntityPlayer)) return false;
        if (mc.gameSettings.thirdPersonView != 0 && entity == mc.thePlayer && !self.get()) return false;
        if (ModuleManager.antiBot.isEnabled() && ModuleManager.antiBot.isBot((EntityPlayer) entity)) return false;

        return true;
    }

    private static float interpolateAngle(float prev, float current, float partialTicks) {
        return prev + MathHelper.wrapAngleTo180_float(current - prev) * partialTicks;
    }

    private enum Mode implements ModeEnum {
        MODE_SHADER("Shader"),
        MODE_2D("2D"),
        MODE_BOX("Box");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String displayName() {
            return name;
        }
    }
}
