package eu.shoroa.ross.fml;

import eu.shoroa.ross.hud.RossHudRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import static eu.shoroa.ross.Client.mc;

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
    public void onInitialize(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RossHudRenderer());
    }
}
