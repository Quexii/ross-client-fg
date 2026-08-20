package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.Bind;
import eu.shoroa.ross.feature.module.HUDAnchor;
import eu.shoroa.ross.feature.module.HUDElement;
import eu.shoroa.ross.feature.module.HUDModule;
import eu.shoroa.ross.feature.setting.ModeEnum;
import eu.shoroa.ross.feature.setting.ModeSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.animate.Easing;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.DampFloat;
import eu.shoroa.ross.type.Size;
import eu.shoroa.ross.utils.math.Mth;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import static eu.shoroa.ross.Client.mc;

public class ModuleTargetHUD extends HUDModule {
    private final SettingCategory settings = addCategory("Settings", "settings", "settings");
    private final ModeSetting<Style> style = register(new ModeSetting<>("Style", "style", Style.ROSS), settings);

    private static final ResourceLocation STEVE_SKIN = new ResourceLocation("textures/entity/steve.png");

    private final Animate animate = new Animate(180, Easing.EXPO_IN_OUT);
    private EntityPlayer currentTarget;
    private DampFloat healthAnim = new DampFloat();
    private float currentHealth;
    private Image headImage;
    private Image steveHeadImage;
    private int cachedHeadTextureId = -1;
    private String cachedHeadUuid;

    public ModuleTargetHUD() {
        super("Target HUD", "Displays information about the target entity", "\uef7c");
        addElement(new Element());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        clearHeadImage();
        clearSteveHeadImage();
    }

    private class Element extends HUDElement {
        protected Element() {
            super("main");
            setPlacement(HUDAnchor.CENTER, 0, 64);
        }

        @Override
        public void render(Hud.Layer layer) {
            if (mc.thePlayer == null || mc.theWorld == null) return;
            if (!layer.is(Hud.Layer.NAME_SKIA_BOTTOM)) return;

            if (mc.pointedEntity != null && mc.pointedEntity instanceof EntityPlayer && !mc.pointedEntity.isDead && ((EntityPlayer) mc.pointedEntity).deathTime == 0) {
                currentTarget = (EntityPlayer) mc.pointedEntity;
            }

            boolean easeIf = (currentTarget != null && currentTarget == mc.pointedEntity && !currentTarget.isDead && (currentTarget).deathTime == 0);

            if (currentTarget == null) return;

            animate.setDuration(300);
            animate.setEase(Easing.BACK_IN_OUT);
            animate.doEase(easeIf);

            Mth.smoothDamp(healthAnim, currentHealth, 0.1f, (float) Animate.getDelta());

            if (currentTarget != null) {
                EntityPlayer livingTarget = currentTarget;
                currentHealth = livingTarget.getHealth() / livingTarget.getMaxHealth();
                currentHealth = Math.max(0f, Math.min(1f, currentHealth));
            }

            float scale = 0.8f + 0.2f * (float) animate.getValue();

            if (style.get() == Style.ROSS) {
                UI.saveLayerAlpha(getBounds().x - 50, getBounds().y - 50, getBounds().width + 100, getBounds().height + 100, (float) animate.getValue());
                UI.scaleFrom(getBounds().x + getBounds().width / 2f, getBounds().y + getBounds().height / 2f, scale, scale);
                styleRoss();
                UI.restore();
            }
        }

        @Override
        public void dummy(Hud.Layer layer) {
            if (mc.thePlayer == null || mc.theWorld == null) return;
            if (!layer.is(Hud.Layer.NAME_SKIA_BOTTOM)) return;

            currentTarget = mc.thePlayer;
            animate.setDuration(300);
            animate.setEase(Easing.BACK_IN_OUT);
            animate.doEase(true);

            Mth.smoothDamp(healthAnim, currentHealth, 0.1f, (float) Animate.getDelta());

            if (currentTarget != null) {
                EntityPlayer livingTarget = currentTarget;
                currentHealth = livingTarget.getHealth() / livingTarget.getMaxHealth();
                currentHealth = Math.max(0f, Math.min(1f, currentHealth));
            }

            float scale = 0.8f + 0.2f * (float) animate.getValue();

            if (style.get() == Style.ROSS) {
                UI.saveLayerAlpha(getBounds().x - 50, getBounds().y - 50, getBounds().width + 100, getBounds().height + 100, (float) animate.getValue());
                UI.scaleFrom(getBounds().x + getBounds().width / 2f, getBounds().y + getBounds().height / 2f, scale, scale);
                styleRoss();
                UI.restore();
            }
        }

        @Override
        public Size getSize() {
            return new Size(200, 60);
        }

        private void styleRoss() {
            StellaTheme t = StellaTheme.get();

            Canvas canvas = Client.INSTANCE.getSkia().getCanvas();

            final float healthBarW = getBounds().width - (getBounds().height + 20);
            final float healthBarH = 10f;

            try (Paint p = new Paint()) {
                StellaHud.card(getBounds().x, getBounds().y, getBounds().width, getBounds().height, p);

                p.setColor(t.foreground);
                UI.drawText(currentTarget.getName(), getBounds().x + 6 + getBounds().height, getBounds().y + 6 + (getBounds().height - 8) / 2f, Fonts.GoogleFlex.weight(650).roundness(25), 16f, Align.BOTTOM_LEFT, p);

                p.setColor(t.outline);
                UI.drawRRect(getBounds().x + 6 + getBounds().height, getBounds().y + 6 + (getBounds().height - 8) / 2f + 4f, healthBarW, healthBarH, StellaHud.RADIUS - 4, p);

                p.setColor(t.accentDeep);
                UI.drawRRect(getBounds().x + 6 + getBounds().height, getBounds().y + 6 + (getBounds().height - 8) / 2f + 4f, healthBarW * healthAnim.value, healthBarH, StellaHud.RADIUS - 4, p);

                p.setColor(t.accent);
                UI.drawRRect(getBounds().x + 6 + getBounds().height, getBounds().y + 6 + (getBounds().height - 8) / 2f + 4f, healthBarW * currentHealth, healthBarH, StellaHud.RADIUS - 4, p);
            }

            UI.save();
            UI.clipRRect(getBounds().x + 6, getBounds().y + 6, getBounds().height - 12, getBounds().height - 12, StellaHud.RADIUS - 4);
            drawPlayerHead(canvas, getBounds().x + 6, getBounds().y + 6, getBounds().height - 12, getBounds().height - 12);
            UI.restore();

            try (Paint p = new Paint()) {
                p.setColor(t.outline);
                p.setStroke(true);
                p.setStrokeWidth(2f);
                UI.drawRRect(getBounds().x + 6, getBounds().y + 6, getBounds().height - 12, getBounds().height - 12, StellaHud.RADIUS - 4, p);
            }
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
