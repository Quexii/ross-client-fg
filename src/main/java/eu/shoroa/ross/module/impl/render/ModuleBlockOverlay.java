package eu.shoroa.ross.module.impl.render;

import eu.shoroa.ross.event.EventRenderBlockSelection;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.settings.BooleanSetting;
import eu.shoroa.ross.settings.ColorSetting;
import eu.shoroa.ross.settings.NumberSetting;
import eu.shoroa.ross.util.render.Renderer3D;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class ModuleBlockOverlay extends Module {
    private final ColorSetting outlineColor = register(new ColorSetting("Outline Color", "outline_color", new Color(-1)));
    private final ColorSetting fillColor = register(new ColorSetting("Fill Color", "fill_color", new Color(255,255,255,50)));
    private final BooleanSetting renderOutline = register(new BooleanSetting("Render Outline", "draw_outline", true));
    private final BooleanSetting renderFill = register(new BooleanSetting("Render Fill", "draw_fill", true));
    private final BooleanSetting depth = register(new BooleanSetting("Depth", "depth", true));
    private final NumberSetting lineWidth = register(new NumberSetting("Line Width", "line_width", 2, 0.1f, 5, 0.1f));

    public ModuleBlockOverlay() {
        super("Block Overlay", "Custom block selection.", Category.RENDER, null);
    }

    @Subscribe
    public void oe$OnRender3D(EventRenderBlockSelection event) {
        event.setCanceled(true);

        Renderer3D.begin3D(lineWidth.get());

        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableCull();

        if (depth.get()) {
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
        }

        if (renderFill.get()) {
            Renderer3D.drawBoxFilled(event.getBoundingBox(), fillColor.getRGB());
        }

        if (renderOutline.get()) {
            Renderer3D.drawBoxWireframe(event.getBoundingBox(), outlineColor.getRGB());
        }
        Renderer3D.end3D();
    }
}
