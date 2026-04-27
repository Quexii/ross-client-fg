#version 120

uniform sampler2D uTex;
uniform float uThickness;
uniform vec2 uTexelSize;
uniform float uFillAlpha;
uniform float uOutlineAlpha;

varying vec4 texCoord;

void main() {
    vec2 uv = texCoord.xy;
    vec4 src = texture2D(uTex, uv);

    if (src.a > 0.0) {
        if (uFillAlpha <= 0.0) discard;
        gl_FragColor = vec4(src.rgb, uFillAlpha);
        return;
    }

    float maxNeighborA = 0.0;
    float minDist = uThickness + 1.0;
    vec3 neighborRGB = vec3(0.0);

    for (float x = -uThickness; x <= uThickness; x++) {
        for (float y = -uThickness; y <= uThickness; y++) {
            float d = length(vec2(x, y));
            if (d > uThickness) continue;

            vec2 offset = vec2(x, y) * uTexelSize;
            vec4 neighborSample = texture2D(uTex, uv + offset);

            if (neighborSample.a > 0.0) {
                if (d < minDist) {
                    minDist = d;
                    neighborRGB = neighborSample.rgb;
                    maxNeighborA = neighborSample.a;
                }
            }
        }
    }

    if (maxNeighborA > 0.0) {
        float distFactor = clamp(minDist / uThickness, 0.0, 1.0);
        float edge = pow(1.0 - distFactor, 3.0);

        gl_FragColor = vec4(neighborRGB, uOutlineAlpha * edge);
    } else {
        discard;
    }
}