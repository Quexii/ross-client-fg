package eu.shoroa.ross.commands.impl;

import eu.shoroa.ross.commands.Command;
import eu.shoroa.ross.commands.CommandManager;
import net.minecraft.network.play.client.C01PacketChatMessage;

import static eu.shoroa.ross.Client.mc;

public class CommandSay extends Command {
    public CommandSay() {
        super(
                "Say",
                "Sends a message to chat",
                new String[]{".say <message>"},
                new String[]{"say"}
        );
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            invalidUsage();
            return;
        }

        String message = String.join(" ", args);
        mc.thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage(message));
    }
}