package eu.shoroa.ross.ui.nodes;

import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.ui.api.PointerNode;
import io.github.humbleui.skija.Paint;

public class RectNode extends PointerNode<RectNode> {
    private float radius;
    private float strokeWidth = 1f;
    private PaintProvider fillProvider;
    private PaintProvider strokeProvider;

    public RectNode radius(float radius) {
        this.radius = radius;
        return this;
    }

    public RectNode strokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public RectNode fill(PaintProvider fillProvider) {
        this.fillProvider = fillProvider;
        return this;
    }

    public RectNode fillColor(int color) {
        return fill((paint, node, mouseX, mouseY, partialTicks) -> paint.setColor(color));
    }

    public RectNode stroke(PaintProvider strokeProvider) {
        this.strokeProvider = strokeProvider;
        return this;
    }

    public RectNode strokeColor(int color) {
        return stroke((paint, node, mouseX, mouseY, partialTicks) -> paint.setColor(color));
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        if (fillProvider != null) {
            try (Paint p = new Paint()) {
                fillProvider.configure(p, this, mouseX, mouseY, partialTicks);
                drawRect(p);
            }
        }

        if (strokeProvider != null && strokeWidth > 0f) {
            try (Paint p = new Paint()) {
                p.setStroke(true);
                p.setStrokeWidth(strokeWidth);
                strokeProvider.configure(p, this, mouseX, mouseY, partialTicks);
                drawRect(p);
            }
        }

        super.render(mouseX, mouseY, partialTicks);
    }

    private void drawRect(Paint paint) {
        if (radius > 0f) {
            Renderer.drawRRect(getX(), getY(), getWidth(), getHeight(), radius, paint);
        } else {
            Renderer.drawRect(getX(), getY(), getWidth(), getHeight(), paint);
        }
    }
}
