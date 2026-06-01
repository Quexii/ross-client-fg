package eu.shoroa.ross.ui.nodes;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.ui.api.Node;
import eu.shoroa.ross.ui.api.ScrollNode;

public class ScrollContainer extends ScrollNode<ScrollContainer> {
    private float scrollOffset;
    private float targetScrollOffset;
    private float scrollSpeed = 18f;
    private float smoothScrollSpeed = 0.25f;
    private boolean smoothScrolling = true;
    private long lastUpdateNanos;

    public ScrollContainer scrollSpeed(float scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
        return this;
    }

    public ScrollContainer smoothScroll(boolean enabled) {
        this.smoothScrolling = enabled;
        return this;
    }

    public ScrollContainer smoothScrollSpeed(float speed) {
        this.smoothScrollSpeed = speed;
        return this;
    }

    public float getScrollOffset() {
        return scrollOffset;
    }

    public ScrollContainer scrollOffset(float scrollOffset) {
        setScrollOffset(scrollOffset);
        return this;
    }

    @Override
    public boolean nodeOnScroll(Node node, float mouseX, float mouseY, float scroll, float partialTicks) {
        float maxScroll = getMaxScroll();
        if (maxScroll <= 0f) {
            return false;
        }
        targetScrollOffset = targetScrollOffset - scroll * scrollSpeed;
        clampTargetScrollOffset();
        if (!smoothScrolling) {
            setScrollOffset(targetScrollOffset);
        }
        return true;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        if (renderHandler != null) {
            renderHandler.nodeOnRender(this, mouseX, mouseY, partialTicks);
        }

        long now = System.nanoTime();
        float deltaSeconds = lastUpdateNanos == 0L ? 0f : (now - lastUpdateNanos) / 1_000_000_000f;
        lastUpdateNanos = now;
        updateSmoothScroll(Math.min(deltaSeconds, 0.1f));

        Renderer.save();
        Renderer.clipRect(getX(), getY(), getWidth(), getHeight());
        Renderer.translate(0f, -scrollOffset);

        float adjustedMouseY = mouseY + scrollOffset;
        for (Node child : children) {
            child.render(mouseX, adjustedMouseY, partialTicks);
        }

        Renderer.restore();
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }
        if (inputHandler != null && inputHandler.nodeOnInput(this, mouseX, mouseY, event)) {
            return true;
        }

        float adjustedMouseY = mouseY + scrollOffset;
        for (Node child : children) {
            if (child.input(mouseX, adjustedMouseY, event)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean scroll(float mouseX, float mouseY, float scroll, float partialTicks) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }

        float adjustedMouseY = mouseY + scrollOffset;
        for (Node child : children) {
            if (child.scroll(mouseX, adjustedMouseY, scroll, partialTicks)) {
                return true;
            }
        }

        return nodeOnScroll(this, mouseX, mouseY, scroll, partialTicks);
    }

    private void clampScrollOffset() {
        float maxScroll = getMaxScroll();
        if (scrollOffset < 0f) {
            scrollOffset = 0f;
        } else if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
    }

    private void clampTargetScrollOffset() {
        float maxScroll = getMaxScroll();
        if (targetScrollOffset < 0f) {
            targetScrollOffset = 0f;
        } else if (targetScrollOffset > maxScroll) {
            targetScrollOffset = maxScroll;
        }
    }

    private void setScrollOffset(float nextOffset) {
        scrollOffset = nextOffset;
        clampScrollOffset();
        targetScrollOffset = scrollOffset;
    }

    private void updateSmoothScroll(float deltaSeconds) {
        clampTargetScrollOffset();
        if (!smoothScrolling) {
            scrollOffset = targetScrollOffset;
            return;
        }
        float deltaFrames = deltaSeconds * 60f;
        float base = 1f - smoothScrollSpeed;
        float alpha = deltaFrames <= 0f ? 0f : 1f - (float) Math.pow(base, deltaFrames);
        float deltaOffset = targetScrollOffset - scrollOffset;
        if (Math.abs(deltaOffset) < 0.01f) {
            scrollOffset = targetScrollOffset;
            return;
        }
        scrollOffset += deltaOffset * alpha;
        clampScrollOffset();
    }

    private float getMaxScroll() {
        float maxBottom = getY();
        for (Node child : children) {
            maxBottom = Math.max(maxBottom, child.getY() + child.getHeight());
        }
        float contentHeight = maxBottom - getY();
        return Math.max(0f, contentHeight - getHeight());
    }
}
