package eu.shoroa.ross.feature.gui.clickgui.stella.events;

import eu.shoroa.ross.feature.gui.clickgui.stella.elements.StellaPopup;
import eu.shoroa.ross.feature.module.Category;

public final class StellaEvents {
    private StellaEvents() {
    }

    public static class ChangeCategory {
        public final Category category;

        public ChangeCategory(Category category) {
            this.category = category;
        }
    }

    public static class Popup {
        public final StellaPopup popup;

        public Popup(StellaPopup popup) {
            this.popup = popup;
        }
    }

    public static class ClosePopup {
    }
}
