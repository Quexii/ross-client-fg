package eu.shoroa.ross.mixins.injection.client.gui;

import eu.shoroa.ross.gui.mainmenu.ScreenMainMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu {
    @Inject(method = "initGui", at = @At("HEAD"))
    public void initGui(CallbackInfo info) {
        Minecraft.getMinecraft().displayGuiScreen(new ScreenMainMenu());
    }
}
