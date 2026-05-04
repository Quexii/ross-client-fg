package eu.shoroa.ross.commands;

import eu.shoroa.ross.util.ChatUtil;
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

    protected void info(String msg) {
        ChatUtil.info(msg);
    }

    protected void info(String label, String value) {
        ChatUtil.info(label, value);
    }

    protected void success(String msg) {
        ChatUtil.success(msg);
    }

    protected void error(String msg) {
        ChatUtil.error(msg);
    }

    protected void header(String title) {
        ChatUtil.header(title);
    }

    protected void entry(String text) {
        ChatUtil.entry(text);
    }

    protected void entry(String key, String value) {
        ChatUtil.entry(key, value);
    }

    protected void invalidUsage() {
        ChatUtil.invalidUsage(usage);
    }
}