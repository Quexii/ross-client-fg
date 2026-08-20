package eu.shoroa.ross.render.skia.image.remote.resolve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DirectImageResolver implements ImageResolver {
    private static final long MAX_BYTES = 64L * 1024 * 1024;

    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public DirectImageResolver() {
        this(5000, 5000);
    }

    public DirectImageResolver(int connectTimeoutMs, int readTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    @Override
    public boolean supports(String url) {
        if (url == null) {
            return false;
        }

        return url.startsWith("http://") || url.startsWith("https://");
    }

    @Override
    public ResolvedImage resolve(String url) throws IOException {
        String referer = originOf(url);

        try {
            return fetchImage(url, referer);
        } catch (HttpStatusException e) {
            if (e.statusCode == 403 && referer != null) {
                return fetchImage(url, null);
            }

            throw e;
        }
    }

    private ResolvedImage fetchImage(String url, String referer) throws IOException {
        HttpURLConnection connection = null;

        try {
            connection = open(url, referer);

            int responseCode = connection.getResponseCode();

            if (responseCode < 200 || responseCode >= 300) {
                throw new HttpStatusException(responseCode, "HTTP " + responseCode + " for " + url);
            }

            String contentType = connection.getContentType();
            String finalUrl = connection.getURL().toExternalForm();

            if (isHtml(contentType)) {
                throw new IOException("Server returned HTML instead of an image: " + finalUrl);
            }

            byte[] bytes = readBody(connection);

            if (bytes.length == 0) {
                throw new IOException("Server returned an empty response");
            }

            return new ResolvedImage(bytes, finalUrl, contentType);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection open(String url, String referer) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout(readTimeoutMs);

        connection.setInstanceFollowRedirects(true);
        connection.setUseCaches(false);

        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " + "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Ross/1.0");

        connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/png,image/jpeg,image/gif,image/*;q=0.8");

        if (referer != null) {
            connection.setRequestProperty("Referer", referer);
        }

        return connection;
    }

    private byte[] readBody(HttpURLConnection connection) throws IOException {
        try (InputStream input = connection.getInputStream()) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[8192];

            int read;
            long total = 0;

            while ((read = input.read(buffer)) != -1) {
                total += read;

                if (total > MAX_BYTES) {
                    throw new IOException("Response exceeded max allowed size of " + MAX_BYTES + " bytes");
                }

                output.write(buffer, 0, read);
            }

            return output.toByteArray();
        }
    }

    private String originOf(String url) {
        try {
            URL parsed = new URL(url);

            StringBuilder origin = new StringBuilder();
            origin.append(parsed.getProtocol()).append("://").append(parsed.getHost());

            if (parsed.getPort() != -1 && parsed.getPort() != parsed.getDefaultPort()) {
                origin.append(':').append(parsed.getPort());
            }

            origin.append('/');

            return origin.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isHtml(String contentType) {
        if (contentType == null) {
            return false;
        }

        String lower = contentType.toLowerCase();

        return lower.contains("text/html") || lower.contains("application/xhtml");
    }

    private static final class HttpStatusException extends IOException {
        private final int statusCode;

        private HttpStatusException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
    }
}