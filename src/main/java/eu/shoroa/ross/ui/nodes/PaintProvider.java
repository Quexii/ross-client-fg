package eu.shoroa.ross.ui.nodes;

import eu.shoroa.ross.ui.api.Node;
import io.github.humbleui.skija.Paint;

@FunctionalInterface
public interface PaintProvider {
    void configure(Paint paint, Node node, float mouseX, float mouseY, float partialTicks);
}

