package eu.shoroa.ross.module.impl.misc;

import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.integration.hypixel.event.EventLeaveBedwars;
import eu.shoroa.ross.integration.hypixel.event.EventStartBedwars;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.notification.Notifications;

public class ModuleBedwars extends Module {
    public ModuleBedwars() {
        super("Bedwars Core", "Adds various features for Hypixel Bedwars.", Category.MISC);
    }

    @Subscribe
    public void oe$GameStarted(EventStartBedwars event) {
        Notifications.add("Bedwars Core", "Started Bedwars game");
    }

    @Subscribe
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
