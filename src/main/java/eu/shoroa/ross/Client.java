package eu.shoroa.ross;

import eu.shoroa.ross.config.ConfigManager;
import eu.shoroa.ross.event.EventBus;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.event.EventResize;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.integration.hypixel.BedwarsSystem;
import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.notification.Notifications;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.SkiaSource;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.image.Images;
import eu.shoroa.ross.render.skia.shader.Shaders;
import eu.shoroa.ross.util.proj.EntityProjection;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class Client {
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static final EventBus EVENT_BUS = new EventBus();

    public static final Client INSTANCE = new Client();

    public SkiaSource skia;
    public ConfigManager config;

    private Client() {
        EVENT_BUS.register(this);
        EVENT_BUS.register(BedwarsSystem.getInstance());
        EVENT_BUS.register(Notifications.getInstance());
        EVENT_BUS.register(EntityProjection.getInstance());
    }

    public void init() {
        System.out.println("Initializing Ross Client...");

        skia = new SkiaSource(mc.getFramebuffer());
        skia.init();
        Fonts.load();
        Images.load();
        Shaders.init();
        Filter.kawase().init();

        ModuleManager.init();

        config = new ConfigManager();
        try {
            config.init();
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void oe$EventKey(EventInput event) {
        if (Keyboard.getEventKey() != 0) {
            ModuleManager.onInput(event);
        }
    }

    @Subscribe
    public void oe$EventMouse(EventInput event) {
        if (Mouse.getEventButton() != -1) {
            ModuleManager.onInput(event);
        }
    }

    @Subscribe
    public void oe$Resize(EventResize event) {
        if (Client.INSTANCE.skia != null) {
            Client.INSTANCE.skia.resize();
        }
        Filter.kawase().resize();
    }
}
