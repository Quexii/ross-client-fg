package eu.shoroa.ross.gui;

import eu.shoroa.ross.event.EventInput;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import static eu.shoroa.ross.Client.EVENT_BUS;

public abstract class RossScreen extends GuiScreen {
    protected abstract void init();

    protected abstract void render(float mouseX, float mouseY, float partialTicks);

    protected abstract void input(float mouseX, float mouseY, EventInput event);

    @Override
    public void initGui() {
        EVENT_BUS.register(this);
        init();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        render(Mouse.getX(), Display.getHeight() - Mouse.getY(), partialTicks);
    }

    @Override
    public void onGuiClosed() {
        EVENT_BUS.unregister(this);
    }

    @Override
    public void handleMouseInput() {
        if (Mouse.getEventButton() != -1) {
            input(Mouse.getX(), Display.getHeight() - Mouse.getY(), new EventInput(Mouse.getEventButton(), EventInput.Type.MOUSE, Mouse.getEventButtonState() ? EventInput.Action.PRESS : EventInput.Action.RELEASE));
        }
        super.handleMouseInput();
    }

    @Override
    public void handleKeyboardInput() {
        if (Keyboard.getEventKey() != 0) {
            input(Mouse.getX(), Display.getHeight() - Mouse.getY(), new EventInput(Keyboard.getEventKey(), EventInput.Type.KEYBOARD, Keyboard.getEventKeyState() ? EventInput.Action.PRESS : EventInput.Action.RELEASE));
        }
        super.handleKeyboardInput();
    }
}
