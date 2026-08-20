package eu.shoroa.ross.render.skia.image.remote;

import eu.shoroa.ross.render.skia.image.ImageSource;
import eu.shoroa.ross.render.skia.image.remote.resolve.DirectImageResolver;
import eu.shoroa.ross.render.skia.image.remote.resolve.GelbooruResolver;
import eu.shoroa.ross.render.skia.image.remote.resolve.ImageResolver;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class RemoteImage extends ImageSource {
    private static final int MAX_CACHE_ENTRIES = 64;

    private static final Map<String, byte[]> MEMORY_CACHE = Collections.synchronizedMap(new LinkedHashMap<String, byte[]>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    });

    private static final List<ImageResolver> RESOLVERS = new ArrayList<>();

    static {
        RESOLVERS.add(new GelbooruResolver());
        RESOLVERS.add(new DirectImageResolver());
    }

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, daemonThreadFactory("ross-remote-image"));

    private final String url;

    private volatile State state = State.NOT_STARTED;
    private volatile Throwable failure;

    public enum State {
        NOT_STARTED, LOADING, LOADED, FAILED
    }

    public RemoteImage(String url) {
        this.url = url;
    }

    @Override
    public void init() {
        if (!startLoading()) {
            return;
        }

        EXECUTOR.execute(() -> {
            try {
                byte[] bytes = MEMORY_CACHE.get(url);

                if (bytes == null) {
                    bytes = resolve(url).getBytes();
                    MEMORY_CACHE.put(url, bytes);
                }

                decodeEncoded(bytes);
                state = State.LOADED;
            } catch (Throwable throwable) {
                fail(throwable);
            }
        });
    }

    private ImageResolver.ResolvedImage resolve(String remoteUrl) throws IOException {
        IOException lastException = null;

        for (ImageResolver resolver : RESOLVERS) {
            if (!resolver.supports(remoteUrl)) {
                continue;
            }

            try {
                return resolver.resolve(remoteUrl);
            } catch (IOException e) {
                lastException = e;
            }
        }

        if (lastException != null) {
            throw lastException;
        }

        throw new IOException("No image resolver supports URL: " + remoteUrl);
    }

    private boolean startLoading() {
        synchronized (this) {
            if (state == State.LOADING || state == State.LOADED) {
                return false;
            }

            state = State.LOADING;
            failure = null;

            return true;
        }
    }

    private void fail(Throwable throwable) {
        failure = throwable;
        state = State.FAILED;

        System.err.println("Failed to load remote image: " + url + " (" + throwable.getMessage() + ")");
        throwable.printStackTrace();
    }

    public void retry() {
        synchronized (this) {
            if (state != State.FAILED) {
                return;
            }

            state = State.NOT_STARTED;
            failure = null;
        }

        init();
    }

    public State getState() {
        return state;
    }

    public boolean isLoading() {
        return state == State.LOADING;
    }

    public boolean isRemoteLoaded() {
        return state == State.LOADED;
    }

    public boolean hasFailed() {
        return state == State.FAILED;
    }

    public Throwable getFailure() {
        return failure;
    }

    @Override
    public String getId() {
        return url;
    }

    @Override
    public synchronized void close() {
        if (state != State.FAILED) {
            state = State.NOT_STARTED;
        }

        super.close();
    }

    private static ThreadFactory daemonThreadFactory(String namePrefix) {
        return new ThreadFactory() {
            private int index;

            @Override
            public Thread newThread(@NotNull Runnable runnable) {
                Thread thread = new Thread(runnable, namePrefix + "-" + index++);
                thread.setDaemon(true);
                return thread;
            }
        };
    }
}