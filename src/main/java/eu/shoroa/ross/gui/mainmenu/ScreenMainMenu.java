package eu.shoroa.ross.gui.mainmenu;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.gui.elements.node.NodeLabel;
import eu.shoroa.ross.gui.mainmenu.node.MenuButton;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.image.ImageSource;
import eu.shoroa.ross.render.skia.image.Images;
import eu.shoroa.ross.types.Rect;
import eu.shoroa.ross.ui.NodeScreen;
import eu.shoroa.ross.ui.api.*;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import org.lwjgl.opengl.Display;

public class ScreenMainMenu extends NodeScreen {
    @Override
    protected Node build() {
        return new Node()
                .direction(Direction.COLUMN)
                .alignItems(Align.CENTER)
                .justify(Justify.CENTER)
                .children(
                        new Node()
                                .direction(Direction.COLUMN)
                                .justify(Justify.CENTER)
                                .alignItems(Align.CENTER)
                                .width(400f)
                                .height(400f)
                                .children(
                                        new Node()
                                                .direction(Direction.ROW)
                                                .alignItems(Align.SPACE_BETWEEN)
                                                .gap(Gutter.ROW, 4f)
                                                .children(
                                                        new NodeLabel(Fonts.GoogleFlex.weight(500))
                                                                .text("ROSS")
                                                                .fontSize(64f),
                                                        new NodeLabel(Fonts.GoogleFlex.weight(400))
                                                                .text("v1.0.0")
                                                                .fontSize(12f)
                                                                .alignSelf(Align.FLEX_END)
                                                                .margin(Edge.BOTTOM, 14f)
                                                )
                                )
                                .children(
                                        MenuButton("Singleplayer", () -> mc.displayGuiScreen(new GuiSelectWorld(mc.currentScreen))),
                                        MenuButton("Multiplayer", () -> mc.displayGuiScreen(new GuiMultiplayer(mc.currentScreen))),
                                        MenuButton("Settings", () -> mc.displayGuiScreen(new GuiOptions(mc.currentScreen, mc.gameSettings))),
                                        MenuButton("Exit", () -> mc.shutdown())
                                )
                );
    }

    private MenuButton MenuButton(String text, Runnable onClick) {
        MenuButton button = new MenuButton(onClick);
        button.margin(4f);
        button.width(240F);
        button.height(40F);
        button.alignItems(Align.CENTER);
        button.justify(Justify.CENTER);
        button.addChild(
                new NodeLabel(Fonts.GoogleFlex.weight(400))
                        .text(text)
                        .fontSize(16f)
        );

        return button;
    }

    @Override
    protected void render(float mouseX, float mouseY, float partialTicks) {
        Client.INSTANCE.skia.beginFrame();
        Renderer.use(Client.INSTANCE.skia);
        ImageSource backgroundImage = Images.BACKGROUND_1;
        Rect cover = Rect.cover(backgroundImage.getSize(), new Rect(0f, 0f, Display.getWidth(), Display.getHeight()));
        Renderer.drawImage(backgroundImage, cover.x, cover.y, cover.width, cover.height);
        Client.INSTANCE.skia.endFrame();

        Filter.kawase().capture(mc.getFramebuffer().framebufferTexture, 6f, true, 6);
        super.render(mouseX, mouseY, partialTicks);
    }
}
