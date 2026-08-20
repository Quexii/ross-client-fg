package eu.shoroa.ross.render.skia.image.remote.resolve;

import java.io.IOException;

public interface ImageResolver {
    boolean supports(String url);

    ResolvedImage resolve(String url) throws IOException;

    final class ResolvedImage {
        private final byte[] bytes;
        private final String finalUrl;
        private final String contentType;

        public ResolvedImage(
                byte[] bytes,
                String finalUrl,
                String contentType
        ) {
            this.bytes = bytes;
            this.finalUrl = finalUrl;
            this.contentType = contentType;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getFinalUrl() {
            return finalUrl;
        }

        public String getContentType() {
            return contentType;
        }
    }
}