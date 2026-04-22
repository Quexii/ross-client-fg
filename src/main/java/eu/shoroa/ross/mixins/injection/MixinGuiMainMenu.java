package eu.shoroa.ross.mixins.injection;

import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu {
    @Inject(method = "initGui", at = @At("HEAD"))
    public void initGui(CallbackInfo info) {
        System.out.println("Ross: Injected into GuiMainMenu.initGui");
    }
}
