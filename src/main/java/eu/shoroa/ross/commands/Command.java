package eu.shoroa.ross.commands;

import net.minecraft.util.ChatComponentText;

import static eu.shoroa.ross.Client.mc;

public abstract class Command {
    public final String name;
    public final String description;
    public final String[] usage;
    public final String[] aliases;

    public Command(String name, String description, String[] usage, String[] aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
    }

    public abstract void execute(String[] args);

    protected void clientMessage(String message) {
        mc.thePlayer.addChatMessage(new ChatComponentText(message));
    }
}
