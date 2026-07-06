package eu.shoroa.ross.feature.module.impl.render;

import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.ModeEnum;
import eu.shoroa.ross.feature.setting.ModeSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.skia.font.MaterialIcons;

public class ModuleAnimations extends Module {
    private final SettingCategory categorySettings = addCategory("Settings", ".", "settings");
    public ModeSetting<AnimationMode> animation = register(new ModeSetting<>("Animation", "animation", AnimationMode.OLD), categorySettings);
    public ModeSetting<SmoothMode> smoothing = register(new ModeSetting<>("Smoothing", "hitting", SmoothMode.NORMAL), categorySettings);

    public ModuleAnimations() {
        super("Animations", "Changes the way your sword animations look.", Category.RENDER, "\uf49a");
    }

    public enum AnimationMode implements ModeEnum {
        OLD("Old"),
        CHILL("Chill"),
        EXHI("Exhibition"),
        EXHI_TAP("Exhi Tap"),
        SLIDE("Slide"),
        ASTOLFO("Astolfo"),
        SWING("Swing"),
        SIGMA("Sigma"),
        SHRED("Shred"),
        STELLA("Stella"),
        BUTTER("Butter"),
        FATHUM("Fathum"),
        OH_THE_MISERY("Oh The Misery");

        private final String displayName;

        AnimationMode(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String displayName() {
            return displayName;
        }
    }

    public enum SmoothMode implements ModeEnum {
        NORMAL("Normal"),
        SMOOTH("Smooth");

        private final String displayName;

        SmoothMode(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String displayName() {
            return displayName;
        }
    }
}