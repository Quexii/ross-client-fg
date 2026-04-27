package eu.shoroa.ross.module.impl.render;

import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.mixins.injection.client.MinecraftAccessor;
import eu.shoroa.ross.mixins.injection.client.renderer.entity.RenderManagerAccessor;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
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
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static eu.shoroa.ross.Client.mc;

public class ModuleESP extends Module {
    private final ModeSetting<Mode> mode = register(new ModeSetting("Mode", Mode.MODE_2D));
    private final BooleanSetting self = register(new BooleanSetting("Self", true));

    // box settings
    private final NumberSetting boxOutlineThickness = register(new NumberSetting("Box Outline", 1f, 0f, 5f, 0.1f));

    // shader settings
    private final NumberSetting shaderOutlineThickness = register(new NumberSetting("Shader Outline", 1f, 0f, 15f, 0.1f));
    private final NumberSetting shaderFillAlpha = register(new NumberSetting("Fill Alpha", 0.2f, 0f, 1f, 0.1f));
    private final NumberSetting shaderOutlineAlpha = register(new NumberSetting("Outline Alpha", 1f, 0f, 1f, 0.1f));

    private Framebuffer colorBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private boolean renderingColorPass;
    private boolean shaderBufferPrepared;
    private static boolean shaderPass;

    private final Shader outlineShader = new Shader("shaders/vertex.vert", "shaders/outline.frag");
    private boolean shaderInit = false;

    public static boolean isShaderPass() {
        return shaderPass;
    }

    public ModuleESP() {
        super("ESP", "TODO", Category.RENDER, null);
    }

    @SubscribeEvent
    public void oe$RenderEntity(RenderLivingEvent.Pre event) {
        if (mode.get() != Mode.MODE_SHADER) return;
        if (!(event.entity instanceof EntityPlayer)) return;
        if (event.renderer == null) return;
        if (renderingColorPass) return;

        renderingColorPass = true;
        shaderPass = true;
        try {
            colorBuffer.bindFramebuffer(false);

            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();

            int color = TeamHelper.getTeamColor((EntityPlayer) event.entity);
            float r = (color >> 16 & 255) / 255.0F;
            float g = (color >> 8 & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            GlStateManager.color(r, g, b, 1.0F);

            event.renderer.doRender(event.entity, event.x, event.y, event.z, event.entity.rotationYaw, ((MinecraftAccessor) mc).getTimer().renderPartialTicks);
        } finally {
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            mc.getFramebuffer().bindFramebuffer(true);
            shaderPass = false;
            renderingColorPass = false;
        }
    }

    @SubscribeEvent
    public void oe$EventHUD(EventHUD.BottomSkia event) {
        EntityPlayer[] entities = mc.theWorld.getLoadedEntityList().stream().filter(entity -> entity instanceof EntityPlayer).toArray(EntityPlayer[]::new);

        if (mode.get() == Mode.MODE_2D) {
            for (EntityPlayer entity : entities) {
                if (entity == mc.thePlayer && !self.get()) continue;
                if (mc.gameSettings.thirdPersonView == 0 && entity == mc.thePlayer && self.get()) continue;
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

    @SubscribeEvent
    public void oe$PreHUD(RenderGameOverlayEvent.Pre event) {
        if (mode.get() == Mode.MODE_SHADER && event.type == RenderGameOverlayEvent.ElementType.ALL) {
            if (!shaderInit) {
                try {
                    outlineShader.init();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                shaderInit = true;
            }
            if (colorBuffer.framebufferWidth != mc.displayWidth || colorBuffer.framebufferHeight != mc.displayHeight) {
                colorBuffer.deleteFramebuffer();
                colorBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
                shaderBufferPrepared = false;
            }

            ScaledResolution sr = new ScaledResolution(mc);
            mc.entityRenderer.setupOverlayRendering();

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

            GlStateManager.translate(0.0F, 0.0F, 2000.0F);

            if (!shaderBufferPrepared) {
                colorBuffer.bindFramebuffer(false);
                colorBuffer.setFramebufferColor(0, 0, 0, 0);
                colorBuffer.framebufferClear();
                mc.getFramebuffer().bindFramebuffer(true);
                shaderBufferPrepared = true;
            }
        }
    }

    @SubscribeEvent
    public void oe$Event3D(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (mode.get() == Mode.MODE_SHADER) {
            shaderBufferPrepared = false;
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
