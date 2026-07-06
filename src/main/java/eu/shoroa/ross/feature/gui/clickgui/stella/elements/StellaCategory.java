package eu.shoroa.ross.feature.gui.clickgui.stella.elements;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.gui.GuiElement;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.gui.clickgui.stella.events.StellaEvents;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.fml.RossMod;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.animate.Easing;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import static eu.shoroa.ross.Client.EVENT_BUS;
import static eu.shoroa.ross.Client.mc;

public class StellaCategory extends GuiElement {
    public final Category category;

    private final Animate selectedEase = new Animate(150L, Easing.CIRC_OUT);
    private final Animate clickEase = new Animate(150L, Easing.CIRC_OUT);
    private boolean clicked = false;

    public StellaCategory(float x, float y, float width, float height, Category category) {
        super(x, y, width, height);
        this.category = category;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        boolean hovered = contains(mouseX, mouseY);
        clickEase.doEase(clicked);

        float cx = getX() + getWidth() / 2f;
        float cy = getY() + getHeight() / 2f;

        try (Paint p = new Paint()) {
            p.setColor(StellaTheme.get().onAccent);
            UI.drawText(category.icon, cx, cy, Fonts.MaterialIcons.weight(300).grade(200).fill(true), 50f, Align.CENTER, p);
        }
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (event.type == EventInput.Type.MOUSE) {
            boolean hovered = contains(mouseX, mouseY);

            if (event.action == EventInput.Action.PRESS && hovered) {
                clicked = true;
                return true;
            }

            if (clicked && event.action == EventInput.Action.RELEASE) {
                if (hovered) {
                    EVENT_BUS.post(new StellaEvents.ChangeCategory(category));
                    mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation(RossMod.MOD_ID, "ui.glass"), 0.7f));
                }

                clicked = false;
                return true;
            }
        }
        return false;
    }
}
