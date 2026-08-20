package eu.shoroa.ross.feature.gui.clickgui.stella;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.gui.RossScreen;
import eu.shoroa.ross.feature.gui.clickgui.stella.elements.StellaPanel;
import eu.shoroa.ross.feature.gui.clickgui.stella.elements.StellaPopup;
import eu.shoroa.ross.feature.gui.clickgui.stella.events.StellaEvents;
import eu.shoroa.ross.utils.math.Mth;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.DampFloat;
import io.github.humbleui.skija.Paint;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import static eu.shoroa.ross.Client.EVENT_BUS;

public class StellaCG extends RossScreen {
    private static StellaCG instance;

    public static StellaCG getInstance() {
        if (instance == null) {
            instance = new StellaCG();
            EVENT_BUS.register(instance);
            EVENT_BUS.register(instance.panel);
        }
        return instance;
    }

    private final float PREFERRED_WIDTH = 1128f;
    private final float PREFERRED_HEIGHT = 734f;
    private final float PANEL_RATIO = PREFERRED_WIDTH / PREFERRED_HEIGHT;

    private StellaPanel panel = new StellaPanel(0f, 0f, PREFERRED_WIDTH, PREFERRED_HEIGHT);
    private DampFloat openEase = new DampFloat();
    private DampFloat blurEase = new DampFloat();
    private boolean closing = false;

    private StellaPopup currentPopup = null;
    private DampFloat popupEase = new DampFloat();
    private boolean closingPopup = false;

    private StellaCG() {
        super();
    }

    @Override
    protected void init() {
        closing = false;
    }

    @Override
    protected void render(float mouseX, float mouseY, float partialTicks) {
        float dw = Display.getWidth();
        float dh = Display.getHeight();

        Mth.smoothDamp(openEase, closing ? 0f : 1f, 0.05f, (float) Animate.getDelta());
        Mth.smoothDamp(blurEase, closing ? 0f : 1f, 0.1f, (float) Animate.getDelta());
        Mth.smoothDamp(popupEase, closingPopup ? 0f : 1f, 0.1f, (float) Animate.getDelta());

        if (closingPopup && popupEase.value <= 0.01) {
            currentPopup = null;
            closingPopup = false;
        }

        if (closing && openEase.value <= 0.01) {
            mc.displayGuiScreen(null);
            return;
        }

        panel.setX((dw - PREFERRED_WIDTH) / 2f);
        panel.setY((dh - PREFERRED_HEIGHT) / 2f);

        Filter.kawase().capture(mc.getFramebuffer().framebufferTexture, 8 * blurEase.value, true, 2);

        if (Client.INSTANCE.getSkia().isStale()) return;
        Client.INSTANCE.getSkia().beginFrame();
        UI.use(Client.INSTANCE.getSkia());
        UI.drawFilter(Filter.kawase(), mc.getFramebuffer().framebufferTexture, 0f, 0f, dw, dw);
        UI.saveLayerAlpha(0, 0, dw, dh, openEase.value);
        UI.translate(0f, 40 * (1 - openEase.value));
        panel.render(mouseX, mouseY, partialTicks);
        if (currentPopup != null) {
            UI.saveLayerAlpha(0, 0, dw, dh, popupEase.value);
            try (Paint p = new Paint()) {
                p.setColor(StellaTheme.get().dimmer);
                UI.drawRect(0f, 0f, dw, dh, p);
            }
            UI.translate(0f, 500 * (1 - popupEase.value));
            currentPopup.setX((dw - currentPopup.getWidth()) / 2f);
            currentPopup.setY((dh - currentPopup.getHeight()) / 2f);
            currentPopup.render(mouseX, mouseY, partialTicks);
            UI.restore();
        }
        UI.restore();
        Client.INSTANCE.getSkia().endFrame();
    }

    @Override
    protected void input(float mouseX, float mouseY, EventInput event) {
        boolean escape = event.type == EventInput.Type.KEYBOARD && event.action == EventInput.Action.PRESS && event.value == Keyboard.KEY_ESCAPE;

        if (currentPopup != null) {
            // The popup gets everything first so e.g. a listening bind row can
            // consume keys (including Escape) before they close anything.
            if (currentPopup.input(mouseX, mouseY, event)) {
                return;
            }
            if (escape
                    || (event.type == EventInput.Type.MOUSE && event.action == EventInput.Action.PRESS
                    && !currentPopup.contains(mouseX, mouseY))) {
                closingPopup = true;
            }
            return;
        }

        if (escape) {
            closing = true;
            return;
        }

        if (panel.input(mouseX, mouseY, event)) {
            return;
        }
    }

    @Subscribe
    @ApiStatus.Internal
    public void onPopup(StellaEvents.Popup event) {
        currentPopup = event.popup;
        closingPopup = false;
        popupEase.value = 0;
        popupEase.velocity = 0;
    }

    @Subscribe
    @ApiStatus.Internal
    public void onClosePopup(StellaEvents.ClosePopup event) {
        closingPopup = true;
    }

    @Override
    protected void scroll(float value, float partialTicks) {
        if (currentPopup != null) {
            currentPopup.scroll(value, partialTicks);
            return;
        }
        panel.scroll(value, partialTicks);
    }

    @Override
    protected boolean cancelEscape() {
        return true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
