package eu.shoroa.ross.render.skia.image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileImage extends ImageSource {
    private final Path path;

    public FileImage(String filePath) {
        this.path = Paths.get(filePath);
    }

    @Override
    public void init() {
        try {
            decodeEncoded(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file image: " + path, e);
        }
    }

    @Override
    public String getId() {
        return path.toAbsolutePath().toString();
    }
}