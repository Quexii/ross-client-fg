package eu.shoroa.ross.mixins.injection.entity.item;

import eu.shoroa.ross.event.EventEntityItemPickup;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;

@Mixin(EntityItem.class)
public class MixinEntityItem {

    @Inject(
        method = "onCollideWithPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;addItemStackToInventory(Lnet/minecraft/item/ItemStack;)Z"
        ),
        cancellable = true
    )
    private void onItemPickup(EntityPlayer player, CallbackInfo ci) {
        EntityItem self = (EntityItem)(Object)this;
        ItemStack stack = self.getEntityItem();

        EVENT_BUS.post(new EventEntityItemPickup(player, stack));
    }
}