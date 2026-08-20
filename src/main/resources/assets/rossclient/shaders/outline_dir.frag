#version 120

uniform sampler2D uTex;
uniform vec2 uTexelSize;
uniform vec2 uDirection;
uniform float uThickness;

varying vec4 texCoord;

void main() {
    vec2 uv = texCoord.xy;
    vec4 center = texture2D(uTex, uv);

    if (center.a > 0.01) {
        gl_FragColor = center;
        return;
    }

    vec4 found = vec4(0.0);
    float bestDist = 1.0e9;

    int r = int(ceil(uThickness));
    for (int x = -64; x <= 64; x++) {
        if (x < -r || x > r) continue;

        vec4 s = texture2D(uTex, uv + uDirection * float(x) * uTexelSize);
        if (s.a > 0.01) {
            float d = abs(float(x));
            if (d < bestDist) {
                bestDist = d;
                found = s;
            }
        }
    }

    gl_FragColor = found;
}
