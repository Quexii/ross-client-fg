package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.nori.Nori;
import eu.shoroa.nori.parse.Node;
import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.HUDAnchor;
import eu.shoroa.ross.feature.module.HUDElement;
import eu.shoroa.ross.feature.module.HUDModule;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.skia.image.FileImage;
import eu.shoroa.ross.render.skia.image.ImageSource;
import eu.shoroa.ross.render.skia.image.remote.RemoteImage;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.Size;
import eu.shoroa.ross.utils.player.ChatUtil;
import io.github.humbleui.skija.Paint;
import org.jetbrains.annotations.ApiStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import static eu.shoroa.ross.Client.mc;

public class ModuleHUDSplash extends HUDModule {
    private final Nori nori = new Nori();
    private final File imagesFolder;
    private final WatchService watchService;

    private final List<ImageItem> imageItems = new ArrayList<>();

    {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ModuleHUDSplash() {
        super("HUD Splash", "A splash screen for the HUD", "\ue3f4");

        nori.addReference("static", BehaviourType.STATIC);

        imagesFolder = new File(mc.mcDataDir, "ross/images");

        if (!imagesFolder.exists() && !imagesFolder.mkdirs()) {
            throw new RuntimeException("Failed to create image directory: " + imagesFolder);
        }

        Path path = imagesFolder.toPath();

        try {
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateElements() {
        File[] files = imagesFolder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".nori")) {
                continue;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append('\n');
                }

                Node<?> root = nori.parse(content.toString());

                if (root.get("behaviour") == null || root.get("source") == null) {
                    ChatUtil.error("Invalid image config: " + file.getName());
                    continue;
                }

                BehaviourType behaviour = (BehaviourType) root.get("behaviour").getRef(BehaviourType.STATIC);

                Node.Array sources = (Node.Array) root.get("source");

                List<SourceItem> srcs = new ArrayList<>();

                for (Node<?> src : sources.value.nodes) {
                    if (src.get("file") == null || src.get("width") == null || src.get("height") == null) {
                        ChatUtil.error("Invalid source in image config: " + file.getName());
                        continue;
                    }

                    String sourceFile = src.get("file").getString().toString();

                    float width = src.get("width").getFloat();

                    float height = src.get("height").getFloat();

                    ImageSource image = createSource(sourceFile);

                    image.init();

                    srcs.add(new SourceItem(image, width, height));
                }

                if (srcs.isEmpty()) {
                    ChatUtil.error("No valid image sources in: " + file.getName());
                    continue;
                }

                ImageItem item = new ImageItem(behaviour, srcs, file.getName());

                if (sources.getDefault() != null) {
                    Node<?> def = sources.getDefault();

                    int indexOfDefault = sources.value.nodes.indexOf(def);

                    if (indexOfDefault >= 0 && indexOfDefault < srcs.size()) {
                        item.currentIndex = indexOfDefault;
                    }
                }

                imageItems.add(item);
                addElement(new Element(item));

                if (mc.thePlayer != null && mc.theWorld != null) {
                    ChatUtil.info("Loaded image: " + file.getName());
                }

            } catch (IOException | RuntimeException e) {
                ChatUtil.error("Failed to load image config: " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    private ImageSource createSource(String sourceFile) {
        if (sourceFile.startsWith("http://") || sourceFile.startsWith("https://")) {
            return new RemoteImage(sourceFile);
        }

        Path imagePath = Paths.get(sourceFile);

        if (!imagePath.isAbsolute()) {
            imagePath = imagesFolder.toPath().resolve(imagePath);
        }

        return new FileImage(imagePath.toString());
    }

    @Override
    public void onEnable() {
        super.onEnable();

        clearElements();
        imageItems.clear();

        populateElements();
    }

    @Subscribe
    @ApiStatus.Internal
    public void onTick(EventTick ev) {
        for (ImageItem item : imageItems) {
            for (SourceItem source : item.source) {
                source.image.tick();
            }
        }

        WatchKey key = watchService.poll();

        if (key == null) {
            return;
        }

        boolean dirty = false;

        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();

            if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_DELETE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                dirty = true;
            }
        }

        if (!key.reset()) {
            return;
        }

        if (dirty) {
            clearElements();
            imageItems.clear();

            populateElements();
        }
    }

    private enum BehaviourType {
        STATIC
    }

    private static class SourceItem {
        private final ImageSource image;
        private final float widthRatio;
        private final float heightRatio;

        private SourceItem(ImageSource image, float widthRatio, float heightRatio) {
            this.image = image;
            this.widthRatio = widthRatio;
            this.heightRatio = heightRatio;
        }
    }

    private static class ImageItem {
        private final BehaviourType behaviour;
        private final List<SourceItem> source;
        private final String name;

        private int currentIndex = 0;

        private ImageItem(BehaviourType behaviour, List<SourceItem> source, String name) {
            this.behaviour = behaviour;
            this.source = source;
            this.name = name;
        }
    }

    private static class Element extends HUDElement {
        private final ImageItem imageItem;
        private final ImageSource currentSource;

        private float currentWidth;
        private float currentHeight;

        protected Element(ImageItem imageItem) {
            super("hud_" + imageItem.name);

            this.imageItem = imageItem;

            SourceItem source = imageItem.source.get(imageItem.currentIndex);

            currentSource = source.image;

            currentWidth = currentSource.getWidth() * source.widthRatio;
            currentHeight = currentSource.getHeight() * source.heightRatio;

            setPlacement(HUDAnchor.LEFT_TOP, 10, 10);
        }

        @Override
        public void render(Hud.Layer layer) {
            if (!layer.is(Hud.Layer.NAME_SKIA_BOTTOM)) {
                return;
            }

            if (currentSource instanceof RemoteImage) {
                renderRemote((RemoteImage) currentSource);
                return;
            }

            UI.drawImage(currentSource, getBounds().x, getBounds().y, getBounds().width, getBounds().height, 0, 1.0f);
        }

        private void renderRemote(RemoteImage remote) {
            switch (remote.getState()) {
                case LOADED:
                    UI.drawImage(remote, getBounds().x, getBounds().y, getBounds().width, getBounds().height, 0, 1.0f);
                    break;

                case LOADING:
                    drawPlaceholder(0xFF220000, 0xFFFFFFFF, MaterialIcons.DOWNLOADING, true, 242);
                    break;

                case FAILED:
                    drawPlaceholder(0xFF000000, 0xFFFFFFFF, MaterialIcons.ERROR, false, 200);
                    break;

                case NOT_STARTED:
                    drawPlaceholder(0xFF222222, 0xFFAAAAAA, MaterialIcons.PAUSE, true, 200);
                    break;
            }
        }

        private void drawPlaceholder(int backgroundColor, int iconColor, String icon, boolean filled, int size) {
            try (Paint p = new Paint()) {
                p.setColor(backgroundColor);
                UI.drawRect(getBounds().x, getBounds().y, getBounds().width, getBounds().height, p);

                p.setColor(iconColor);
                float cx = getBounds().x + getBounds().width / 2f;
                float cy = getBounds().y + getBounds().height / 2f;
                UI.drawText(icon, cx, cy, filled ? Fonts.MaterialIcons.fill(true) : Fonts.MaterialIcons, size, Align.CENTER, p);
            }
        }

        @Override
        public Size getSize() {
            if (currentSource instanceof RemoteImage) {
                RemoteImage remote = (RemoteImage) currentSource;

                switch (remote.getState()) {
                    case LOADED:
                        SourceItem source = imageItem.source.get(imageItem.currentIndex);

                        currentWidth = currentSource.getWidth() * source.widthRatio;
                        currentHeight = currentSource.getHeight() * source.heightRatio;

                        return new Size(currentWidth, currentHeight);

                    case LOADING:
                    case FAILED:
                    case NOT_STARTED:
                        return new Size(100, 100);
                }
            }

            return new Size(currentWidth, currentHeight);
        }

        @Override
        public void onRemove() {
            for (SourceItem source : imageItem.source) {
                source.image.close();
            }
        }
    }
}