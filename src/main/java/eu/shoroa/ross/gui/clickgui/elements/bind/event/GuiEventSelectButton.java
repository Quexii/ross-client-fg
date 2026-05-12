package eu.shoroa.ross.gui.clickgui.elements.bind.event;

import eu.shoroa.ross.gui.clickgui.elements.bind.BindPanel;
import eu.shoroa.ross.gui.clickgui.elements.bind.EleKey;

public class GuiEventSelectButton {
    public final EleKey element;
    public final BindPanel.Btn button;

    public GuiEventSelectButton(EleKey element, BindPanel.Btn button) {
        this.element = element;
        this.button = button;
    }
}
