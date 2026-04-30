package eu.shoroa.ross.integration.hypixel;

import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.integration.hypixel.event.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import static eu.shoroa.ross.Client.mc;

public class BedwarsSystem {
    private static final BedwarsSystem INSTANCE = new BedwarsSystem();

    public static BedwarsSystem getInstance() {
        return INSTANCE;
    }

    private boolean startBlock;
    private boolean inGame;

    @SubscribeEvent
    public void oe$Tick(EventTick event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
    }

    @SubscribeEvent
    public void oe$Chat(ClientChatReceivedEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (event.type != 0) return;

        String message = event.message.getUnformattedText();

        if (startBlock) {
            if (message.trim().equalsIgnoreCase("bed wars")) {
                MinecraftForge.EVENT_BUS.post(new EventStartBedwars());
                inGame = true;
            }
        }

        if (message.equalsIgnoreCase("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) {
            startBlock = !startBlock;
        }
    }

    @SubscribeEvent
    public void oe$WorldChanged(WorldEvent.Load event) {
        if (inGame) {
            MinecraftForge.EVENT_BUS.post(new EventLeaveBedwars());
            inGame = false;
        }
    }

    private BedwarsSystem() {}
}
