package eu.shoroa.ross.integration.hypixel;

import eu.shoroa.ross.event.EventChatReceived;
import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.EventWorld;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.integration.hypixel.event.EventLeaveBedwars;
import eu.shoroa.ross.integration.hypixel.event.EventStartBedwars;

import static eu.shoroa.ross.Client.EVENT_BUS;
import static eu.shoroa.ross.Client.mc;

public class BedwarsSystem {
    private static final BedwarsSystem INSTANCE = new BedwarsSystem();

    public static BedwarsSystem getInstance() {
        return INSTANCE;
    }

    private boolean startBlock;
    private boolean inGame;

    @Subscribe
    public void oe$Tick(EventTick event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
    }

    @Subscribe
    public void oe$Chat(EventChatReceived event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (event.type != 0) return;

        String message = event.message.getUnformattedText();

        if (startBlock) {
            if (message.trim().equalsIgnoreCase("bed wars")) {
                EVENT_BUS.post(new EventStartBedwars());
                inGame = true;
            }
        }

        if (message.equalsIgnoreCase("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) {
            startBlock = !startBlock;
        }
    }

    @Subscribe
    public void oe$WorldChanged(EventWorld.LoadChunk event) {
        if (inGame) {
            EVENT_BUS.post(new EventLeaveBedwars());
            inGame = false;
        }
    }

    private BedwarsSystem() {}
}
