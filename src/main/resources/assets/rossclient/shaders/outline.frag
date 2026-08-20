#version 120

uniform sampler2D uTex;
uniform float uThickness;
uniform float uSoftness;
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

    vec3 accumColor = vec3(0.0);
    float accumWeight = 0.0;
    float minDist = uThickness + 1.0;

    for (float x = -uThickness; x <= uThickness; x++) {
        for (float y = -uThickness; y <= uThickness; y++) {
            float d = length(vec2(x, y));
            if (d > uThickness) continue;

            vec2 offset = vec2(x, y) * uTexelSize;
            vec4 neighborSample = texture2D(uTex, uv + offset);
            if (neighborSample.a <= 0.0) continue;

            minDist = min(minDist, d);

            float contribW = neighborSample.a / (1.0 + d * d);
            accumColor += neighborSample.rgb * contribW;
            accumWeight += contribW;
        }
    }

    if (accumWeight > 0.0) {
        float soft = max(uSoftness, 0.0001);
        float edgeDist = uThickness - minDist;
        float w = clamp(edgeDist / soft, 0.0, 1.0);
        w = w * w * (3.0 - 2.0 * w);

        vec3 outColor = accumColor / accumWeight;
        gl_FragColor = vec4(outColor, uOutlineAlpha * w);
    } else {
        discard;
    }
}