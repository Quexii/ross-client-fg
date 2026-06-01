package eu.shoroa.ross.module.impl.hud;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.settings.ModeEnum;
import eu.shoroa.ross.settings.ModeSetting;
import eu.shoroa.ross.types.Rect;
import eu.shoroa.ross.ui.api.*;
import eu.shoroa.ross.util.MathHelper;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.yoga.Yoga;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import static eu.shoroa.ross.Client.mc;

public class ModuleTargetHUD extends Module {
    private static final ResourceLocation STEVE_SKIN = new ResourceLocation("textures/entity/steve.png");

    private final ModeSetting<Style> style = register(new ModeSetting<>("Style", "style", Style.ROSS));

    private Node rootNode = new Node();
    private Node panelNode = new Node();
    private Node headNode = new Node();
    private Node statsNode = new Node();
    private Node labelNode = new Node();
    private Node healthNode = new Node();

    private final Animate animate = new Animate(180, Easing.EXPO_IN_OUT);
    private EntityPlayer currentTarget;
    private float healthAnim;
    private float currentHealth;
    private Image headImage;
    private Image steveHeadImage;
    private int cachedHeadTextureId = -1;
    private String cachedHeadUuid;

    public ModuleTargetHUD() {
        super("TargetHUD", "Displays information about your current target.", Category.HUD);

        rootNode.width(Display.getWidth());
        rootNode.height(Display.getHeight());
        rootNode.justify(Justify.CENTER);
        rootNode.alignItems(Align.CENTER);

        float marginBottom = 18f;
        marginBottom = 8f; // will be removed after finishing

        float headSize = 60 - 16;

        panelNode.width(200);
        panelNode.height(60);
        panelNode.direction(Direction.ROW);
        panelNode.gap(Gutter.COLUMN, 8f);
        panelNode.padding(8f);
        Yoga.YGNodeStyleSetMarginPercent(panelNode.yogaNode, Edge.TOP.value, marginBottom);

        headNode.width(headSize);
        headNode.height(headSize);

        statsNode.direction(Direction.COLUMN);
        statsNode.justify(Justify.CENTER);
        statsNode.gap(Gutter.ROW, 10f);
        Yoga.YGNodeStyleSetFlexGrow(statsNode.yogaNode, 1f);

        labelNode.height(12f);
        healthNode.height(10f);
        statsNode.children(labelNode, healthNode);

        panelNode.children(headNode, statsNode);
        rootNode.children(panelNode);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        clearHeadImage();
        clearSteveHeadImage();
    }

    @Subscribe
    public void oe$OnHUD(EventHUD.BottomSkia event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (mc.pointedEntity != null && mc.pointedEntity instanceof EntityPlayer && !mc.pointedEntity.isDead && ((EntityPlayer) mc.pointedEntity).deathTime == 0) {
            currentTarget = (EntityPlayer) mc.pointedEntity;
        }

        boolean easeIf = (mc.currentScreen instanceof GuiChat) || (currentTarget != null && currentTarget == mc.pointedEntity && !currentTarget.isDead && (currentTarget).deathTime == 0);

        if (mc.currentScreen instanceof GuiChat) currentTarget = mc.thePlayer;

        if (currentTarget == null) return;

        animate.setDuration(300);
        animate.setEase(Easing.BACK_IN_OUT);
        animate.doEase(easeIf);

        int fps = Math.max(Minecraft.getDebugFPS(), 1);
        float lerpDelta = (1f / fps) * 8f;
        healthAnim = MathHelper.lerp(healthAnim, currentHealth, lerpDelta);
        healthAnim = Math.max(0f, Math.min(1f, healthAnim));

        if (currentTarget != null) {
            EntityPlayer livingTarget = currentTarget;
            currentHealth = livingTarget.getHealth() / livingTarget.getMaxHealth();
            currentHealth = Math.max(0f, Math.min(1f, currentHealth));
        }

        if (style.get() == Style.ROSS) {
            styleRoss();
        }
    }

    private void styleRoss() {
        float w = Display.getWidth();
        float h = Display.getHeight();

        float scale = (float) (0.6f + animate.getValue() * 0.4f);
        float healthHue = currentHealth * 0.27f;
        int healthColor = Color.HSBtoRGB(healthHue, 0.75f, 1f);
        int healthColorDarker = Color.HSBtoRGB(healthHue, 0.75f, 0.5f);

        Canvas canvas = Client.INSTANCE.skia.getCanvas();
        if (canvas == null) return;

        int alpha = (int) (Math.max(0f, Math.min(1f, animate.getValue())) * 255f);

        VariableFont.DerivedFont font = Fonts.GoogleFlex
                .weight(400)
                .roundness(100)
                .opticSize(24);

        if (rootNode.getWidth() != Display.getWidth() || rootNode.getHeight() != Display.getHeight()) {
            rootNode.width(Display.getWidth());
            rootNode.height(Display.getHeight());
            rootNode.markDirty();
        }

        if (rootNode.consumeDirty()) {
            rootNode.calcLayout(Yoga.YGUndefined, Yoga.YGUndefined, LayoutDirection.LTR);
            rootNode.resolveAbsolutePositions(0, 0);
        }

        canvas.saveLayerAlpha(io.github.humbleui.types.Rect.makeWH(w, h), alpha);
        canvas.translate(panelNode.getX() + panelNode.getWidth() / 2f, panelNode.getY() + panelNode.getHeight() / 2f);
        canvas.scale(scale, scale);
        canvas.translate(-(panelNode.getX() + panelNode.getWidth() / 2f), -(panelNode.getY() + panelNode.getHeight() / 2f));
        try (Paint p = new Paint()) {
            p.setColor(0xFF202020);
            Renderer.drawRRect(panelNode.getX(), panelNode.getY(), panelNode.getWidth(), panelNode.getHeight(), 10f, p);
        }

        Renderer.save();
        Renderer.clipRRect(headNode.getX(), headNode.getY(), headNode.getWidth(), headNode.getHeight(), 5f);
        drawPlayerHead(canvas, headNode.getX(), headNode.getY(), headNode.getWidth(), headNode.getHeight());
        Renderer.restore();

        try (Paint p = new Paint()) {
            p.setColor(0xFF303030);
            p.setStrokeWidth(2f);
            p.setStroke(true);
            Renderer.drawRRect(headNode.getX(), headNode.getY(), headNode.getWidth(), headNode.getHeight(), 5f, p);
        }

        try (Paint p = new Paint()) {
            p.setColor(-1);
            Renderer.drawText(currentTarget.getName(), labelNode.getX(), labelNode.getY() + labelNode.getHeight(), font, 15f, Font.Align.BASELINE_LEFT, p);
        }

        try (Paint p = new Paint()) {
            p.setColor(healthColorDarker);
            Renderer.drawRRect(healthNode.getX(), healthNode.getY(), healthNode.getWidth(), healthNode.getHeight(), 5f, p);

            p.setColor(healthColor);
            Renderer.drawRRect(healthNode.getX(), healthNode.getY(), healthNode.getWidth() * healthAnim, healthNode.getHeight(), 5f, p);
        }
        canvas.restore();
    }

