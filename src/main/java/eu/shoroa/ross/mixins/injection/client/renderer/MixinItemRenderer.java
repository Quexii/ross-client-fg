package eu.shoroa.ross.mixins.injection.client.renderer;

import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.module.impl.render.ModuleAnimations;
import eu.shoroa.ross.module.impl.render.ModuleFakeBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = ItemRenderer.class, priority = 100)
public abstract class MixinItemRenderer {
    @Shadow
    private float prevEquippedProgress;

    @Shadow
    private float equippedProgress;

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    protected abstract void rotateArroundXAndY(float angle, float angleY);

    @Shadow
    protected abstract void setLightMapFromPlayer(AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void rotateWithPlayerRotations(EntityPlayerSP entityplayerspIn, float partialTicks);

    @Shadow
    private ItemStack itemToRender;

    @Shadow
    protected abstract void renderItemMap(AbstractClientPlayer clientPlayer, float pitch, float equipmentProgress, float swingProgress);

    @Shadow
    protected abstract void transformFirstPersonItem(float equipProgress, float swingProgress);

    @Shadow
    protected abstract void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks);

    @Shadow
    protected abstract void doBlockTransformations();

    @Shadow
    protected abstract void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void doItemUsedTransformations(float swingProgress);

    @Shadow
    public abstract void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform);

    @Shadow
    protected abstract void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress);

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    public void renderItemInFirstPerson(float partialTicks, CallbackInfo ci) {
        if (ModuleManager.animations.isEnabled()) {
            ci.cancel();
            doAnimations(partialTicks);
        }
    }

    private void doAnimations(float partialTicks) {
        ModuleAnimations blockMod = ModuleManager.animations;
        ModuleFakeBlock fakeBlock = ModuleManager.fakeBlock;
        float equipProgress = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
        EntityPlayerSP player = this.mc.thePlayer;
        float swingProgress = player.getSwingProgress(partialTicks);
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
        this.rotateArroundXAndY(pitch, yaw);
        this.setLightMapFromPlayer(player);
        this.rotateWithPlayerRotations(player, partialTicks);
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();

        float blockHitSwingProgress = blockMod.isEnabled() ? swingProgress : 0.0F;

        if (this.itemToRender != null) {
            if (this.itemToRender.getItem() instanceof ItemMap) {
                this.renderItemMap(player, pitch, equipProgress, swingProgress);
            } else if ((player.getItemInUseCount() > 0) || (fakeBlock.shouldBlock() && player.getItemInUseCount() == 0)) {
                EnumAction enumaction = this.itemToRender.getItemUseAction();

                switch (enumaction) {
                    case NONE:
                        this.transformFirstPersonItem(equipProgress, blockHitSwingProgress);
                        break;

                    case EAT:
                    case DRINK:
                        this.performDrinking(player, partialTicks);
                        this.transformFirstPersonItem(equipProgress, blockHitSwingProgress);
                        break;

                    case BLOCK:
                        if (blockMod.isEnabled()) {
                            float f8 = MathHelper.sin(MathHelper.sqrt_float(blockHitSwingProgress) * 3.0F);
                            float var9 = MathHelper.sin(MathHelper.sqrt_float(blockHitSwingProgress) * (float) Math.PI);
                            switch (blockMod.animation.get()) {
                                case OLD:
                                    this.transformFirstPersonItem(equipProgress, blockHitSwingProgress);
                                    this.doBlockTransformations();
                                    break;
                                case CHILL:
                                    GlStateManager.translate(0.055F, 0.12F, 0.0f);
                                    this.transformFirstPersonItem(equipProgress, blockHitSwingProgress);
                                    this.doBlockTransformations();
                                    break;
                                case EXHI:
                                    GlStateManager.translate(0.56, -0.52, -0.71999997F);
                                    GlStateManager.translate(0, 0.07, 0);
                                    float funny1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
                                    GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
                                    GlStateManager.translate(0.0F - funny1 / 100.0F, 0.0F + funny1 / 15.0F, 0.0F + funny1 / 15.0F);
                                    GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                                    float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
                                    GlStateManager.rotate(f1 * -40.0F, 1.0F, 0.4F, 0.9F);
                                    GlStateManager.rotate(f1 * -20.0F, 1.0F, 0.0F, 0.0F);
                                    GlStateManager.scale(0.4f, 0.4f, 0.4f);
                                    this.doBlockTransformations();
                                    break;
                                case EXHI_TAP:
                                    GL11.glTranslated(-0.06D, 0.17D, -0.0D);
                                    this.transformFirstPersonItem(equipProgress / 2.5F, 0.0F);
                                    GlStateManager.rotate(-f8 * 40.0F / 2.0F, f8 / 2.0F, 1.0F, 4.0F);
                                    GlStateManager.rotate(-f8 * 30.0F, 1.0F, f8 / 3.0F, -0.0F);
                                    this.doBlockTransformations();
                                    break;
                                case SLIDE:
                                    GlStateManager.translate(-0.02, 0.05F, 0);
                                    this.transformFirstPersonItem(equipProgress, 0.0F);
                                    this.doBlockTransformations();
                                    GlStateManager.translate(-0.05F, 0.2F, 0.2F);
                                    GlStateManager.rotate(-var9 * 70.0F / 2.0F, -8.0F, -0.0F, 9.0F);
                                    GlStateManager.rotate(-var9 * 70.0F, 1.0F, -0.4F, -0.0F);
                                    break;
                                case ASTOLFO:
                                    GlStateManager.translate(0.05f, 0f, -0.35f);
                                    this.transformFirstPersonItem(equipProgress, blockHitSwingProgress);
                                    this.doBlockTransformations();
                                    break;
                                case SWING:
                                    this.transformFirstPersonItem(equipProgress, blockHitSwingProgress);
                                    this.doBlockTransformations();
                                    GlStateManager.translate(-0.3F, 0.2F, 0.2F);
                                    break;
                                case SIGMA: {
                                    this.transformFirstPersonItem(equipProgress * 0.5f, blockHitSwingProgress);
                                    this.doBlockTransformations();
                                    GL11.glTranslated(1.2, 0.3, 0.5);
                                    GL11.glTranslatef(-1, this.mc.thePlayer.isSneaking() ? -0.1F : -0.2F, 0.2F);
                                    break;
                                }
                                case SHRED: {
                                    this.transformFirstPersonItem(equipProgress / 2, blockHitSwingProgress);
                                    this.doBlockTransformations();
                                    GL11.glTranslatef(-0.05F, this.mc.thePlayer.isSneaking() ? -0.2F : 0.0F, 0.1F);
                                    break;
                                }
                                case BUTTER: {
                                    this.transformFirstPersonItem(equipProgress, blockHitSwingProgress);
                                    GlStateManager.translate(0, 0.3, 0);
                                    this.doBlockTransformations();
                                    break;
                                }
                                case STELLA: {
                                    this.transformFirstPersonItem(-0.1F, blockHitSwingProgress);
                                    GlStateManager.translate(-0.5F, 0.4F, -0.2F);
                                    GlStateManager.rotate(32, 0, 1, 0);
                                    GlStateManager.rotate(-70, 1, 0, 0);
                                    GlStateManager.rotate(40, 0, 1, 0);
                                    this.doBlockTransformations();
                                    break;
                                }
                                case FATHUM: {
                                    GlStateManager.popMatrix();
                                    GL11.glRotated(25, 0, 0.2, 0);
                                    this.transformFirstPersonItem(0.0f, blockHitSwingProgress);
                                    GlStateManager.scale(0.9F, 0.9F, 0.9F);
                                    this.doBlockTransformations();
                                    GlStateManager.pushMatrix();
                                    break;
                                }
                                case OH_THE_MISERY:
                                    GlStateManager.translate(0, 0.125f, 0);
                                    this.transformFirstPersonItem(equipProgress, blockHitSwingProgress);
                                    float var15 = MathHelper.sin((float) ((double) MathHelper.sqrt_float(blockHitSwingProgress) * 3.141592653589793D));
                                    GlStateManager.rotate(var15 * 30.0F / 2.0F, -var15, -0.0F, 9.0F);
                                    GlStateManager.rotate(var15 * 40.0F, 1.0F, -var15 / 2.0F, -0.0F);
                                    this.doBlockTransformations();
                                    break;
                            }
                        } else {
                            this.transformFirstPersonItem(equipProgress, blockHitSwingProgress);
                            this.doBlockTransformations();
                        }
                        break;

                    case BOW:
                        this.transformFirstPersonItem(equipProgress, blockHitSwingProgress);
                        this.doBowTransformations(partialTicks, player);
                }
            } else {
                if (blockMod.isEnabled()) {
                    switch (blockMod.smoothing.getCurrent()) {
                        case NORMAL:
                            this.doItemUsedTransformations(swingProgress);
                            this.transformFirstPersonItem(equipProgress, swingProgress);
                            break;
                        case SMOOTH:
                            this.doItemUsedTransformations(0);
                            this.transformFirstPersonItem(equipProgress, swingProgress);
                            break;
                    }
                } else {
                    this.doItemUsedTransformations(swingProgress);
                    this.transformFirstPersonItem(equipProgress, swingProgress);
                }
            }

            // old rod
            if (blockMod.isEnabled() && Objects.equals(itemToRender, new ItemStack(Items.fishing_rod))) {
                if (itemToRender.getItem().shouldRotateAroundWhenRendering()) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.translate(0.58800083f, 0.06999986f, -0.77000016f);
                GlStateManager.scale(1.5f, 1.5f, 1.5f);
                GlStateManager.rotate(50.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(335.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translate(-0.9375F, -0.0625F, 0.0F);
                GlStateManager.scale(-2, 2, -2);
                GlStateManager.scale(0.5f, 0.5f, 0.5f);

                renderItem(player, itemToRender, ItemCameraTransforms.TransformType.NONE);
            } else {
                renderItem(player, itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
            }
        } else if (!player.isInvisible()) {
            this.renderPlayerArm(player, equipProgress, swingProgress);
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }
}
