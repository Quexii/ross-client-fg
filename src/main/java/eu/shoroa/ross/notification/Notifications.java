package eu.shoroa.ross.notification;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.image.Images;
import eu.shoroa.ross.util.render.MaterialIcons;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import org.lwjgl.opengl.Display;

import java.util.ArrayDeque;

import static eu.shoroa.ross.Client.mc;

public class Notifications {
    private static final Notifications INSTANCE = new Notifications();

    public static Notifications getInstance() {
        return INSTANCE;
    }

    private final ArrayDeque<Notification> queue = new ArrayDeque<>();

    public static void add(String title, String message) {
        add(title, message, Notification.Type.INFO, 3000);
    }

    public static void add(String title, String message, Notification.Type type, long duration) {
        INSTANCE.queue.add(new Notification(title, message, type, duration));
    }

    @Subscribe
    public void onRender(EventHUD.TopSkia event) {
        if (queue.isEmpty()) return;

        final float screenW = Display.getWidth();
        final float screenH = Display.getHeight();

        final float notifWidth = 300;
        final float notifHeight = 60;

        float yOffset = 0;
        int max = 5;

        Canvas canvas = Client.INSTANCE.skia.getCanvas();

        int i = 0;
        for (Notification notif : queue) {
            if (i++ >= max) break;

            float x = (float) (screenW - (notifWidth + 12) * (notif.entryAnim.getValue() - notif.exitAnim.getValue()));
            float y = screenH - 12 - yOffset - notifHeight;

            float scale = 1 - (float) (notif.exitAnim.getValue() * 0.5f);
            float rotation = (float) (10f - notif.entryAnim.getValue() * 10f - notif.exitAnim.getValue() * 15f);

            canvas.saveLayerAlpha(null, (int) ((notif.entryAnim.getValue() - notif.exitAnim.getValue()) * 255));
            canvas.translate(x + notifWidth, y);
            canvas.scale(scale, scale);
            canvas.rotate(rotation);
            canvas.translate(-x- notifWidth, -y);
            render(notif, x, y, notifWidth, notifHeight);
            canvas.restore();

            yOffset += (notifHeight + 8) * (notif.entryAnim.getValue() - notif.exitAnim.getValue());
        }

        queue.removeIf((n) -> n.isExpired() && n.exitAnim.getValue() >= 0.99);
    }

    private void render(Notification notification, float x, float y, float width, float height) {
        Canvas canvas = Client.INSTANCE.skia.getCanvas();
        if (canvas == null) return;

        int accent;

        switch (notification.type) {
            case SUCCESS:
                accent = 0xFF4CAF50;
                break;
            case ERROR:
                accent = 0xFFF44336;
                break;
            case WARNING:
                accent = 0xFFFFC107;
                break;
            default:
                accent = 0xFF2196F3;
                break;
        }

        Font iconFont = Fonts.MaterialIcons.opticSize(20).weight(400).grade(100);
        Font titleFont = Fonts.GoogleFlex.weight(500).opticSize(14);
        Font messageFont = Fonts.GoogleFlex.weight(400).opticSize(12);

        float imageSize = 280;

        try (Paint p = new Paint()) {
            Renderer.drawFilter(Filter.kawase(), mc.getFramebuffer().framebufferTexture, x, y, width, height, 8);

            p.setColor(0x99000000);
            Renderer.drawRRect(x, y, width, height, 8, p);

            canvas.save();
            canvas.clipRRect(RRect.makeXYWH(x, y, width, height, 8), true);
            p.setColor(0x33FFFFFF);
            Renderer.drawRect(x, y, width * notification.getProgress(), height, p);

            p.setColorFilter(ColorFilter.makeBlend(accent, BlendMode.SRC_IN));
            p.setColor(accent & 0x66FFFFFF);
            Renderer.drawImage(
                    Images.HALFTONE_CIRCLE,
                    x + width - 160,
                    y + (height - imageSize) / 2f + 60,
                    imageSize,
                    imageSize,
                    p
            );
            p.setColor(accent & 0xCCFFFFFF);
            p.setImageFilter(ImageFilter.makeBlur(6f, 6f, FilterTileMode.DECAL));
            Renderer.drawImage(
                    Images.HALFTONE_CIRCLE,
                    x + width - 160,
                    y + (height - imageSize) / 2f + 60,
                    imageSize,
                    imageSize,
                    p
            );

            p.setImageFilter(null);
            p.setColorFilter(null);
            canvas.restore();

            p.setStroke(true);
            p.setStrokeWidth(2f);
            p.setColor(0x75000000);
            Renderer.drawRRect(x, y, width, height, 8, p);
            p.setStroke(false);

            String icon = getIcon(notification.type);

            p.setColor(accent);
            Renderer.drawText(icon, x + 12f, y + height / 2f, iconFont, 24f, Font.Align.CENTER_LEFT, p);

            p.setColor(0xFFFFFFFF);
            Renderer.drawText(notification.title, x + 48f, y + height / 2f - 6f, titleFont, 16f, Font.Align.CENTER_LEFT, p);
            Renderer.drawText(notification.message, x + 48f, y + height / 2f + 10f, messageFont, 12f, Font.Align.CENTER_LEFT, p);
        }
    }

    private int withAlpha(int color, float alpha) {
        return (int) (color & 0x00FFFFFF) | (int) (alpha * 255) << 24;
    }

    private String getIcon(Notification.Type t) {
        String character;
        switch (t) {
            case SUCCESS:
                character = MaterialIcons.CHECK;
                break;
            case ERROR:
                character = MaterialIcons.CLOSE;
                break;
            case WARNING:
                character = MaterialIcons.WARNING;
                break;
            default:
                character = MaterialIcons.INFO;
                break;
        }
        return character;
    }
}