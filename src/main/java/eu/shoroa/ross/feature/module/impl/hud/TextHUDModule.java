package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.feature.module.HUDAnchor;
import eu.shoroa.ross.feature.module.HUDModule;

public abstract class TextHUDModule extends HUDModule {

    private final TextHUDElement main;

    public TextHUDModule(String name, String description, String moduleIcon, String chipIcon) {
        super(name, description, moduleIcon);

        main = addElement(new TextHUDElement("main", chipIcon) {
            @Override
            protected String value() {
                return TextHUDModule.this.value();
            }

            @Override
            protected String suffix() {
                return TextHUDModule.this.suffix();
            }
        });
    }

    protected abstract String value();

    protected abstract String suffix();

    protected final TextHUDElement getMainElement() {
        return main;
    }

    protected final void setDefaultPosition(HUDAnchor anchor, double offsetX, double offsetY) {
        main.setPlacement(anchor, offsetX, offsetY);
    }
}
