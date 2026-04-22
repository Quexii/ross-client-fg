package com.google.common.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public final class MoreFiles {
    private MoreFiles() {
    }

    public static void deleteRecursively(Path path, RecursiveDeleteOption... options) throws IOException {
        // Keep signature compatible with newer Guava API expected by Mixin.
        if (options != null && options.length > 0) {
            // No special handling needed for this compatibility shim.
        }
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
        }
    }
}



