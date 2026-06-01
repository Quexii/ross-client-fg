package eu.shoroa.ross.ui.api;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.types.Rect;
import eu.shoroa.ross.ui.handlers.InputHandler;
import eu.shoroa.ross.ui.handlers.RenderHandler;
import eu.shoroa.ross.ui.handlers.ScrollHandler;
import io.github.humbleui.skija.Paint;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.yoga.Yoga;

import java.util.ArrayList;
import java.util.List;

public class Node<T extends Node> {
    public final long yogaNode;

    protected Node parent;
    public final List<Node> children = new ArrayList<>();

    protected float absX, absY, absWidth, absHeight;

    private boolean dirty;

    protected InputHandler inputHandler;
    protected RenderHandler renderHandler;
    protected ScrollHandler scrollHandler;

    private static long config = -1;

    public Node() {
        if (config == -1) {
            config = Yoga.YGConfigNew();
            Yoga.YGConfigSetUseWebDefaults(config, true);
            Yoga.YGConfigSetExperimentalFeatureEnabled(config, Yoga.YGExperimentalFeatureWebFlexBasis, true);
        }

        this.yogaNode = Yoga.YGNodeNewWithConfig(config);
    }

    public void markDirty() {
        dirty = true;
        if (parent != null) {
            parent.markDirty();
        }
    }

    public boolean consumeDirty() {
        boolean wasDirty = dirty;
        dirty = false;
        return wasDirty;
    }

    public T addChild(Node child) {
        child.parent = this;
        children.add(child);

        int index = Math.toIntExact(Yoga.YGNodeGetChildCount(this.yogaNode));
        Yoga.YGNodeInsertChild(this.yogaNode, child.yogaNode, index);

        return (T) this;
    }

    public T children(Node... children) {
        for (Node child : children) {
            addChild(child);
        }

        return (T) this;
    }

    public void calcLayout(float width, float height, LayoutDirection direction) {
        Yoga.YGNodeCalculateLayout(yogaNode, width, height, direction.value);
    }

    public void resolveAbsolutePositions(float parentX, float parentY) {
        float localX = Yoga.YGNodeLayoutGetLeft(yogaNode);
        float localY = Yoga.YGNodeLayoutGetTop(yogaNode);

        this.absWidth = Yoga.YGNodeLayoutGetWidth(yogaNode);
        this.absHeight = Yoga.YGNodeLayoutGetHeight(yogaNode);

        this.absX = parentX + localX;
        this.absY = parentY + localY;

        for (Node child : children) {
            child.resolveAbsolutePositions(this.absX, this.absY);
        }
    }

    public void dispose() {
        children.clear();
        Yoga.YGNodeFreeRecursive(yogaNode);
    }

