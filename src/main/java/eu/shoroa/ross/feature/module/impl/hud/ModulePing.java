package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.feature.module.HUDAnchor;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import net.minecraft.client.network.NetworkPlayerInfo;

import static eu.shoroa.ross.Client.mc;

public class ModulePing extends TextHUDModule {
    public ModulePing() {
        super("Ping", "Shows your latency to the server", MaterialIcons.SIGNAL_CELLULAR_ALT, MaterialIcons.SIGNAL_CELLULAR_ALT);
        setDefaultPosition(HUDAnchor.LEFT_TOP, 10, 116);
    }

    @Override
    protected String value() {
        return Integer.toString(getPing());
    }

    @Override
    protected String suffix() {
        return "ms";
    }

    private int getPing() {
        if (isInEditor()) return 32;
        if (mc.thePlayer == null || mc.getNetHandler() == null) return 0;

        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        return info == null ? 0 : Math.max(0, info.getResponseTime());
    }
}