    private boolean drawPlayerHead(Canvas canvas, float x, float y, float width, float height) {
        if (!(currentTarget instanceof AbstractClientPlayer)) {
            clearHeadImage();
            return drawSteveHead(canvas, x, y, width, height);
        }

        AbstractClientPlayer clientPlayer = (AbstractClientPlayer) currentTarget;
        ResourceLocation skin = clientPlayer.getLocationSkin();
        if (skin == null) {
            clearHeadImage();
            return drawSteveHead(canvas, x, y, width, height);
        }

        ITextureObject textureObject = mc.getTextureManager().getTexture(skin);
        if (textureObject == null) {
            mc.getTextureManager().bindTexture(skin);
            textureObject = mc.getTextureManager().getTexture(skin);
        }

        if (textureObject == null) {
            clearHeadImage();
            return drawSteveHead(canvas, x, y, width, height);
        }

        int textureId = textureObject.getGlTextureId();
        if (textureId <= 0) {
            clearHeadImage();
            return drawSteveHead(canvas, x, y, width, height);
        }

        String uuid = currentTarget.getUniqueID().toString();
        if (headImage == null || cachedHeadTextureId != textureId || !uuid.equals(cachedHeadUuid)) {
            clearHeadImage();
            BufferedImage bufferedSkin = extractBufferedImage(textureObject);
            if (bufferedSkin == null) {
                return drawSteveHead(canvas, x, y, width, height);
            }

            Image newImage = decodeSkinImage(bufferedSkin);
            if (newImage == null) {
                return drawSteveHead(canvas, x, y, width, height);
            }

            headImage = newImage;
            cachedHeadTextureId = textureId;
            cachedHeadUuid = uuid;
        }

        drawHeadImage(canvas, headImage, x, y, width, height);
        return true;
    }

    private boolean drawSteveHead(Canvas canvas, float x, float y, float width, float height) {
        if (steveHeadImage == null) {
            steveHeadImage = loadSkinFromResource(STEVE_SKIN);
        }

        if (steveHeadImage == null) {
            return false;
        }

        drawHeadImage(canvas, steveHeadImage, x, y, width, height);
        return true;
    }

    private void drawHeadImage(Canvas canvas, Image image, float x, float y, float width, float height) {
        float atlasScale = Math.max(1f, image.getWidth() / 64f);
        io.github.humbleui.types.Rect srcFace = io.github.humbleui.types.Rect.makeXYWH(8f * atlasScale, 8f * atlasScale, 8f * atlasScale, 8f * atlasScale);
        io.github.humbleui.types.Rect srcHat = io.github.humbleui.types.Rect.makeXYWH(40f * atlasScale, 8f * atlasScale, 8f * atlasScale, 8f * atlasScale);
        io.github.humbleui.types.Rect dst = io.github.humbleui.types.Rect.makeXYWH(x, y, width, height);

        canvas.drawImageRect(image, srcFace, dst);
        canvas.drawImageRect(image, srcHat, dst);
    }

    private Image loadSkinFromResource(ResourceLocation location) {
        try (InputStream stream = mc.getResourceManager().getResource(location).getInputStream()) {
            BufferedImage skin = ImageIO.read(stream);
            if (skin == null) {
                return null;
            }
            return decodeSkinImage(skin);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private BufferedImage extractBufferedImage(ITextureObject textureObject) {
        Class<?> clazz = textureObject.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (BufferedImage.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(textureObject);
                        if (value instanceof BufferedImage) {
                            return (BufferedImage) value;
                        }
                    } catch (ReflectiveOperationException ignored) {
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private Image decodeSkinImage(BufferedImage skin) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(skin, "png", output)) {
                return null;
            }
            return Image.makeDeferredFromEncodedBytes(output.toByteArray());
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private void clearHeadImage() {
        if (headImage != null) {
            headImage.close();
            headImage = null;
        }
        cachedHeadTextureId = -1;
        cachedHeadUuid = null;
    }

    private void clearSteveHeadImage() {
        if (steveHeadImage != null) {
            steveHeadImage.close();
            steveHeadImage = null;
        }
    }

    public enum Style implements ModeEnum {
        ROSS("Ross");

        private final String displayName;

        Style(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String displayName() {
            return displayName;
        }
    }
}
