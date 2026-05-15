package eu.shoroa.ross.gui.clickgui;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.gui.RossScreen;
import eu.shoroa.ross.gui.clickgui.elements.EleCategory;
import eu.shoroa.ross.gui.clickgui.elements.bind.BindPanel;
import eu.shoroa.ross.gui.elements.IconButton;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.util.render.MaterialIcons;
import io.github.humbleui.types.Rect;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import static eu.shoroa.ross.Client.EVENT_BUS;

public class ScreenClickGUI extends RossScreen {
    private final EleCategory[] categories = new EleCategory[Category.values().length];
    private IconButton bindsButton;
    private BindPanel bindPanel;

    private Animate bindsAnim = new Animate(180L, Easing.CIRC_OUT);
    private boolean bindsOpen = false;

    private boolean init;

    // TODO: center categories, clamp max height, add scrolling

    @Override
    protected void init() {
        if (!init) {
            for (int i = 0; i < categories.length; i++) {
                categories[i] = new EleCategory(Category.values()[i]);
            }

            float xi = 10f;

            for (EleCategory category : categories) {
                category.setX(xi);
                category.setY(10f);
                xi += category.getWidth() + 20f;
            }

            init = true;
        }

        final float bindsButtonSize = 44;
        bindsButton = new IconButton(Display.getWidth() - 10 - bindsButtonSize, Display.getHeight() - 10 - bindsButtonSize, bindsButtonSize, bindsButtonSize, Fonts.MaterialIcons.opticSize(40), MaterialIcons.KEYBOARD);
        bindsButton.Action(() -> bindsOpen = true);

        final float bpW = 900f;
        final float bpH = 324f;
        bindPanel = new BindPanel(Display.getWidth() / 2f - bpW / 2f, Display.getHeight() / 2f - bpH / 2f, bpW, bpH);
        bindsAnim.setDuration(120L);

        EVENT_BUS.register(bindPanel);
    }

    @Override
    protected void render(float mouseX, float mouseY, float partialTicks) {
        bindsAnim.doEase(bindsOpen);

        float passedMX = bindsOpen ? -1 : mouseX;
        float passedMY = bindsOpen ? -1 : mouseY;

        Filter.kawase().capture(mc.getFramebuffer().framebufferTexture, 4f, true, 4);

        Client.INSTANCE.skia.beginFrame();
        Renderer.use(Client.INSTANCE.skia);
        Renderer.drawFilter(Filter.kawase(), mc.getFramebuffer().framebufferTexture, 0f, 0f, Display.getWidth(), Display.getHeight());
        for (EleCategory category : categories) {
            Client.INSTANCE.skia.getCanvas().drawRectShadowNoclip(Rect.makeXYWH(category.getX(), category.getY(), category.getWidth(), category.getModulesHeight()), 0f, 0f, 16f, 2f, 0xAA000000);
        }

        for (EleCategory category : categories) {
            category.render(passedMX, passedMY, partialTicks);
        }

        bindsButton.render(passedMX, passedMY, partialTicks);

        Renderer.saveAlpha((float) bindsAnim.getLinearValue());
        Renderer.drawFilter(Filter.kawase(), mc.getFramebuffer().framebufferTexture, 0f, 0f, Display.getWidth(), Display.getHeight());
        Renderer.translate(0f, (float) (80 - 80 * bindsAnim.getValue()));
        bindPanel.render(mouseX, mouseY, partialTicks);
        Renderer.restore();
        Client.INSTANCE.skia.endFrame();
    }

    @Override
    protected void input(float mouseX, float mouseY, EventInput event) {
        if (bindsOpen && !bindPanel.cancelEscape() && event.type == EventInput.Type.KEYBOARD && event.value == Keyboard.KEY_ESCAPE && event.action == EventInput.Action.PRESS) {
            bindsOpen = false;
            return;
        }

        if (bindsOpen) {
            if (bindPanel.input(mouseX, mouseY, event)) return;
            return;
        }
        if (!bindsOpen && bindsButton.input(mouseX, mouseY, event)) return;
        for (EleCategory category : categories) {
            if (category.input(mouseX, mouseY, event)) return;
        }
    }

    @Override
    protected void scroll(float value, float partialTicks) {
        if (bindsOpen) {
            bindPanel.scroll(value, partialTicks);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected boolean cancelEscape() {
        return bindsOpen || bindPanel.cancelEscape();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        Client.INSTANCE.config.saveQueued();
    }
}
