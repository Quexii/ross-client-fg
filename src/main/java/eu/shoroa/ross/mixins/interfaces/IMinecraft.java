package eu.shoroa.ross.mixins.interfaces;

import net.minecraft.util.Timer;

public interface IMinecraft {
    Timer getTimer();

    void setLeftClickCounter(int leftClickCounter);

    void setRightClickDelayTimer(int rightClickDelayTimer);
}
