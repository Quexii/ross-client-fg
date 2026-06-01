package eu.shoroa.ross.ui.api;

public enum MouseButton {
    LEFT(0),
    RIGHT(1),
    MIDDLE(2);

    public final int value;

    MouseButton(int value) {
        this.value = value;
    }

    public static MouseButton from(int value) {
        for (MouseButton button : values()) {
            if (button.value == value) {
                return button;
            }
        }
        return null;
    }
}

