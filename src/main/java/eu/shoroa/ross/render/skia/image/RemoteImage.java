package eu.shoroa.ross.render.skia.image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemoteImage extends ImageSource {
    private static final Map<String, byte[]> MEMORY_CACHE = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private int index;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "ross-remote-image-" + index++);
            thread.setDaemon(true);
            return thread;
        }
    });

    private final String url;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final AtomicBoolean loading = new AtomicBoolean(false);

    public RemoteImage(String url) {
        this(url, 5000, 5000);
    }

    public RemoteImage(String url, int connectTimeoutMs, int readTimeoutMs) {
        this.url = url;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    @Override
    public void init() {
        if (!loading.compareAndSet(false, true)) {
            return;
        }

        EXECUTOR.execute(() -> {
            try {
                byte[] bytes = MEMORY_CACHE.get(url);
                if (bytes == null) {
                    bytes = download(url);
                    MEMORY_CACHE.put(url, bytes);
                }

                setImage(decode(bytes));
            } catch (IOException e) {
                System.err.println("Failed to load remote image: " + url + " (" + e.getMessage() + ")");
            } catch (RuntimeException e) {
                System.err.println("Failed to decode remote image: " + url + " (" + e.getMessage() + ")");
            } finally {
                loading.set(false);
            }
        });
    }

    public boolean isLoading() {
        return loading.get();
    }

    @Override
    public String getId() {
        return url;
    }

    private byte[] download(String remoteUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(remoteUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout(readTimeoutMs);
        connection.setUseCaches(true);
        connection.setRequestProperty("User-Agent", "RossClient/1.0");

        try {
            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IOException("HTTP " + code);
            }

            InputStream stream = connection.getInputStream();
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                while (true) {
                    int read = stream.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    output.write(buffer, 0, read);
                }
                return output.toByteArray();
            } finally {
                stream.close();
            }
        } finally {
            connection.disconnect();
        }
    }
}