    public void clearChildren() {
        for (Node child : children) {
            Yoga.YGNodeRemoveChild(yogaNode, child.yogaNode);
            child.dispose();
        }
        children.clear();
    }

    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (inputHandler != null && inputHandler.nodeOnInput(this, mouseX, mouseY, event)) {
            return true;
        }
        for (Node child : children) {
            if (child.input(mouseX, mouseY, event)) {
                return true;
            }
        }
        return false;
    }

    public boolean scroll(float mouseX, float mouseY, float scroll, float partialTicks) {
        if (scrollHandler != null && scrollHandler.nodeOnScroll(this, mouseX, mouseY, scroll, partialTicks)) {
            return true;
        }
        for (Node child : children) {
            if (child.scroll(mouseX, mouseY, scroll, partialTicks)) {
                return true;
            }
        }
        return false;
    }

    public void render(float mouseX, float mouseY, float partialTicks) {
        if (renderHandler != null) {
            renderHandler.nodeOnRender(this, mouseX, mouseY, partialTicks);
        }

        for (Node child : children) {
            child.render(mouseX, mouseY, partialTicks);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
            try (Paint p = new Paint()) {
                p.setColor(0xFFFF0000);
                p.setStroke(true);
                Renderer.drawRect(getX(), getY(), getWidth(), getHeight(), p);
            }
        }
    }

    public T border(Edge edge, float value) {
        Yoga.YGNodeStyleSetBorder(yogaNode, edge.value, value);
        return (T) this;
    }

    public T gap(Gutter gutter, float value) {
        Yoga.YGNodeStyleSetGap(yogaNode, gutter.value, value);
        return (T) this;
    }

    public T left(float value) {
        Yoga.YGNodeStyleSetPosition(yogaNode, Edge.LEFT.value, value);
        return (T) this;
    }

    public T top(float value) {
        Yoga.YGNodeStyleSetPosition(yogaNode, Edge.TOP.value, value);
        return (T) this;
    }

    public T right(float value) {
        Yoga.YGNodeStyleSetPosition(yogaNode, Edge.RIGHT.value, value);
        return (T) this;
    }

    public T bottom(float value) {
        Yoga.YGNodeStyleSetPosition(yogaNode, Edge.BOTTOM.value, value);
        return (T) this;
    }

    public T width(float width) {
        Yoga.YGNodeStyleSetWidth(yogaNode, width);
        return (T) this;
    }

    public T widthPercent(float percent) {
        Yoga.YGNodeStyleSetWidthPercent(yogaNode, percent);
        return (T) this;
    }

    public T widthAuto() {
        Yoga.YGNodeStyleSetWidthAuto(yogaNode);
        return (T) this;
    }

    public T minWidth(float minWidth) {
        Yoga.YGNodeStyleSetMinWidth(yogaNode, minWidth);
        return (T) this;
    }

    public T maxWidth(float maxWidth) {
        Yoga.YGNodeStyleSetMaxWidth(yogaNode, maxWidth);
        return (T) this;
    }

    public T height(float height) {
        Yoga.YGNodeStyleSetHeight(yogaNode, height);
        return (T) this;
    }

    public T heightPercent(float percent) {
        Yoga.YGNodeStyleSetHeightPercent(yogaNode, percent);
        return (T) this;
    }

    public T heightAuto() {
        Yoga.YGNodeStyleSetHeightAuto(yogaNode);
        return (T) this;
    }

    public T minHeight(float minHeight) {
        Yoga.YGNodeStyleSetMinHeight(yogaNode, minHeight);
        return (T) this;
    }

    public T maxHeight(float maxHeight) {
        Yoga.YGNodeStyleSetMaxHeight(yogaNode, maxHeight);
        return (T) this;
    }

    public T direction(Direction direction) {
        Yoga.YGNodeStyleSetFlexDirection(yogaNode, direction.value);
        return (T) this;
    }

    public T alignItems(Align align) {
        Yoga.YGNodeStyleSetAlignItems(yogaNode, align.value);
        return (T) this;
    }

    public T alignContent(Align align) {
        Yoga.YGNodeStyleSetAlignContent(yogaNode, align.value);
        return (T) this;
    }

    public T alignSelf(Align align) {
        Yoga.YGNodeStyleSetAlignSelf(yogaNode, align.value);
        return (T) this;
    }

    public T position(PositionType type) {
        Yoga.YGNodeStyleSetPositionType(yogaNode, type.value);
        return (T) this;
    }

    public T margin(Edge edge, float value) {
        Yoga.YGNodeStyleSetMargin(yogaNode, edge.value, value);
        return (T) this;
    }

    public T margin(float margin) {
        Yoga.YGNodeStyleSetMargin(yogaNode, Edge.ALL.value, margin);
        return (T) this;
    }

    public T padding(Edge edge, float value) {
        Yoga.YGNodeStyleSetPadding(yogaNode, edge.value, value);
        return (T) this;
    }

    public T padding(float padding) {
        Yoga.YGNodeStyleSetPadding(yogaNode, Edge.ALL.value, padding);
        return (T) this;
    }

    public T overflow(Overflow overflow) {
        Yoga.YGNodeStyleSetOverflow(yogaNode, overflow.value);
        return (T) this;
    }

    public T rect(Rect rect) {
        left(rect.x);
        top(rect.y);
        width(rect.width);
        height(rect.height);
        return (T) this;
    }

    public T justify(Justify justify) {
        Yoga.YGNodeStyleSetJustifyContent(yogaNode, justify.value);
        return (T) this;
    }

    public float getX() {
        return absX;
    }

    public float getY() {
        return absY;
    }

    public float getWidth() {
        return absWidth;
    }

    public float getHeight() {
        return absHeight;
    }

    public boolean contains(float x, float y) {
        return x >= absX && x <= absX + absWidth && y >= absY && y <= absY + absHeight;
    }
}
