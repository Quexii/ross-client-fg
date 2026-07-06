package eu.shoroa.ross;

import eu.shoroa.ross.config.Config;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.event.api.EventBus;
import eu.shoroa.ross.event.LifeCycle;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.ModuleManager;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.Skia;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.Images;
import eu.shoroa.ross.utils.proj.EntityProjection;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

public class Client {
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static final EventBus EVENT_BUS = new EventBus();
    public static final Client INSTANCE = new Client();
    private static final Logger logger = LogManager.getLogger();

    private Skia skia;
    private Config config;

    private Client() {
        EVENT_BUS.register(this);
        EVENT_BUS.register(EntityProjection.getInstance());
    }

    @Subscribe
    @ApiStatus.Internal
    public void onStart(LifeCycle.Start e) {
        config = new Config();

        skia = new Skia(mc.getFramebuffer());
        skia.init();
        Fonts.load();
        Images.load();
        Filter.kawase().init();

        ModuleManager.init();
    }

    @Subscribe
    @ApiStatus.Internal
    public void onResize(LifeCycle.Resize e) {
        skia.resize();
        Filter.kawase().resize();
    }

    @Subscribe
    @ApiStatus.Internal
    public void onStop(LifeCycle.Stop e) {
        skia.destroy();
    }

    @Subscribe
    @ApiStatus.Internal
    public void onInput(EventInput e) {
        if (mc.currentScreen == null) {
            ModuleManager.onInput(e);
        }
    }

    public Skia getSkia() {
        return skia;
    }

    public Config getConfig() {
        return config;
    }
}
