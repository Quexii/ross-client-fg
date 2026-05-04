package eu.shoroa.ross.render.skia.image;

import java.util.HashMap;
import java.util.Map;

public class Images {
    private static final Map<String, ResourceImage> resourceImages = new HashMap<String, ResourceImage>();
    private static final Map<String, FileImage> fileImages = new HashMap<String, FileImage>();
    private static final Map<String, RemoteImage> remoteImages = new HashMap<String, RemoteImage>();
    private static final Map<String, GLImage> glImages = new HashMap<String, GLImage>();

    public static final ResourceImage WATERMARK_WEEDHACK = createResourceImage("/assets/rossclient/images/watermark/weedhack.png");
    public static final ResourceImage HALFTONE_CIRCLE = createResourceImage("/assets/rossclient/images/halftone_circle.png");
    public static final ResourceImage HALFTONE_RECT = createResourceImage("/assets/rossclient/images/halftone_rect.png");
    public static final ResourceImage TOPOLOGY = createResourceImage("/assets/rossclient/images/topology.png");

    public static ResourceImage createResourceImage(String path) {
        ResourceImage image = new ResourceImage(path);
        resourceImages.put(path, image);
        return image;
    }

    public static FileImage createFileImage(String path) {
        FileImage image = new FileImage(path);
        fileImages.put(path, image);
        return image;
    }

    public static RemoteImage createRemoteImage(String url) {
        RemoteImage image = new RemoteImage(url);
        remoteImages.put(url, image);
        return image;
    }

    public static RemoteImage createRemoteImage(String url, int connectTimeoutMs, int readTimeoutMs) {
        RemoteImage image = new RemoteImage(url, connectTimeoutMs, readTimeoutMs);
        remoteImages.put(url, image);
        return image;
    }

    public static GLImage createGLImage(String key, int texture, int width, int height) {
        GLImage image = new GLImage(texture, width, height);
        glImages.put(key, image);
        return image;
    }

    public static GLImage createGLImage(String key, int texture) {
        GLImage image = new GLImage(texture);
        glImages.put(key, image);
        return image;
    }

    public static void load() {
        for (ResourceImage image : resourceImages.values()) {
            image.init();
        }

        for (FileImage image : fileImages.values()) {
            image.init();
        }
    }

    public static void preloadRemote() {
        for (RemoteImage image : remoteImages.values()) {
            image.init();
        }
    }

    public static void initGL() {
        for (GLImage image : glImages.values()) {
            image.init();
        }
    }

    public static void close() {
        for (ResourceImage image : resourceImages.values()) {
            image.close();
        }
        for (FileImage image : fileImages.values()) {
            image.close();
        }
        for (RemoteImage image : remoteImages.values()) {
            image.close();
        }
        for (GLImage image : glImages.values()) {
            image.close();
        }
    }
}

