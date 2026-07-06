package eu.shoroa.ross.fml;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.render.ui.UI;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(
        modid = RossMod.MOD_ID,
        name = RossMod.MOD_NAME,
        version = RossMod.VERSION
)
public class RossMod {
    public static final String MOD_ID = "rossclient";
    public static final String MOD_NAME = "Ross";
    public static final String VERSION = "1.0.0";

    @Mod.EventHandler
    public void onFmlInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onHUD(RenderGameOverlayEvent.Post event) {
        if (Client.INSTANCE.getSkia().isStale()) return;

        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            Client.INSTANCE.getSkia().beginFrame();
            UI.use(Client.INSTANCE.getSkia());
            Client.EVENT_BUS.post(new Hud.Layer(Hud.Layer.NAME_SKIA_BOTTOM));
            Client.INSTANCE.getSkia().endFrame();

            Client.EVENT_BUS.post(new Hud.Layer(Hud.Layer.NAME_VANILLA_BOTTOM));

            Client.INSTANCE.getSkia().beginFrame();
            UI.use(Client.INSTANCE.getSkia());
            Client.EVENT_BUS.post(new Hud.Layer(Hud.Layer.NAME_SKIA_TOP));
            Client.INSTANCE.getSkia().endFrame();

            Client.EVENT_BUS.post(new Hud.Layer(Hud.Layer.NAME_VANILLA_TOP));
        }
    }
}
