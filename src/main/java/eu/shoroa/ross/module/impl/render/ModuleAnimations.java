package eu.shoroa.ross.module.impl.render;

import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.settings.ModeEnum;
import eu.shoroa.ross.settings.ModeSetting;

public class ModuleAnimations extends Module {
    public ModeSetting<AnimationMode> animation = register(new ModeSetting<>("Animation", "blockhit.animation", AnimationMode.OLD));
    public ModeSetting<SmoothMode> smoothing = register(new ModeSetting<>("Smoothing", "blockhit.hitting", SmoothMode.NORMAL));

    public ModuleAnimations() {
        super("Animations", "Changes the way your animations look", Category.RENDER, null);
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
