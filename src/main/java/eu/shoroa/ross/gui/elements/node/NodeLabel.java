package eu.shoroa.ross.gui.elements.node;

import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.types.Size;
import eu.shoroa.ross.ui.api.Node;
import eu.shoroa.ross.ui.api.RenderNode;
import eu.shoroa.ross.ui.state.State;
import io.github.humbleui.skija.Paint;
import org.jetbrains.annotations.NotNull;

public class NodeLabel extends RenderNode<NodeLabel> {
    @NotNull
    private Font font;
    private float fontSize = 16f;
    private String text = "";

    public NodeLabel(@NotNull Font font) {
        this.font = font;
    }

    @Override
    public void nodeOnRender(Node node, float mouseX, float mouseY, float partialTicks) {
        try (Paint p = new Paint()) {
            p.setColor(-1);
            Renderer.drawText(text, getX(), getY(), font, fontSize, Font.Align.TOP_LEFT, p);
        }
    }

    public NodeLabel text(String text) {
        if (this.text != text) {
            if (text == null) {
                text = "";
            }
            this.text = text;
            markDirty();
        }

        return this;
    }

    public NodeLabel text(State<String> state) {
        text(state.get());
        state.onChange(this::text);
        return this;
    }

    public NodeLabel font(@NotNull Font font) {
        if (this.font != font) {
            this.font = font;
            markDirty();
        }

        return this;
    }

    public NodeLabel fontSize(float fontSize) {
        if (this.fontSize != fontSize) {
            this.fontSize = fontSize;
            markDirty();
        }

        return this;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Size textBounds = Renderer.getTextBounds(text, font, fontSize);
        width(textBounds.width);
        height(textBounds.height);
    }
}
