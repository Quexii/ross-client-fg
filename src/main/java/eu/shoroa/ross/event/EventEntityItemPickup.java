package eu.shoroa.ross.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class EventEntityItemPickup {
    public final EntityPlayer entityPlayer;
    public final ItemStack item;

    public EventEntityItemPickup(EntityPlayer entityPlayer, ItemStack item) {
        this.entityPlayer = entityPlayer;
        this.item = item;
    }
}