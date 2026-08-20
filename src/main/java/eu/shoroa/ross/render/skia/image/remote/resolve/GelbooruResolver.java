package eu.shoroa.ross.render.skia.image.remote.resolve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GelbooruResolver implements ImageResolver {

    private static final Pattern META_IMAGE_PATTERN = Pattern.compile("<meta[^>]+(?:property|name)\\s*=\\s*[\"']" + "(?:og:image|twitter:image)" + "[\"'][^>]+content\\s*=\\s*[\"']" + "([^\"']+)" + "[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern META_IMAGE_PATTERN_REVERSED = Pattern.compile("<meta[^>]+content\\s*=\\s*[\"']" + "([^\"']+)" + "[\"'][^>]+(?:property|name)\\s*=\\s*[\"']" + "(?:og:image|twitter:image)" + "[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMAGE_PATTERN = Pattern.compile("<img[^>]+src\\s*=\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    private static final long MAX_BYTES = 64L * 1024 * 1024;

    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public GelbooruResolver() {
        this(5000, 5000);
    }

    public GelbooruResolver(int connectTimeoutMs, int readTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    @Override
    public boolean supports(String url) {
        if (url == null) {
            return false;
        }

        String lower = url.toLowerCase();

        return lower.contains("gelbooru.com") || lower.contains("gelbooru.org") || lower.contains("gelbooru.cc");
    }

    @Override
    public ResolvedImage resolve(String url) throws IOException {
        FetchResult first = fetch(url, originOf(url));

        if (isImage(first.contentType, first.bytes)) {
            return new ResolvedImage(first.bytes, first.finalUrl, first.contentType);
        }

        if (!isHtml(first.contentType, first.bytes)) {
            throw new IOException("Gelbooru returned unsupported content: " + first.contentType);
        }

        String imageUrl = findImageUrl(first.finalUrl, first.bytes);

        if (imageUrl == null) {
            throw new IOException("Could not find image URL on Gelbooru page: " + first.finalUrl);
        }

        FetchResult image = fetch(imageUrl, first.finalUrl);

        if (!isImage(image.contentType, image.bytes)) {
            throw new IOException("Resolved Gelbooru URL was not an image: " + image.finalUrl + " (" + image.contentType + ")");
        }

        return new ResolvedImage(image.bytes, image.finalUrl, image.contentType);
    }

    private FetchResult fetch(String url, String referer) throws IOException {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectTimeoutMs);
            connection.setReadTimeout(readTimeoutMs);

            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " + "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Ross/1.0");
            connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/png,image/jpeg,image/gif,image/*;q=0.8,text/html;q=0.5");

            if (referer != null) {
                connection.setRequestProperty("Referer", referer);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("HTTP " + responseCode + " for " + url);
            }

            String contentType = connection.getContentType();
            String finalUrl = connection.getURL().toExternalForm();

            byte[] bytes;

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

                bytes = output.toByteArray();
            }

            if (bytes.length == 0) {
                throw new IOException("Empty response from " + finalUrl);
            }

            return new FetchResult(bytes, finalUrl, contentType);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String findImageUrl(String pageUrl, byte[] bytes) {
        String html = new String(bytes, StandardCharsets.UTF_8);

        Matcher matcher = META_IMAGE_PATTERN.matcher(html);

        if (matcher.find()) {
            return absolute(pageUrl, matcher.group(1));
        }

        matcher = META_IMAGE_PATTERN_REVERSED.matcher(html);

        if (matcher.find()) {
            return absolute(pageUrl, matcher.group(1));
        }

        matcher = IMAGE_PATTERN.matcher(html);

        while (matcher.find()) {
            String candidate = matcher.group(1);

            String lower = candidate.toLowerCase();

            if (lower.contains("favicon") || lower.contains("avatar") || lower.contains("logo") || lower.contains("icon") || lower.contains("thumb")) {
                continue;
            }

            return absolute(pageUrl, candidate);
        }

        return null;
    }

    private String absolute(String base, String value) {
        try {
            return URI.create(base).resolve(value).toString();
        } catch (Exception ignored) {
            return value;
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

    private boolean isHtml(String contentType, byte[] bytes) {
        if (contentType != null) {
            String lower = contentType.toLowerCase();

            if (lower.contains("text/html") || lower.contains("application/xhtml")) {
                return true;
            }
        }

        return bytes.length >= 5 && bytes[0] == '<';
    }

    private boolean isImage(String contentType, byte[] bytes) {
        if (contentType != null && contentType.toLowerCase().startsWith("image/")) {
            return true;
        }

        return isPng(bytes) || isJpeg(bytes) || isGif(bytes) || isWebp(bytes);
    }

    private boolean isPng(byte[] b) {
        return b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47 && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A;
    }

    private boolean isJpeg(byte[] b) {
        return b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    private boolean isGif(byte[] b) {
        return b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8' && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    private boolean isWebp(byte[] b) {
        return b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }

    private static final class FetchResult {
        private final byte[] bytes;
        private final String finalUrl;
        private final String contentType;

        private FetchResult(byte[] bytes, String finalUrl, String contentType) {
            this.bytes = bytes;
            this.finalUrl = finalUrl;
            this.contentType = contentType;
        }
    }
}