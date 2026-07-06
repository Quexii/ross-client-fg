package eu.shoroa.ross.feature.module;

import eu.shoroa.ross.render.skia.font.MaterialIcons;

public enum Category {
    COMBAT("Combat", "\uf889"),
    PLAYER("Player", MaterialIcons.PERSON),
    RENDER("Render", MaterialIcons.VISIBILITY),
    HUD("HUD", MaterialIcons.VIEW_QUILT),
    MISC("Misc", MaterialIcons.CATEGORY);

    public final String name;
    public final String icon;

    Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }
}
