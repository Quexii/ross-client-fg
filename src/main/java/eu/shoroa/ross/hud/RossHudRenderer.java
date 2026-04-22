package eu.shoroa.ross.hud;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static eu.shoroa.ross.Client.mc;
import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

public class RossHudRenderer {
    @SubscribeEvent
    public void onHUD(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            Filter.kawase().capture(mc.getFramebuffer().framebufferTexture, 4f, true, 4);

            EVENT_BUS.post(new EventHUD.BottomVanilla(event.partialTicks));
            if (Client.INSTANCE.skia != null && Client.INSTANCE.skia.getCanvas() != null) {
                Client.INSTANCE.skia.beginFrame();
                Renderer.use(Client.INSTANCE.skia);
                EVENT_BUS.post(new EventHUD.BottomSkia(event.partialTicks));
                Client.INSTANCE.skia.endFrame();
            }

            EVENT_BUS.post(new EventHUD.TopVanilla(event.partialTicks));
            if (Client.INSTANCE.skia != null && Client.INSTANCE.skia.getCanvas() != null) {
                Client.INSTANCE.skia.beginFrame();
                Renderer.use(Client.INSTANCE.skia);
                EVENT_BUS.post(new EventHUD.TopSkia(event.partialTicks));
                Client.INSTANCE.skia.endFrame();
            }
        }
    }
}

