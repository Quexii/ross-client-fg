package eu.shoroa.ross.render.ui;

import eu.shoroa.ross.render.skia.image.*;
import eu.shoroa.ross.render.skia.image.remote.RemoteImage;

import java.util.HashMap;
import java.util.Map;

public class Images {
    private static final Map<String, ResourceImage> resourceImages = new HashMap<String, ResourceImage>();
    private static final Map<String, FileImage> fileImages = new HashMap<String, FileImage>();
    private static final Map<String, RemoteImage> remoteImages = new HashMap<String, RemoteImage>();
    private static final Map<String, GLImage> glImages = new HashMap<String, GLImage>();
    private static final Map<String, ImageSource> imagesById = new HashMap<String, ImageSource>();

    public static final ResourceImage WATERMARK_WEEDHACK = createResourceImage("/assets/rossclient/images/watermark/weedhack.png");
    public static final ResourceImage HALFTONE_CIRCLE = createResourceImage("/assets/rossclient/images/halftone_circle.png");
    public static final ResourceImage HALFTONE_RECT = createResourceImage("/assets/rossclient/images/halftone_rect.png");
    public static final ResourceImage TOPOLOGY = createResourceImage("/assets/rossclient/images/topology.png");
    public static final ResourceImage BACKGROUND_1 = createResourceImage("/assets/rossclient/images/bg1.jpg");
    public static final ResourceImage BACKGROUND_2 = createResourceImage("/assets/rossclient/images/bg2.jpg");

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

    public static GLImage createGLImage(String key, int texture, int width, int height, boolean mipmaps) {
        GLImage image = new GLImage(texture, width, height, mipmaps);
        glImages.put(key, image);
        return image;
    }

    public static GLImage createGLImage(String key, int texture, boolean mipmaps) {
        GLImage image = new GLImage(texture, mipmaps);
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