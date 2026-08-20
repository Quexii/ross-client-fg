#version 120

uniform sampler2D uTex;
uniform sampler2D uSrc;

uniform vec2 uDirection;
uniform vec2 uTexelSize;
uniform float uRadius;
uniform float uFalloffStart;
uniform float uFalloffEnd;

varying vec4 texCoord;

void main() {
    vec4 result = vec4(0.0);
    float weightSum = 0.0;

    float radius = max(uRadius, 0.001);

    for (int i = -6; i <= 6; i++) {
        float x = float(i);
        float weight = exp(-(x * x) / (2.0 * radius * radius));

        vec2 offset = uDirection * uTexelSize * x;
        vec4 smpp = texture2D(uTex, texCoord.xy + offset);

        smpp.rgb *= smpp.a;

        result += smpp * weight;
        weightSum += weight;
    }

    result /= weightSum;

    if (result.a > 0.0001)
    result.rgb /= result.a;

    result.a = smoothstep(uFalloffStart, uFalloffEnd, result.a);

    gl_FragColor = result;
}