package eu.shoroa.ross.feature.module.impl.render;

import eu.shoroa.ross.event.EventRenderBlockSelection;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.ColorSetting;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.opengl.Renderer3D;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;

public class ModuleBlockOverlay extends Module {
    private final SettingCategory categorySettings = addCategory("Settings", "settings", "settings");
    private final SettingCategory categoryColors = addCategory("Colors", "colors", "colors");
    private final SettingCategory categoryMisc = addCategory("Misc", "misc", "misc");
    private final ColorSetting outlineColor = register(new ColorSetting("Outline Color", "outline_color", new Color(-1)), categoryColors);
    private final ColorSetting fillColor = register(new ColorSetting("Fill Color", "fill_color", new Color(255,255,255,50)), categoryColors);

    private final BooleanSetting renderOutline = register(new BooleanSetting("Render Outline", "draw_outline", true), categorySettings);
    private final BooleanSetting renderFill = register(new BooleanSetting("Render Fill", "draw_fill", true), categorySettings);

    private final BooleanSetting depth = register(new BooleanSetting("Depth", "depth", true), categoryMisc);
    private final NumberSetting lineWidth = register(new NumberSetting("Line Width", "line_width", 2, 0.1f, 5, 0.1f), categorySettings);

    public ModuleBlockOverlay() {
        super("Block Overlay", "Custom block selection", Category.RENDER, "\ue9fe");
    }

    @Subscribe
    @ApiStatus.Internal
    public void onBlockSelection(EventRenderBlockSelection event) {
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