package eu.shoroa.ross.feature.module;

import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.gui.editor.HUDEditor;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static eu.shoroa.ross.Client.mc;

public abstract class HUDModule extends Module {
    private final List<HUDElement> elements = new ArrayList<>();

    private boolean inEditor = false;

    public HUDModule(String name, String description, Bind bind, String icon) {
        super(name, description, Category.HUD, bind, icon);
    }

    public HUDModule(String name, String description, String icon) {
        super(name, description, Category.HUD, icon);
    }

    protected final <T extends HUDElement> T addElement(T element) {
        if (getElement(element.getId()) != null) {
            throw new IllegalArgumentException("Duplicate HUD element id '" + element.getId() + "' in module '" + name + "'");
        }

        elements.add(element);
        return element;
    }

    protected final void removeElement(HUDElement element) {
        element.onRemove();
        elements.remove(element);
    }

    protected final void clearElements() {
        for (HUDElement element : elements) {
            element.onRemove();
        }
        elements.clear();
    }

    protected final void removeElement(String id) {
        HUDElement element = getElement(id);
        if (element != null) {
            element.onRemove();
            elements.remove(element);
        }
    }

    public final List<HUDElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public final HUDElement getElement(String id) {
        for (HUDElement element : elements) {
            if (element.getId().equals(id)) {
                return element;
            }
        }

        return null;
    }

    public final boolean isInEditor() {
        return inEditor;
    }

    @Subscribe
    @ApiStatus.Internal
    public void onTick(EventTick event) {
        inEditor = mc.currentScreen instanceof HUDEditor;
    }

    @Subscribe
    @ApiStatus.Internal
    public void onHud(Hud.Layer layer) {
        if (mc.currentScreen instanceof HUDEditor) {
            return;
        }

        for (HUDElement element : elements) {
            if (!element.isEnabled()) {
                continue;
            }

            element.render(layer);
        }
    }
}
