package eu.shoroa.ross.module.impl.misc;

import eu.shoroa.ross.integration.hypixel.event.*;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.notification.Notification;
import eu.shoroa.ross.notification.Notifications;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModuleBedwars extends Module {
    public ModuleBedwars() {
        super("Bedwars Core", "Adds various features for Hypixel Bedwars", Category.MISC);
    }

    @SubscribeEvent
    public void oe$GameStarted(EventStartBedwars event) {
        Notifications.add("Bedwars Core", "Started Bedwars game");
    }

    @SubscribeEvent
    public void oe$GameLeft(EventLeaveBedwars event) {
        Notifications.add("Bedwars Core", "Left Bedwars game");
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
