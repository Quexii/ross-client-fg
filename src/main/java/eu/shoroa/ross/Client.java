package eu.shoroa.ross;

import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.SkiaSource;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.image.Images;
import eu.shoroa.ross.util.proj.EntityProjection;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import eu.shoroa.ross.event.EventInput;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

public class Client {
    public static final Client INSTANCE = new Client();

    public static final Minecraft mc = Minecraft.getMinecraft();

    public SkiaSource skia;

    private Client() {
        EVENT_BUS.register(this);
    }

    public void init() {
        skia = new SkiaSource(mc.getFramebuffer());
        skia.init();
        Fonts.load();
        Images.load();
        Filter.kawase().init();

        EntityProjection.getInstance();

        ModuleManager.init();
    }

    @SubscribeEvent
    public void oe$EventKey(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKey() != 0) {
            ModuleManager.onInput(new EventInput(Keyboard.getEventKey(), EventInput.Type.KEYBOARD, Keyboard.getEventKeyState() ? EventInput.Action.PRESS : EventInput.Action.RELEASE));
        }
    }

    @SubscribeEvent
    public void oe$EventMouse(InputEvent.MouseInputEvent event) {
        if (Mouse.getEventButton() != -1) {
            ModuleManager.onInput(new EventInput(Mouse.getEventButton(), EventInput.Type.MOUSE, Mouse.getEventButtonState() ? EventInput.Action.PRESS : EventInput.Action.RELEASE));
        }
    }
}
